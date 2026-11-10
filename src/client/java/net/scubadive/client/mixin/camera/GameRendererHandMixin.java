package net.scubadive.client.mixin.camera;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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
}
