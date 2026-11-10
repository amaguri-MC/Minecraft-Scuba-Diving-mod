package net.scubadive.tour;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import net.scubadive.camera.ScubaCamera;
import net.scubadive.dive.ScubaDiveGear;
import net.scubadive.equipment.WristSlot;

/** The kit a tour diver must bring: full dive set, dive computer on the wrist, camera and light. Locked during a tour. */
public final class TourGear {
	private TourGear() {
	}

	/** Names of the missing items (empty list = ready to dive). */
	public static List<Component> missing(Player p) {
		List<Component> out = new ArrayList<>();
		wear(p, EquipmentSlot.HEAD, ScubaDiveGear.DIVE_MASK, out);
		wear(p, EquipmentSlot.CHEST, ScubaDiveGear.BCD, out);
		wear(p, EquipmentSlot.LEGS, ScubaDiveGear.WETSUIT, out);
		wear(p, EquipmentSlot.FEET, ScubaDiveGear.DIVE_FINS, out);
		if (!WristSlot.hasDiveComputer(p)) {
			out.add(Component.translatable(ScubaDiveGear.DIVE_COMPUTER.getDescriptionId()));
		}
		carry(p, ScubaCamera.CAMERA, out);
		carry(p, ScubaDiveGear.DIVE_LIGHT, out);
		return out;
	}

	/** Items that cannot be dropped during a tour. */
	public static boolean isTourItem(ItemStack stack) {
		return stack.is(ScubaDiveGear.DIVE_MASK) || stack.is(ScubaDiveGear.BCD) || stack.is(ScubaDiveGear.WETSUIT)
				|| stack.is(ScubaDiveGear.DIVE_FINS) || stack.is(ScubaDiveGear.DIVE_COMPUTER)
				|| stack.is(ScubaCamera.CAMERA) || stack.is(ScubaDiveGear.DIVE_LIGHT);
	}

	/** True while the player is anywhere in the tour dimension (gear is locked there). Both sides. */
	public static boolean locked(Player p) {
		return p.level().dimension().equals(TourApi.DIMENSION) && !p.isCreative();
	}

	private static void wear(Player p, EquipmentSlot slot, Item item, List<Component> out) {
		if (!p.getItemBySlot(slot).is(item)) {
			out.add(Component.translatable(item.getDescriptionId()));
		}
	}

	private static void carry(Player p, Item item, List<Component> out) {
		if (!p.getInventory().contains(s -> s.is(item))) {
			out.add(Component.translatable(item.getDescriptionId()));
		}
	}
}
