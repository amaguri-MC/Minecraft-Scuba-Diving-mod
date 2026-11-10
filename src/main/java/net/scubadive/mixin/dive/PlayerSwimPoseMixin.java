package net.scubadive.mixin.dive;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.world.entity.player.Player;

import net.scubadive.dive.ScubaDiveGear;

/** Full dive set + body in water: always in the swimming pose (server and client run the same check). No sprint is implied, so no speed bonus. */
@Mixin(Player.class)
public abstract class PlayerSwimPoseMixin {
	@Inject(method = "updateSwimming()V", at = @At("HEAD"), cancellable = true)
	private void scubadive$alwaysSwim(CallbackInfo ci) {
		Player self = (Player) (Object) this;
		if (self.isInWater() && !self.isPassenger() && ScubaDiveGear.hasFullSet(self)) {
			self.setSwimming(true);
			ci.cancel();
		}
	}
}
