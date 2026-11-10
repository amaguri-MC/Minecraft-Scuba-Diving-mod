package net.scubadive.tour.site;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LightBlock;
import net.minecraft.world.level.block.SpeleothemBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SpeleothemThickness;

/**
 * A limestone cenote: a round spring in a wooded plateau, a funnel down into a big dark cavern (floor at 30 m)
 * and four tunnels leading away to end rooms. Glass skylights over three shafts let light fall into the cavern.
 * Stalactites and stalagmites are pointed dripstone.
 */
final class CenoteSite extends SiteGen {
	/** Lowest y of the rock mass. */
	private static final int BOTTOM = 62;
	private static final int[][] SHAFTS = {{-13, 14}, {14, -8}, {5, 26}};
	private static final int TREE_CELL = 8;

	/** Tunnel data: x0, z0, dirX, dirZ, length, y0, slope, phase. */
	private final double[][] tunnels;
	private final boolean[] cv = new boolean[RING_Y - BOTTOM + 8];

	CenoteSite(long seed) {
		super(seed);
		double[][] def = {
				{18, 6, 0, 34, 79, 0.20}, {-18, 3, 165, 34, 78, 0.18}, {2, 24, 82, 28, 80, 0.20}, {-4, -16, -102, 30, 77, 0.18}};
		tunnels = new double[def.length][];
		for (int i = 0; i < def.length; i++) {
			double a = Math.toRadians(def[i][2]);
			tunnels[i] = new double[] {def[i][0], def[i][1], Math.cos(a), Math.sin(a), def[i][3], def[i][4], def[i][5],
					Noise.rnd(seed + 300, i, 9) * 6.28};
		}
	}

	@Override
	int rimWidth() {
		return 0;
	}

	/** Radius of the spring at the surface. */
	private double rp(int lx, int lz) {
		return 12.0 + (Noise.value(seed + 301, lx * 0.12, lz * 0.12) - 0.5) * 3.0;
	}

	private double shaftRadius(int lx, int lz, int y) {
		return rp(lx, lz) - 2.8 * Noise.smooth((WATER_TOP - y) / 10.0);
	}

	/** Top solid y of the ground (WATER_TOP inside the spring). */
	private int ground(int lx, int lz) {
		double r = Math.sqrt((double) lx * lx + (double) lz * lz);
		double p = rp(lx, lz);
		if (r < p) {
			return WATER_TOP;
		}
		if (r < p + 4.0) {
			return H;
		}
		double n = Noise.fbm(seed + 302, lx * 0.05, lz * 0.05, 3);
		return H + Math.max(0, Math.min(3, (int) Math.round((n - 0.42) * 7.0)));
	}

	@Override
	int baseFloor(int lx, int lz) {
		return ground(lx, lz);
	}

	private boolean isShaft(int lx, int lz) {
		for (int[] s : SHAFTS) {
			int dx = lx - s[0];
			int dz = lz - s[1];
			if (dx * dx + dz * dz <= 2) {
				return true;
			}
		}
		return false;
	}

	private static double tunnelWobble(double[] t, double along) {
		return 3.5 * Math.sin(along * 0.13 + t[7]) + 1.2 * Math.sin(along * 0.31 + 2.0 * t[7]);
	}

	private static double tunnelY(double[] t, double along) {
		return t[5] - t[6] * Math.max(0.0, along) + 1.4 * Math.sin(along * 0.2 + t[7]);
	}

	/** True if the cell is open (water-filled) cave. */
	private boolean cave(int lx, int y, int lz) {
		if (y > WATER_TOP) {
			return false;
		}
		// spring funnel
		if (y >= 86) {
			double r = Math.sqrt((double) lx * lx + (double) lz * lz);
			if (r < shaftRadius(lx, lz, y)) {
				return true;
			}
		}
		// skylight shafts down to the cavern
		if (y >= 84 && isShaft(lx, lz)) {
			return true;
		}
		// main cavern: a lens with a distorted outline
		if (y <= 95 && Math.abs(lx) <= 32 && lz >= -22 && lz <= 30) {
			double a = lx / 30.0;
			double b = (lz - 4) / 24.0;
			double rho2 = (a * a + b * b) * (1.0 + (Noise.fbm(seed + 305, lx * 0.06, lz * 0.06, 2) - 0.5) * 0.5);
			if (rho2 < 0.9) {
				double fl = 69.0 + 11.0 * rho2 + (Noise.fbm(seed + 306, lx * 0.15, lz * 0.15, 1) - 0.5) * 2.0;
				double ce = 93.0 - 13.0 * rho2;
				if (y >= fl && y <= ce) {
					return true;
				}
			}
		}
		// tunnels and their end rooms
		for (double[] t : tunnels) {
			double px = lx - t[0];
			double pz = lz - t[1];
			double along = px * t[2] + pz * t[3];
			if (along < -4.0 || along > t[4] + 7.0) {
				continue;
			}
			double perp = -px * t[3] + pz * t[2];
			if (Math.abs(perp) > 12.0) {
				continue;
			}
			if (along <= t[4]) {
				double wob = tunnelWobble(t, along);
				double rh = 3.4 + 1.1 * Math.sin(along * 0.17 + t[7]);
				double rv = 2.6 + 0.5 * Math.sin(along * 0.23 + t[7]);
				double q = (perp - wob) / rh;
				double v = (y - tunnelY(t, along)) / rv;
				if (q * q + v * v < 1.0) {
					return true;
				}
			}
			double ea = along - (t[4] - 2.0);
			double ep = perp - tunnelWobble(t, t[4] - 2.0);
			double ey = y - (tunnelY(t, t[4] - 2.0) + 0.5);
			if ((ea * ea + ep * ep) / 42.25 + ey * ey / 17.6 < 1.0) {
				return true;
			}
		}
		return false;
	}

	private BlockState limestone(int lx, int y, int lz) {
		double r = Noise.rnd(seed + 303, lx >> 1, y, lz >> 1);
		if (r < 0.38) {
			return Blocks.SMOOTH_SANDSTONE.defaultBlockState();
		} else if (r < 0.60) {
			return Blocks.CALCITE.defaultBlockState();
		} else if (r < 0.70) {
			return Blocks.SANDSTONE.defaultBlockState();
		} else if (r < 0.84) {
			return Blocks.DRIPSTONE_BLOCK.defaultBlockState();
		} else if (r < 0.92) {
			return Blocks.TUFF.defaultBlockState();
		}
		return Blocks.SMOOTH_SANDSTONE.defaultBlockState();
	}

	private BlockState caveFloor(int lx, int lz) {
		double r = Noise.rnd(seed + 304, lx >> 1, lz >> 1);
		if (r < 0.45) {
			return Blocks.SAND.defaultBlockState();
		} else if (r < 0.65) {
			return Blocks.GRAVEL.defaultBlockState();
		} else if (r < 0.82) {
			return Blocks.CLAY.defaultBlockState();
		}
		return Blocks.CALCITE.defaultBlockState();
	}

	@Override
	BlockState solid(int lx, int lz, int y, int depth, int f) {
		return limestone(lx, y, lz);
	}

	@Override
	BlockState ringBlock(int lx, int lz, int y, int depth) {
		return depth == 0 ? Blocks.GRASS_BLOCK.defaultBlockState() : limestone(lx, y, lz);
	}

	@Override
	void column(SiteCtx c, int lx, int lz) {
		int d = edge(lx, lz);
		if (d <= 1) {
			ring(c, lx, lz);
			return;
		}
		int ground = ground(lx, lz);
		boolean pool = ground == WATER_TOP && Math.sqrt((double) lx * lx + (double) lz * lz) < rp(lx, lz);
		boolean shaft = isShaft(lx, lz);
		double r = Math.sqrt((double) lx * lx + (double) lz * lz);
		boolean terrace = r < rp(lx, lz) + 4.0;
		int top = pool ? WATER_TOP : ground;
		int n = top - BOTTOM + 1;
		for (int i = 0; i < n; i++) {
			cv[i] = cave(lx, BOTTOM + i, lz);
		}
		for (int i = 0; i < n; i++) {
			int y = BOTTOM + i;
			BlockState s;
			if (cv[i]) {
				s = WATER;
			} else if (y > WATER_TOP && shaft) {
				s = Blocks.GLASS.defaultBlockState();
			} else if (!pool && y == ground) {
				s = terrace ? (Noise.rnd(seed + 307, lx, lz) < 0.5 ? Blocks.SMOOTH_SANDSTONE.defaultBlockState() : Blocks.CALCITE.defaultBlockState())
						: Blocks.GRASS_BLOCK.defaultBlockState();
			} else if (!pool && y >= ground - 3) {
				s = terrace ? Blocks.SMOOTH_SANDSTONE.defaultBlockState() : Blocks.DIRT.defaultBlockState();
			} else if (i > 0 && i + 1 < n && cv[i + 1] && !cv[i - 1]) {
				s = caveFloor(lx, lz);
			} else {
				s = limestone(lx, y, lz);
			}
			c.set(lx, y, lz, s);
		}
		// grass cover on the plateau
		if (!pool && !terrace && !shaft && ground >= H) {
			double g = Noise.rnd(seed + 308, lx, lz);
			if (g < 0.2) {
				c.set(lx, ground + 1, lz, Blocks.SHORT_GRASS.defaultBlockState());
			} else if (g < 0.25) {
				c.set(lx, ground + 1, lz, Blocks.FERN.defaultBlockState());
			}
		}
		speleothems(c, lx, lz, n, shaft);
		if (lx == SHAFTS[0][0] && lz == SHAFTS[0][1] || lx == SHAFTS[1][0] && lz == SHAFTS[1][1] || lx == SHAFTS[2][0] && lz == SHAFTS[2][1]) {
			shaftLights(c, lx, lz, n);
		}
	}

	/** Stalactites under ceilings and stalagmites on floors (pointed dripstone). */
	private void speleothems(SiteCtx c, int lx, int lz, int n, boolean shaft) {
		if (shaft) {
			return;
		}
		for (int i = 1; i + 1 < n; i++) {
			int y = BOTTOM + i;
			if (y > WATER_TOP - 3) {
				break;
			}
			if (cv[i] && !cv[i + 1]) {
				// ceiling: hang down
				if (Noise.rnd(seed + 309, lx, y, lz) < 0.08) {
					int run = 0;
					while (i - run >= 0 && cv[i - run]) {
						run++;
					}
					int len = Math.min(1 + (int) (Noise.rnd(seed + 310, lx, y, lz) * 6.0), run - 1);
					pillar(c, lx, lz, y, len, Direction.DOWN);
				}
			} else if (cv[i] && !cv[i - 1]) {
				// floor: grow up
				if (Noise.rnd(seed + 311, lx, y, lz) < 0.06) {
					int run = 0;
					while (i + run < n && cv[i + run]) {
						run++;
					}
					int len = Math.min(1 + (int) (Noise.rnd(seed + 312, lx, y, lz) * 4.0), run - 2);
					pillar(c, lx, lz, y, len, Direction.UP);
				}
			}
		}
	}

	/** Pointed dripstone of len blocks starting at the attached end y, tip pointing in tip direction. */
	private static void pillar(SiteCtx c, int lx, int lz, int y, int len, Direction tip) {
		int step = tip == Direction.DOWN ? -1 : 1;
		for (int j = 0; j < len; j++) {
			SpeleothemThickness th;
			if (j == len - 1) {
				th = SpeleothemThickness.TIP;
			} else if (j == len - 2) {
				th = SpeleothemThickness.FRUSTUM;
			} else if (j == 0) {
				th = SpeleothemThickness.BASE;
			} else {
				th = SpeleothemThickness.MIDDLE;
			}
			c.set(lx, y + step * j, lz, Blocks.POINTED_DRIPSTONE.defaultBlockState()
					.setValue(SpeleothemBlock.TIP_DIRECTION, tip)
					.setValue(SpeleothemBlock.THICKNESS, th)
					.setValue(SpeleothemBlock.WATERLOGGED, true));
		}
	}

	/** Invisible light blocks down a shaft so a column of light reaches the cavern floor. */
	private void shaftLights(SiteCtx c, int lx, int lz, int n) {
		BlockState light = Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL, 13).setValue(LightBlock.WATERLOGGED, true);
		for (int i = 0; i < n; i++) {
			int y = BOTTOM + i;
			if (cv[i] && y <= 88 && y % 4 == 0) {
				c.set(lx, y, lz, light);
			}
		}
	}

	@Override
	void structures(SiteCtx c) {
		int r = DiveSites.RADIUS;
		int cells = (2 * r) / TREE_CELL + 1;
		for (int gx = 0; gx <= cells; gx++) {
			for (int gz = 0; gz <= cells; gz++) {
				int tx = -r + gx * TREE_CELL + (int) (Noise.rnd(seed + 321, gx, gz) * (TREE_CELL - 1));
				int tz = -r + gz * TREE_CELL + (int) (Noise.rnd(seed + 322, gx, gz) * (TREE_CELL - 1));
				if (tx < c.x0 - 3 || tx > c.x1 + 3 || tz < c.z0 - 3 || tz > c.z1 + 3) {
					continue;
				}
				if (edge(tx, tz) < 4 || Noise.rnd(seed + 323, gx, gz) > 0.5) {
					continue;
				}
				double dist = Math.sqrt((double) tx * tx + (double) tz * tz);
				int ground = ground(tx, tz);
				if (ground < H || dist < rp(tx, tz) + 7.0 || nearShaft(tx, tz)) {
					continue;
				}
				int h = 5 + (int) (Noise.rnd(seed + 324, gx, gz) * 4.0);
				Plants.tree(c, tx, ground, tz, h, Blocks.JUNGLE_LOG.defaultBlockState(), Blocks.JUNGLE_LEAVES.defaultBlockState());
			}
		}
	}

	private boolean nearShaft(int x, int z) {
		for (int[] s : SHAFTS) {
			if (Math.abs(x - s[0]) <= 3 && Math.abs(z - s[1]) <= 3) {
				return true;
			}
		}
		return false;
	}
}
