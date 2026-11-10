package net.scubadive.client.dive;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;

import net.scubadive.ScubaDive;
import net.scubadive.dive.ScubaDiveGear;

/**
 * Client-side dive visuals:
 * <ul>
 * <li>Colour absorption: the deeper the eyes, the stronger a blue tint (red disappears first,
 * then the scene turns from light blue to navy). The colour only ever moves in one direction with depth.</li>
 * <li>Dive light: the tint gets a clear, soft-edged hole in the middle of the screen (a beam), edges stay dark.</li>
 * <li>Dive computer: while held, a small panel shows dive time, tank pressure and depth.</li>
 * </ul>
 * Depth is measured from the water surface straight above the player, so rock overhangs
 * (basalt columns, shipwreck decks) no longer reset it.
 */
public final class ScubaDiveGearClient {
	private static final Identifier LIGHT_MASK = ScubaDive.id("textures/gui/light_mask.png");
	private static final int MASK_SIZE = 256;
	private static final int MAX_SCAN = 160;
	private static final int DEPTH_INTERVAL = 4;

	/** depth (m) -> tint alpha / colour. Every channel is monotonic so the colour never swings back. */
	private static final float[] D = {0f, 3f, 8f, 15f, 30f, 50f};
	private static final float[] A = {0f, 0.15f, 0.42f, 0.58f, 0.70f, 0.80f};
	private static final float[] G = {120f, 110f, 85f, 60f, 32f, 14f};
	private static final float[] B = {140f, 150f, 150f, 130f, 95f, 55f};

	// Tank: 200 bar. At 10 m (2 bar ambient) a full tank lasts about 10 minutes; refills in ~10 s at the surface.
	public static final float FULL_TANK = 200f;
	private static final float USE_PER_TICK_PER_BAR = FULL_TANK / (20f * 600f * 2f);
	private static final float REFILL_PER_TICK = FULL_TANK / (20f * 10f);
	private static final int NEW_DIVE_SURFACE_TICKS = 20 * 10;

	private static int ticks;
	private static boolean inWater;
	private static float depth;
	private static float alpha;
	private static float green;
	private static float blue;

	private static float pressure = FULL_TANK;
	private static int diveTicks;
	private static int surfaceTicks = NEW_DIVE_SURFACE_TICKS;
	private static float maxDepth;

	private ScubaDiveGearClient() {
	}

	public static void init() {
		ClientTickEvents.END_CLIENT_TICK.register(ScubaDiveGearClient::tick);
		HudElementRegistry.attachElementBefore(VanillaHudElements.MISC_OVERLAYS, ScubaDive.id("depth_tint"), ScubaDiveGearClient::renderTint);
		HudElementRegistry.attachElementAfter(VanillaHudElements.MISC_OVERLAYS, ScubaDive.id("dive_computer"), ScubaDiveGearClient::renderComputer);
	}

	public static float depth() {
		return inWater ? depth : 0f;
	}

	private static void tick(Minecraft client) {
		Player player = client.player;
		if (player == null || client.level == null) {
			inWater = false;
			alpha = 0;
			return;
		}
		inWater = player.isEyeInFluid(FluidTags.WATER);
		if (inWater && (ticks++ % DEPTH_INTERVAL == 0 || depth <= 0f)) {
			depth = measureDepth(client, player);
		}
		tickComputer();

		if (!inWater) {
			alpha *= 0.5f;
			return;
		}
		float targetAlpha = curve(A, depth);
		alpha += (targetAlpha - alpha) * 0.2f;
		green += (curve(G, depth) - green) * 0.2f;
		blue += (curve(B, depth) - blue) * 0.2f;
	}

	private static void tickComputer() {
		if (inWater) {
			if (surfaceTicks >= NEW_DIVE_SURFACE_TICKS) {
				diveTicks = 0;
				maxDepth = 0f;
			}
			surfaceTicks = 0;
			diveTicks++;
			maxDepth = Math.max(maxDepth, depth);
			float ambientBar = 1f + depth / 10f;
			pressure = Math.max(0f, pressure - USE_PER_TICK_PER_BAR * ambientBar);
		} else {
			surfaceTicks++;
			pressure = Math.min(FULL_TANK, pressure + REFILL_PER_TICK);
		}
	}

	/**
	 * Walks up from the eyes until open air and returns the distance from the eyes to the top of the
	 * highest water block met on the way. Solid blocks in between (overhangs) are skipped.
	 */
	private static float measureDepth(Minecraft client, Player player) {
		double eyeY = player.getEyeY();
		BlockPos.MutableBlockPos pos = BlockPos.containing(player.getEyePosition()).mutable();
		int topWater = pos.getY();
		for (int i = 0; i < MAX_SCAN; i++) {
			BlockState state = client.level.getBlockState(pos);
			if (client.level.getFluidState(pos).is(FluidTags.WATER)) {
				topWater = pos.getY();
			} else if (state.isAir()) {
				break;
			}
			pos.move(0, 1, 0);
		}
		return Math.max(0f, (float) (topWater + 1 - eyeY));
	}

	private static float curve(float[] values, float d) {
		if (d <= D[0]) {
			return values[0];
		}
		for (int i = 1; i < D.length; i++) {
			if (d <= D[i]) {
				float t = (d - D[i - 1]) / (D[i] - D[i - 1]);
				return values[i - 1] + (values[i] - values[i - 1]) * t;
			}
		}
		return values[values.length - 1];
	}

	private static int argb(float a, float r, float g, float b) {
		int ai = Math.max(0, Math.min(255, Math.round(a * 255f)));
		return (ai << 24) | (Math.round(r) << 16) | (Math.round(g) << 8) | Math.round(b);
	}

	private static void renderTint(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
		if (alpha < 0.01f) {
			return;
		}
		int w = graphics.guiWidth();
		int h = graphics.guiHeight();
		Player player = Minecraft.getInstance().player;
		boolean light = inWater && player != null && ScubaDiveGear.isHoldingLight(player);
		if (!light) {
			graphics.fill(0, 0, w, h, argb(alpha, 0f, green, blue));
			return;
		}
		// Beam: a faint tint everywhere, plus a radial mask that is clear in the middle and full at the rim.
		graphics.fill(0, 0, w, h, argb(alpha * 0.12f, 0f, green, blue));
		int size = Math.round(Math.max(w, h) * 1.25f);
		int x = (w - size) / 2;
		int y = (h - size) / 2;
		graphics.blit(RenderPipelines.GUI_TEXTURED, LIGHT_MASK, x, y, 0f, 0f, size, size, size, size,
				argb(alpha * 0.88f, 0f, green * 0.6f, blue * 0.6f));
	}

	private static void renderComputer(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
		Minecraft client = Minecraft.getInstance();
		Player player = client.player;
		if (player == null || !ScubaDiveGear.isHoldingDiveComputer(player)) {
			return;
		}
		Font font = client.font;
		int w = graphics.guiWidth();
		int h = graphics.guiHeight();
		int pw = 112;
		int ph = 52;
		int x = w - pw - 6;
		int y = h - ph - 30;

		graphics.fill(x - 1, y - 1, x + pw + 1, y + ph + 1, 0xFFE07820);
		graphics.fill(x, y, x + pw, y + ph, 0xE0101820);

		int seconds = diveTicks / 20;
		String time = String.format("%d:%02d", seconds / 60, seconds % 60);
		float shownDepth = depth();
		int bar = Math.round(pressure);
		int pColor = pressure > 100f ? 0xFF7CFFAA : pressure > 50f ? 0xFFFFD84A : 0xFFFF5050;
		boolean blink = (System.currentTimeMillis() / 400L) % 2L == 0L;

		graphics.text(font, Component.translatable("hud.scubadive.dc.time", time), x + 5, y + 4, 0xFFFFFFFF, false);
		graphics.text(font, Component.translatable("hud.scubadive.dc.depth", String.format("%.1f", shownDepth)), x + 5, y + 15, 0xFF9AD8FF, false);
		graphics.text(font, Component.translatable("hud.scubadive.dc.max", String.format("%.1f", maxDepth)), x + 5, y + 26, 0xFF80A0B0, false);
		if (pressure > 50f || blink) {
			graphics.text(font, Component.translatable("hud.scubadive.dc.pressure", bar), x + 5, y + 37, pColor, false);
		}
		// Tank gauge on the right edge.
		int gx = x + pw - 9;
		int gTop = y + 5;
		int gBottom = y + ph - 5;
		graphics.fill(gx, gTop, gx + 4, gBottom, 0xFF303840);
		int filled = Math.round((gBottom - gTop) * (pressure / FULL_TANK));
		graphics.fill(gx, gBottom - filled, gx + 4, gBottom, pColor);

		if (pressure <= 0f && blink) {
			graphics.centeredText(font, Component.translatable("hud.scubadive.dc.empty"), w / 2, h / 2 + 20, 0xFFFF5050);
		} else if (pressure <= 50f && inWater && blink) {
			graphics.centeredText(font, Component.translatable("hud.scubadive.dc.low"), w / 2, h / 2 + 20, 0xFFFFD84A);
		}
	}
}
