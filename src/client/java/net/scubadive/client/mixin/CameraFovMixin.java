package net.scubadive.client.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.Camera;

import net.scubadive.client.camera.ScubaCameraClient;

@Mixin(Camera.class)
public abstract class CameraFovMixin {
	@Inject(method = "calculateFov(F)F", at = @At("RETURN"), cancellable = true)
	private void scubadive$zoom(float partialTick, CallbackInfoReturnable<Float> cir) {
		double zoom = ScubaCameraClient.getZoom();
		if (zoom > 1.0) {
			cir.setReturnValue((float) (cir.getReturnValueF() / zoom));
		}
	}
}
