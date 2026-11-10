package net.scubadive.client.camera;

import com.mojang.blaze3d.platform.NativeImage;

import net.scubadive.camera.Breakdown;

/** Pixel post-processing for saved photos: vertical shake blur and turbidity haze. */
public final class PhotoEffects {
	private static final int BLUR_RADIUS = 3;
	private static final int WATER = 0x7FC4E6;
	private static final int SAND = 0xC9B48A;
	private static final float MAX_HAZE = 0.7f;

	private PhotoEffects() { }

	/** Haze strength 0..0.7 from the distance and silt factors. */
	public static float hazeStrength(Breakdown b) {
		float siltAmount = clamp01((1.0f - b.silt()) / 0.7f);
		float strength = (1.0f - b.distance()) * 0.45f + siltAmount * 0.5f;
		return Math.min(MAX_HAZE, Math.max(0.0f, strength));
	}

	/** Returns a processed copy; the input stays untouched and owned by the caller. */
	public static NativeImage apply(NativeImage src, Breakdown b) {
		int w = src.getWidth();
		int h = src.getHeight();
		boolean shake = b.neutral() < 1.0f;
		float haze = hazeStrength(b);

		// Haze colour: blue for distance, sand for silt, weighted by their contribution.
		float wd = (1.0f - b.distance()) * 0.45f;
		float ws = clamp01((1.0f - b.silt()) / 0.7f) * 0.5f;
		float sum = wd + ws;
		float mix = sum > 1.0E-6f ? ws / sum : 0.0f;
		int hr = lerp((WATER >> 16) & 0xFF, (SAND >> 16) & 0xFF, mix);
		int hg = lerp((WATER >> 8) & 0xFF, (SAND >> 8) & 0xFF, mix);
		int hb = lerp(WATER & 0xFF, SAND & 0xFF, mix);

		NativeImage out = new NativeImage(w, h, false);
		for (int x = 0; x < w; x++) {
			for (int y = 0; y < h; y++) {
				int r;
				int g;
				int bl;
				if (shake) {
					int sr = 0;
					int sg = 0;
					int sb = 0;
					int n = 0;
					for (int k = -BLUR_RADIUS; k <= BLUR_RADIUS; k++) {
						int yy = Math.max(0, Math.min(h - 1, y + k));
						int p = src.getPixel(x, yy);
						sr += (p >> 16) & 0xFF;
						sg += (p >> 8) & 0xFF;
						sb += p & 0xFF;
						n++;
					}
					r = sr / n;
					g = sg / n;
					bl = sb / n;
				} else {
					int p = src.getPixel(x, y);
					r = (p >> 16) & 0xFF;
					g = (p >> 8) & 0xFF;
					bl = p & 0xFF;
				}
				if (haze > 0.0f) {
					r = lerp(r, hr, haze);
					g = lerp(g, hg, haze);
					bl = lerp(bl, hb, haze);
				}
				out.setPixel(x, y, 0xFF000000 | (r << 16) | (g << 8) | bl);
			}
		}
		return out;
	}

	private static int lerp(int a, int b, float t) {
		return Math.round(a + (b - a) * t);
	}

	private static float clamp01(float v) {
		return Math.max(0.0f, Math.min(1.0f, v));
	}
}
