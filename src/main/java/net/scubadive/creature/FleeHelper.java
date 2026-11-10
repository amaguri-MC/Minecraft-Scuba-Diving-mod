package net.scubadive.creature;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

/** Shared helper for skittish fish. Read-only for all creature modules. */
public final class FleeHelper {
	private FleeHelper() {
	}

	/** Nearest non-spectator player within radius whose approach is "too close", or null. */
	public static Player tooClose(Mob mob, double radius) {
		Player p = mob.level().getNearestPlayer(mob, radius);
		if (p == null || p.isSpectator() || (p.isCreative() && p.isShiftKeyDown())) {
			return null;
		}
		return p;
	}

	/** Horizontal-ish unit vector pointing away from the player (small vertical part). */
	public static Vec3 awayFrom(Mob mob, Player player) {
		Vec3 d = mob.position().subtract(player.position());
		Vec3 v = new Vec3(d.x, d.y * 0.3, d.z);
		double len = v.length();
		return len < 1.0E-4 ? new Vec3(1, 0, 0) : v.scale(1.0 / len);
	}
}
