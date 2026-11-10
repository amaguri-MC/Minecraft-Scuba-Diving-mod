package net.scubadive.dive;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * Cross-module contract for dive state. Other modules (camera, silt) call only these methods.
 * Owner: dive module. Method signatures must not change.
 */
public final class DiveApi {
	private static final int MAX_SCAN = 200;

	private DiveApi() {
	}

	/** Metres from the player's eyes up to the top of the highest water block above (overhangs skipped); 0 if eyes are not in water. Both sides. */
	public static float depth(Level level, Player player) {
		if (!player.isEyeInFluid(FluidTags.WATER)) {
			return 0f;
		}
		double eyeY = player.getEyeY();
		BlockPos.MutableBlockPos pos = BlockPos.containing(player.getEyePosition()).mutable();
		int topWater = pos.getY();
		for (int i = 0; i < MAX_SCAN; i++) {
			if (level.getFluidState(pos).is(FluidTags.WATER)) {
				topWater = pos.getY();
			} else if (level.getBlockState(pos).isAir()) {
				break;
			}
			pos.move(0, 1, 0);
		}
		return Math.max(0f, (float) (topWater + 1 - eyeY));
	}

	/** True if the player wears the BCD (which carries the cylinder). Both sides. */
	public static boolean hasTank(Player player) {
		return player.getItemBySlot(EquipmentSlot.CHEST).is(ScubaDiveGear.BCD);
	}

	/** Tank pressure in bar (0..200). 0 without a tank. Server-authoritative, synced to the owning client. */
	public static float pressure(Player player) {
		if (!hasTank(player)) {
			return 0f;
		}
		if (!player.level().isClientSide()) {
			DiveServer.Live live = DiveServer.peek(player);
			if (live != null) {
				return live.pressure;
			}
		}
		return player.getAttachedOrElse(ScubaDiveGear.DIVE, DiveData.DEFAULT).pressure();
	}

	/** True while the diver is neutrally buoyant (BCD worn, in water, net buoyancy and vertical speed near zero). Server and owning client. */
	public static boolean isNeutral(Player player) {
		if (!hasTank(player)) {
			return false;
		}
		if (player.level().isClientSide()) {
			return player.isLocalPlayer() && DiveClientState.neutral;
		}
		DiveServer.Live live = DiveServer.peek(player);
		return live != null && live.neutral;
	}
}
