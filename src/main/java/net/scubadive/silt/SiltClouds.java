package net.scubadive.silt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/** Server-side sediment clouds. Owner: silt module. */
public final class SiltClouds {
	private static final double MIN_SPEED = 0.12;
	private static final double SEDIMENT_REACH = 1.2;
	private static final int LIFETIME_TICKS = 300;
	private static final float GROW = 0.8f;
	private static final double MERGE_DIST = 1.5;
	private static final int MAX_CLOUDS = 256;
	private static final int SYNC_INTERVAL = 10;
	private static final double SYNC_RANGE = 64.0;
	private static final int PARTICLE_INTERVAL = 4;

	private static final Map<ResourceKey<Level>, LevelState> STATES = new HashMap<>();

	private SiltClouds() {
	}

	/** One cloud. radius0 is the radius when created; it widens a little while the density fades. */
	static final class Cloud {
		double x, y, z;
		float radius0;
		float radius;
		float density;
		BlockState state;
		int age;
	}

	private static final class LevelState {
		final List<Cloud> clouds = new ArrayList<>();
		final Map<UUID, Vec3> lastPos = new HashMap<>();
		final Set<UUID> hadClouds = new HashSet<>();
		int ticks;
	}

	public static void init() {
		PayloadTypeRegistry.clientboundPlay().register(SiltSyncPayload.TYPE, SiltSyncPayload.CODEC);
		ServerTickEvents.END_LEVEL_TICK.register(SiltClouds::tick);
		ServerLifecycleEvents.SERVER_STOPPED.register(server -> STATES.clear());
	}

	static boolean isSediment(BlockState state) {
		return state.is(Blocks.SAND) || state.is(Blocks.RED_SAND) || state.is(Blocks.GRAVEL) || state.is(Blocks.MUD)
				|| state.is(Blocks.CLAY) || state.is(Blocks.SUSPICIOUS_SAND) || state.is(Blocks.SUSPICIOUS_GRAVEL);
	}

	/** Density contribution of one cloud at distance d from its centre. Shared by server and client. */
	public static float falloff(float density, float radius, double dist) {
		if (radius <= 0f || dist >= radius) {
			return 0f;
		}
		float t = (float) (dist / radius);
		return density * (1f - t * t);
	}

	static List<SiltSyncPayload.Entry> snapshot(Level level) {
		List<SiltSyncPayload.Entry> out = new ArrayList<>();
		LevelState ls = STATES.get(level.dimension());
		if (ls != null) {
			for (Cloud c : ls.clouds) {
				out.add(new SiltSyncPayload.Entry((float) c.x, (float) c.y, (float) c.z, c.radius, c.density));
			}
		}
		return out;
	}

	static float densityAt(Level level, double x, double y, double z) {
		LevelState ls = STATES.get(level.dimension());
		if (ls == null) {
			return 0f;
		}
		float best = 0f;
		for (Cloud c : ls.clouds) {
			double dx = c.x - x;
			double dy = c.y - y;
			double dz = c.z - z;
			best = Math.max(best, falloff(c.density, c.radius, Math.sqrt(dx * dx + dy * dy + dz * dz)));
		}
		return best;
	}

	private static void tick(ServerLevel level) {
		LevelState ls = STATES.computeIfAbsent(level.dimension(), k -> new LevelState());
		ls.ticks++;

		// Fade and widen; drop the gone ones.
		Iterator<Cloud> it = ls.clouds.iterator();
		while (it.hasNext()) {
			Cloud c = it.next();
			c.age++;
			c.density -= 1f / LIFETIME_TICKS;
			c.radius = c.radius0 + GROW * Math.min(1f, c.age / (float) LIFETIME_TICKS);
			if (c.density <= 0.01f || c.age > LIFETIME_TICKS * 3) {
				it.remove();
			}
		}

		// Stir up from fast swimmers over sediment.
		Set<UUID> present = new HashSet<>();
		for (ServerPlayer player : level.players()) {
			present.add(player.getUUID());
			Vec3 now = player.position();
			Vec3 prev = ls.lastPos.put(player.getUUID(), now);
			if (prev == null || player.isSpectator() || !player.isUnderWater() && !player.isInWater()) {
				continue;
			}
			double speed = Math.sqrt((now.x - prev.x) * (now.x - prev.x) + (now.z - prev.z) * (now.z - prev.z));
			if (speed <= MIN_SPEED || speed > 4.0) {
				continue;
			}
			BlockPos floor = findSediment(level, now);
			if (floor != null) {
				stir(ls, new Vec3(now.x, floor.getY() + 1.3, now.z), speed, level.getBlockState(floor));
			}
		}
		ls.lastPos.keySet().retainAll(present);

		if (ls.ticks % PARTICLE_INTERVAL == 0) {
			emitParticles(level, ls);
		}
		if (ls.ticks % SYNC_INTERVAL == 0) {
			sync(level, ls);
		}
	}

	private static BlockPos findSediment(ServerLevel level, Vec3 feet) {
		double[] offsets = {-0.05, -0.65, -SEDIMENT_REACH};
		for (double off : offsets) {
			BlockPos pos = BlockPos.containing(feet.x, feet.y + off, feet.z);
			if (isSediment(level.getBlockState(pos))) {
				return pos;
			}
		}
		return null;
	}

	private static void stir(LevelState ls, Vec3 at, double speed, BlockState sediment) {
		float add = (float) Math.min(0.5, 0.12 + (speed - MIN_SPEED) * 1.5);
		float radius = (float) Math.max(2.0, Math.min(3.0, 2.0 + (speed - MIN_SPEED) * 6.0));
		for (Cloud c : ls.clouds) {
			double dx = c.x - at.x;
			double dy = c.y - at.y;
			double dz = c.z - at.z;
			if (dx * dx + dy * dy + dz * dz <= MERGE_DIST * MERGE_DIST) {
				c.density = Math.min(1f, c.density + add);
				c.radius0 = Math.max(c.radius0, radius);
				c.radius = Math.max(c.radius, c.radius0);
				c.age = Math.min(c.age, LIFETIME_TICKS / 3);
				c.state = sediment;
				return;
			}
		}
		Cloud c = new Cloud();
		c.x = at.x;
		c.y = at.y;
		c.z = at.z;
		c.radius0 = radius;
		c.radius = radius;
		c.density = Math.min(1f, add + 0.1f);
		c.state = sediment;
		ls.clouds.add(c);
		if (ls.clouds.size() > MAX_CLOUDS) {
			Cloud weakest = ls.clouds.get(0);
			for (Cloud o : ls.clouds) {
				if (o.density < weakest.density) {
					weakest = o;
				}
			}
			ls.clouds.remove(weakest);
		}
	}

	private static void emitParticles(ServerLevel level, LevelState ls) {
		for (Cloud c : ls.clouds) {
			if (c.density < 0.15f) {
				continue;
			}
			int count = 1 + Math.round(c.density * 3f);
			double spread = c.radius * 0.45;
			level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, c.state), c.x, c.y, c.z, count, spread, 0.4, spread, 0.02);
			if (level.getRandom().nextInt(2) == 0) {
				level.sendParticles(new BlockParticleOption(ParticleTypes.FALLING_DUST, c.state), c.x, c.y + 0.3, c.z, count, spread, 0.4, spread, 0.0);
			}
		}
	}

	private static void sync(ServerLevel level, LevelState ls) {
		for (ServerPlayer player : level.players()) {
			List<SiltSyncPayload.Entry> near = new ArrayList<>();
			double r2 = SYNC_RANGE * SYNC_RANGE;
			for (Cloud c : ls.clouds) {
				double dx = c.x - player.getX();
				double dy = c.y - player.getY();
				double dz = c.z - player.getZ();
				if (dx * dx + dy * dy + dz * dz <= r2) {
					near.add(new SiltSyncPayload.Entry((float) c.x, (float) c.y, (float) c.z, c.radius, c.density));
				}
			}
			// Send an empty list once after the last cloud so the client clears it.
			if (!near.isEmpty()) {
				ls.hadClouds.add(player.getUUID());
			} else if (!ls.hadClouds.remove(player.getUUID())) {
				continue;
			}
			ServerPlayNetworking.send(player, new SiltSyncPayload(near));
		}
	}
}
