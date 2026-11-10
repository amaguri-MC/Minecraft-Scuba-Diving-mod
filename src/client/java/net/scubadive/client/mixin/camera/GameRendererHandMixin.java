package net.scubadive.client.mixin.camera;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.renderer.GameRenderer;

import net.scubadive.client.camera.ScubaCameraClient;

@Mixin(GameRenderer.class)
public abstract class GameRendererHandMixin {
	/** The first-person hand is not drawn while the clean photo frame is produced. */
	@Inject(method = "renderItemInHand", at = @At("HEAD"), cancellable = true)
	private void scubadive$hideHand(CallbackInfo ci) {
		if (ScubaCameraClient.isCapturing()) {
			ci.cancel();
		}
	}

	/** No block outline in the clean photo frame. */
	@Inject(method = "shouldRenderBlockOutline", at = @At("HEAD"), cancellable = true)
	private void scubadive$noOutline(CallbackInfoReturnable<Boolean> cir) {
		if (ScubaCameraClient.isCapturing()) {
			cir.setReturnValue(false);
		}
	}

	/** Counts frames fully rendered while capturing, so the grab waits for a clean frame. */
	@Inject(method = "render", at = @At("RETURN"))
	private void scubadive$frameDone(CallbackInfo ci) {
		ScubaCameraClient.onFrameRendered();
	}
}
