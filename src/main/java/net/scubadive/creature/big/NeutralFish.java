package net.scubadive.creature.big;

import net.minecraft.util.RandomSource;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.NeutralMob;

/** NeutralMob plumbing shared by the big fish: peaceful until hurt, then angry for 20-39 seconds. */
public interface NeutralFish extends NeutralMob {
	UniformInt ANGER_TIME = TimeUtil.rangeOfSeconds(20, 39);

	/** Mutable anger state owned by the entity. */
	final class AngerState {
		long endTime;
		EntityReference<LivingEntity> target;
	}

	AngerState angerState();

	@Override
	default long getPersistentAngerEndTime() {
		return this.angerState().endTime;
	}

	@Override
	default void setPersistentAngerEndTime(long endTime) {
		this.angerState().endTime = endTime;
	}

	@Override
	default EntityReference<LivingEntity> getPersistentAngerTarget() {
		return this.angerState().target;
	}

	@Override
	default void setPersistentAngerTarget(EntityReference<LivingEntity> target) {
		this.angerState().target = target;
	}

	@Override
	default void startPersistentAngerTimer() {
		this.setTimeToRemainAngry(ANGER_TIME.sample(RandomSource.createThreadLocalInstance()));
	}
}
