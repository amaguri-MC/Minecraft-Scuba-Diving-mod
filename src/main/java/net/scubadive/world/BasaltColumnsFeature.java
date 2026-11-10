package net.scubadive.world;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.Direction;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Honeycomb basalt columns on trench floors. Columns are cells of a global hexagonal grid, so every
 * block decides its own cell from world coordinates and chunk borders line up. Called by DeepTrenchFeature
 * after carving; only writes inside the given 16x16 chunk and only into water.
 */
public final class BasaltColumnsFeature {
	/** Hexagon circumradius in blocks (pixel hexagons about 5-6 blocks across). */
	private static final double SIZE = 2.9;
	private static final double SQRT3 = Math.sqrt(3.0);
	private static final int REF_FLOOR = -40;
	private static final int MIN_HEIGHT = 12;
	private static final int MAX_HEIGHT = 35;

	private static final BlockState BASALT = Blocks.BASALT.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y);
	private static final BlockState SMOOTH = Blocks.SMOOTH_BASALT.defaultBlockState();
	private static final BlockState POLISHED = Blocks.POLISHED_BASALT.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y);

	/**
	 * @param floorY top solid block of each column after carving, indexed [dx][dz]; Integer.MIN_VALUE = not part of the trench
	 */
	public static void place(WorldGenLevel level, int minX, int minZ, long seed, int[][] floorY) {
		Map<Long, int[]> cells = new HashMap<>(); // key -> {present, topY}
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
		for (int dx = 0; dx < 16; dx++) {
			for (int dz = 0; dz < 16; dz++) {
				int floor = floorY[dx][dz];
				if (floor == Integer.MIN_VALUE) {
					continue;
				}
				int x = minX + dx;
				int z = minZ + dz;
				// Pointy-top hex grid, cube rounding of the block centre.
				double qf = (SQRT3 / 3.0 * (x + 0.5) - (z + 0.5) / 3.0) / SIZE;
				double rf = (2.0 / 3.0 * (z + 0.5)) / SIZE;
				double sf = -qf - rf;
				int q = (int) Math.round(qf);
				int r = (int) Math.round(rf);
				int s = (int) Math.round(sf);
				double dq = Math.abs(q - qf);
				double dr = Math.abs(r - rf);
				double ds = Math.abs(s - sf);
				if (dq > dr && dq > ds) {
					q = -r - s;
				} else if (dr > ds) {
					r = -q - s;
				}
				long key = ((long) q << 32) ^ (r & 0xFFFFFFFFL);
				int[] cell = cells.get(key);
				if (cell == null) {
					cell = cell(seed, q, r);
					cells.put(key, cell);
				}
				if (cell[0] == 0) {
					continue;
				}
				int top = cell[1];
				for (int y = floor + 1; y <= top; y++) {
					pos.set(x, y, z);
					if (!level.getBlockState(pos).is(Blocks.WATER)) {
						continue; // never overwrite solid blocks
					}
					level.setBlock(pos, pick(seed, x, y, z, y == top), 2);
				}
			}
		}
	}

	/** Decides, from the hex cell alone, whether it holds a column and how tall. */
	private static int[] cell(long seed, int q, int r) {
		double cx = SIZE * SQRT3 * (q + r / 2.0);
		double cz = SIZE * 1.5 * r;
		double t = TrenchField.shape(seed, cx, cz);
		if (t < 0.6) {
			return new int[] {0, 0};
		}
		double cluster = TrenchField.cluster(seed, cx, cz);
		if (cluster < 0.15) {
			return new int[] {0, 0};
		}
		double h01 = hash01(seed, q, 0, r);
		if (h01 < 0.1) {
			return new int[] {0, 0}; // a few missing cells
		}
		double strength = Math.min(1.0, (cluster - 0.15) / 0.6);
		int height = MIN_HEIGHT + (int) Math.round((MAX_HEIGHT - MIN_HEIGHT) * (0.6 * strength + 0.4 * h01));
		height = Math.max(MIN_HEIGHT, Math.min(MAX_HEIGHT, height));
		return new int[] {1, REF_FLOOR + height};
	}

	private static BlockState pick(long seed, int x, int y, int z, boolean isTop) {
		double h = hash01(seed, x, y, z);
		if (isTop) {
			return h < 0.4 ? POLISHED : (h < 0.7 ? SMOOTH : BASALT);
		}
		return h < 0.07 ? SMOOTH : BASALT;
	}

	private static double hash01(long seed, int x, int y, int z) {
		long h = seed ^ (x * 0x9E3779B97F4A7C15L) ^ (y * 0xC2B2AE3D27D4EB4FL) ^ (z * 0x165667B19E3779F9L);
		h ^= h >>> 33;
		h *= 0xFF51AFD7ED558CCDL;
		h ^= h >>> 33;
		h *= 0xC4CEB9FE1A85EC53L;
		h ^= h >>> 33;
		return (h >>> 11) * (1.0 / (1L << 53));
	}

	private BasaltColumnsFeature() {
	}
}
