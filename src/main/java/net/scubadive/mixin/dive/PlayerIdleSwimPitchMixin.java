package net.scubadive.mixin.dive;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import net.scubadive.dive.ScubaDiveGear;

/**
 * Vanilla nudges a swimming player up or down toward where they look, even with no keys held.
 * A diver in full gear who is not finning should stay put (buoyancy alone decides), so the look
 * direction is flattened while idle. Moving divers keep vanilla behaviour.
 */
@Mixin(Player.class)
public abstract class PlayerIdleSwimPitchMixin {
	@Redirect(method = "travel(Lnet/minecraft/world/phys/Vec3;)V",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;getLookAngle()Lnet/minecraft/world/phys/Vec3;"),
			require = 0)
	private Vec3 scubadive$flatLookWhenIdle(Player self) {
		Vec3 look = self.getLookAngle();
		boolean idle = self.xxa == 0.0f && self.zza == 0.0f && self.yya == 0.0f && !self.isShiftKeyDown();
		if (idle && self.isUnderWater() && ScubaDiveGear.hasFullSet(self)) {
			return new Vec3(look.x, 0.0, look.z);
		}
		return look;
	}
}
