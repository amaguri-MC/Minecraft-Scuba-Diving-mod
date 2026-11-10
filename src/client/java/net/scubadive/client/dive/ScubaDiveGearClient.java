package net.scubadive.client.dive;

import com.mojang.blaze3d.platform.InputConstants;
import org.lwjgl.glfw.GLFW;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityRenderLayerRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import net.scubadive.ScubaDive;
import net.scubadive.dive.Buoyancy;
import net.scubadive.dive.DiveApi;
import net.scubadive.dive.DiveClientState;
import net.scubadive.dive.DiveData;
import net.scubadive.dive.DiveInputPayload;
import net.scubadive.dive.ScubaDiveGear;
import net.scubadive.tour.TourApi;
import net.scubadive.equipment.WristSlot;

/**
 * Client-side dive visuals:
 * <ul>
 * <li>Colour absorption: the deeper the eyes, the stronger a blue tint (red disappears first,
 * then the scene turns from light blue to navy). The colour only ever moves in one direction with depth.</li>
 * <li>Dive light: the tint gets a clear, soft-edged hole in the middle of the screen (a beam), edges stay dark.</li>
 * <li>Dive computer: while held, a small panel shows dive time, depth, tank pressure (synced from the server), BCD air and buoyancy.</li>
 * <li>BCD: N inflates, M deflates; buoyancy is applied to the local player every tick.</li>
 * </ul>
 * Depth is measured from the water surface straight above the player, so rock overhangs
 * (basalt columns, shipwreck decks) no longer reset it.
 */
public final class ScubaDiveGearClient {
	private static final Identifier LIGHT_MASK = ScubaDive.id("textures/gui/light_mask.png");
	private static final int MASK_SIZE = 256;

	/** depth (m) -> tint alpha / colour. Every channel is monotonic so the colour never swings back. */
	private static final float[] D = {0f, 3f, 8f, 15f, 30f, 50f, 80f, 100f};
	private static final float[] A = {0f, 0.15f, 0.42f, 0.58f, 0.70f, 0.80f, 0.90f, 0.94f};
	private static final float[] G = {120f, 110f, 85f, 60f, 32f, 14f, 6f, 3f};
	private static final float[] B = {140f, 150f, 150f, 130f, 95f, 55f, 24f, 14f};

	private static final int KEEPALIVE_TICKS = 10;
	/** Neutral-buoyancy bob: amplitude in blocks, angular speed in rad/tick. */
	private static final double NEUTRAL_BOB = Buoyancy.NEUTRAL_BOB_AMPLITUDE;
	private static final double NEUTRAL_BOB_SPEED = 2.0 * Math.PI / Buoyancy.NEUTRAL_BOB_PERIOD_TICKS;

	private static KeyMapping inflateKey;
	private static KeyMapping deflateKey;
	private static int lastSentDir;
	private static int clientTick;

	private static boolean inWater;
	private static float depth;
	private static float alpha;
	private static float green;
	private static float blue;

	private ScubaDiveGearClient() {
	}

	public static void init() {
		KeyMapping.Category category = KeyMapping.Category.register(ScubaDive.id("dive"));
		inflateKey = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.scubadive.dive_inflate", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_M, category));
		deflateKey = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.scubadive.dive_deflate", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_N, category));
		ClientTickEvents.END_CLIENT_TICK.register(ScubaDiveGearClient::tick);
		LivingEntityRenderLayerRegistrationCallback.EVENT.register((entityType, renderer, helper, context) -> {
			if (renderer instanceof AvatarRenderer<?> avatarRenderer) {
				helper.register(new DiveGearLayer(avatarRenderer));
			}
		});
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
			lastSentDir = 0;
			DiveClientState.reset();
			return;
		}
		clientTick++;
		inWater = player.isEyeInFluid(FluidTags.WATER);
		depth = inWater ? DiveApi.depth(client.level, player) : 0f;
		tickBcd(client, player);

		if (!inWater) {
			alpha *= 0.5f;
			return;
		}
		float targetAlpha = curve(A, depth);
		alpha += (targetAlpha - alpha) * 0.2f;
		green += (curve(G, depth) - green) * 0.2f;
		blue += (curve(B, depth) - blue) * 0.2f;
	}

	/** BCD keys (predicted locally, sent to the server), buoyancy on the local player, neutral flag. */
	private static void tickBcd(Minecraft client, Player player) {
		DiveData synced = player.getAttachedOrElse(ScubaDiveGear.DIVE, DiveData.DEFAULT);
		boolean bcd = DiveApi.hasTank(player);

		int dir = 0;
		if (bcd && client.gui.screen() == null && !player.isSpectator()) {
			dir = (inflateKey.isDown() ? 1 : 0) - (deflateKey.isDown() ? 1 : 0);
		}
		if (dir != 0) {
			if (DiveClientState.sinceInput > DiveClientState.PREDICT_TICKS) {
				DiveClientState.predictedAir = synced.air();
			}
			DiveClientState.predictedAir = Math.max(0f, Math.min(1f, DiveClientState.predictedAir + dir * Buoyancy.AIR_STEP));
			DiveClientState.sinceInput = 0;
		} else if (DiveClientState.sinceInput < 1000) {
			DiveClientState.sinceInput++;
		}
		if (dir != lastSentDir || (dir != 0 && clientTick % KEEPALIVE_TICKS == 0)) {
			ClientPlayNetworking.send(new DiveInputPayload(dir));
			lastSentDir = dir;
		}

		boolean swimming = bcd && player.isInWater();
		float net = Buoyancy.net(DiveClientState.air(synced.air()), depth);
		double speed = Math.abs(player.getY() - player.yo);
		DiveClientState.neutral = swimming && Buoyancy.isNeutral(net, (float) speed);
		if (swimming && !player.getAbilities().flying && !player.isPassenger()) {
			Vec3 v = player.getDeltaMovement();
			boolean finning = false;
			if (player instanceof net.minecraft.client.player.LocalPlayer local) {
				var keys = local.input.keyPresses;
				finning = keys.forward() || keys.backward() || keys.left() || keys.right() || keys.jump() || keys.shift();
			}
			if (!finning) {
				// Left alone the diver drifts purely by buoyancy (vanilla's underwater sinking is replaced):
				// positive rises, negative sinks, neutral holds height with a slow sine bob.
				double target = Buoyancy.driftSpeed(net);
				if (DiveClientState.neutral) {
					target = NEUTRAL_BOB * NEUTRAL_BOB_SPEED * Math.cos(clientTick * NEUTRAL_BOB_SPEED);
				}
				player.setDeltaMovement(v.x, v.y + (target - v.y) * 0.35, v.z);
			} else if (!DiveClientState.neutral) {
				// While finning, buoyancy still pulls up or down on top of the swimming motion.
				player.addDeltaMovement(new Vec3(0.0, Buoyancy.ACCEL * net, 0.0));
			}
		}
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
		if (player == null || !(WristSlot.hasDiveComputer(player) || ScubaDiveGear.isHoldingDiveComputer(player))) {
			return;
		}
		Font font = client.font;
		int w = graphics.guiWidth();
		int h = graphics.guiHeight();
		int pw = 124;
		int ph = 74;
		int x = w - pw - 6;
		int y = h - ph - 30;

		DiveData data = player.getAttachedOrElse(ScubaDiveGear.DIVE, DiveData.DEFAULT);
		boolean bcd = DiveApi.hasTank(player);
		float pressure = bcd ? DiveApi.pressure(player) : 0f;
		float air = DiveClientState.air(data.air());
		float net = Buoyancy.net(air, depth());

		graphics.fill(x - 1, y - 1, x + pw + 1, y + ph + 1, 0xFFE07820);
		graphics.fill(x, y, x + pw, y + ph, 0xE0101820);

		int seconds = data.diveTicks() / 20;
		String time = String.format("%d:%02d", seconds / 60, seconds % 60);
		float shownDepth = depth();
		// Tours use a single 100 bar cylinder, so the gauge and warnings scale to it.
		boolean tour = TourApi.noRefill(player);
		float tank = tour ? TourApi.TOUR_TANK : DiveData.FULL_TANK;
		float low = tour ? 15f : 50f;
		int pColor = !bcd ? 0xFF80A0B0 : pressure > tank * 0.5f ? 0xFF7CFFAA : pressure > low ? 0xFFFFD84A : 0xFFFF5050;
		boolean blink = (System.currentTimeMillis() / 400L) % 2L == 0L;

		graphics.text(font, Component.translatable("hud.scubadive.dc.time", time), x + 5, y + 4, 0xFFFFFFFF, false);
		graphics.text(font, Component.translatable("hud.scubadive.dc.depth", String.format("%.1f", shownDepth)), x + 5, y + 15, 0xFF9AD8FF, false);
		graphics.text(font, Component.translatable("hud.scubadive.dc.max", String.format("%.1f", data.maxDepth())), x + 5, y + 26, 0xFF80A0B0, false);
		if (!bcd) {
			graphics.text(font, Component.translatable("hud.scubadive.dc.pressure_none"), x + 5, y + 37, pColor, false);
			graphics.text(font, Component.translatable("hud.scubadive.dc.bcd", "---"), x + 5, y + 48, 0xFF80A0B0, false);
		} else {
			if (pressure > low || blink) {
				graphics.text(font, Component.translatable("hud.scubadive.dc.pressure", Math.round(pressure)), x + 5, y + 37, pColor, false);
			}
			graphics.text(font, Component.translatable("hud.scubadive.dc.bcd", Math.round(air * 100f) + "%"), x + 5, y + 48, 0xFF9AD8FF, false);
			boolean neutral = DiveApi.isNeutral(player);
			String key;
			int color;
			if (neutral || Math.abs(net) < Buoyancy.NEUTRAL_NET) {
				key = "hud.scubadive.dc.buoy.neutral";
				color = neutral ? 0xFF7CFFAA : 0xFFFFFFFF;
			} else if (net > 0f) {
				key = "hud.scubadive.dc.buoy.up";
				color = 0xFFFFD84A;
			} else {
				key = "hud.scubadive.dc.buoy.down";
				color = 0xFF9AD8FF;
			}
			graphics.text(font, Component.translatable(key), x + 5, y + 59, color, false);
		}
		// Tank gauge on the right edge.
		int gx = x + pw - 9;
		int gTop = y + 5;
		int gBottom = y + ph - 5;
		graphics.fill(gx, gTop, gx + 4, gBottom, 0xFF303840);
		int filled = Math.round((gBottom - gTop) * Math.min(1f, pressure / tank));
		graphics.fill(gx, gBottom - filled, gx + 4, gBottom, pColor);

		if (!bcd) {
			return;
		}
		if (pressure <= 0f && blink) {
			graphics.centeredText(font, Component.translatable("hud.scubadive.dc.empty"), w / 2, h / 2 + 20, 0xFFFF5050);
		} else if (pressure > 0f && pressure <= low && inWater && blink) {
			graphics.centeredText(font, Component.translatable("hud.scubadive.dc.low"), w / 2, h / 2 + 20, 0xFFFFD84A);
		}
	}
}
