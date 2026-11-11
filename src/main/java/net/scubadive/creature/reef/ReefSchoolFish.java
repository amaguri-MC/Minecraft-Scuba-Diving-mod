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
	/** Slot index in the leader's school (unique per follower, lowest free number). */
	private int slot;
	/** Slots handed out to followers, kept on the leader. */
	private final java.util.BitSet usedSlots = new java.util.BitSet();

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
		this.releaseSlot();
		this.reefLeader = leader;
		this.slot = leader instanceof ReefSchoolFish l ? l.takeSlot() : 0;
		return super.startFollowing(leader);
	}

	@Override
	public void stopFollowing() {
		this.releaseSlot();
		this.reefLeader = null;
		super.stopFollowing();
	}

	private int takeSlot() {
		int n = this.usedSlots.nextClearBit(0);
		this.usedSlots.set(n);
		return n;
	}

	private void releaseSlot() {
		if (this.reefLeader instanceof ReefSchoolFish l) {
			l.usedSlots.clear(this.slot);
		}
	}

	int reefSlot() {
		return this.slot;
	}

	/** The living leader this fish follows, or null. */
	AbstractSchoolingFish reefLeader() {
		return this.isFollower() ? this.reefLeader : null;
	}

	@Override
	public int getMaxSchoolSize() {
		return this.profile.schoolSize();
	}

	/**
	 * Vanilla takes (max - current size) new followers; a school already larger than the max (a big spawn group)
	 * would pass a negative limit and crash the server tick. A full school simply takes nobody.
	 */
	@Override
	public void addFollowers(java.util.stream.Stream<? extends AbstractSchoolingFish> fish) {
		if (this.hasFollowers() && !this.canBeFollowed()) {
			return;
		}
		super.addFollowers(fish);
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
