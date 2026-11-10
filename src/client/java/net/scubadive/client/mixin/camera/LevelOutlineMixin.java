package net.scubadive.client.mixin.camera;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.renderer.LevelRenderer;

import net.scubadive.client.camera.ScubaCameraClient;

@Mixin(LevelRenderer.class)
public abstract class LevelOutlineMixin {
	/** Second layer for the block outline, in case another path skips the GameRenderer check. */
	@Inject(method = "submitBlockOutline", at = @At("HEAD"), cancellable = true)
	private void scubadive$noOutline(CallbackInfo ci) {
		if (ScubaCameraClient.isCapturing()) {
			ci.cancel();
		}
	}
}
