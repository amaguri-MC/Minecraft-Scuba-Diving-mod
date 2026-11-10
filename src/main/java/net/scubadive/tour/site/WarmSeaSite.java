package net.scubadive.tour.site;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/** White-sand shallows (6-22 m) with coral mounds, rock outcrops, anemone patches and seagrass. */
final class WarmSeaSite extends SiteGen {
	private static final int MOUNDS = 48;
	/** Mound data: x, z, radius, height, type (0 coral, 1 rock). */
	private final int[][] mounds = new int[MOUNDS][];

	WarmSeaSite(long seed) {
		super(seed);
		for (int i = 0; i < MOUNDS; i++) {
			int x = (int) (Noise.rnd(seed + 100, i, 1) * 104.0) - 52;
			int z = (int) (Noise.rnd(seed + 100, i, 2) * 104.0) - 52;
			if (Math.abs(x) < 10 && Math.abs(z) < 12) {
				x += x < 0 ? -12 : 12; // keep the water under the boat clear
			}
			int type = Noise.rnd(seed + 100, i, 5) < 0.3 ? 1 : 0;
			int radius = 3 + (int) (Noise.rnd(seed + 100, i, 3) * 5.0);
			int height = (type == 1 ? 3 : 2) + (int) (Noise.rnd(seed + 100, i, 4) * (type == 1 ? 6.0 : 4.0));
			mounds[i] = new int[] {x, z, radius, height, type};
		}
	}

	private int seabed(int lx, int lz) {
		double n = Noise.fbm(seed + 41, lx * 0.025, lz * 0.025, 3) - 0.5;
		double depth = 14.0 + n * 20.0 + lz * 0.06;
		int d = (int) Math.round(Math.max(7.0, Math.min(22.0, depth)));
		return WATER_TOP - d;
	}

	/** Mound height at the column (0 = none). */
	private int moundHeight(int lx, int lz) {
		int best = 0;
		for (int[] m : mounds) {
			int dx = lx - m[0];
			int dz = lz - m[1];
			int r = m[2];
			int r2 = dx * dx + dz * dz;
			if (r2 >= r * r) {
				continue;
			}
			double t = 1.0 - (double) r2 / (r * r);
			int h = (int) Math.round(m[3] * Math.pow(t, 0.7));
			best = Math.max(best, h);
		}
		return best;
	}

	private int moundType(int lx, int lz) {
		int best = -1;
		int type = 0;
		for (int[] m : mounds) {
			int dx = lx - m[0];
			int dz = lz - m[1];
			int r = m[2];
			int r2 = dx * dx + dz * dz;
			if (r2 >= r * r) {
				continue;
			}
			int h = (int) Math.round(m[3] * Math.pow(1.0 - (double) r2 / (r * r), 0.7));
			if (h > best) {
				best = h;
				type = m[4];
			}
		}
		return type;
	}

	@Override
	int baseFloor(int lx, int lz) {
		int b = seabed(lx, lz);
		int h = moundHeight(lx, lz);
		return h > 0 ? Math.min(b + h, WATER_TOP - 3) : b;
	}

	@Override
	BlockState solid(int lx, int lz, int y, int depth, int f) {
		int h = moundHeight(lx, lz);
		if (h > 0) {
			if (moundType(lx, lz) == 0 && depth <= 1) {
				return Plants.moundCoral(seed, lx, y, lz);
			}
			return rock(lx, y, lz);
		}
		if (depth <= 1) {
			return Blocks.SAND.defaultBlockState();
		}
		return depth <= 8 ? Blocks.SANDSTONE.defaultBlockState() : Blocks.STONE.defaultBlockState();
	}

	@Override
	BlockState rimBlock(int lx, int lz, int y, int depth, int f) {
		// Pale reef rock rising to the surface.
		double r = Noise.rnd(seed + 91, lx >> 1, y >> 1, lz >> 1);
		return r < 0.4 ? Blocks.ANDESITE.defaultBlockState() : (r < 0.7 ? Blocks.DIORITE.defaultBlockState() : Blocks.STONE.defaultBlockState());
	}

	@Override
	void decorateColumn(SiteCtx c, int lx, int lz, int f, boolean rimmed) {
		if (rimmed) {
			return;
		}
		int h = moundHeight(lx, lz);
		if (h > 0 && moundType(lx, lz) == 1) {
			if (Noise.rnd(seed + 15, lx, lz) < 0.12) {
				Plants.seagrass(c, lx, f + 1, lz, false);
			}
			return;
		}
		Plants.reef(c, seed, lx, lz, f, h > 0 ? 0.7 : 0.45, 0.12);
	}
}
