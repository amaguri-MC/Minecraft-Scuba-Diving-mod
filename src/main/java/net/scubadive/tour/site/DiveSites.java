package net.scubadive.tour.site;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import net.scubadive.tour.DivePoint;

/**
 * Builds and clears one tour dive site around an origin in the tour dimension, a slice per call
 * so loading is spread over ticks. Owner: dive site module (H3). Signatures must not change.
 * Origin: x/z centre of the site, y = water surface level (top water block is origin.y - 1).
 *
 * <p>A slice is the part of the site inside one world chunk column (see {@link SiteCtx}); the site square
 * never touches more than 9x9 of them, so there are 81 slices and some may be empty. Everything is generated
 * from the seed and local coordinates only, and written with flag 2 (no neighbour updates).
 */
public final class DiveSites {
	/** Half width of the site in blocks; nothing is written outside origin +- RADIUS horizontally. */
	public static final int RADIUS = 64;
	/** Water surface y used by every site. */
	public static final int SURFACE_Y = 100;
	/** Clear range: below the lowest site block up to above the highest tree and barrier. */
	private static final int CLEAR_BELOW = 66;
	private static final int CLEAR_ABOVE = 30;

	private DiveSites() {
	}

	/** Number of build slices for the point (each slice should take at most a few ms). */
	public static int buildSteps(DivePoint point) {
		return SiteCtx.GRID * SiteCtx.GRID;
	}

	/** Builds slice step (0-based) of the site. The chunks of the site area are force-loaded by the caller. */
	public static void buildStep(ServerLevel level, BlockPos origin, DivePoint point, long seed, int step) {
		SiteCtx ctx = SiteCtx.forStep(level, origin, seed, step);
		if (ctx == null) {
			return;
		}
		SiteGen gen = switch (point) {
			case WARM_SEA -> new WarmSeaSite(seed);
			case ICE_FLOE -> new IceFloeSite(seed);
			case LAKE -> new LakeSite(seed);
			case WRECK -> new WreckSite(seed);
			case DROP_OFF -> new DropOffSite(seed);
			case CENOTE -> new CenoteSite(seed);
		};
		gen.build(ctx);
	}

	/** Where divers stand on the boat at the start (top of the boat deck). */
	public static BlockPos boatSpawn(BlockPos origin, int index) {
		return Boat.spawn(origin, index);
	}

	/** Boat deck area check for players who must wait on the boat. */
	public static boolean onBoat(BlockPos origin, BlockPos pos) {
		return Boat.on(origin, pos);
	}

	/** Number of clear slices. */
	public static int clearSteps() {
		return SiteCtx.GRID * SiteCtx.GRID;
	}

	/** Clears slice step of the site back to empty (air above SURFACE_Y, nothing below). */
	public static void clearStep(ServerLevel level, BlockPos origin, int step) {
		SiteCtx ctx = SiteCtx.forStep(level, origin, 0L, step);
		if (ctx == null) {
			return;
		}
		BlockState air = Blocks.AIR.defaultBlockState();
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
		int lo = Math.max(ctx.minY, SURFACE_Y - CLEAR_BELOW);
		int hi = Math.min(ctx.maxY, SURFACE_Y + CLEAR_ABOVE);
		for (int lx = ctx.x0; lx <= ctx.x1; lx++) {
			for (int lz = ctx.z0; lz <= ctx.z1; lz++) {
				for (int y = lo; y <= hi; y++) {
					pos.set(origin.getX() + lx, y, origin.getZ() + lz);
					if (!level.getBlockState(pos).isAir()) {
						// flag 2 = send to clients, 32 = do not drop contents of removed containers
						level.setBlock(pos, air, Block.UPDATE_CLIENTS | Block.UPDATE_SUPPRESS_DROPS);
					}
				}
			}
		}
	}
}
