package net.scubadive.tour.site;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Base of the six dive sites. A site is a height-field (baseFloor) with a rock rim, filled with water up to
 * the surface, plus per-column decoration and a few structures. Everything is a pure function of the seed and
 * the local coordinates, so any slice can be built on its own and in any order.
 */
abstract class SiteGen {
	static final int H = DiveSites.SURFACE_Y;
	/** y of the top water block. */
	static final int WATER_TOP = H - 1;
	/** Top of the 2-block outer wall. */
	static final int RING_Y = H + 2;
	/** Invisible barrier reaches this high above the wall. */
	static final int BARRIER_TOP = H + 24;
	static final BlockState WATER = Blocks.WATER.defaultBlockState();
	static final BlockState AIR = Blocks.AIR.defaultBlockState();
	static final BlockState BARRIER = Blocks.BARRIER.defaultBlockState();

	final long seed;

	SiteGen(long seed) {
		this.seed = seed;
	}

	/** Terrain height (top solid y) before the rim is applied. */
	abstract int baseFloor(int lx, int lz);

	/** Solid block of the terrain. depth = f - y, 0 at the top. Falling blocks only at depth 0..1. */
	abstract BlockState solid(int lx, int lz, int y, int depth, int f);

	/** Width of the sloping rock rim inside the 2-block wall. */
	int rimWidth() {
		return 14;
	}

	double rimSlope() {
		return 2.4;
	}

	/** Rock for the rim slope. */
	BlockState rimBlock(int lx, int lz, int y, int depth, int f) {
		return rock(lx, y, lz);
	}

	/** Rock for the 2-block wall. */
	BlockState ringBlock(int lx, int lz, int y, int depth) {
		return rock(lx, y, lz);
	}

	/** Lowest solid y written for a column of floor height f (default: 3 thick, down to the lowest neighbour top). */
	int lowest(int lx, int lz, int f) {
		int m = Math.min(Math.min(floor(lx - 1, lz), floor(lx + 1, lz)), Math.min(floor(lx, lz - 1), floor(lx, lz + 1)));
		return Math.min(f - 2, m);
	}

	/** Plants, ice and other things that depend on one column only. f = floor height. */
	void decorateColumn(SiteCtx c, int lx, int lz, int f, boolean rimmed) {
	}

	/** Multi-column structures; must only call c.set (which clips to the slice). */
	void structures(SiteCtx c) {
	}

	/** Builds the slice. */
	final void build(SiteCtx c) {
		for (int lx = c.x0; lx <= c.x1; lx++) {
			for (int lz = c.z0; lz <= c.z1; lz++) {
				column(c, lx, lz);
			}
		}
		structures(c);
		Boat.build(c);
	}

	void column(SiteCtx c, int lx, int lz) {
		int d = edge(lx, lz);
		if (d <= 1) {
			ring(c, lx, lz);
			return;
		}
		int base = baseFloor(lx, lz);
		int rim = rimTop(lx, lz, d);
		boolean rimmed = rim > base;
		int f = Math.max(base, rim);
		int low = Math.max(c.bottom, lowest(lx, lz, f));
		for (int y = low; y <= f; y++) {
			c.set(lx, y, lz, rimmed ? rimBlock(lx, lz, y, f - y, f) : solid(lx, lz, y, f - y, f));
		}
		for (int y = f + 1; y <= WATER_TOP; y++) {
			c.set(lx, y, lz, WATER);
		}
		decorateColumn(c, lx, lz, f, rimmed);
	}

	/** The outer 2 blocks: rock up to RING_Y, invisible barrier above. */
	final void ring(SiteCtx c, int lx, int lz) {
		for (int y = c.bottom; y <= RING_Y; y++) {
			c.set(lx, y, lz, ringBlock(lx, lz, y, RING_Y - y));
		}
		for (int y = RING_Y + 1; y <= BARRIER_TOP; y++) {
			c.set(lx, y, lz, BARRIER);
		}
	}

	/** Final floor height of a column (rim included). */
	final int floor(int lx, int lz) {
		int d = edge(lx, lz);
		if (d <= 1) {
			return RING_Y;
		}
		return Math.max(baseFloor(lx, lz), rimTop(lx, lz, d));
	}

	/** Distance from the site edge: 0 on the outermost block row. */
	static int edge(int lx, int lz) {
		return DiveSites.RADIUS - Math.max(Math.abs(lx), Math.abs(lz));
	}

	final int rimTop(int lx, int lz, int d) {
		if (d <= 1) {
			return RING_Y;
		}
		if (d > rimWidth() + 1) {
			return Integer.MIN_VALUE;
		}
		double n = Noise.fbm(seed + 11, lx * 0.12, lz * 0.12, 2) - 0.5;
		return (int) Math.round(RING_Y - (d - 1) * rimSlope() + n * 5.0);
	}

	/** Mixed grey rock, chunky so it does not look like noise. */
	final BlockState rock(int lx, int y, int lz) {
		double r = Noise.rnd(seed + 90, lx >> 1, y >> 1, lz >> 1);
		if (r < 0.5) {
			return Blocks.STONE.defaultBlockState();
		} else if (r < 0.74) {
			return Blocks.ANDESITE.defaultBlockState();
		} else if (r < 0.88) {
			return Blocks.COBBLESTONE.defaultBlockState();
		} else if (r < 0.95) {
			return Blocks.TUFF.defaultBlockState();
		}
		return Blocks.DIORITE.defaultBlockState();
	}
}
