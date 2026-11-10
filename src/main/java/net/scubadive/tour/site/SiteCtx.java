package net.scubadive.tour.site;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * One build or clear slice: the part of the site that lies inside a single world chunk column.
 * Coordinates are local to the site origin; writes outside the slice (or the world height) are ignored,
 * so structures can be generated in full for every slice and still only touch their own chunk column.
 */
final class SiteCtx {
	/** Number of chunk columns per axis the site square can touch (129 blocks never spans more than 9). */
	static final int GRID = 9;

	final ServerLevel level;
	final BlockPos origin;
	final long seed;
	/** Inclusive local bounds of this slice. */
	final int x0;
	final int x1;
	final int z0;
	final int z1;
	final int minY;
	final int maxY;
	/** Lowest y any site writes. */
	final int bottom = DiveSites.SURFACE_Y - 66;
	private final BlockPos.MutableBlockPos mp = new BlockPos.MutableBlockPos();

	private SiteCtx(ServerLevel level, BlockPos origin, long seed, int x0, int x1, int z0, int z1) {
		this.level = level;
		this.origin = origin;
		this.seed = seed;
		this.x0 = x0;
		this.x1 = x1;
		this.z0 = z0;
		this.z1 = z1;
		this.minY = level.getMinY();
		this.maxY = level.getMinY() + level.getHeight() - 1;
	}

	/** Slice number step (0 .. GRID*GRID-1) or null when that chunk column does not meet the site. */
	static SiteCtx forStep(ServerLevel level, BlockPos origin, long seed, int step) {
		if (step < 0 || step >= GRID * GRID) {
			return null;
		}
		int r = DiveSites.RADIUS;
		int minWx = origin.getX() - r;
		int minWz = origin.getZ() - r;
		int cx = Math.floorDiv(minWx, 16) + step % GRID;
		int cz = Math.floorDiv(minWz, 16) + step / GRID;
		int wx0 = Math.max(cx * 16, minWx);
		int wx1 = Math.min(cx * 16 + 15, origin.getX() + r);
		int wz0 = Math.max(cz * 16, minWz);
		int wz1 = Math.min(cz * 16 + 15, origin.getZ() + r);
		if (wx0 > wx1 || wz0 > wz1) {
			return null;
		}
		return new SiteCtx(level, origin, seed, wx0 - origin.getX(), wx1 - origin.getX(), wz0 - origin.getZ(), wz1 - origin.getZ());
	}

	boolean in(int lx, int lz) {
		return lx >= x0 && lx <= x1 && lz >= z0 && lz <= z1;
	}

	/** Places a block (flag 2: no neighbour updates). Returns false when the position is outside the slice. */
	boolean set(int lx, int y, int lz, BlockState state) {
		if (!in(lx, lz) || y < minY || y > maxY) {
			return false;
		}
		mp.set(origin.getX() + lx, y, origin.getZ() + lz);
		level.setBlock(mp, state, 2);
		return true;
	}

	/** Current block, or air outside the slice. */
	BlockState get(int lx, int y, int lz) {
		if (!in(lx, lz) || y < minY || y > maxY) {
			return Blocks.AIR.defaultBlockState();
		}
		mp.set(origin.getX() + lx, y, origin.getZ() + lz);
		return level.getBlockState(mp);
	}

	BlockPos pos(int lx, int y, int lz) {
		return new BlockPos(origin.getX() + lx, y, origin.getZ() + lz);
	}

	RandomSource random(long salt) {
		return RandomSource.create(seed * 31L + salt * 0x9E3779B97F4A7C15L);
	}
}
