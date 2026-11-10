package net.scubadive.dive;

/** Local-player prediction state. Written only by the client tick, read by DiveApi on the client. */
public final class DiveClientState {
	/** Ticks the predicted air stays authoritative after the last key press (covers sync lag). */
	public static final int PREDICT_TICKS = 25;

	public static float predictedAir;
	public static int sinceInput = 1000;
	public static boolean neutral;

	private DiveClientState() {
	}

	/** BCD air to use on the client: own prediction right after input, otherwise the synced value. */
	public static float air(float synced) {
		return sinceInput <= PREDICT_TICKS ? predictedAir : synced;
	}

	public static void reset() {
		sinceInput = 1000;
		neutral = false;
	}
}
