package net.scubadive.dive;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.player.Player;

/** Server-authoritative dive state: tank, BCD air, dive log, neutral buoyancy. Synced to the owner every SYNC_INTERVAL ticks. */
public final class DiveServer {
	private static final float USE_PER_TICK_PER_BAR = DiveData.FULL_TANK / (20f * 600f * 2f);
	/** Air use multiplier while not neutrally buoyant. */
	private static final float NOT_NEUTRAL_FACTOR = 1.25f;
	/** Blocks per tick (smoothed). Gentle finning upward stays below it; a full BCD or sprint-swimming up exceeds it. */
	private static final float ASCENT_LIMIT = 0.17f;
	/** With a nearly full BCD a slower climb already counts as a runaway ascent. */
	private static final float ASCENT_LIMIT_FULL_BCD = 0.08f;
	/** Ticks of ignored warning before decompression sickness is certain. */
	private static final int RAPID_LIMIT = 60;
	/** Only dives that went this deep (m) can cause it. */
	private static final float ASCENT_MIN_DEPTH = 8f;
	private static final float REFILL_PER_TICK = DiveData.FULL_TANK / (20f * 10f);
	private static final int SURFACE_RESET_TICKS = 20 * 10;
	/** Minimum time at the surface before a full tank resets the log (avoids resets while bobbing). */
	private static final int SURFACE_GUARD_TICKS = 20;
	private static final int INPUT_TIMEOUT = 30;
	private static final int SYNC_INTERVAL = 10;
	/** Ticks between BCD hiss/gurgle sounds while a key is held. */
	private static final int SOUND_INTERVAL = 5;

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
		double lastX;
		double lastZ;
		float speed;
		float hSpeed;
		/** Smoothed ascent rate (blocks/tick, up positive). */
		float ascent;
		/** Ticks spent ascending too fast during the current ascent. */
		int rapidTicks;
		/** Ignored the rapid-ascent warning: decompression sickness hits at the surface. */
		boolean bent;
		boolean neutral;
		long lastSound = -1000;

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
			l.lastX = player.getX();
			l.lastZ = player.getZ();
			return l;
		});
	}

	/**
	 * Breathing rate relative to calm swimming: resting on the bottom or hovering still in neutral
	 * buoyancy breathes least, finning fast or fighting poor buoyancy breathes most.
	 */
	private static float breathingEffort(Player p, Live l) {
		float swim = Math.min(l.hSpeed, 0.3f) * 2.5f;
		if (p.onGround()) {
			return 0.55f + swim * 0.5f;
		}
		if (l.neutral) {
			return (l.hSpeed < 0.02f ? 0.7f : 0.85f) + swim;
		}
		return NOT_NEUTRAL_FACTOR + swim;
	}

	/**
	 * Rapid-ascent warning: rising faster than ASCENT_LIMIT while breathing from the tank shows a warning.
	 * Keeping it up for RAPID_LIMIT ticks means the diver is "bent" and gets decompression sickness on surfacing.
	 */
	private static void checkAscent(ServerPlayer p, Live l, boolean bcd, boolean eyeInWater) {
		boolean runaway = l.ascent > ASCENT_LIMIT || (l.air >= 0.9f && l.ascent > ASCENT_LIMIT_FULL_BCD);
		if (bcd && eyeInWater && runaway && l.maxDepth >= ASCENT_MIN_DEPTH) {
			l.rapidTicks++;
			if (l.rapidTicks % 10 == 1) {
				p.sendOverlayMessage(net.minecraft.network.chat.Component.translatable("hud.scubadive.dc.rapid_ascent")
						.withStyle(net.minecraft.ChatFormatting.RED));
				p.level().playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.NOTE_BLOCK_BASS.value(), SoundSource.PLAYERS, 0.8f, 1.6f);
			}
			if (l.rapidTicks >= RAPID_LIMIT) {
				l.bent = true;
			}
		} else if (l.rapidTicks > 0) {
			l.rapidTicks = Math.max(0, l.rapidTicks - 2);
		}
		if (!eyeInWater && l.bent) {
			l.bent = false;
			l.rapidTicks = 0;
			p.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.NAUSEA, 300, 0));
			p.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.SLOWNESS, 400, 1));
			p.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.WEAKNESS, 400, 0));
			p.sendSystemMessage(net.minecraft.network.chat.Component.translatable("message.scubadive.bent")
					.withStyle(net.minecraft.ChatFormatting.RED));
		}
	}

	static void setPressure(Player player, float bar) {
		live(player).pressure = Math.max(0f, Math.min(DiveData.FULL_TANK, bar));
	}

	/** Fills the tank and clears the dive log (tour start). */
	static void refill(Player player) {
		Live l = live(player);
		l.bent = false;
		l.rapidTicks = 0;
		l.pressure = DiveData.FULL_TANK;
		resetLog(l);
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
		// Vest swelling for other players: quantised so it only resends on visible change.
		float shownAir = Math.round(l.air * 20f) / 20f;
		if (p.getAttachedOrElse(ScubaDiveGear.BCD_AIR, 0.0f) != shownAir) {
			p.setAttached(ScubaDiveGear.BCD_AIR, shownAir);
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
			float before = l.air;
			l.air = Math.max(0f, Math.min(1f, l.air + l.dir * Buoyancy.AIR_STEP));
			if (l.air != before) {
				bcdEffects(p, l.dir);
			}
		}

		double y = p.getY();
		float rise = (float) (y - l.lastY);
		if (Math.abs(rise) > 2f) {
			rise = 0f;
		}
		l.ascent = l.ascent * 0.85f + rise * 0.15f;
		checkAscent(p, l, bcd, eyeInWater);
		l.speed = (float) Math.abs(y - l.lastY);
		l.lastY = y;
		double hx = p.getX() - l.lastX;
		double hz = p.getZ() - l.lastZ;
		// Ignore teleports: a jump of several blocks is not swimming.
		float h = (float) Math.sqrt(hx * hx + hz * hz);
		l.hSpeed = h > 2f ? 0f : h;
		l.lastX = p.getX();
		l.lastZ = p.getZ();

		if (eyeInWater) {
			l.surfaceTicks = 0;
			l.diveTicks++;
			l.maxDepth = Math.max(l.maxDepth, depth);
			if (bcd) {
				// Deeper = denser air; poor buoyancy control (constant finning) burns air faster.
				float effort = breathingEffort(p, l);
				float before = l.pressure;
				l.pressure = Math.max(0f, l.pressure - USE_PER_TICK_PER_BAR * (1f + depth / 10f) * effort);
				if (before > 0f && l.pressure <= 0f && net.scubadive.tour.TourApi.isInTour(p)) {
					net.scubadive.tour.TourApi.onOutOfAir(p);
				}
			}
		} else {
			l.surfaceTicks = Math.min(l.surfaceTicks + 1, SURFACE_RESET_TICKS);
			if (bcd && !net.scubadive.tour.TourApi.noRefill(p)) {
				l.pressure = Math.min(DiveData.FULL_TANK, l.pressure + REFILL_PER_TICK);
				if (l.pressure >= DiveData.FULL_TANK && l.surfaceTicks >= SURFACE_GUARD_TICKS) {
					resetLog(l);
				}
			} else if (l.surfaceTicks >= SURFACE_RESET_TICKS && !net.scubadive.tour.TourApi.noRefill(p)) {
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

	/** Hiss on inflate, gurgle + rising bubbles on deflate; only while the air level actually changes. */
	private static void bcdEffects(ServerPlayer p, int dir) {
		Live l = live(p);
		if (now - l.lastSound >= SOUND_INTERVAL) {
			l.lastSound = now;
			if (dir > 0) {
				p.level().playSound(null, p.getX(), p.getY() + 1.0, p.getZ(), SoundEvents.BUBBLE_COLUMN_UPWARDS_AMBIENT, SoundSource.PLAYERS, 1.5f, 1.7f);
				p.level().playSound(null, p.getX(), p.getY() + 1.0, p.getZ(), SoundEvents.BUBBLE_COLUMN_UPWARDS_AMBIENT, SoundSource.PLAYERS, 1.2f, 1.3f);
			} else {
				p.level().playSound(null, p.getX(), p.getY() + 1.0, p.getZ(), SoundEvents.BUBBLE_COLUMN_BUBBLE_POP, SoundSource.PLAYERS, 1.7f, 0.7f);
				p.level().playSound(null, p.getX(), p.getY() + 1.0, p.getZ(), SoundEvents.BUBBLE_COLUMN_BUBBLE_POP, SoundSource.PLAYERS, 1.2f, 0.9f);
			}
		}
		if (dir < 0 && now % 2 == 0 && p.level() instanceof ServerLevel level) {
			// Shoulder height, slightly behind the diver (where the cylinder valve sits).
			double yaw = Math.toRadians(p.getYRot());
			double x = p.getX() + Math.sin(yaw) * 0.3;
			double z = p.getZ() - Math.cos(yaw) * 0.3;
			level.sendParticles(ParticleTypes.BUBBLE, x, p.getY() + 1.3, z, 3, 0.12, 0.05, 0.12, 0.08);
		}
	}

	private static void resetLog(Live l) {
		l.diveTicks = 0;
		l.maxDepth = 0f;
	}
}
