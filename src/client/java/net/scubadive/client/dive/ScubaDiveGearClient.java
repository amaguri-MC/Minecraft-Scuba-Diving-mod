package net.scubadive.client.dive;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.player.Player;

import net.scubadive.ScubaDive;
import net.scubadive.dive.ScubaDiveGear;

/**
 * Underwater color absorption: the deeper the player's eyes, the less red is visible.
 * Implemented as a full-screen tint on the HUD layer (Fabric has no fog color event).
 */
public final class ScubaDiveGearClient {
	private static final int MAX_SCAN = 64;
	private static final int DEPTH_INTERVAL = 5;
	/** How much of the tint remains while a dive light is held. */
	private static final float LIGHT_FACTOR = 0.4f;

	private static int ticks;
	private static int depth;
	private static boolean inWater;
	private static float alpha;
	private static float red;
	private static float green;
	private static float blue;

	private ScubaDiveGearClient() {
	}

	public static void init() {
		ClientTickEvents.END_CLIENT_TICK.register(ScubaDiveGearClient::tick);
		HudElementRegistry.attachElementBefore(VanillaHudElements.MISC_OVERLAYS, ScubaDive.id("depth_tint"), ScubaDiveGearClient::render);
	}

	private static void tick(Minecraft client) {
		Player player = client.player;
		if (player == null || client.level == null) {
			inWater = false;
			alpha = 0;
			return;
		}
		inWater = player.isEyeInFluid(FluidTags.WATER);
		if (!inWater) {
			alpha *= 0.5f;
			return;
		}
		if (ticks++ % DEPTH_INTERVAL == 0) {
			depth = measureDepth(client, player);
		}

		float targetAlpha = alphaForDepth(depth);
		float mix = Math.min(1f, Math.max(0f, (depth - 8f) / 12f));
		float tr = 0f;
		float tg = lerp(70f, 10f, mix);
		float tb = lerp(90f, 45f, mix);
		if (ScubaDiveGear.isHoldingLight(player)) {
			targetAlpha *= LIGHT_FACTOR;
		}
		alpha += (targetAlpha - alpha) * 0.2f;
		red += (tr - red) * 0.2f;
		green += (tg - green) * 0.2f;
		blue += (tb - blue) * 0.2f;
	}

	/** Number of consecutive water blocks above (and including) the eye block, capped. */
	private static int measureDepth(Minecraft client, Player player) {
		BlockPos.MutableBlockPos pos = BlockPos.containing(player.getEyePosition()).mutable();
		int count = 0;
		while (count < MAX_SCAN && client.level.getFluidState(pos).is(FluidTags.WATER)) {
			count++;
			pos.move(0, 1, 0);
		}
		return count;
	}

	/** 0m: none, 5m: slight, 10m: red nearly gone, 20m+: dark blue. */
	private static float alphaForDepth(int d) {
		if (d <= 0) {
			return 0f;
		}
		if (d <= 5) {
			return lerp(0f, 0.25f, d / 5f);
		}
		if (d <= 10) {
			return lerp(0.25f, 0.75f, (d - 5) / 5f);
		}
		if (d <= 20) {
			return lerp(0.75f, 0.82f, (d - 10) / 10f);
		}
		return 0.82f;
	}

	private static float lerp(float a, float b, float t) {
		return a + (b - a) * t;
	}

	private static void render(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
		if (alpha < 0.01f) {
			return;
		}
		int a = Math.min(255, Math.round(alpha * 255f));
		int color = (a << 24) | (Math.round(red) << 16) | (Math.round(green) << 8) | Math.round(blue);
		graphics.fill(0, 0, graphics.guiWidth(), graphics.guiHeight(), color);
	}
}
