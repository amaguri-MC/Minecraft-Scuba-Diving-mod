package net.scubadive.mixin.dive;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;

import net.scubadive.tour.TourGear;

/** Tour kit (gear, dive computer, camera, light) cannot be thrown away during a tour: it goes back into the inventory. */
@Mixin(ServerPlayer.class)
public abstract class ServerPlayerTourDropMixin {
	@Inject(method = "drop(Lnet/minecraft/world/item/ItemStack;ZZ)Lnet/minecraft/world/entity/item/ItemEntity;", at = @At("HEAD"), cancellable = true)
	private void scubadive$keepTourKit(ItemStack stack, boolean throwRandomly, boolean retainOwnership, CallbackInfoReturnable<ItemEntity> cir) {
		ServerPlayer self = (ServerPlayer) (Object) this;
		if (!stack.isEmpty() && TourGear.isTourItem(stack) && TourGear.locked(self) && self.isAlive()) {
			if (!self.getInventory().add(stack.copy())) {
				return;
			}
			self.containerMenu.broadcastChanges();
			cir.setReturnValue(null);
		}
	}
}
