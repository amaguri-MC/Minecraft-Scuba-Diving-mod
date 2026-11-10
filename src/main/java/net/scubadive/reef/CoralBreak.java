package net.scubadive.reef;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/** Mod corals are fragile: a player rushing through or landing hard breaks them into items. */
final class CoralBreak {
	/** Blocks per tick; a calm swim stays below this, sprint-swimming or ramming exceeds it. */
	private static final double PASS_SPEED = 0.22;
	private static final double LAND_SPEED = 0.3;
	private static final double LAND_FALL = 1.5;

	private CoralBreak() {
	}

	static void onPassThrough(Level level, BlockPos pos, Entity entity) {
		if (!level.isClientSide() && entity instanceof Player player && !player.isSpectator()
				&& player.getKnownMovement().length() > PASS_SPEED) {
			level.destroyBlock(pos, true);
		}
	}

	static void onLand(Level level, BlockPos pos, Entity entity, double fallDistance) {
		if (!level.isClientSide() && entity instanceof Player player && !player.isSpectator()
				&& (fallDistance > LAND_FALL || -player.getKnownMovement().y > LAND_SPEED)) {
			level.destroyBlock(pos, true);
		}
	}
}
