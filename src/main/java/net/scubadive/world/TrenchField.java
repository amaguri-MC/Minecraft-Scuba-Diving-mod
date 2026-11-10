package net.scubadive.world;

import net.minecraft.world.level.levelgen.XoroshiroRandomSource;
import net.minecraft.world.level.levelgen.synth.SimplexNoise;

/**
 * Deterministic deep-trench shape, a pure function of (seed, world x, world z).
 * Trenches follow the zero contour of a low-frequency noise (domain-warped so they wind),
 * thinned out by a coverage mask and given a varying width. Used by worldgen and the /scubadive trench command.
 */
public final class TrenchField {
	/** Frequency of the contour noise: roughly one trench per few hundred blocks. */
	private static final double MAIN_FREQ = 1.0 / 700.0;
	private static final double WARP_FREQ = 1.0 / 180.0;
	private static final double WARP_AMP = 70.0;
	private static final double MASK_FREQ = 1.0 / 450.0;
	private static final double WIDTH_FREQ = 1.0 / 300.0;
	private static final double GRAD_EPS = 3.0;

	private record Noises(long seed, SimplexNoise main, SimplexNoise warpX, SimplexNoise warpZ, SimplexNoise mask,
			SimplexNoise width, SimplexNoise floor, SimplexNoise cluster) {
	}

	private static volatile Noises cached;

	private static Noises noises(long seed) {
		Noises n = cached;
		if (n == null || n.seed != seed) {
			n = new Noises(seed,
					new SimplexNoise(new XoroshiroRandomSource(seed ^ 0x5CBA_D1BEL)),
					new SimplexNoise(new XoroshiroRandomSource(seed ^ 0x7A11_0001L)),
					new SimplexNoise(new XoroshiroRandomSource(seed ^ 0x7A11_0002L)),
					new SimplexNoise(new XoroshiroRandomSource(seed ^ 0x3A5C_0003L)),
					new SimplexNoise(new XoroshiroRandomSource(seed ^ 0x817D_0004L)),
					new SimplexNoise(new XoroshiroRandomSource(seed ^ 0xF100_0005L)),
					new SimplexNoise(new XoroshiroRandomSource(seed ^ 0xC105_0006L)));
			cached = n;
		}
		return n;
	}

	private static double smooth(double v) {
		v = Math.max(0.0, Math.min(1.0, v));
		return v * v * (3.0 - 2.0 * v);
	}

	/** Trench-ness in [0,1]: 0 outside, 1 on the centre line. Half width is 12..24 blocks. */
	public static double shape(long seed, double x, double z) {
		Noises n = noises(seed);
		double cover = smooth((n.mask.getValue(x * MASK_FREQ, z * MASK_FREQ) + 0.1) / 0.4);
		if (cover <= 0.0) {
			return 0.0;
		}
		double w01 = Math.max(0.0, Math.min(1.0, (n.width.getValue(x * WIDTH_FREQ, z * WIDTH_FREQ) + 1.0) * 0.5));
		double halfWidth = (12.0 + 12.0 * w01) * cover;
		if (halfWidth < 3.0) {
			return 0.0;
		}
		double px = x + n.warpX.getValue(x * WARP_FREQ, z * WARP_FREQ) * WARP_AMP;
		double pz = z + n.warpZ.getValue(x * WARP_FREQ + 311.0, z * WARP_FREQ - 127.0) * WARP_AMP;
		double f0 = n.main.getValue(px * MAIN_FREQ, pz * MAIN_FREQ);
		double gx = (n.main.getValue((px + GRAD_EPS) * MAIN_FREQ, pz * MAIN_FREQ)
				- n.main.getValue((px - GRAD_EPS) * MAIN_FREQ, pz * MAIN_FREQ)) / (2.0 * GRAD_EPS);
		double gz = (n.main.getValue(px * MAIN_FREQ, (pz + GRAD_EPS) * MAIN_FREQ)
				- n.main.getValue(px * MAIN_FREQ, (pz - GRAD_EPS) * MAIN_FREQ)) / (2.0 * GRAD_EPS);
		double grad = Math.max(Math.sqrt(gx * gx + gz * gz), 1.0e-6);
		double dist = Math.abs(f0) / grad; // approximate distance (blocks) to the contour line
		return Math.max(0.0, Math.min(1.0, 1.0 - dist / halfWidth));
	}

	/** Cross-section profile: 0 at the rim, 1 on the floor, steep walls in between. */
	public static double profile(double t) {
		return smooth(t * 2.2);
	}

	/** Small undulation of the trench floor, about -1..1. */
	public static double floorNoise(long seed, double x, double z) {
		return noises(seed).floor.getValue(x / 9.0, z / 9.0);
	}

	/** Low-frequency noise (about -1..1) that groups basalt columns into clusters. */
	public static double cluster(long seed, double x, double z) {
		return noises(seed).cluster.getValue(x / 36.0, z / 36.0);
	}

	private TrenchField() {
	}
}
