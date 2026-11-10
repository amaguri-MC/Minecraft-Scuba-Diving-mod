package net.scubadive.client.camera;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayDeque;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.NativeImage;
import org.lwjgl.glfw.GLFW;

import net.minecraft.ChatFormatting;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.fabric.api.event.client.player.ClientHotbarScrollEvents;

import net.scubadive.ScubaDive;
import net.scubadive.camera.Breakdown;
import net.scubadive.camera.CameraLogic;
import net.scubadive.camera.CapturePayload;
import net.scubadive.camera.CaptureResultPayload;
import net.scubadive.camera.ScubaCamera;
import net.scubadive.species.Species;

public final class ScubaCameraClient {
	private static final long FLASH_MS = 300L;
	private static final long SHOT_INTERVAL_MS = 500L;
	private static final DateTimeFormatter FILE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH.mm.ss", Locale.ROOT);

	private static KeyMapping openJournal;
	private static long flashStart = -100000L;
	private static long lastShotMs = -100000L;

	public static final double ZOOM_MIN = 1.0;
	public static final double ZOOM_MAX = 8.0;
	private static final double ZOOM_STEP = 1.25;
	private static double zoom = ZOOM_MIN;

	/** Shooting mode: entered/left with left click while holding the camera in either hand. */
	private static boolean shooting;
	/** True from the shutter press until the clean frame has been grabbed (HUD and hand are hidden). */
	private static boolean capturing;
	private static int hiddenFrames;
	private static int captureWaitTicks;
	private static Pending capturePending;
	private static final int CAPTURE_TIMEOUT_TICKS = 40;
	private static final int CLEAN_FRAMES = 3;

	/** Photos waiting for the server verdict (FIFO: results come back in the order captures were sent). */
	private static final ArrayDeque<Pending> PENDING = new ArrayDeque<>();
	private static final ExecutorService PHOTO_IO = Executors.newSingleThreadExecutor(r -> {
		Thread t = new Thread(r, "scubadive-photo-io");
		t.setDaemon(true);
		return t;
	});

	private static final class Pending {
		final Species species;
		final String time;
		final Path file;
		NativeImage image;
		CaptureResultPayload result;
		boolean discarded;
		boolean finished;

		Pending(Species species, String time, Path file) {
			this.species = species;
			this.time = time;
			this.file = file;
		}
	}

	private ScubaCameraClient() { }

	public static void init() {
		KeyMapping.Category category = KeyMapping.Category.register(ScubaDive.id("main"));
		openJournal = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.scubadive.journal", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_J, category));

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (client.player == null || !holdingCamera(client.player)) {
				shooting = false;
			}
			if (!shooting) {
				zoom = ZOOM_MIN;
			}
			if (client.player == null) {
				capturing = false;
				capturePending = null;
				return;
			}
			if (capturing) {
				captureWaitTicks++;
				// Frames fully rendered with HUD, hand and outline hidden; wait for several so the target is clean.
				if (hiddenFrames >= CLEAN_FRAMES || captureWaitTicks > CAPTURE_TIMEOUT_TICKS) {
					grabFrame(client);
				}
			}
			while (openJournal.consumeClick()) {
				if (client.gui.screen() == null) {
					client.gui.setScreen(new JournalScreen());
				}
			}
		});

		ClientHotbarScrollEvents.ALLOW.register((inventory, currentSlot, selected, xOffset, yOffset) -> {
			Player player = Minecraft.getInstance().player;
			// In shooting mode the wheel zooms instead of switching the hotbar slot.
			if (player == null || !shooting || !holdingCamera(player)) return true;
			if (yOffset > 0) {
				zoom = Math.min(ZOOM_MAX, zoom * ZOOM_STEP);
			} else if (yOffset < 0) {
				zoom = Math.max(ZOOM_MIN, zoom / ZOOM_STEP);
			}
			return false;
		});

		// Hide every vanilla HUD element (and the dive computer) while the clean frame is rendered.
		// The depth tint and silt haze are not wrapped, so they stay in the photo.
		for (Identifier id : HIDDEN_ON_CAPTURE) {
			HudElementRegistry.replaceElement(id, original -> (HudElement) (graphics, delta) -> {
				if (!capturing) original.extractRenderState(graphics, delta);
			});
		}

		ClientPlayNetworking.registerGlobalReceiver(CaptureResultPayload.TYPE, (payload, context) -> onResult(context.client(), payload));

		HudElementRegistry.attachElementAfter(VanillaHudElements.SUBTITLES, ScubaDive.id("camera_flash"), ScubaCameraClient::extractFlash);
		HudElementRegistry.attachElementAfter(VanillaHudElements.SUBTITLES, ScubaDive.id("camera_finder"), ScubaCameraClient::extractFinder);
	}

	private static final Identifier[] HIDDEN_ON_CAPTURE = {
			VanillaHudElements.MISC_OVERLAYS, VanillaHudElements.CROSSHAIR, VanillaHudElements.SPECTATOR_MENU,
			VanillaHudElements.HOTBAR, VanillaHudElements.ARMOR_BAR, VanillaHudElements.HEALTH_BAR,
			VanillaHudElements.FOOD_BAR, VanillaHudElements.AIR_BAR, VanillaHudElements.MOUNT_HEALTH,
			VanillaHudElements.INFO_BAR, VanillaHudElements.EXPERIENCE_LEVEL, VanillaHudElements.HELD_ITEM_TOOLTIP,
			VanillaHudElements.SPECTATOR_TOOLTIP, VanillaHudElements.MOB_EFFECTS, VanillaHudElements.BOSS_BAR,
			VanillaHudElements.SLEEP, VanillaHudElements.DEMO_TIMER, VanillaHudElements.SCOREBOARD,
			VanillaHudElements.OVERLAY_MESSAGE, VanillaHudElements.TITLE_AND_SUBTITLE, VanillaHudElements.CHAT,
			VanillaHudElements.PLAYER_LIST, VanillaHudElements.SUBTITLES, ScubaDive.id("dive_computer")
	};

	/** Current zoom factor (1.0 = none). Read by CameraFovMixin. */
	public static double getZoom() {
		return zoom;
	}

	/** Called after each GameRenderer.render; counts clean frames while capturing. */
	public static void onFrameRendered() {
		if (capturing) hiddenFrames++;
	}

	/** True while the clean frame is being produced; read by the hand mixin. */
	public static boolean isCapturing() {
		return capturing;
	}

	private static boolean holdingCamera(Player player) {
		return player.getMainHandItem().is(ScubaCamera.CAMERA) || player.getOffhandItem().is(ScubaCamera.CAMERA);
	}

	/** Left click (called from the mixin). Returns true when the click is consumed (no attack). */
	public static boolean onAttackKey(Minecraft client) {
		Player player = client.player;
		if (player == null || client.gui.screen() != null || !holdingCamera(player)) return false;
		if (!capturing) {
			shooting = !shooting;
			if (!shooting) zoom = ZOOM_MIN;
		}
		return true;
	}

	/** True while a held left button must not mine (camera in hand). */
	public static boolean blocksMining(Minecraft client) {
		return client.player != null && holdingCamera(client.player);
	}

	/** Right click (called from the mixin). Returns true when consumed: shooting mode takes the photo. */
	public static boolean onUseKey(Minecraft client) {
		Player player = client.player;
		if (player == null || !shooting || client.gui.screen() != null || !holdingCamera(player)) return false;
		takePhoto(client, player);
		return true;
	}

	private static void extractFinder(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
		if (capturing) return;
		Minecraft client = Minecraft.getInstance();
		if (!shooting || client.player == null || !holdingCamera(client.player)) return;
		int w = graphics.guiWidth();
		int h = graphics.guiHeight();
		int color = 0xE0FFFFFF;
		int mx = w / 8;
		int my = h / 8;
		int len = Math.max(10, h / 14);
		// Corner brackets.
		for (int i = 0; i < 4; i++) {
			int x0 = (i & 1) == 0 ? mx : w - mx;
			int y0 = (i & 2) == 0 ? my : h - my;
			int dx = (i & 1) == 0 ? 1 : -1;
			int dy = (i & 2) == 0 ? 1 : -1;
			graphics.fill(Math.min(x0, x0 + dx * len), Math.min(y0, y0 + dy), Math.max(x0, x0 + dx * len), Math.max(y0, y0 + dy) + 1, color);
			graphics.fill(Math.min(x0, x0 + dx), Math.min(y0, y0 + dy * len), Math.max(x0, x0 + dx) + 1, Math.max(y0, y0 + dy * len), color);
		}
		// Centre cross.
		graphics.fill(w / 2 - 6, h / 2, w / 2 + 7, h / 2 + 1, color);
		graphics.fill(w / 2, h / 2 - 6, w / 2 + 1, h / 2 + 7, color);
		Component zoomText = Component.translatable("hud.scubadive.camera.zoom", String.format(Locale.ROOT, "%.1f", zoom));
		graphics.centeredText(client.font, zoomText, w / 2, my + 6, 0xFFFFFFFF);
		graphics.centeredText(client.font, Component.translatable("hud.scubadive.camera.guide"), w / 2, h - my - 14, 0xFFFFFFFF);
	}

	private static void takePhoto(Minecraft client, Player player) {
		long now = System.currentTimeMillis();
		if (capturing || now - lastShotMs < SHOT_INTERVAL_MS) return;
		lastShotMs = now;

		double fov = client.options.fov().get() / zoom;
		CameraLogic.Shot shot = CameraLogic.pickBest(player, fov);

		client.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.GLASS_PLACE, 1.6F, 1.0F));

		if (shot == null) {
			flashStart = now;
			client.gui.hud.setOverlayMessage(Component.translatable("message.scubadive.camera.nothing"), false);
			return;
		}

		LocalDateTime time = LocalDateTime.now();
		String name = FILE_TIME.format(time) + "_" + shot.species().id + ".png";
		Pending pending = new Pending(shot.species(), time.toString(), PhotoLog.dir().resolve(name));
		ClientPlayNetworking.send(new CapturePayload(shot.species().id, shot.entityId(),
				shot.pos().getX(), shot.pos().getY(), shot.pos().getZ(), (float) fov));
		synchronized (PENDING) {
			PENDING.add(pending);
		}
		// Hide HUD and hand; the frame is grabbed from the tick handler two rendered frames later.
		capturePending = pending;
		capturing = true;
		hiddenFrames = 0;
		captureWaitTicks = 0;
	}

	/** Grabs the clean frame; it is processed and saved once the server verdict has arrived. */
	private static void grabFrame(Minecraft client) {
		Pending pending = capturePending;
		capturing = false;
		capturePending = null;
		flashStart = System.currentTimeMillis();
		if (pending == null) return;
		try {
			Screenshot.takeScreenshot(client.gameRenderer.mainRenderTarget(), image -> {
				NativeImage copy;
				synchronized (pending) {
					if (pending.discarded) return;
					copy = new NativeImage(image.getWidth(), image.getHeight(), false);
					copy.copyFrom(image);
					pending.image = copy;
				}
				maybeFinish(pending);
			});
		} catch (RuntimeException e) {
			ScubaDive.LOGGER.warn("Failed to prepare camera screenshot", e);
			pending.discarded = true;
		}
	}

	/** Once both the frame and the verdict are here: apply blur/haze, save the PNG, append photos.json. */
	private static void maybeFinish(Pending p) {
		CaptureResultPayload result;
		NativeImage image;
		synchronized (p) {
			if (p.finished || p.discarded || p.result == null || p.image == null) return;
			p.finished = true;
			result = p.result;
			image = p.image;
			p.image = null;
		}
		PHOTO_IO.execute(() -> {
			try {
				Files.createDirectories(p.file.getParent());
				Breakdown b = result.breakdown();
				NativeImage processed = PhotoEffects.apply(image, b);
				try {
					processed.writeToFile(p.file);
				} finally {
					processed.close();
				}
				PhotoLog.append(new PhotoLog.Photo(p.species.id, result.score(), b, p.time, p.file.getFileName().toString()));
			} catch (IOException | RuntimeException e) {
				ScubaDive.LOGGER.warn("Failed to save camera screenshot", e);
			} finally {
				image.close();
			}
		});
	}

	private static void onResult(Minecraft client, CaptureResultPayload payload) {
		Species species = Species.byId(payload.species());
		Pending pending;
		synchronized (PENDING) {
			pending = PENDING.poll();
		}
		if (pending != null) {
			boolean rejected = species == null || payload.score() <= 0;
			NativeImage toClose = null;
			synchronized (pending) {
				if (rejected) {
					pending.discarded = true;
					toClose = pending.image;
					pending.image = null;
				} else {
					pending.result = payload;
				}
			}
			if (toClose != null) toClose.close();
			if (!rejected) maybeFinish(pending);
		}
		if (species == null || payload.score() <= 0) {
			client.gui.hud.setOverlayMessage(Component.translatable("message.scubadive.camera.rejected"), false);
			return;
		}
		MutableComponent text = Component.translatable("message.scubadive.camera.shot",
				Component.translatable(species.langKey()), payload.score());
		if (payload.isNew()) {
			text.append(" ").append(Component.translatable("message.scubadive.camera.new").withStyle(ChatFormatting.GOLD));
		}
		client.gui.hud.setOverlayMessage(text, false);
	}

	private static void extractFlash(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
		long elapsed = System.currentTimeMillis() - flashStart;
		if (capturing || elapsed < 0 || elapsed >= FLASH_MS) return;
		int alpha = (int) (255.0 * (1.0 - (double) elapsed / FLASH_MS));
		graphics.fill(0, 0, graphics.guiWidth(), graphics.guiHeight(), (alpha << 24) | 0xFFFFFF);
	}
}
