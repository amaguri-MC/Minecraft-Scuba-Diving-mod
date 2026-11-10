package net.scubadive.tour.site;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

/** Fresh-water lake (4-14 m) with mud, clay and sand bed, water plants, sunken logs and a wooded shore all around. */
final class LakeSite extends SiteGen {
	private static final int TREE_CELL = 7;
	private static final int LOGS = 8;
	/** Sunken log data: x, z, length, axis (0 = x, 1 = z). */
	private final int[][] logs = new int[LOGS][];

	LakeSite(long seed) {
		super(seed);
		for (int i = 0; i < LOGS; i++) {
			double ang = Noise.rnd(seed + 150, i, 1) * Math.PI * 2.0;
			double rad = 12.0 + Noise.rnd(seed + 150, i, 2) * 24.0;
			int x = (int) Math.round(Math.cos(ang) * rad);
			int z = (int) Math.round(Math.sin(ang) * rad);
			if (Math.abs(x) < 9 && Math.abs(z) < 11) {
				x += x < 0 ? -10 : 10;
			}
			int len = 5 + (int) (Noise.rnd(seed + 150, i, 3) * 5.0);
			int axis = Noise.rnd(seed + 150, i, 4) < 0.5 ? 0 : 1;
			logs[i] = new int[] {x, z, len, axis};
		}
	}

	@Override
	int rimWidth() {
		return 0; // the land itself closes the lake
	}

	/** Shore factor: below 1 inside the lake, 1 at the waterline. */
	private double shore(int lx, int lz) {
		double r = Math.sqrt((double) lx * lx + (double) lz * lz);
		double n = Noise.fbm(seed + 151, lx * 0.035, lz * 0.035, 2) - 0.5;
		return r / (47.0 + n * 20.0);
	}

	@Override
	int baseFloor(int lx, int lz) {
		double s = shore(lx, lz);
		if (s < 1.0) {
			double t = Math.pow(1.0 - s * s, 0.7);
			double bump = (Noise.fbm(seed + 152, lx * 0.08, lz * 0.08, 2) - 0.5) * 3.0;
			int depth = (int) Math.round(Math.min(14.0, 14.0 * t + bump));
			return depth <= 0 ? WATER_TOP : WATER_TOP - depth;
		}
		double n = Noise.fbm(seed + 153, lx * 0.06, lz * 0.06, 3) - 0.5;
		int hgt = (int) Math.round((s - 1.0) * 7.5 + n * 5.0);
		return WATER_TOP + Math.max(0, hgt);
	}

	@Override
	BlockState solid(int lx, int lz, int y, int depth, int f) {
		if (f >= H + 1) {
			if (depth == 0) {
				return Blocks.GRASS_BLOCK.defaultBlockState();
			}
			return depth <= 3 ? Blocks.DIRT.defaultBlockState() : Blocks.STONE.defaultBlockState();
		}
		if (f >= WATER_TOP - 2) {
			// beach and shallows
			return depth == 0 ? Blocks.SAND.defaultBlockState() : Blocks.DIRT.defaultBlockState();
		}
		double n = Noise.fbm(seed + 154, lx * 0.1, lz * 0.1, 2);
		if (depth == 0) {
			if (n < 0.4) {
				return Blocks.SAND.defaultBlockState();
			}
			return n < 0.58 ? Blocks.MUD.defaultBlockState() : Blocks.CLAY.defaultBlockState();
		}
		if (depth <= 3) {
			return n >= 0.4 && n < 0.58 ? Blocks.MUD.defaultBlockState() : (n >= 0.58 ? Blocks.CLAY.defaultBlockState() : Blocks.DIRT.defaultBlockState());
		}
		return Blocks.STONE.defaultBlockState();
	}

	@Override
	BlockState ringBlock(int lx, int lz, int y, int depth) {
		return solid(lx, lz, y, depth, RING_Y);
	}

	@Override
	void decorateColumn(SiteCtx c, int lx, int lz, int f, boolean rimmed) {
		if (f >= H + 1) {
			double r = Noise.rnd(seed + 155, lx, lz);
			if (r < 0.22) {
				c.set(lx, f + 1, lz, Blocks.SHORT_GRASS.defaultBlockState());
			} else if (r < 0.245) {
				c.set(lx, f + 1, lz, Blocks.FERN.defaultBlockState());
			} else if (r < 0.26) {
				c.set(lx, f + 1, lz, (r < 0.253 ? Blocks.DANDELION : Blocks.POPPY).defaultBlockState());
			}
			return;
		}
		int depth = WATER_TOP - f;
		if (depth < 1) {
			return;
		}
		double r = Noise.rnd(seed + 156, lx, lz);
		if (depth <= 4 && depth >= 1 && r > 0.94) {
			c.set(lx, H, lz, Blocks.LILY_PAD.defaultBlockState());
			return;
		}
		if (depth >= 2 && depth <= 11) {
			if (r < 0.17) {
				Plants.seagrass(c, lx, f + 1, lz, depth >= 3 && Noise.rnd(seed + 157, lx, lz) < 0.3);
			} else if (r < 0.215 && depth >= 4) {
				int len = Math.min(depth - 1, 2 + (int) (Noise.rnd(seed + 158, lx, lz) * 5.0));
				Plants.kelp(c, lx, f + 1, lz, len);
			}
		}
	}

	@Override
	void structures(SiteCtx c) {
		sunkenLogs(c);
		trees(c);
	}

	private void sunkenLogs(SiteCtx c) {
		for (int i = 0; i < LOGS; i++) {
			int[] l = logs[i];
			boolean alongX = l[3] == 0;
			int half = l[2] / 2;
			for (int k = -half; k <= half; k++) {
				int x = l[0] + (alongX ? k : 0);
				int z = l[1] + (alongX ? 0 : k);
				if (!c.in(x, z)) {
					continue;
				}
				int f = floor(x, z);
				if (f > WATER_TOP - 3) {
					continue; // do not leave the water
				}
				BlockState log = Blocks.OAK_LOG.defaultBlockState().setValue(BlockStateProperties.AXIS, alongX ? Direction.Axis.X : Direction.Axis.Z);
				c.set(x, f + 1, z, log);
				// a short branch stub
				if (Noise.rnd(seed + 159, x, z) < 0.2) {
					BlockState up = Blocks.OAK_LOG.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y);
					c.set(x, f + 2, z, up);
					if (Noise.rnd(seed + 160, x, z) < 0.5) {
						c.set(x, f + 3, z, up);
					}
				}
			}
		}
	}

	private void trees(SiteCtx c) {
		int r = DiveSites.RADIUS;
		int cells = (2 * r) / TREE_CELL + 1;
		for (int gx = 0; gx <= cells; gx++) {
			for (int gz = 0; gz <= cells; gz++) {
				int tx = -r + gx * TREE_CELL + (int) (Noise.rnd(seed + 161, gx, gz) * (TREE_CELL - 1));
				int tz = -r + gz * TREE_CELL + (int) (Noise.rnd(seed + 162, gx, gz) * (TREE_CELL - 1));
				if (tx < c.x0 - 3 || tx > c.x1 + 3 || tz < c.z0 - 3 || tz > c.z1 + 3) {
					continue;
				}
				if (edge(tx, tz) < 4 || Noise.rnd(seed + 163, gx, gz) > 0.55) {
					continue;
				}
				int ground = floor(tx, tz);
				if (ground < H + 1) {
					continue;
				}
				int h = 4 + (int) (Noise.rnd(seed + 164, gx, gz) * 3.0);
				boolean birch = Noise.rnd(seed + 165, gx, gz) < 0.3;
				Plants.tree(c, tx, ground, tz, h,
						(birch ? Blocks.BIRCH_LOG : Blocks.OAK_LOG).defaultBlockState(),
						(birch ? Blocks.BIRCH_LEAVES : Blocks.OAK_LEAVES).defaultBlockState());
			}
		}
	}
}
