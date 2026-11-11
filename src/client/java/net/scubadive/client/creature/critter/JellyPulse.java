package net.scubadive.client.creature.critter;

import net.minecraft.client.model.geom.ModelPart;

/** Shared bell-pulse and tentacle-sway maths for the jellyfish models. */
final class JellyPulse {
	private JellyPulse() {
	}

	/** 0..1..0 bell contraction during the first 20% of each pulse period, 0 while relaxing. */
	static float squeeze(float ageInTicks, float period) {
		float phase = (ageInTicks % period) / period;
		return phase < 0.2f ? (float) Math.sin(phase / 0.2f * Math.PI) : 0.0f;
	}

	/** Bell narrows and gets taller while contracting. */
	static void bell(ModelPart bell, float squeeze, float amount) {
		bell.xScale = 1.0f - amount * squeeze;
		bell.zScale = 1.0f - amount * squeeze;
		bell.yScale = 1.0f + amount * 0.65f * squeeze;
	}

	/**
	 * Sways a two-segment tentacle: the tip lags behind the base, so a slow wave runs down it.
	 * Contraction sweeps the tentacles inward a little.
	 */
	static void sway(ModelPart base, ModelPart tip, float t, int i, float speed, float amp, float squeeze) {
		float p = i * 0.7f;
		base.xRot += (float) Math.sin(t * speed + p) * amp + squeeze * 0.12f;
		base.zRot += (float) Math.cos(t * speed * 0.8f + p * 1.3f) * amp;
		if (tip != null) {
			tip.xRot += (float) Math.sin(t * speed + p - 0.9f) * amp * 1.3f - squeeze * 0.08f;
			tip.zRot += (float) Math.cos(t * speed * 0.8f + p * 1.3f - 0.9f) * amp * 1.3f;
		}
	}
}
