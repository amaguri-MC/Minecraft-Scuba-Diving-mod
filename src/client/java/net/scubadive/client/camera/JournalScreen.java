package net.scubadive.client.camera;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import net.scubadive.camera.Journal;
import net.scubadive.camera.ScubaCamera;
import net.scubadive.species.Species;

/**
 * Species journal, split into tabs by {@link Species.Category}. Unphotographed species show as "？？？" with a hint
 * (habitat and length).
 */
public final class JournalScreen extends Screen {
	private static final int ROW_HEIGHT = 14;
	private static final int LIST_TOP = 96;
	private static final int DIM = 0xFF7A8A99;
	private static final int WHITE = 0xFFFFFFFF;
	private static final int GRAY = 0xFFA0A0A0;
	private static final int GOLD = 0xFFFFD84A;

	private int page;
	private boolean tour;
	private Button wildTab;
	private Button tourTab;
	private Button prev;
	private Button next;
	/** Selected category tab; null = all. */
	private Species.Category category;
	private final List<Button> categoryTabs = new ArrayList<>();

	public JournalScreen() {
		super(Component.translatable("screen.scubadive.journal.title"));
	}

	private int perPage() {
		return Math.max(1, (this.height - LIST_TOP - 60) / ROW_HEIGHT);
	}

	private List<Species> shown() {
		List<Species> list = new ArrayList<>();
		for (Species s : Species.values()) {
			if (this.category == null || s.category == this.category) {
				list.add(s);
			}
		}
		return list;
	}

	private int pageCount() {
		return Math.max(1, (shown().size() + perPage() - 1) / perPage());
	}

	@Override
	protected void init() {
		this.page = Math.min(this.page, pageCount() - 1);
		this.wildTab = this.addRenderableWidget(Button.builder(Component.translatable("screen.scubadive.journal.tab_wild"), b -> setTab(false))
				.pos(this.width / 2 - 82, 4).size(80, 20).build());
		this.tourTab = this.addRenderableWidget(Button.builder(Component.translatable("screen.scubadive.journal.tab_tour"), b -> setTab(true))
				.pos(this.width / 2 + 2, 4).size(80, 20).build());
		// Category tabs: "all" + one per category, in two rows of five.
		this.categoryTabs.clear();
		Species.Category[] cats = Species.Category.values();
		int perRow = 5;
		int tabW = Math.min(84, (this.width - 16) / perRow);
		int rowLeft = this.width / 2 - tabW * perRow / 2;
		for (int i = 0; i <= cats.length; i++) {
			Species.Category c = i == 0 ? null : cats[i - 1];
			Component label = c == null ? Component.translatable("screen.scubadive.journal.tab_all") : Component.translatable(c.langKey());
			Button tab = this.addRenderableWidget(Button.builder(label, b -> setCategory(c))
					.pos(rowLeft + (i % perRow) * tabW, 28 + (i / perRow) * 20).size(tabW, 20).build());
			this.categoryTabs.add(tab);
		}
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

	/** Selects a category tab (null = all). Public for the automated client test. */
	public void selectCategory(Species.Category c) {
		setCategory(c);
	}

	private void setCategory(Species.Category c) {
		this.category = c;
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
		Species.Category[] cats = Species.Category.values();
		for (int i = 0; i < this.categoryTabs.size(); i++) {
			Species.Category c = i == 0 ? null : cats[i - 1];
			this.categoryTabs.get(i).active = c != this.category;
		}
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		if (super.mouseClicked(event, doubleClick)) return true;
		if (event.button() != 0 || this.minecraft == null || this.minecraft.player == null) return false;
		List<Species> all = shown();
		int left = this.width / 2 - 150;
		int per = perPage();
		int start = this.page * per;
		double mx = event.x();
		double my = event.y();
		if (mx < left || mx > left + 260 || my < LIST_TOP) return false;
		int i = start + (int) ((my - LIST_TOP) / ROW_HEIGHT);
		if (i < start || i >= Math.min(all.size(), start + per)) return false;
		Species s = all.get(i);
		if (!currentJournal().has(s.id)) return false;
		this.minecraft.gui.setScreen(new PhotoViewerScreen(this, s, this.tour));
		return true;
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
		// Widgets only; the background is drawn by the screen framework (never call extractBackground here).
		super.extractRenderState(graphics, mouseX, mouseY, delta);

		Journal journal = currentJournal();
		List<Species> all = shown();
		int found = 0;
		for (Species s : all) if (journal.has(s.id)) found++;

		String summary = Component.translatable("screen.scubadive.journal.found", found, all.size()).getString()
				+ "    " + Component.translatable("screen.scubadive.journal.total", journal.total()).getString();
		graphics.centeredText(this.font, summary, this.width / 2, 72, GOLD);

		int left = this.width / 2 - 150;
		graphics.text(this.font, Component.translatable("screen.scubadive.journal.col_name").getString(), left, 84, GRAY);
		graphics.text(this.font, Component.translatable("screen.scubadive.journal.col_best").getString(), left + 170, 84, GRAY);
		graphics.text(this.font, Component.translatable("screen.scubadive.journal.col_count").getString(), left + 230, 84, GRAY);

		int per = perPage();
		int start = this.page * per;
		for (int i = start; i < Math.min(all.size(), start + per); i++) {
			Species s = all.get(i);
			Journal.Entry entry = journal.get(s.id);
			int y = LIST_TOP + (i - start) * ROW_HEIGHT;
			if (entry == null) {
				String unknown = Component.translatable("screen.scubadive.journal.unknown").getString();
				graphics.text(this.font, unknown, left, y, GRAY);
				graphics.text(this.font, hint(s), left + Math.max(44, this.font.width(unknown) + 8), y, DIM);
			} else {
				graphics.text(this.font, Component.translatable(s.langKey()).getString(), left, y, WHITE);
				graphics.text(this.font, String.valueOf(entry.best()), left + 170, y, GOLD);
				graphics.text(this.font, String.valueOf(entry.count()), left + 230, y, WHITE);
			}
		}
		graphics.centeredText(this.font, Component.translatable("screen.scubadive.journal.hint").getString(), this.width / 2, this.height - 54, GRAY);
		graphics.centeredText(this.font, (this.page + 1) + " / " + pageCount(), this.width / 2, this.height - 42, GRAY);
	}

	/** "Habitat: … / Length: …" for a species not photographed yet. */
	private static String hint(Species s) {
		String length = s.lengthCm >= 100
				? Component.translatable("screen.scubadive.journal.length_m", trimmed(s.lengthCm / 100.0)).getString()
				: Component.translatable("screen.scubadive.journal.length_cm", s.lengthCm).getString();
		return Component.translatable("screen.scubadive.journal.hint_habitat", Component.translatable(s.habitat.langKey())).getString()
				+ "  " + Component.translatable("screen.scubadive.journal.hint_length", length).getString();
	}

	private static String trimmed(double v) {
		return v == Math.rint(v) ? String.valueOf((int) v) : String.format(java.util.Locale.ROOT, "%.1f", v);
	}
}
