package net.scubadive.silt;

import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Cross-module contract for stirred-up sediment. Owner: silt module. Signatures must not change.
 */
public final class SiltApi {
	private static final double STEP = 0.5;

	private SiltApi() {
	}

	/** Sediment density at a point, 0 (clear) .. 1 (opaque). Server side. */
	public static float siltAt(Level level, Vec3 pos) {
		if (level == null || level.isClientSide() || pos == null) {
			return 0f;
		}
		return Math.min(1f, SiltClouds.densityAt(level, pos.x, pos.y, pos.z));
	}

	/** Strongest sediment density met along the segment from -> to, 0..1. Server side. */
	public static float siltAlong(Level level, Vec3 from, Vec3 to) {
		if (level == null || level.isClientSide() || from == null || to == null) {
			return 0f;
		}
		double len = from.distanceTo(to);
		int steps = Math.max(1, (int) Math.ceil(len / STEP));
		float best = 0f;
		for (int i = 0; i <= steps; i++) {
			double t = (double) i / steps;
			best = Math.max(best, SiltClouds.densityAt(level, from.x + (to.x - from.x) * t, from.y + (to.y - from.y) * t, from.z + (to.z - from.z) * t));
		}
		return Math.min(1f, best);
	}
}
