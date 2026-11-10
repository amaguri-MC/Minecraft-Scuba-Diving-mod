package net.scubadive.client.mixin.camera;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.renderer.ItemInHandRenderer;

import net.scubadive.client.camera.ScubaCameraClient;

@Mixin(ItemInHandRenderer.class)
public abstract class ItemInHandRendererMixin {
	/** Upstream of the hand and held item submission: nothing is drawn while capturing. */
	@Inject(method = "submitHandsWithItems", at = @At("HEAD"), cancellable = true)
	private void scubadive$hideHands(CallbackInfo ci) {
		if (ScubaCameraClient.isCapturing()) {
			ci.cancel();
		}
	}
}
