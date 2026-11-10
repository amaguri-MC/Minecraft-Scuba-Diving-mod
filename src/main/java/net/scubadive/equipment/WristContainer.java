package net.scubadive.equipment;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** One-slot container that reads and writes the player's wrist attachment directly. */
public final class WristContainer implements Container {
	private final Player player;

	public WristContainer(Player player) {
		this.player = player;
	}

	private ItemStack stack() {
		return this.player.getAttachedOrElse(WristSlot.ITEM, ItemStack.EMPTY);
	}

	private void store(ItemStack stack) {
		// Always store a copy so the attachment change is detected and synced.
		this.player.setAttached(WristSlot.ITEM, stack.isEmpty() ? ItemStack.EMPTY : stack.copy());
	}

	@Override
	public int getContainerSize() {
		return 1;
	}

	@Override
	public boolean isEmpty() {
		return stack().isEmpty();
	}

	@Override
	public ItemStack getItem(int slot) {
		return slot == 0 ? stack() : ItemStack.EMPTY;
	}

	@Override
	public ItemStack removeItem(int slot, int amount) {
		if (slot != 0 || amount <= 0) {
			return ItemStack.EMPTY;
		}
		ItemStack current = stack();
		if (current.isEmpty()) {
			return ItemStack.EMPTY;
		}
		ItemStack out = current.split(amount);
		store(current);
		return out;
	}

	@Override
	public ItemStack removeItemNoUpdate(int slot) {
		if (slot != 0) {
			return ItemStack.EMPTY;
		}
		ItemStack current = stack();
		if (current.isEmpty()) {
			return ItemStack.EMPTY;
		}
		ItemStack out = current.copy();
		this.player.setAttached(WristSlot.ITEM, ItemStack.EMPTY);
		return out;
	}

	@Override
	public void setItem(int slot, ItemStack stack) {
		if (slot == 0) {
			store(stack);
		}
	}

	@Override
	public int getMaxStackSize() {
		return 1;
	}

	@Override
	public void setChanged() {
		store(stack());
	}

	@Override
	public boolean stillValid(Player player) {
		return true;
	}

	@Override
	public void clearContent() {
		this.player.setAttached(WristSlot.ITEM, ItemStack.EMPTY);
	}
}
