package net.scubadive.tour.site;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/** Cold blue sea (8-28 m) under a drifting floe of packed and blue ice with holes, icicles below. */
final class IceFloeSite extends SiteGen {
	private static final BlockState PACKED = Blocks.PACKED_ICE.defaultBlockState();
	private static final BlockState BLUE = Blocks.BLUE_ICE.defaultBlockState();
	private static final BlockState THIN = Blocks.ICE.defaultBlockState();

	IceFloeSite(long seed) {
		super(seed);
	}

	@Override
	int baseFloor(int lx, int lz) {
		double n = Noise.fbm(seed + 111, lx * 0.03, lz * 0.03, 3) - 0.5;
		double depth = 19.0 + n * 24.0;
		double bump = Math.max(0.0, Noise.fbm(seed + 112, lx * 0.07, lz * 0.07, 2) - 0.58) * 36.0;
		depth -= bump;
		// keep the water under the boat deep enough
		double w = Noise.clamp01(1.0 - Math.sqrt((double) lx * lx + (double) lz * lz) / 26.0);
		depth += w * Math.max(0.0, 16.0 - depth);
		int d = (int) Math.round(Math.max(8.0, Math.min(28.0, depth)));
		return WATER_TOP - d;
	}

	private boolean outcrop(int lx, int lz) {
		return Noise.fbm(seed + 112, lx * 0.07, lz * 0.07, 2) > 0.62;
	}

	@Override
	BlockState solid(int lx, int lz, int y, int depth, int f) {
		if (outcrop(lx, lz)) {
			return rock(lx, y, lz);
		}
		if (depth <= 1) {
			return Noise.rnd(seed + 113, lx >> 1, lz >> 1) < 0.8 ? Blocks.GRAVEL.defaultBlockState() : Blocks.ANDESITE.defaultBlockState();
		}
		return rock(lx, y, lz);
	}

	@Override
	BlockState rimBlock(int lx, int lz, int y, int depth, int f) {
		if (depth == 0 && f > WATER_TOP) {
			return Blocks.SNOW_BLOCK.defaultBlockState();
		}
		return rock(lx, y, lz);
	}

	/** Ice thickness at the column: 0 = open water, 1..3 = plate. */
	private int iceThickness(int lx, int lz) {
		// open water around the boat
		double bx = lx / 9.0;
		double bz = lz / 12.5;
		if (bx * bx + bz * bz < 1.0) {
			return 0;
		}
		double hole = Noise.fbm(seed + 132, lx * 0.09, lz * 0.09, 2);
		if (hole > 0.67) {
			return 0;
		}
		double crack = Noise.fbm(seed + 133, lx * 0.06, lz * 0.06, 2);
		if (Math.abs(crack - 0.5) < 0.016) {
			return 0;
		}
		double n = Noise.fbm(seed + 131, lx * 0.045, lz * 0.045, 3);
		return 1 + (n > 0.5 ? 1 : 0) + (n > 0.58 ? 1 : 0);
	}

	@Override
	void decorateColumn(SiteCtx c, int lx, int lz, int f, boolean rimmed) {
		if (f >= WATER_TOP - 1) {
			return;
		}
		int t = iceThickness(lx, lz);
		if (t > 0) {
			for (int i = 0; i < t; i++) {
				BlockState s;
				if (t == 1 && Noise.rnd(seed + 134, lx, lz) < 0.25) {
					s = THIN;
				} else if (t == 3 && i == 1) {
					s = BLUE;
				} else {
					s = PACKED;
				}
				c.set(lx, WATER_TOP - i, lz, s);
			}
			double n = Noise.fbm(seed + 131, lx * 0.045, lz * 0.045, 3);
			if (n > 0.6) {
				c.set(lx, H, lz, PACKED); // a slab sticking out of the water
			}
			// icicles hanging under the plate
			if (Noise.rnd(seed + 135, lx, lz) < 0.07) {
				int len = 2 + (int) (Noise.rnd(seed + 136, lx, lz) * 7.0);
				for (int i = 0; i < len; i++) {
					int y = WATER_TOP - t - i;
					if (y <= f + 1) {
						break;
					}
					c.set(lx, y, lz, i == len - 1 ? BLUE : PACKED);
				}
			}
			return;
		}
		// sparse kelp in the open water
		if (Noise.rnd(seed + 137, lx, lz) < 0.03 && !outcrop(lx, lz)) {
			int room = WATER_TOP - 3 - (f + 1) + 1;
			int len = Math.min(room, 3 + (int) (Noise.rnd(seed + 138, lx, lz) * 7.0));
			if (len >= 2) {
				Plants.kelp(c, lx, f + 1, lz, len);
			}
		}
	}
}
