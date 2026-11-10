package net.scubadive.creature.big;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.fish.AbstractFish;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** Harmless giant that glides on a {@link CruiseGoal} with a free pitch (whale shark, manta ray). */
public abstract class GliderFish extends AbstractFish {
	protected GliderFish(EntityType<? extends GliderFish> type, Level level) {
		super(type, level);
		// The default look control resets pitch every tick; keep the climb/dive angle.
		this.lookControl = new LookControl(this) {
			@Override
			protected boolean resetXRotOnTick() {
				return false;
			}
		};
	}

	/** The cruise goal with this species' tuning. Called from registerGoals, so no instance fields. */
	protected abstract Goal createCruiseGoal();

	@Override
	protected void registerGoals() {
		// No flee / random-swim goals: the glide goal owns all movement.
		this.goalSelector.addGoal(1, this.createCruiseGoal());
	}

	@Override
	public ItemStack getBucketItemStack() {
		return ItemStack.EMPTY;
	}

	@Override
	public boolean canBePickedUpWithBucket(ItemStack stack) {
		return false;
	}

	@Override
	protected SoundEvent getFlopSound() {
		return SoundEvents.COD_FLOP;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return SoundEvents.DOLPHIN_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.DOLPHIN_DEATH;
	}
}
