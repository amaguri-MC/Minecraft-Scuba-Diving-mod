package net.scubadive.client.camera;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

import com.mojang.blaze3d.platform.NativeImage;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import net.scubadive.ScubaDive;
import net.scubadive.camera.Breakdown;
import net.scubadive.species.Species;

/** Shows the saved photos of one species (newest first) with the score breakdown. */
public final class PhotoViewerScreen extends Screen {
	private static final int WHITE = 0xFFFFFFFF;
	private static final int GRAY = 0xFFA0A0A0;
	private static final int GOLD = 0xFFFFD84A;
	private static final int TEXT_AREA = 66;

	private final Screen parent;
	private final Species species;
	private final List<PhotoLog.Photo> photos;
	private int index;

	private Identifier textureId;
	private int imageW;
	private int imageH;
	private boolean loadFailed;
	private int serial;

	private Button prev;
	private Button next;
	private Button delete;
	private boolean confirmDelete;

	public PhotoViewerScreen(Screen parent, Species species, boolean tour) {
		super(Component.translatable("screen.scubadive.photos.title", Component.translatable(species.langKey())));
		this.parent = parent;
		this.species = species;
		this.photos = PhotoLog.load(species.id, tour);
	}

	@Override
	protected void init() {
		this.prev = this.addRenderableWidget(Button.builder(Component.literal("◀"), b -> go(-1))
				.pos(this.width / 2 - 110, this.height - 26).size(30, 20).build());
		this.addRenderableWidget(Button.builder(Component.translatable("screen.scubadive.photos.back"), b -> this.onClose())
				.pos(this.width / 2 - 40, this.height - 26).size(80, 20).build());
		this.next = this.addRenderableWidget(Button.builder(Component.literal("▶"), b -> go(1))
				.pos(this.width / 2 + 80, this.height - 26).size(30, 20).build());
		this.delete = this.addRenderableWidget(Button.builder(Component.translatable("screen.scubadive.photos.delete"), b -> onDelete())
				.pos(this.width / 2 + 116, this.height - 26).size(70, 20).build());
		if (this.textureId == null && !this.photos.isEmpty()) loadCurrent();
		updateButtons();
	}

	private void go(int delta) {
		int n = Math.max(0, Math.min(this.photos.size() - 1, this.index + delta));
		if (n == this.index) return;
		this.confirmDelete = false;
		this.index = n;
		loadCurrent();
		updateButtons();
	}

	private void onDelete() {
		if (this.photos.isEmpty()) return;
		if (!this.confirmDelete) {
			this.confirmDelete = true;
			updateButtons();
			return;
		}
		releaseTexture();
		PhotoLog.Photo photo = this.photos.get(this.index);
		if (PhotoLog.remove(photo.file())) {
			this.photos.remove(this.index);
		}
		this.confirmDelete = false;
		if (this.photos.isEmpty()) {
			this.onClose();
			return;
		}
		this.index = Math.min(this.index, this.photos.size() - 1);
		loadCurrent();
		updateButtons();
	}

	private void updateButtons() {
		this.delete.active = !this.photos.isEmpty();
		this.delete.setMessage(Component.translatable(this.confirmDelete ? "screen.scubadive.photos.delete_confirm" : "screen.scubadive.photos.delete"));
		this.prev.active = this.index > 0;
		this.next.active = this.index < this.photos.size() - 1;
	}

	private void releaseTexture() {
		if (this.textureId != null && this.minecraft != null) {
			// Releasing closes the DynamicTexture together with its NativeImage.
			this.minecraft.getTextureManager().release(this.textureId);
		}
		this.textureId = null;
	}

	private void loadCurrent() {
		releaseTexture();
		this.loadFailed = false;
		if (this.minecraft == null || this.photos.isEmpty()) return;
		Path file = PhotoLog.dir().resolve(this.photos.get(this.index).file());
		try (InputStream in = Files.newInputStream(file)) {
			NativeImage image = NativeImage.read(in);
			Identifier id = ScubaDive.id("photo_view/" + (this.serial++));
			DynamicTexture texture = new DynamicTexture(() -> "scubadive photo", image);
			texture.upload();
			this.minecraft.getTextureManager().register(id, texture);
			this.textureId = id;
			this.imageW = image.getWidth();
			this.imageH = image.getHeight();
		} catch (IOException | RuntimeException e) {
			ScubaDive.LOGGER.warn("Failed to load photo {}", file, e);
			this.loadFailed = true;
		}
	}

	@Override
	public void onClose() {
		if (this.minecraft != null) this.minecraft.gui.setScreen(this.parent);
	}

	@Override
	public void removed() {
		releaseTexture();
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
		// Widgets only; the background is drawn by the screen framework (never call extractBackground here).
		super.extractRenderState(graphics, mouseX, mouseY, delta);

		graphics.centeredText(this.font, this.title, this.width / 2, 8, WHITE);
		if (this.photos.isEmpty() || this.loadFailed) {
			Component msg = Component.translatable(this.loadFailed ? "screen.scubadive.photos.load_failed" : "screen.scubadive.photos.empty");
			graphics.centeredText(this.font, msg, this.width / 2, this.height / 2, GRAY);
			return;
		}

		PhotoLog.Photo photo = this.photos.get(this.index);
		graphics.centeredText(this.font, Component.translatable("screen.scubadive.photos.index", this.index + 1, this.photos.size()), this.width / 2, 20, GRAY);

		if (this.textureId != null) {
			int top = 32;
			int maxW = this.width - 40;
			int maxH = this.height - top - TEXT_AREA - 32;
			double scale = Math.min(1.0, Math.min((double) maxW / this.imageW, (double) maxH / this.imageH));
			int dw = Math.max(1, (int) (this.imageW * scale));
			int dh = Math.max(1, (int) (this.imageH * scale));
			int x = (this.width - dw) / 2;
			graphics.blit(RenderPipelines.GUI_TEXTURED, this.textureId, x, top, 0f, 0f, dw, dh, this.imageW, this.imageH, this.imageW, this.imageH);

			int ty = top + dh + 6;
			graphics.centeredText(this.font, Component.translatable("screen.scubadive.photos.score", photo.score()), this.width / 2, ty, GOLD);
			Breakdown b = photo.breakdown();
			String line1 = factor("distance", b.distance()) + "   " + factor("neutral", b.neutral())
					+ "   " + factor("brightness", b.brightness()) + "   " + factor("silt", b.silt());
			String line2 = factor("center", b.center()) + "   " + factor("size", b.size()) + "   " + factor("group", b.group());
			graphics.centeredText(this.font, line1, this.width / 2, ty + 14, WHITE);
			graphics.centeredText(this.font, line2, this.width / 2, ty + 26, WHITE);
			graphics.centeredText(this.font, photo.time().replace('T', ' ').split("\\.")[0], this.width / 2, ty + 40, GRAY);
		}
	}

	private static String factor(String key, float value) {
		return Component.translatable("screen.scubadive.photos.factor",
				Component.translatable("screen.scubadive.photos.b_" + key), String.format(Locale.ROOT, "%.2f", value)).getString();
	}
}
