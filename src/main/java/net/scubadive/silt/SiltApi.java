package net.scubadive.silt;

import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Cross-module contract for stirred-up sediment. Owner: silt module. Signatures must not change.
 */
public final class SiltApi {
	private SiltApi() {
	}

	/** Sediment density at a point, 0 (clear) .. 1 (opaque). Server side. */
	public static float siltAt(Level level, Vec3 pos) {
		return 0f;
	}

	/** Strongest sediment density met along the segment from -> to, 0..1. Server side. */
	public static float siltAlong(Level level, Vec3 from, Vec3 to) {
		return 0f;
	}
}
