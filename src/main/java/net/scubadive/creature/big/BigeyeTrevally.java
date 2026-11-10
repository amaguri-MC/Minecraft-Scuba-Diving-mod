package net.scubadive.creature.big;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.FollowFlockLeaderGoal;
import net.minecraft.world.entity.ai.goal.RandomSwimmingGoal;
import net.minecraft.world.entity.animal.fish.AbstractSchoolingFish;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** Bigeye trevally: a silver fish that lives in big schools (20-40) swirling in a slow vortex. Harmless. */
public class BigeyeTrevally extends AbstractSchoolingFish {
	public static final int MAX_SCHOOL = 40;

	public BigeyeTrevally(EntityType<? extends BigeyeTrevally> type, Level level) {
		super(type, level);
	}

	public static AttributeSupplier.Builder trevallyAttributes() {
		return Mob.createMobAttributes()
				.add(Attributes.MAX_HEALTH, 6.0)
				.add(Attributes.MOVEMENT_SPEED, 1.0);
	}

	@Override
	protected void registerGoals() {
		super.registerGoals();
		// Unafraid of divers; the swirl goal replaces random swimming and leader following.
		this.goalSelector.removeAllGoals(goal -> goal instanceof AvoidEntityGoal<?>
				|| goal instanceof RandomSwimmingGoal || goal instanceof FollowFlockLeaderGoal);
		this.goalSelector.addGoal(4, new SwirlGoal(this));
	}

	@Override
	public int getMaxSchoolSize() {
		return MAX_SCHOOL;
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
		return SoundEvents.COD_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.COD_DEATH;
	}
}
