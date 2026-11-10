package net.scubadive.mixin.dive;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import net.scubadive.dive.ScubaDiveGear;

/** Mask + BCD + non-empty tank: the player breathes underwater (no air loss, no drowning). */
@Mixin(LivingEntity.class)
public abstract class LivingEntityBreathMixin {
	@Inject(method = "canBreatheUnderwater()Z", at = @At("HEAD"), cancellable = true)
	private void scubadive$maskBreathing(CallbackInfoReturnable<Boolean> cir) {
		if ((Object) this instanceof Player player && ScubaDiveGear.canBreathe(player)) {
			cir.setReturnValue(true);
		}
	}
}
