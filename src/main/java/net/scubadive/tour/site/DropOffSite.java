package net.scubadive.tour.site;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * A reef shelf about 10 m deep on the north side; at the edge a vertical wall drops to a dark sand floor at 56-60 m.
 * Horizontal caves are cut into the wall. The boat floats over the shelf, near the edge.
 */
final class DropOffSite extends SiteGen {
	/** Caves: x of the mouth, y of the centre, length into the wall. */
	private static final int[][] CAVES = {
			{-46, 66, 8}, {-31, 58, 9}, {-14, 72, 7}, {13, 64, 9}, {31, 56, 8}, {47, 69, 8}};
	private static final int CAVE_BAND = 11;
	private static final int DEEP_Y = WATER_TOP - 60;

	DropOffSite(long seed) {
		super(seed);
	}

	@Override
	int rimWidth() {
		return 26;
	}

	@Override
	double rimSlope() {
		return 2.6;
	}

	/** z of the wall top at column x. */
	private double edgeZ(int lx) {
		double n = Noise.fbm(seed + 201, lx * 0.03, 7.3, 2) - 0.5;
		double bay = Noise.fbm(seed + 202, lx * 0.15, 3.1, 1) - 0.5;
		return 15.0 + n * 12.0 + bay * 4.0;
	}

	@Override
	int baseFloor(int lx, int lz) {
		double t = lz - edgeZ(lx);
		if (t < 0) {
			double shelf = 10.0 + (Noise.fbm(seed + 203, lx * 0.06, lz * 0.06, 2) - 0.5) * 4.0;
			shelf += Math.max(0.0, -40.0 - lz) * 0.2; // the shelf slopes away to the north
			return WATER_TOP - (int) Math.round(shelf);
		}
		if (t < 2.0) {
			return WATER_TOP - 17 - (int) Math.round(Noise.rnd(seed + 204, lx, 0) * 2.0);
		}
		double deep = 56.0 + Noise.fbm(seed + 205, lx * 0.05, lz * 0.05, 2) * 4.0;
		return WATER_TOP - (int) Math.round(Math.min(60.0, deep));
	}

	@Override
	int lowest(int lx, int lz, int f) {
		int base = super.lowest(lx, lz, f);
		double t = lz - edgeZ(lx);
		if (t < 2.0 && t > -CAVE_BAND) {
			return Math.min(base, DEEP_Y - 2); // the wall is solid behind its face so caves can be cut
		}
		return base;
	}

	/** True if (lx, y, lz) is inside one of the wall caves. */
	private boolean cave(int lx, int y, int lz) {
		double into = edgeZ(lx) + 2.0 - lz; // blocks behind the wall face (the face is at t = 2)
		if (into < -0.5 || into > 10.0) {
			return false;
		}
		for (int i = 0; i < CAVES.length; i++) {
			int[] cv = CAVES[i];
			double dx = lx - cv[0];
			if (Math.abs(dx) > 4.0 || into > cv[2]) {
				continue;
			}
			double yc = cv[1] + 1.3 * Math.sin(into * 0.6 + i);
			boolean room = into > cv[2] - 3.0;
			double rx = room ? 3.8 : 2.7;
			double ry = room ? 3.0 : 2.3;
			double q = dx / rx;
			double v = (y - yc) / ry;
			if (q * q + v * v < 1.0) {
				return true;
			}
		}
		return false;
	}

	/** Wall and deep floor rock in horizontal strata. */
	private BlockState strata(int lx, int y, int lz) {
		double r = Noise.rnd(seed + 206, (lx + lz) >> 3, y >> 1, 0);
		if (r < 0.45) {
			return Blocks.STONE.defaultBlockState();
		} else if (r < 0.68) {
			return Blocks.ANDESITE.defaultBlockState();
		} else if (r < 0.84) {
			return Blocks.TUFF.defaultBlockState();
		} else if (r < 0.94) {
			return Blocks.DIORITE.defaultBlockState();
		}
		return Blocks.GRANITE.defaultBlockState();
	}

	@Override
	BlockState solid(int lx, int lz, int y, int depth, int f) {
		double t = lz - edgeZ(lx);
		if (t < 2.0 && t > -CAVE_BAND - 1 && cave(lx, y, lz)) {
			return WATER;
		}
		if (depth <= 1) {
			if (t >= 2.0) {
				// dark floor: sand with gravel and tuff patches
				double r = Noise.rnd(seed + 207, lx >> 1, lz >> 1);
				return r < 0.7 ? Blocks.SAND.defaultBlockState() : (r < 0.9 ? Blocks.GRAVEL.defaultBlockState() : Blocks.TUFF.defaultBlockState());
			}
			return Blocks.SAND.defaultBlockState();
		}
		return strata(lx, y, lz);
	}

	@Override
	BlockState rimBlock(int lx, int lz, int y, int depth, int f) {
		return strata(lx, y, lz);
	}

	@Override
	void decorateColumn(SiteCtx c, int lx, int lz, int f, boolean rimmed) {
		double t = lz - edgeZ(lx);
		if (rimmed) {
			return;
		}
		if (t < 0) {
			// dense reef, richer towards the wall
			double edgeBoost = t > -8.0 ? 0.15 : 0.0;
			Plants.reef(c, seed, lx, lz, f, 0.45 + edgeBoost, 0.1);
		} else if (t >= 2.0 && Noise.rnd(seed + 208, lx, lz) < 0.004) {
			// lone boulder on the deep floor
			for (int i = 0; i < 2 + (int) (Noise.rnd(seed + 209, lx, lz) * 3.0); i++) {
				c.set(lx, f + 1 + i, lz, rock(lx, f + 1 + i, lz));
			}
		}
	}
}
