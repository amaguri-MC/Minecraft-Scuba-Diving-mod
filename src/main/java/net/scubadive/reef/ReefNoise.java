package net.scubadive.reef;

/** Cheap deterministic value noise used to cluster reefs into zones. */
final class ReefNoise {
	private static final float CELL = 44.0F;

	private ReefNoise() {
	}

	/** 0..1, high values mark a reef zone (tens of blocks across). */
	static float zone(long seed, int x, int z) {
		int s = (int) (seed ^ (seed >>> 32));
		float fx = x / CELL;
		float fz = z / CELL;
		float n = noise(fx, fz, s) * 0.75F + noise(fx * 2.7F + 3.1F, fz * 2.7F - 8.4F, s + 7) * 0.25F;
		return Math.max(0.0F, Math.min(1.0F, (n - 0.5F) * 1.7F + 0.5F));
	}

	private static float noise(float x, float z, int salt) {
		int x0 = (int) Math.floor(x);
		int z0 = (int) Math.floor(z);
		float tx = smooth(x - x0);
		float tz = smooth(z - z0);
		float a = hash(x0, z0, salt);
		float b = hash(x0 + 1, z0, salt);
		float c = hash(x0, z0 + 1, salt);
		float d = hash(x0 + 1, z0 + 1, salt);
		float top = a + (b - a) * tx;
		float bottom = c + (d - c) * tx;
		return top + (bottom - top) * tz;
	}

	private static float smooth(float t) {
		return t * t * (3.0F - 2.0F * t);
	}

	/** Uniform 0..1 hash of an integer cell. */
	static float hash(int x, int z, int salt) {
		int h = x * 374761393 + z * 668265263 + salt * 1274126177;
		h = (h ^ (h >>> 13)) * 1274126177;
		h ^= h >>> 16;
		return (h & 0xFFFFFF) / (float) 0x1000000;
	}
}
