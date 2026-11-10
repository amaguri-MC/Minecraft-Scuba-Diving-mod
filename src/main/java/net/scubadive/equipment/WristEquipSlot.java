package net.scubadive.equipment;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import net.scubadive.ScubaDive;
import net.scubadive.dive.ScubaDiveGear;

/** Inventory slot that accepts only the dive computer (max 1). Hidden and inert for creative players. */
public final class WristEquipSlot extends Slot {
	/** Position in InventoryScreen: just above the off-hand slot (77, 62). */
	public static final int X = 77;
	public static final int Y = 44;
	/** Empty-slot outline icon (GUI atlas sprite). */
	public static final Identifier EMPTY_ICON = ScubaDive.id("container/slot/dive_computer");
	/** 18x18 slot frame drawn behind the slot, since the vanilla background has none at this spot. */
	public static final Identifier FRAME = ScubaDive.id("container/wrist_slot_frame");

	private final Player player;

	public WristEquipSlot(Player player) {
		super(new WristContainer(player), 0, X, Y);
		this.player = player;
	}

	@Override
	public boolean mayPickup(net.minecraft.world.entity.player.Player player) {
		// The dive computer stays on the wrist during a tour.
		return !net.scubadive.tour.TourGear.locked(player) && super.mayPickup(player);
	}

	@Override
	public boolean mayPlace(ItemStack stack) {
		return !this.player.hasInfiniteMaterials()
				&& ScubaDiveGear.DIVE_COMPUTER != null
				&& stack.is(ScubaDiveGear.DIVE_COMPUTER);
	}

	@Override
	public int getMaxStackSize() {
		return 1;
	}

	/** The creative inventory tab re-lays out slots by index and would overlap the hotbar, so hide it there. */
	@Override
	public boolean isActive() {
		return !this.player.hasInfiniteMaterials();
	}

	@Override
	public Identifier getNoItemIcon() {
		return EMPTY_ICON;
	}
}
