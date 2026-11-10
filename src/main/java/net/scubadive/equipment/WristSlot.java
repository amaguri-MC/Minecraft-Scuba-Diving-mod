package net.scubadive.equipment;

import net.minecraft.world.entity.player.Player;

/**
 * Dedicated wrist slot for the dive computer. Contract: the signature of hasDiveComputer must not change.
 * Owner: equipment module (F6).
 */
public final class WristSlot {
	private WristSlot() {
	}

	public static void init() {
	}

	/** True if the player's wrist slot holds the dive computer. Server and owning client. */
	public static boolean hasDiveComputer(Player player) {
		return false;
	}
}
