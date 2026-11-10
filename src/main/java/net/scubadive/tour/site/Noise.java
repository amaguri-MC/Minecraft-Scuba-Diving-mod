package net.scubadive.tour.site;

/** Stateless hash noise: everything a site builds is a pure function of (seed, local coordinates). */
final class Noise {
	private Noise() {
	}

	private static long mix(long h) {
		h ^= h >>> 33;
		h *= 0xff51afd7ed558ccdL;
		h ^= h >>> 33;
		h *= 0xc4ceb9fe1a85ec53L;
		h ^= h >>> 33;
		return h;
	}

	static long hash(long seed, int x, int y, int z) {
		long h = mix(seed * 0x9E3779B97F4A7C15L + x * 0xC2B2AE3D27D4EB4FL);
		h = mix(h ^ (y * 0x165667B19E3779F9L));
		return mix(h + z * 0x27D4EB2F165667C5L);
	}

	/** Uniform value in [0, 1). */
	static double rnd(long seed, int x, int z) {
		return (hash(seed, x, 0, z) >>> 11) * 0x1.0p-53;
	}

	static double rnd(long seed, int x, int y, int z) {
		return (hash(seed, x, y, z) >>> 11) * 0x1.0p-53;
	}

	/** Smooth value noise in [0, 1). */
	static double value(long seed, double x, double z) {
		int x0 = (int) Math.floor(x);
		int z0 = (int) Math.floor(z);
		double fx = x - x0;
		double fz = z - z0;
		fx = fx * fx * (3.0 - 2.0 * fx);
		fz = fz * fz * (3.0 - 2.0 * fz);
		double a = rnd(seed, x0, z0);
		double b = rnd(seed, x0 + 1, z0);
		double c = rnd(seed, x0, z0 + 1);
		double d = rnd(seed, x0 + 1, z0 + 1);
		double top = a + (b - a) * fx;
		double bottom = c + (d - c) * fx;
		return top + (bottom - top) * fz;
	}

	/** Fractal value noise in [0, 1), mostly between 0.25 and 0.75. */
	static double fbm(long seed, double x, double z, int octaves) {
		double sum = 0.0;
		double total = 0.0;
		double amp = 1.0;
		double freq = 1.0;
		for (int i = 0; i < octaves; i++) {
			sum += amp * value(seed + i * 131L, x * freq, z * freq);
			total += amp;
			amp *= 0.5;
			freq *= 2.0;
		}
		return sum / total;
	}

	static double clamp01(double v) {
		return v < 0.0 ? 0.0 : (v > 1.0 ? 1.0 : v);
	}

	static double smooth(double t) {
		t = clamp01(t);
		return t * t * (3.0 - 2.0 * t);
	}
}
