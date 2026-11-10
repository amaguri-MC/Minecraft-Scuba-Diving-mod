package net.scubadive.creature.big;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.animal.fish.AbstractFish;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** Whale shark: huge, very slow and completely harmless. Does not shy away from divers. */
public class WhaleShark extends AbstractFish {
	public WhaleShark(EntityType<? extends WhaleShark> type, Level level) {
		super(type, level);
	}

	public static AttributeSupplier.Builder whaleSharkAttributes() {
		return Mob.createMobAttributes()
				.add(Attributes.MAX_HEALTH, 60.0)
				.add(Attributes.MOVEMENT_SPEED, 0.35);
	}

	@Override
	protected void registerGoals() {
		super.registerGoals();
		// Divers can swim right up to it.
		this.goalSelector.removeAllGoals(goal -> goal instanceof AvoidEntityGoal<?>);
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
