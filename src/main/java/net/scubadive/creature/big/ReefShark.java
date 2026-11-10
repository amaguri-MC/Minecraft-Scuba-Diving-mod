package net.scubadive.creature.big;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.fish.AbstractFish;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/** Whitetip-style reef shark: cruises calmly, bites back for a while when attacked. */
public class ReefShark extends AbstractFish implements NeutralFish {
	private final AngerState anger = new AngerState();

	public ReefShark(EntityType<? extends ReefShark> type, Level level) {
		super(type, level);
	}

	public static AttributeSupplier.Builder sharkAttributes() {
		return Mob.createMobAttributes()
				.add(Attributes.MAX_HEALTH, 18.0)
				.add(Attributes.MOVEMENT_SPEED, 1.0)
				.add(Attributes.ATTACK_DAMAGE, 4.0);
	}

	@Override
	public AngerState angerState() {
		return this.anger;
	}

	@Override
	protected void registerGoals() {
		super.registerGoals();
		// Not skittish: drop the flee goals, add retaliation.
		this.goalSelector.removeAllGoals(goal -> goal instanceof PanicGoal || goal instanceof AvoidEntityGoal<?>);
		this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.6, true));
		this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
		this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false, this::isAngryAt));
	}

	@Override
	protected void customServerAiStep(ServerLevel level) {
		this.updatePersistentAnger(level, false);
		super.customServerAiStep(level);
	}

	@Override
	protected void addAdditionalSaveData(ValueOutput output) {
		super.addAdditionalSaveData(output);
		this.addPersistentAngerSaveData(output);
	}

	@Override
	protected void readAdditionalSaveData(ValueInput input) {
		super.readAdditionalSaveData(input);
		this.readPersistentAngerSaveData(this.level(), input);
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
