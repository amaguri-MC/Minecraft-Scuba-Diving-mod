package net.scubadive.mixin.dive;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;

import net.scubadive.dive.ScubaDiveGear;

/** Hostile mobs ignore players wearing the full dive set, unless that player hurt them. */
@Mixin(Mob.class)
public abstract class MobFullSetMixin {
	@Inject(method = "setTarget(Lnet/minecraft/world/entity/LivingEntity;)V", at = @At("HEAD"), cancellable = true)
	private void scubadive$noTargetInFullSet(LivingEntity target, CallbackInfo ci) {
		if (scubadive$ignores(target)) {
			ci.cancel();
		}
	}

	@Inject(method = "canAttack(Lnet/minecraft/world/entity/LivingEntity;)Z", at = @At("HEAD"), cancellable = true)
	private void scubadive$cannotAttackInFullSet(LivingEntity target, CallbackInfoReturnable<Boolean> cir) {
		if (scubadive$ignores(target)) {
			cir.setReturnValue(false);
		}
	}

	/** Drops a target that was acquired before the suit was put on. */
	@Inject(method = "tick()V", at = @At("HEAD"))
	private void scubadive$dropTarget(CallbackInfo ci) {
		Mob self = (Mob) (Object) this;
		if (!self.level().isClientSide() && scubadive$ignores(self.getTarget())) {
			self.setTarget(null);
		}
	}

	private boolean scubadive$ignores(LivingEntity target) {
		Mob self = (Mob) (Object) this;
		return self instanceof Enemy
				&& target instanceof Player player
				&& ScubaDiveGear.hasFullSet(player)
				&& self.getLastHurtByMob() != player;
	}
}
