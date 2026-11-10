package net.scubadive.client.tour;

import java.util.List;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Items;

import net.scubadive.tour.DivePoint;
import net.scubadive.tour.center.GuideActionPayload;
import net.scubadive.tour.center.GuideStatePayload;

/**
 * Tour reception screen of a dive guide. Purely a view of the server state plus buttons: select a dive point,
 * join, start, cancel or leave. Every press is a {@link GuideActionPayload}; the server decides.
 */
public final class DiveCenterGuideScreen extends Screen {
	private static final int WHITE = 0xFFFFFFFF;
	private static final int GRAY = 0xFFA0A0A0;
	private static final int GOLD = 0xFFFFD84A;
	private static final int ROW_W = 280;

	private final int guideId;
	private int mode;
	private DivePoint point;
	private List<String> names;
	private int secondsLeft;
	private long receivedAt;
	/** Selected point in the select list (client only until "sign up" is pressed). */
	private int selected = -1;

	private DiveCenterGuideScreen(GuideStatePayload state) {
		super(Component.translatable("screen.scubadive.guide.title"));
		this.guideId = state.guideId();
		apply(state);
	}

	private void apply(GuideStatePayload state) {
		this.mode = state.mode();
		this.point = DivePoint.byId(state.point());
		this.names = state.names();
		this.secondsLeft = state.secondsLeft();
		this.receivedAt = System.currentTimeMillis();
	}

	/** Packet handler: open the screen or refresh/close the one that is showing. */
	public static void onState(Minecraft client, GuideStatePayload state) {
		Screen current = client.gui.screen();
		if (current instanceof DiveCenterGuideScreen screen && screen.guideId == state.guideId()) {
			if (state.mode() == GuideStatePayload.MODE_CLOSE) {
				screen.onClose();
				return;
			}
			screen.apply(state);
			screen.rebuildWidgets();
		} else if (state.open() && state.mode() != GuideStatePayload.MODE_CLOSE) {
			client.gui.setScreen(new DiveCenterGuideScreen(state));
		}
	}

	private void send(int action, String arg) {
		ClientPlayNetworking.send(new GuideActionPayload(this.guideId, action, arg));
	}

	private static Component depth(DivePoint p) {
		return Component.translatable("screen.scubadive.guide.depth", p.shallowDepth, p.maxDepth);
	}

	@Override
	protected void init() {
		int cx = this.width / 2;
		int bottom = this.height - 28;
		switch (this.mode) {
			case GuideStatePayload.MODE_SELECT -> {
				DivePoint[] points = DivePoint.values();
				int top = 70;
				for (int i = 0; i < points.length; i++) {
					DivePoint p = points[i];
					Component label = Component.translatable("screen.scubadive.guide.point_row",
							Component.translatable(p.langKey()), p.cost, p.shallowDepth, p.maxDepth);
					if (i == this.selected) {
						label = Component.literal("> ").append(label);
					}
					final int index = i;
					this.addRenderableWidget(Button.builder(label, b -> {
						this.selected = index;
						this.rebuildWidgets();
					}).pos(cx - ROW_W / 2, top + i * 22).size(ROW_W, 20).build());
				}
				Button apply = Button.builder(this.selected < 0
								? Component.translatable("screen.scubadive.guide.apply")
								: Component.translatable("screen.scubadive.guide.apply_cost", points[this.selected].cost),
						b -> send(GuideActionPayload.CREATE, points[this.selected].id))
						.pos(cx - 100, bottom - 24).size(200, 20).build();
				apply.active = this.selected >= 0;
				this.addRenderableWidget(apply);
			}
			case GuideStatePayload.MODE_JOIN -> {
				if (this.point != null) {
					this.addRenderableWidget(Button.builder(
							Component.translatable("screen.scubadive.guide.join", this.point.cost),
							b -> send(GuideActionPayload.JOIN, "")).pos(cx - 100, bottom - 24).size(200, 20).build());
				}
			}
			case GuideStatePayload.MODE_HOST -> {
				this.addRenderableWidget(Button.builder(Component.translatable("screen.scubadive.guide.start"),
						b -> send(GuideActionPayload.START, "")).pos(cx - 102, bottom - 24).size(100, 20).build());
				this.addRenderableWidget(Button.builder(Component.translatable("screen.scubadive.guide.cancel"),
						b -> send(GuideActionPayload.CANCEL, "")).pos(cx + 2, bottom - 24).size(100, 20).build());
			}
			case GuideStatePayload.MODE_MEMBER -> this.addRenderableWidget(Button.builder(
					Component.translatable("screen.scubadive.guide.leave"),
					b -> send(GuideActionPayload.LEAVE, "")).pos(cx - 100, bottom - 24).size(200, 20).build());
			default -> {
			}
		}
		this.addRenderableWidget(Button.builder(Component.translatable("gui.done"), b -> this.onClose())
				.pos(cx - 40, bottom).size(80, 20).build());
	}

	private int emeralds() {
		if (this.minecraft == null || this.minecraft.player == null) {
			return 0;
		}
		Inventory inventory = this.minecraft.player.getInventory();
		int total = 0;
		for (int i = 0; i < inventory.getContainerSize(); i++) {
			if (inventory.getItem(i).is(Items.EMERALD)) {
				total += inventory.getItem(i).getCount();
			}
		}
		return total;
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
		// Widgets only; the background is drawn by the screen framework.
		super.extractRenderState(graphics, mouseX, mouseY, delta);
		int cx = this.width / 2;
		graphics.centeredText(this.font, this.title.getString(), cx, 20, WHITE);
		graphics.text(this.font, Component.translatable("screen.scubadive.guide.emeralds", emeralds()).getString(),
				this.width - 110, 8, GOLD);

		if (this.mode == GuideStatePayload.MODE_SELECT) {
			graphics.centeredText(this.font, Component.translatable("screen.scubadive.guide.choose").getString(), cx, 44, GOLD);
			return;
		}
		if (this.point == null) {
			return;
		}
		int y = 44;
		graphics.centeredText(this.font, Component.translatable(this.point.langKey()).getString(), cx, y, GOLD);
		y += 14;
		graphics.centeredText(this.font, Component.translatable("screen.scubadive.guide.info", this.point.cost, depth(this.point)).getString(),
				cx, y, WHITE);
		y += 14;
		long elapsed = (System.currentTimeMillis() - this.receivedAt) / 1000L;
		int left = (int) Math.max(0L, this.secondsLeft - elapsed);
		graphics.centeredText(this.font,
				Component.translatable("screen.scubadive.guide.time_left", left / 60, String.format("%02d", left % 60)).getString(),
				cx, y, GRAY);
		y += 18;
		graphics.centeredText(this.font, Component.translatable("screen.scubadive.guide.members", this.names.size()).getString(), cx, y, WHITE);
		y += 12;
		for (int i = 0; i < this.names.size(); i++) {
			String line = i == 0
					? Component.translatable("screen.scubadive.guide.host", this.names.get(i)).getString()
					: this.names.get(i);
			graphics.centeredText(this.font, line, cx, y, i == 0 ? GOLD : GRAY);
			y += 11;
		}
	}
}
