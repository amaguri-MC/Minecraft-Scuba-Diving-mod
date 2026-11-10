package net.scubadive.tour.site;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

import net.scubadive.tour.DivePoint;

/**
 * Builds and clears one tour dive site around an origin in the tour dimension, a slice per call
 * so loading is spread over ticks. Owner: dive site module (H3). Signatures must not change.
 * Origin: x/z centre of the site, y = water surface level (top water block is origin.y - 1).
 */
public final class DiveSites {
	/** Half width of the site in blocks; nothing is written outside origin +- RADIUS horizontally. */
	public static final int RADIUS = 64;
	/** Water surface y used by every site. */
	public static final int SURFACE_Y = 100;

	private DiveSites() {
	}

	/** Number of build slices for the point (each slice should take at most a few ms). */
	public static int buildSteps(DivePoint point) {
		return 1;
	}

	/** Builds slice step (0-based) of the site. The chunks of the site area are force-loaded by the caller. */
	public static void buildStep(ServerLevel level, BlockPos origin, DivePoint point, long seed, int step) {
	}

	/** Where divers stand on the boat at the start (top of the boat deck). */
	public static BlockPos boatSpawn(BlockPos origin, int index) {
		return origin.offset(index % 4 - 2, 1, index / 4);
	}

	/** Boat deck area check for players who must wait on the boat. */
	public static boolean onBoat(BlockPos origin, BlockPos pos) {
		return Math.abs(pos.getX() - origin.getX()) <= 4 && Math.abs(pos.getZ() - origin.getZ()) <= 6 && pos.getY() >= origin.getY();
	}

	/** Number of clear slices. */
	public static int clearSteps() {
		return 1;
	}

	/** Clears slice step of the site back to empty (air above SURFACE_Y, nothing below). */
	public static void clearStep(ServerLevel level, BlockPos origin, int step) {
	}
}
