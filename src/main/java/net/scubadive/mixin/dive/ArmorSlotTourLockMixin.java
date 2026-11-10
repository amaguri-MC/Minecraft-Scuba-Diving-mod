package net.scubadive.mixin.dive;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ArmorSlot;

import net.scubadive.tour.TourGear;

/** Dive gear cannot be taken off during a tour. */
@Mixin(ArmorSlot.class)
public abstract class ArmorSlotTourLockMixin {
	@Inject(method = "mayPickup(Lnet/minecraft/world/entity/player/Player;)Z", at = @At("RETURN"), cancellable = true)
	private void scubadive$lockInTour(Player player, CallbackInfoReturnable<Boolean> cir) {
		if (cir.getReturnValueZ() && TourGear.locked(player)) {
			cir.setReturnValue(false);
		}
	}
}
