package net.scubadive.creature.reef;

import net.minecraft.core.BlockPos;

/** Implemented by reef fish so the shared goals can read their home and alarm state. */
public interface ReefCreature {
	ReefProfile reefProfile();

	/** Spot the fish keeps returning to; null until first tick. */
	BlockPos reefHome();

	/** True after a bolt, until the fish has settled back near home. */
	boolean hasFled();

	void setFled(boolean fled);
}
