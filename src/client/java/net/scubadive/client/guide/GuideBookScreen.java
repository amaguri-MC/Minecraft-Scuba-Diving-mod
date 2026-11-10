package net.scubadive.client.guide;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.glfw.GLFW;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import net.scubadive.tour.DivePoint;

/**
 * The illustrated dive guide: a two-page spread with arrows to turn pages, a clickable contents page and a ribbon
 * bookmark back to the contents. Pages are described in {@link GuidePages}.
 */
public final class GuideBookScreen extends Screen {
	private static final Identifier BOOK = GuidePages.tex("book");
	private static final Identifier ARROWS = GuidePages.tex("arrows");
	private static final Identifier BOOKMARK = GuidePages.tex("bookmark");

	private static final int BOOK_W = 352;
	private static final int BOOK_H = 220;
	/** Content column of each page, relative to the book's top-left corner. */
	private static final int[] PAGE_X = {19, 193};
	private static final int CONTENT_W = 140;
	private static final int CONTENT_TOP = 20;
	private static final int CONTENT_BOTTOM = 190;
	private static final int NUMBER_Y = 194;
	private static final int ART_H = 56;
	private static final int TOC_ROW = 11;

	private static final int HEAD = 0xFF1E5F75;
	private static final int BODY = 0xFF3A2E22;
	private static final int MUTED = 0xFF7A6A55;
	private static final int ACCENT = 0xFFD0652A;
	private static final int RULE = 0xFF7CC7D8;

	/** Remembered while the game runs, so the book reopens where it was left. */
	private static int lastSpread;

	private final GuideCreatures creatures = new GuideCreatures();
	private int spread;

	public GuideBookScreen() {
		super(Component.translatable("item.scubadive.dive_manual"));
		this.spread = Math.min(lastSpread, spreadCount() - 1);
	}

	public static int spreadCount() {
		return (GuidePages.PAGES.size() + 1) / 2;
	}

	private int left() {
		return (this.width - BOOK_W) / 2;
	}

	private int top() {
		return Math.max(0, (this.height - BOOK_H) / 2);
	}

	// ---- navigation ---------------------------------------------------------------------------------

	private void goTo(int newSpread) {
		int clamped = Math.max(0, Math.min(spreadCount() - 1, newSpread));
		if (clamped != this.spread) {
			this.spread = clamped;
			lastSpread = clamped;
			if (this.minecraft != null) {
				this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN, 1.0f));
			}
		}
	}

	/** Arrow positions: outside the book when there is room, otherwise on its lower corners. */
	private int[] prevArrow() {
		return left() >= 24 ? new int[]{left() - 22, top() + 104} : new int[]{left() + 12, top() + 204};
	}

	private int[] nextArrow() {
		return left() >= 24 ? new int[]{left() + BOOK_W + 4, top() + 104} : new int[]{left() + BOOK_W - 30, top() + 204};
	}

	private int[] bookmark() {
		return new int[]{left() + 300, Math.max(0, top() - 6)};
	}

	private static boolean inside(double mx, double my, int x, int y, int w, int h) {
		return mx >= x && mx < x + w && my >= y && my < y + h;
	}

	private boolean overPrev(double mx, double my) {
		int[] p = prevArrow();
		return this.spread > 0 && inside(mx, my, p[0], p[1], 18, 12);
	}

	private boolean overNext(double mx, double my) {
		int[] p = nextArrow();
		return this.spread < spreadCount() - 1 && inside(mx, my, p[0], p[1], 18, 12);
	}

	private boolean overBookmark(double mx, double my) {
		int[] p = bookmark();
		return inside(mx, my, p[0], p[1], 12, 24);
	}

	private static List<Integer> chapters() {
		List<Integer> list = new ArrayList<>();
		for (int i = 0; i < GuidePages.PAGES.size(); i++) {
			if (GuidePages.PAGES.get(i).chapter()) {
				list.add(i);
			}
		}
		return list;
	}

	/** Top-left of the page's content column, or null when the page is not on the open spread. */
	private int[] pageOrigin(int page) {
		if (page / 2 != this.spread) {
			return null;
		}
		return new int[]{left() + PAGE_X[page % 2], top() + CONTENT_TOP};
	}

	/** The chapter page under the mouse in the contents list, or -1. */
	private int tocRowAt(double mx, double my) {
		int[] o = pageOrigin(GuidePages.TOC_PAGE);
		if (o == null) {
			return -1;
		}
		List<Integer> list = chapters();
		int y = o[1] + 14;
		for (int page : list) {
			if (inside(mx, my, o[0], y - 1, CONTENT_W, TOC_ROW)) {
				return page;
			}
			y += TOC_ROW;
		}
		return -1;
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		if (event.button() == 0) {
			double mx = event.x();
			double my = event.y();
			if (overPrev(mx, my)) {
				goTo(this.spread - 1);
				return true;
			}
			if (overNext(mx, my)) {
				goTo(this.spread + 1);
				return true;
			}
			if (overBookmark(mx, my)) {
				goTo(GuidePages.TOC_PAGE / 2);
				return true;
			}
			int target = tocRowAt(mx, my);
			if (target >= 0) {
				goTo(target / 2);
				return true;
			}
		}
		return super.mouseClicked(event, doubleClick);
	}

	@Override
	public boolean keyPressed(KeyEvent event) {
		int key = event.key();
		if (key == GLFW.GLFW_KEY_LEFT || key == GLFW.GLFW_KEY_PAGE_UP) {
			goTo(this.spread - 1);
			return true;
		}
		if (key == GLFW.GLFW_KEY_RIGHT || key == GLFW.GLFW_KEY_PAGE_DOWN) {
			goTo(this.spread + 1);
			return true;
		}
		if (key == GLFW.GLFW_KEY_HOME) {
			goTo(GuidePages.TOC_PAGE / 2);
			return true;
		}
		return super.keyPressed(event);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
		if (scrollY > 0) {
			goTo(this.spread - 1);
			return true;
		}
		if (scrollY < 0) {
			goTo(this.spread + 1);
			return true;
		}
		return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
	}

	// ---- drawing ------------------------------------------------------------------------------------

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
		// The dimmed background is drawn by the screen framework.
		super.extractRenderState(graphics, mouseX, mouseY, delta);
		int left = left();
		int top = top();
		graphics.blit(RenderPipelines.GUI_TEXTURED, BOOK, left, top, 0.0f, 0.0f, BOOK_W, BOOK_H, BOOK_W, BOOK_H);

		for (int side = 0; side < 2; side++) {
			int page = this.spread * 2 + side;
			if (page >= GuidePages.PAGES.size()) {
				continue;
			}
			int x = left + PAGE_X[side];
			drawPage(graphics, GuidePages.PAGES.get(page), x, top + CONTENT_TOP, top + CONTENT_BOTTOM, mouseX, mouseY);
			String number = String.valueOf(page + 1);
			graphics.text(this.font, number, x + CONTENT_W / 2 - this.font.width(number) / 2, top + NUMBER_Y, MUTED, false);
		}

		// Ribbon bookmark (back to the contents) and page arrows.
		int[] b = bookmark();
		boolean bHover = overBookmark(mouseX, mouseY);
		graphics.blit(RenderPipelines.GUI_TEXTURED, BOOKMARK, b[0], b[1], bHover ? 12.0f : 0.0f, 0.0f, 12, 24, 24, 24);
		if (bHover) {
			graphics.setTooltipForNextFrame(this.font, Component.translatable("guide.scubadive.toc"), mouseX, mouseY);
		}
		if (this.spread > 0) {
			int[] p = prevArrow();
			boolean hover = overPrev(mouseX, mouseY);
			graphics.blit(RenderPipelines.GUI_TEXTURED, ARROWS, p[0], p[1], hover ? 18.0f : 0.0f, 0.0f, 18, 12, 72, 12);
			if (hover) {
				graphics.setTooltipForNextFrame(this.font, Component.translatable("guide.scubadive.prev"), mouseX, mouseY);
			}
		}
		if (this.spread < spreadCount() - 1) {
			int[] p = nextArrow();
			boolean hover = overNext(mouseX, mouseY);
			graphics.blit(RenderPipelines.GUI_TEXTURED, ARROWS, p[0], p[1], hover ? 54.0f : 36.0f, 0.0f, 18, 12, 72, 12);
			if (hover) {
				graphics.setTooltipForNextFrame(this.font, Component.translatable("guide.scubadive.next"), mouseX, mouseY);
			}
		}
	}

	private void drawPage(GuiGraphicsExtractor graphics, GuidePages.Page page, int x, int y, int bottom, int mouseX, int mouseY) {
		graphics.text(this.font, Component.translatable(page.titleKey()).withStyle(ChatFormatting.BOLD), x, y, HEAD, false);
		graphics.fill(x, y + 10, x + CONTENT_W, y + 11, RULE);
		int yy = y + 14;

		GuidePages.Art art = page.art();
		switch (art) {
			case GuidePages.Picture pic -> yy = drawPicture(graphics, page, pic, x, yy);
			case GuidePages.Items items -> yy = drawItems(graphics, items, x, yy, mouseX, mouseY);
			case GuidePages.Creatures cs -> yy = drawCreatures(graphics, cs, x, yy);
			case GuidePages.Toc toc -> {
				drawToc(graphics, x, yy, mouseX, mouseY);
				return;
			}
			case GuidePages.Sites sites -> yy = drawSites(graphics, x, yy, mouseX, mouseY);
			case GuidePages.None none -> {
			}
		}

		for (FormattedCharSequence line : this.font.split(Component.translatable(page.bodyKey()), CONTENT_W)) {
			if (yy + this.font.lineHeight > bottom) {
				break;
			}
			graphics.text(this.font, line, x, yy, BODY, false);
			yy += this.font.lineHeight;
		}
	}

	private int drawPicture(GuiGraphicsExtractor graphics, GuidePages.Page page, GuidePages.Picture pic, int x, int y) {
		graphics.blit(RenderPipelines.GUI_TEXTURED, pic.texture(), x, y, 0.0f, 0.0f, CONTENT_W, ART_H, CONTENT_W, ART_H);
		y += ART_H + 2;
		int n = pic.captions();
		if (n == 0) {
			return y + 2;
		}
		// Captions under each panel; staggered on two rows when neighbours would touch.
		String[] text = new String[n];
		boolean stagger = false;
		float spacing = CONTENT_W / (float) n;
		for (int i = 0; i < n; i++) {
			text[i] = Component.translatable(page.captionKey(i)).getString();
			if (i > 0 && (this.font.width(text[i - 1]) + this.font.width(text[i])) / 2.0f + 2 > spacing) {
				stagger = true;
			}
		}
		for (int i = 0; i < n; i++) {
			int cx = x + Math.round(spacing * (i + 0.5f));
			int row = stagger && i % 2 == 1 ? this.font.lineHeight : 0;
			graphics.text(this.font, text[i], cx - this.font.width(text[i]) / 2, y + row, HEAD, false);
		}
		return y + (stagger ? 2 * this.font.lineHeight : this.font.lineHeight) + 2;
	}

	private int drawItems(GuiGraphicsExtractor graphics, GuidePages.Items items, int x, int y, int mouseX, int mouseY) {
		int ix = x;
		for (String id : items.ids()) {
			ItemStack stack = stackOf(id);
			if (stack.isEmpty()) {
				continue;
			}
			graphics.fill(ix, y, ix + 20, y + 20, 0xFFE6DBBF);
			border(graphics, ix, y, 20, 20, 0xFFC9B48A);
			graphics.item(stack, ix + 2, y + 2);
			if (inside(mouseX, mouseY, ix, y, 20, 20)) {
				graphics.setTooltipForNextFrame(this.font, stack, mouseX, mouseY);
			}
			ix += 24;
		}
		return y + 24;
	}

	private int drawCreatures(GuiGraphicsExtractor graphics, GuidePages.Creatures cs, int x, int y) {
		int n = cs.ids().size();
		int w = CONTENT_W / n;
		long now = System.currentTimeMillis();
		float age = (now % 1_000_000L) / 50.0f;
		for (int i = 0; i < n; i++) {
			String id = cs.ids().get(i);
			int bx = x + i * w;
			graphics.fill(bx + 1, y, bx + w - 1, y + ART_H, 0xFFD5ECF2);
			border(graphics, bx + 1, y, w - 2, ART_H, 0xFF9CC5D4);
			LivingEntity entity = this.creatures.get(id);
			if (entity != null) {
				// One slow turn every 12 seconds, each creature at its own angle.
				float spin = ((now % 12_000L) / 12_000.0f) * 360.0f + i * 120.0f;
				GuideCreatures.draw(graphics, entity, bx + 2, y + 1, bx + w - 2, y + ART_H - 1, spin, age);
			}
			EntityType<?> type = GuideCreatures.typeOf(id);
			if (type != null) {
				List<FormattedCharSequence> name = this.font.split(type.getDescription(), w - 2);
				for (int l = 0; l < Math.min(2, name.size()); l++) {
					FormattedCharSequence line = name.get(l);
					graphics.text(this.font, line, bx + w / 2 - this.font.width(line) / 2, y + ART_H + 2 + l * this.font.lineHeight, HEAD, false);
				}
			}
		}
		return y + ART_H + 2 + 2 * this.font.lineHeight + 2;
	}

	private void drawToc(GuiGraphicsExtractor graphics, int x, int y, int mouseX, int mouseY) {
		int hovered = tocRowAt(mouseX, mouseY);
		for (int page : chapters()) {
			boolean hover = page == hovered;
			if (hover) {
				graphics.fill(x - 1, y - 1, x + CONTENT_W + 1, y + TOC_ROW - 2, 0x30D0652A);
			}
			int color = hover ? ACCENT : BODY;
			String title = Component.translatable(GuidePages.PAGES.get(page).titleKey()).getString();
			String number = String.valueOf(page + 1);
			int numberX = x + CONTENT_W - this.font.width(number);
			String shown = this.font.plainSubstrByWidth(title, numberX - x - 8);
			graphics.text(this.font, shown, x, y, color, false);
			// Dotted leader between the title and the page number.
			for (int dx = x + this.font.width(shown) + 3; dx < numberX - 3; dx += 3) {
				graphics.fill(dx, y + 6, dx + 1, y + 7, MUTED);
			}
			graphics.text(this.font, number, numberX, y, color, false);
			y += TOC_ROW;
		}
	}

	private int drawSites(GuiGraphicsExtractor graphics, int x, int y, int mouseX, int mouseY) {
		DivePoint[] points = DivePoint.values();
		ItemStack emerald = stackOf("minecraft:emerald");
		for (int i = 0; i < points.length; i++) {
			DivePoint p = points[i];
			ItemStack icon = stackOf(i < GuidePages.SITE_ICONS.size() ? GuidePages.SITE_ICONS.get(i) : "minecraft:water_bucket");
			graphics.item(icon, x, y + 1);
			String cost = String.valueOf(p.cost);
			int costX = x + CONTENT_W - 16 - 1 - this.font.width(cost);
			String name = this.font.plainSubstrByWidth(Component.translatable(p.langKey()).getString(), costX - x - 22);
			graphics.text(this.font, name, x + 19, y + 1, BODY, false);
			String depth = Component.translatable("guide.scubadive.sites.detail", p.shallowDepth, p.maxDepth).getString();
			graphics.text(this.font, depth, x + 19, y + 10, MUTED, false);
			graphics.text(this.font, cost, costX, y + 5, BODY, false);
			graphics.item(emerald, x + CONTENT_W - 16, y + 1);
			if (inside(mouseX, mouseY, x, y, CONTENT_W, 19) && !icon.isEmpty()) {
				graphics.setTooltipForNextFrame(this.font, Component.translatable(p.langKey()), mouseX, mouseY);
			}
			y += 20;
		}
		return y + 2;
	}

	private static void border(GuiGraphicsExtractor graphics, int x, int y, int w, int h, int color) {
		graphics.fill(x, y, x + w, y + 1, color);
		graphics.fill(x, y + h - 1, x + w, y + h, color);
		graphics.fill(x, y + 1, x + 1, y + h - 1, color);
		graphics.fill(x + w - 1, y + 1, x + w, y + h - 1, color);
	}

	private static ItemStack stackOf(String id) {
		Identifier key = Identifier.tryParse(id);
		if (key == null) {
			return ItemStack.EMPTY;
		}
		return BuiltInRegistries.ITEM.getOptional(key).map(ItemStack::new).orElse(ItemStack.EMPTY);
	}
}
