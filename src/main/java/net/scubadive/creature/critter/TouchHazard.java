package net.scubadive.creature.critter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;

/** Shared "touching it hurts" check for spiny or stinging creatures, with a per-player cooldown. */
final class TouchHazard {
	private final Map<UUID, Long> lastHit = new HashMap<>();
	private final int cooldown;

	TouchHazard(int cooldown) {
		this.cooldown = cooldown;
	}

	/** Players whose hitbox overlaps the creature's (slightly grown) hitbox and are off cooldown. */
	List<Player> touching(Mob mob, double grow) {
		if (!(mob.level() instanceof ServerLevel)) {
			return List.of();
		}
		long now = mob.level().getGameTime();
		List<Player> hits = mob.level().getEntitiesOfClass(Player.class, mob.getBoundingBox().inflate(grow),
				p -> !p.isSpectator() && !p.isCreative());
		hits.removeIf(p -> {
			Long last = this.lastHit.get(p.getUUID());
			return last != null && now - last < this.cooldown;
		});
		for (Player p : hits) {
			this.lastHit.put(p.getUUID(), now);
		}
		if (this.lastHit.size() > 32) {
			this.lastHit.values().removeIf(t -> now - t > 200);
		}
		return hits;
	}

	static void prick(Mob mob, Player player, float damage) {
		if (mob.level() instanceof ServerLevel level) {
			player.hurtServer(level, mob.damageSources().cactus(), damage);
		}
	}

	static void sting(Player player, int ticks, int amplifier) {
		player.addEffect(new MobEffectInstance(MobEffects.POISON, ticks, amplifier));
	}
}
