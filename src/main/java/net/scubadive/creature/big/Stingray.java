package net.scubadive.creature.big;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.fish.AbstractFish;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** Stingray: lies still on sandy bottoms, slides away from divers, stings (short poison) when stepped on or attacked. */
public class Stingray extends AbstractFish {
	private static final int POISON_TICKS = 80;
	private static final int STING_COOLDOWN = 40;
	private int stingCooldown;

	public Stingray(EntityType<? extends Stingray> type, Level level) {
		super(type, level);
	}

	public static AttributeSupplier.Builder stingrayAttributes() {
		return Mob.createMobAttributes()
				.add(Attributes.MAX_HEALTH, 10.0)
				.add(Attributes.MOVEMENT_SPEED, 0.6);
	}

	@Override
	protected void registerGoals() {
		// Only the rest / slide-away goal; no random swimming.
		this.goalSelector.addGoal(1, new StingrayGoal(this));
	}

	@Override
	public void aiStep() {
		super.aiStep();
		if (this.stingCooldown > 0) {
			this.stingCooldown--;
		}
	}

	/** Stepping on (or bumping from above) triggers the barb. */
	@Override
	public void playerTouch(Player player) {
		if (player.getY() > this.getBoundingBox().maxY - 0.35) {
			this.sting(player);
		}
	}

	@Override
	public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
		boolean hurt = super.hurtServer(level, source, amount);
		if (hurt && source.getEntity() instanceof LivingEntity attacker && this.distanceToSqr(attacker) < 25.0) {
			this.sting(attacker);
		}
		return hurt;
	}

	private void sting(LivingEntity target) {
		if (this.stingCooldown > 0 || !(this.level() instanceof ServerLevel level) || !this.isAlive()) {
			return;
		}
		if (target.hurtServer(level, this.damageSources().mobAttack(this), 1.0f)) {
			target.addEffect(new MobEffectInstance(MobEffects.POISON, POISON_TICKS, 0), this);
			this.playSound(SoundEvents.PUFFER_FISH_STING, 1.0f, 1.0f);
			this.stingCooldown = STING_COOLDOWN;
		}
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
