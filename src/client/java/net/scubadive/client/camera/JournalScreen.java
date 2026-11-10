package net.scubadive.client.camera;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import net.scubadive.camera.Journal;
import net.scubadive.camera.ScubaCamera;
import net.scubadive.species.Species;

/** Species journal. Lists every {@link Species}; unphotographed ones show as "???". */
public final class JournalScreen extends Screen {
	private static final int ROW_HEIGHT = 14;
	private static final int LIST_TOP = 66;
	private static final int WHITE = 0xFFFFFFFF;
	private static final int GRAY = 0xFFA0A0A0;
	private static final int GOLD = 0xFFFFD84A;

	private int page;
	private boolean tour;
	private Button wildTab;
	private Button tourTab;
	private Button prev;
	private Button next;

	public JournalScreen() {
		super(Component.translatable("screen.scubadive.journal.title"));
	}

	private int perPage() {
		return Math.max(1, (this.height - LIST_TOP - 60) / ROW_HEIGHT);
	}

	private int pageCount() {
		return Math.max(1, (Species.values().length + perPage() - 1) / perPage());
	}

	@Override
	protected void init() {
		this.page = Math.min(this.page, pageCount() - 1);
		this.wildTab = this.addRenderableWidget(Button.builder(Component.translatable("screen.scubadive.journal.tab_wild"), b -> setTab(false))
				.pos(this.width / 2 - 82, 4).size(80, 20).build());
		this.tourTab = this.addRenderableWidget(Button.builder(Component.translatable("screen.scubadive.journal.tab_tour"), b -> setTab(true))
				.pos(this.width / 2 + 2, 4).size(80, 20).build());
		this.prev = this.addRenderableWidget(Button.builder(Component.literal("<"), b -> changePage(-1))
				.pos(this.width / 2 - 110, this.height - 28).size(30, 20).build());
		this.addRenderableWidget(Button.builder(Component.translatable("gui.done"), b -> this.onClose())
				.pos(this.width / 2 - 40, this.height - 28).size(80, 20).build());
		this.next = this.addRenderableWidget(Button.builder(Component.literal(">"), b -> changePage(1))
				.pos(this.width / 2 + 80, this.height - 28).size(30, 20).build());
		updateButtons();
	}

	private void setTab(boolean tour) {
		this.tour = tour;
		this.page = 0;
		updateButtons();
	}

	private Journal currentJournal() {
		if (this.minecraft == null || this.minecraft.player == null) return Journal.EMPTY;
		return this.tour ? ScubaCamera.tourJournalOf(this.minecraft.player) : ScubaCamera.journalOf(this.minecraft.player);
	}

	private void changePage(int delta) {
		this.page = Math.max(0, Math.min(pageCount() - 1, this.page + delta));
		updateButtons();
	}

	private void updateButtons() {
		this.wildTab.active = this.tour;
		this.tourTab.active = !this.tour;
		this.prev.active = this.page > 0;
		this.next.active = this.page < pageCount() - 1;
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		if (super.mouseClicked(event, doubleClick)) return true;
		if (event.button() != 0 || this.minecraft == null || this.minecraft.player == null) return false;
		Species[] all = Species.values();
		int left = this.width / 2 - 150;
		int per = perPage();
		int start = this.page * per;
		double mx = event.x();
		double my = event.y();
		if (mx < left || mx > left + 260 || my < LIST_TOP) return false;
		int i = start + (int) ((my - LIST_TOP) / ROW_HEIGHT);
		if (i < start || i >= Math.min(all.length, start + per)) return false;
		Species s = all[i];
		if (!currentJournal().has(s.id)) return false;
		this.minecraft.gui.setScreen(new PhotoViewerScreen(this, s, this.tour));
		return true;
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
		// Widgets only; the background is drawn by the screen framework (never call extractBackground here).
		super.extractRenderState(graphics, mouseX, mouseY, delta);

		Journal journal = currentJournal();
		Species[] all = Species.values();
		int found = 0;
		for (Species s : all) if (journal.has(s.id)) found++;

		graphics.centeredText(this.font, this.title.getString(), this.width / 2, 30, WHITE);
		String summary = Component.translatable("screen.scubadive.journal.found", found, all.length).getString()
				+ "    " + Component.translatable("screen.scubadive.journal.total", journal.total()).getString();
		graphics.centeredText(this.font, summary, this.width / 2, 42, GOLD);

		int left = this.width / 2 - 150;
		graphics.text(this.font, Component.translatable("screen.scubadive.journal.col_name").getString(), left, 54, GRAY);
		graphics.text(this.font, Component.translatable("screen.scubadive.journal.col_best").getString(), left + 170, 54, GRAY);
		graphics.text(this.font, Component.translatable("screen.scubadive.journal.col_count").getString(), left + 230, 54, GRAY);

		int per = perPage();
		int start = this.page * per;
		for (int i = start; i < Math.min(all.length, start + per); i++) {
			Species s = all[i];
			Journal.Entry entry = journal.get(s.id);
			int y = LIST_TOP + (i - start) * ROW_HEIGHT;
			if (entry == null) {
				graphics.text(this.font, Component.translatable("screen.scubadive.journal.unknown").getString(), left, y, GRAY);
			} else {
				graphics.text(this.font, Component.translatable(s.langKey()).getString(), left, y, WHITE);
				graphics.text(this.font, String.valueOf(entry.best()), left + 170, y, GOLD);
				graphics.text(this.font, String.valueOf(entry.count()), left + 230, y, WHITE);
			}
		}
		graphics.centeredText(this.font, Component.translatable("screen.scubadive.journal.hint").getString(), this.width / 2, this.height - 54, GRAY);
		graphics.centeredText(this.font, (this.page + 1) + " / " + pageCount(), this.width / 2, this.height - 42, GRAY);
	}
}
