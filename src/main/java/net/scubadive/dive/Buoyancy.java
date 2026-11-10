package net.scubadive.dive;

/** Pure buoyancy maths shared by server and client. */
public final class Buoyancy {
	/** Per-tick change of BCD air while a key is held. */
	public static final float AIR_STEP = 0.01f;
	/** Vertical acceleration per unit of net buoyancy (blocks/tick^2). */
	public static final double ACCEL = 0.02;
	/** |net| below this counts as neutral. */
	public static final float NEUTRAL_NET = 0.06f;
	/** Vertical speed (blocks/tick) below this counts as neutral. */
	public static final float NEUTRAL_SPEED = 0.03f;

	private Buoyancy() {
	}

	/** Net buoyancy in [-1, 1]; negative sinks. Air expands/compresses with ambient pressure. */
	public static float net(float air, float depth) {
		float a = Math.max(0f, Math.min(1f, air));
		float net = (4f * a + 0.3f) / (1f + Math.max(0f, depth) / 10f) - 0.3f;
		return Math.max(-1f, Math.min(1f, net));
	}

	public static boolean isNeutral(float net, float verticalSpeed) {
		return Math.abs(net) < NEUTRAL_NET && Math.abs(verticalSpeed) < NEUTRAL_SPEED;
	}
}
