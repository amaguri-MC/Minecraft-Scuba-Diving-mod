package net.scubadive.equipment;

import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import net.scubadive.ScubaDive;
import net.scubadive.dive.ScubaDiveGear;

/**
 * Dedicated wrist slot for the dive computer. Contract: the signature of hasDiveComputer must not change.
 * The stack lives in a persistent player attachment (synced to the owner); the extra
 * inventory slot (see InventoryMenuWristMixin) is a view onto it.
 * Owner: equipment module (F6).
 */
public final class WristSlot {
	/** Wrist slot contents. Persistent, kept on respawn (dropped on death unless keepInventory), synced to the owner. */
	public static final AttachmentType<ItemStack> ITEM = AttachmentRegistry.create(
			ScubaDive.id("wrist_item"),
			builder -> builder
					.initializer(() -> ItemStack.EMPTY)
					.persistent(ItemStack.OPTIONAL_CODEC)
					.copyOnDeath()
					.syncWith(ItemStack.OPTIONAL_STREAM_CODEC, AttachmentSyncPredicate.targetOnly())
	);

	private WristSlot() {
	}

	public static void init() {
		// Attachment type is registered by the static initializer above; touch it here so it is loaded early.
		ITEM.identifier();
	}

	/** True if the player's wrist slot holds the dive computer. Server and owning client. */
	public static boolean hasDiveComputer(Player player) {
		if (player == null || ScubaDiveGear.DIVE_COMPUTER == null) {
			return false;
		}
		return player.getAttachedOrElse(ITEM, ItemStack.EMPTY).is(ScubaDiveGear.DIVE_COMPUTER);
	}

	/** Removes and returns the wrist slot contents (used to drop it on death). */
	public static ItemStack take(Player player) {
		ItemStack stack = player.getAttachedOrElse(ITEM, ItemStack.EMPTY);
		if (stack.isEmpty()) {
			return ItemStack.EMPTY;
		}
		ItemStack out = stack.copy();
		player.setAttached(ITEM, ItemStack.EMPTY);
		return out;
	}

	/** The wrist slot of a menu, or null if the menu has none. */
	public static WristEquipSlot find(AbstractContainerMenu menu) {
		for (Slot slot : menu.slots) {
			if (slot instanceof WristEquipSlot wrist) {
				return wrist;
			}
		}
		return null;
	}
}
