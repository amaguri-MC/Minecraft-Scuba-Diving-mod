package net.scubadive.client.mixin.camera;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.Minecraft;

import net.scubadive.client.camera.ScubaCameraClient;

@Mixin(Minecraft.class)
public abstract class MinecraftCameraMixin {
	/** Left click with the camera toggles shooting mode instead of attacking. */
	@Inject(method = "startAttack", at = @At("HEAD"), cancellable = true)
	private void scubadive$attack(CallbackInfoReturnable<Boolean> cir) {
		if (ScubaCameraClient.onAttackKey((Minecraft) (Object) this)) {
			cir.setReturnValue(false);
		}
	}

	/** Holding the button must not break blocks while the camera is in hand. */
	@Inject(method = "continueAttack", at = @At("HEAD"), cancellable = true)
	private void scubadive$continueAttack(boolean leftClick, CallbackInfo ci) {
		if (leftClick && ScubaCameraClient.blocksMining((Minecraft) (Object) this)) {
			ci.cancel();
		}
	}

	/** Right click in shooting mode takes the photo and does nothing else. */
	@Inject(method = "startUseItem", at = @At("HEAD"), cancellable = true)
	private void scubadive$use(CallbackInfo ci) {
		if (ScubaCameraClient.onUseKey((Minecraft) (Object) this)) {
			ci.cancel();
		}
	}
}
