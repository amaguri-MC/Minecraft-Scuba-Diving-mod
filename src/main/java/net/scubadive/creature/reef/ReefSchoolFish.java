package net.scubadive.creature.reef;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.fish.AbstractSchoolingFish;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** Reef fish that swim in a school (damselfish, chromis, anthias, butterflyfish pairs, snappers). */
public class ReefSchoolFish extends AbstractSchoolingFish implements ReefCreature {
	private final ReefProfile profile;
	private BlockPos home;
	private boolean fled;
	private AbstractSchoolingFish reefLeader;

	public ReefSchoolFish(EntityType<? extends ReefSchoolFish> type, Level level, ReefProfile profile) {
		super(type, level);
		this.profile = profile;
		ReefBehavior.addGoals(this, profile);
	}

	@Override
	public ReefProfile reefProfile() {
		return this.profile;
	}

	@Override
	public BlockPos reefHome() {
		return this.home;
	}

	@Override
	public boolean hasFled() {
		return this.fled;
	}

	@Override
	public void setFled(boolean fled) {
		this.fled = fled;
	}

	@Override
	public AbstractSchoolingFish startFollowing(AbstractSchoolingFish leader) {
		this.reefLeader = leader;
		return super.startFollowing(leader);
	}

	@Override
	public void stopFollowing() {
		this.reefLeader = null;
		super.stopFollowing();
	}

	/** The living leader this fish follows, or null. */
	AbstractSchoolingFish reefLeader() {
		return this.isFollower() ? this.reefLeader : null;
	}

	@Override
	public int getMaxSchoolSize() {
		return this.profile.schoolSize();
	}

	@Override
	public void tick() {
		super.tick();
		if (this.home == null && !this.level().isClientSide()) {
			this.home = this.blockPosition();
		}
	}

	@Override
	protected InteractionResult mobInteract(Player player, InteractionHand hand) {
		// Too small for a bucket.
		return InteractionResult.PASS;
	}

	@Override
	public ItemStack getBucketItemStack() {
		return ItemStack.EMPTY;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.COD_DEATH;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return SoundEvents.COD_HURT;
	}

	@Override
	protected SoundEvent getFlopSound() {
		return SoundEvents.COD_FLOP;
	}
}
