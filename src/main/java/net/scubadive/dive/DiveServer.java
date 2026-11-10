package net.scubadive.dive;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.player.Player;

/** Server-authoritative dive state: tank, BCD air, dive log, neutral buoyancy. Synced to the owner every SYNC_INTERVAL ticks. */
public final class DiveServer {
	private static final float USE_PER_TICK_PER_BAR = DiveData.FULL_TANK / (20f * 600f * 2f);
	private static final float REFILL_PER_TICK = DiveData.FULL_TANK / (20f * 10f);
	private static final int SURFACE_RESET_TICKS = 20 * 10;
	/** Minimum time at the surface before a full tank resets the log (avoids resets while bobbing). */
	private static final int SURFACE_GUARD_TICKS = 20;
	private static final int INPUT_TIMEOUT = 30;
	private static final int SYNC_INTERVAL = 10;

	/** Live (unsynced) state of one player. */
	static final class Live {
		float pressure;
		float air;
		int diveTicks;
		float maxDepth;
		int surfaceTicks = SURFACE_RESET_TICKS;
		int dir;
		long dirTick;
		double lastY;
		float speed;
		boolean neutral;

		DiveData toData() {
			return new DiveData(pressure, air, diveTicks, maxDepth);
		}
	}

	private static final Map<UUID, Live> LIVE = new HashMap<>();
	private static long now;

	private DiveServer() {
	}

	static void init() {
		PayloadTypeRegistry.serverboundPlay().register(DiveInputPayload.TYPE, DiveInputPayload.CODEC);
		ServerPlayNetworking.registerGlobalReceiver(DiveInputPayload.TYPE, (payload, context) -> {
			Live live = live(context.player());
			live.dir = Integer.signum(payload.dir());
			live.dirTick = now;
		});
		ServerTickEvents.END_SERVER_TICK.register(DiveServer::tick);
		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
			ServerPlayer p = handler.getPlayer();
			Live live = LIVE.remove(p.getUUID());
			if (live != null) {
				p.setAttached(ScubaDiveGear.DIVE, live.toData());
			}
		});
	}

	/** Live state for the player, loaded from the persistent attachment on first use. */
	static Live live(Player player) {
		return LIVE.computeIfAbsent(player.getUUID(), id -> {
			DiveData d = player.getAttachedOrElse(ScubaDiveGear.DIVE, DiveData.DEFAULT);
			Live l = new Live();
			l.pressure = d.pressure();
			l.air = d.air();
			l.diveTicks = d.diveTicks();
			l.maxDepth = d.maxDepth();
			l.lastY = player.getY();
			return l;
		});
	}

	/** Live state if the player has one (server only), else null. */
	static Live peek(Player player) {
		return LIVE.get(player.getUUID());
	}

	private static void tick(MinecraftServer server) {
		now++;
		for (ServerPlayer p : server.getPlayerList().getPlayers()) {
			if (!p.isAlive()) {
				continue;
			}
			tickPlayer(p);
			if ((now + p.getId()) % SYNC_INTERVAL == 0) {
				flush(p);
			}
		}
	}

	private static void flush(ServerPlayer p) {
		Live l = LIVE.get(p.getUUID());
		if (l == null) {
			return;
		}
		DiveData data = l.toData();
		if (!data.equals(p.getAttachedOrElse(ScubaDiveGear.DIVE, DiveData.DEFAULT))) {
			p.setAttached(ScubaDiveGear.DIVE, data);
		}
	}

	private static void tickPlayer(ServerPlayer p) {
		Live l = live(p);
		boolean bcd = DiveApi.hasTank(p);
		boolean eyeInWater = p.isEyeInFluid(FluidTags.WATER);
		float depth = eyeInWater ? DiveApi.depth(p.level(), p) : 0f;

		// BCD input (held keys are refreshed by the client).
		if (bcd && l.dir != 0 && now - l.dirTick <= INPUT_TIMEOUT) {
			l.air = Math.max(0f, Math.min(1f, l.air + l.dir * Buoyancy.AIR_STEP));
		}

		double y = p.getY();
		l.speed = (float) Math.abs(y - l.lastY);
		l.lastY = y;

		if (eyeInWater) {
			l.surfaceTicks = 0;
			l.diveTicks++;
			l.maxDepth = Math.max(l.maxDepth, depth);
			if (bcd) {
				l.pressure = Math.max(0f, l.pressure - USE_PER_TICK_PER_BAR * (1f + depth / 10f));
			}
		} else {
			l.surfaceTicks = Math.min(l.surfaceTicks + 1, SURFACE_RESET_TICKS);
			if (bcd) {
				l.pressure = Math.min(DiveData.FULL_TANK, l.pressure + REFILL_PER_TICK);
				if (l.pressure >= DiveData.FULL_TANK && l.surfaceTicks >= SURFACE_GUARD_TICKS) {
					resetLog(l);
				}
			} else if (l.surfaceTicks >= SURFACE_RESET_TICKS) {
				resetLog(l);
			}
		}

		float net = Buoyancy.net(l.air, depth);
		l.neutral = bcd && p.isInWater() && Buoyancy.isNeutral(net, l.speed);

		// Safety net: the mixin already keeps breath up; this covers any path that bypasses it.
		if (eyeInWater && ScubaDiveGear.canBreathe(p, l.pressure) && p.getAirSupply() < p.getMaxAirSupply()) {
			p.setAirSupply(p.getMaxAirSupply());
		}
	}

	private static void resetLog(Live l) {
		l.diveTicks = 0;
		l.maxDepth = 0f;
	}
}
