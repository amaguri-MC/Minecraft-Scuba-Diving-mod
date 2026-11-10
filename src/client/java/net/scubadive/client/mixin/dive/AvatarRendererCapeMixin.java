package net.scubadive.client.mixin.dive;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.Avatar;

import net.scubadive.dive.ScubaDiveGear;

/** Hides the cape while a BCD (with its cylinder on the back) is worn. */
@Mixin(AvatarRenderer.class)
public abstract class AvatarRendererCapeMixin {
	@Inject(method = "extractRenderState(Lnet/minecraft/world/entity/Avatar;Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;F)V", at = @At("TAIL"))
	private void scubadive$hideCapeWithBcd(Avatar entity, AvatarRenderState state, float partialTick, CallbackInfo ci) {
		if (ScubaDiveGear.BCD != null && state.chestEquipment.is(ScubaDiveGear.BCD)) {
			state.showCape = false;
		}
	}
}
