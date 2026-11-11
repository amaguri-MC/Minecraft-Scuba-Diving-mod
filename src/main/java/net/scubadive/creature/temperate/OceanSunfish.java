package net.scubadive.creature.temperate;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.animal.fish.AbstractFish;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Ocean sunfish (mola): a huge flat disc that sculls slowly with its tall dorsal and anal fins.
 * Not afraid of divers. Now and then it rises to just below the surface and lies on its side to bask.
 */
public class OceanSunfish extends AbstractFish {
	private static final EntityDataAccessor<Boolean> BASKING = SynchedEntityData.defineId(OceanSunfish.class, EntityDataSerializers.BOOLEAN);

	/** Client-side roll toward the side (0 upright .. 1 flat), smoothed per tick. */
	private float roll;
	private float rollO;

	public OceanSunfish(EntityType<? extends OceanSunfish> type, Level level) {
		super(type, level);
		// Keep the climb/dive angle chosen by the swim goal.
		this.lookControl = new LookControl(this) {
			@Override
			protected boolean resetXRotOnTick() {
				return false;
			}
		};
	}

	public static AttributeSupplier.Builder sunfishAttributes() {
		return Mob.createMobAttributes()
				.add(Attributes.MAX_HEALTH, 30.0)
				.add(Attributes.MOVEMENT_SPEED, 0.3);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(BASKING, false);
	}

	public boolean isBasking() {
		return this.entityData.get(BASKING);
	}

	void setBasking(boolean basking) {
		this.entityData.set(BASKING, basking);
	}

	@Override
	protected void registerGoals() {
		// No avoid / panic goals: the sunfish ignores divers. The swim goal owns all movement.
		this.goalSelector.addGoal(1, new SunfishSwimGoal(this));
	}

	@Override
	public void tick() {
		super.tick();
		if (this.level().isClientSide()) {
			this.rollO = this.roll;
			float want = this.isBasking() && this.isInWater() ? 1.0f : 0.0f;
			this.roll += Mth.clamp(want - this.roll, -0.02f, 0.02f);
		}
	}

	/** Roll toward lying flat, 0..1, interpolated for rendering. */
	public float getRoll(float partialTick) {
		return Mth.lerp(partialTick, this.rollO, this.roll);
	}

	@Override
	protected InteractionResult mobInteract(Player player, InteractionHand hand) {
		// Far too big for a bucket.
		return InteractionResult.PASS;
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
