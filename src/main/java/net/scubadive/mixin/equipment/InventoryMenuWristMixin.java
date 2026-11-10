package net.scubadive.mixin.equipment;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import net.scubadive.equipment.WristEquipSlot;
import net.scubadive.equipment.WristSlot;

/** Adds the wrist slot as the last slot of the player inventory menu and routes shift-click for it. */
@Mixin(InventoryMenu.class)
public abstract class InventoryMenuWristMixin {
	@Shadow
	protected abstract Slot addSlot(Slot slot);

	@Shadow
	protected abstract boolean moveItemStackTo(ItemStack stack, int start, int end, boolean reverse);

	@Inject(method = "<init>(Lnet/minecraft/world/entity/player/Inventory;ZLnet/minecraft/world/entity/player/Player;)V", at = @At("TAIL"))
	private void scubadive$addWristSlot(Inventory inventory, boolean active, Player owner, CallbackInfo ci) {
		this.addSlot(new WristEquipSlot(owner));
	}

	@Inject(method = "quickMoveStack(Lnet/minecraft/world/entity/player/Player;I)Lnet/minecraft/world/item/ItemStack;", at = @At("HEAD"), cancellable = true)
	private void scubadive$quickMoveWrist(Player player, int index, CallbackInfoReturnable<ItemStack> cir) {
		InventoryMenu menu = (InventoryMenu) (Object) this;
		WristEquipSlot wrist = WristSlot.find(menu);
		if (wrist == null || index < 0 || index >= menu.slots.size()) {
			return;
		}
		Slot slot = menu.slots.get(index);
		if (slot == wrist) {
			// Wrist -> main inventory / hotbar. Always return EMPTY so the caller never repeats the move.
			ItemStack stack = slot.getItem();
			if (!stack.isEmpty() && slot.mayPickup(player)
					&& this.moveItemStackTo(stack, InventoryMenu.INV_SLOT_START, InventoryMenu.USE_ROW_SLOT_END, false)) {
				if (stack.isEmpty()) {
					slot.setByPlayer(ItemStack.EMPTY);
				} else {
					slot.setChanged();
				}
			}
			cir.setReturnValue(ItemStack.EMPTY);
			return;
		}
		if (index >= InventoryMenu.INV_SLOT_START && index <= InventoryMenu.SHIELD_SLOT
				&& !wrist.hasItem() && slot.hasItem() && slot.mayPickup(player)
				&& wrist.mayPlace(slot.getItem())) {
			// Main inventory / hotbar / off-hand -> wrist (one item).
			ItemStack moved = slot.remove(1);
			if (!moved.isEmpty()) {
				wrist.setByPlayer(moved);
				slot.setChanged();
			}
			cir.setReturnValue(ItemStack.EMPTY);
		}
	}
}
