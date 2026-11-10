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
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.fabric.api.event.client.player.ClientHotbarScrollEvents;
import net.fabricmc.fabric.api.event.player.UseItemCallback;

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
			if (client.player == null || !zoomActive(client.player)) {
				zoom = ZOOM_MIN;
			}
			if (client.player == null) return;
			while (openJournal.consumeClick()) {
				if (client.gui.screen() == null) {
					client.gui.setScreen(new JournalScreen());
				}
			}
		});

		ClientHotbarScrollEvents.ALLOW.register((inventory, currentSlot, selected, xOffset, yOffset) -> {
			Player player = Minecraft.getInstance().player;
			// Wheel zooms only while the camera is in the off hand; otherwise it is the normal hotbar scroll.
			if (player == null || !zoomActive(player)) return true;
			if (yOffset > 0) {
				zoom = Math.min(ZOOM_MAX, zoom * ZOOM_STEP);
			} else if (yOffset < 0) {
				zoom = Math.max(ZOOM_MIN, zoom / ZOOM_STEP);
			}
			return false;
		});

		UseItemCallback.EVENT.register((player, level, hand) -> {
			if (!level.isClientSide()) return InteractionResult.PASS;
			if (!player.getItemInHand(hand).is(ScubaCamera.CAMERA)) return InteractionResult.PASS;
			takePhoto(Minecraft.getInstance(), player);
			return InteractionResult.SUCCESS;
		});

		ClientPlayNetworking.registerGlobalReceiver(CaptureResultPayload.TYPE, (payload, context) -> onResult(context.client(), payload));

		HudElementRegistry.attachElementAfter(VanillaHudElements.SUBTITLES, ScubaDive.id("camera_flash"), ScubaCameraClient::extractFlash);
		HudElementRegistry.attachElementAfter(VanillaHudElements.SUBTITLES, ScubaDive.id("camera_zoom"), ScubaCameraClient::extractZoom);
	}

	/** Current zoom factor (1.0 = none). Read by CameraFovMixin. */
	public static double getZoom() {
		return zoom;
	}

	private static boolean zoomActive(Player player) {
		return player.getOffhandItem().is(ScubaCamera.CAMERA);
	}

	private static void extractZoom(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
		if (zoom <= 1.0) return;
		Minecraft client = Minecraft.getInstance();
		if (client.player == null || !zoomActive(client.player)) return;
		Component text = Component.translatable("hud.scubadive.camera.zoom", String.format(Locale.ROOT, "%.1f", zoom));
		graphics.centeredText(client.font, text, graphics.guiWidth() / 2, 10, 0xFFFFFFFF);
	}

	private static void takePhoto(Minecraft client, Player player) {
		long now = System.currentTimeMillis();
		if (now - lastShotMs < SHOT_INTERVAL_MS) return;
		lastShotMs = now;

		double fov = client.options.fov().get() / zoom;
		CameraLogic.Shot shot = CameraLogic.pickBest(player, fov);

		client.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.GLASS_PLACE, 1.6F, 1.0F));

		if (shot == null) {
			flashStart = now;
			client.gui.hud.setOverlayMessage(Component.translatable("message.scubadive.camera.nothing"), false);
			return;
		}

		Pending pending = requestScreenshot(shot.species());
		flashStart = now;
		ClientPlayNetworking.send(new CapturePayload(shot.species().id, shot.entityId(),
				shot.pos().getX(), shot.pos().getY(), shot.pos().getZ(), (float) fov));
		synchronized (PENDING) {
			PENDING.add(pending);
		}
	}

	/** Grabs the current frame; it is processed and saved once the server verdict has arrived. */
	private static Pending requestScreenshot(Species species) {
		LocalDateTime now = LocalDateTime.now();
		String name = FILE_TIME.format(now) + "_" + species.id + ".png";
		Pending pending = new Pending(species, now.toString(), PhotoLog.dir().resolve(name));
		try {
			Minecraft client = Minecraft.getInstance();
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
		return pending;
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
		if (elapsed < 0 || elapsed >= FLASH_MS) return;
		int alpha = (int) (255.0 * (1.0 - (double) elapsed / FLASH_MS));
		graphics.fill(0, 0, graphics.guiWidth(), graphics.guiHeight(), (alpha << 24) | 0xFFFFFF);
	}
}
