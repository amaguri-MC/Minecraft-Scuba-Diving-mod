package net.scubadive.client.reef;

/** Client-side registration for the reef module. Entry point: init(). */
public final class ScubaReefsClient {
	private ScubaReefsClient() {
	}

	public static void init() {
		ReefTint.init();
	}
}
