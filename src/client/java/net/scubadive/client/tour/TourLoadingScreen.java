package net.scubadive.client.tour;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Full-screen "preparing the tour" screen with a progress bar. Closed by the server. */
public final class TourLoadingScreen extends Screen {
	private static final int WHITE = 0xFFFFFFFF;
	private static final int GRAY = 0xFFA0A0A0;
	private static final int BAR_BG = 0xFF303030;
	private static final int BAR_FG = 0xFF3FB6D9;
	/** Closes itself if the server stops sending progress. */
	private static final int STALE_TICKS = 60 * 20;

	private final String pointKey;
	private int percent;
	private int idle;

	public TourLoadingScreen(String pointKey) {
		super(Component.translatable("screen.scubadive.tour_loading"));
		this.pointKey = pointKey;
	}

	void update(int percent) {
		this.percent = Math.max(0, Math.min(100, percent));
		this.idle = 0;
	}

	@Override
	public boolean shouldCloseOnEsc() {
		return false;
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	@Override
	public void tick() {
		if (++this.idle > STALE_TICKS && this.minecraft != null) {
			this.minecraft.gui.setScreen(null);
		}
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
		graphics.fill(0, 0, this.width, this.height, 0xFF06141F);
		int cx = this.width / 2;
		int cy = this.height / 2;
		graphics.centeredText(this.font, this.title, cx, cy - 30, WHITE);
		graphics.centeredText(this.font, Component.translatable(this.pointKey), cx, cy - 16, GRAY);
		int w = Math.min(200, this.width - 40);
		int x0 = cx - w / 2;
		graphics.fill(x0 - 1, cy - 1, x0 + w + 1, cy + 11, WHITE);
		graphics.fill(x0, cy, x0 + w, cy + 10, BAR_BG);
		graphics.fill(x0, cy, x0 + w * this.percent / 100, cy + 10, BAR_FG);
		graphics.centeredText(this.font, this.percent + "%", cx, cy + 16, WHITE);
	}
}
