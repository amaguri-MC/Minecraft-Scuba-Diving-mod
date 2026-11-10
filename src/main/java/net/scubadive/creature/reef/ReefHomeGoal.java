package net.scubadive.creature.reef;

import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.scubadive.creature.FleeHelper;

/** Swims back to the home spot after drifting off or after being scared. */
final class ReefHomeGoal extends Goal {
	private final PathfinderMob mob;
	private final ReefCreature fish;
	private final ReefProfile profile;
	private int cooldown;

	ReefHomeGoal(PathfinderMob mob, ReefProfile profile) {
		this.mob = mob;
		this.fish = (ReefCreature) mob;
		this.profile = profile;
		this.setFlags(EnumSet.of(Goal.Flag.MOVE));
	}

	private double homeDistSqr() {
		BlockPos h = this.fish.reefHome();
		return this.mob.distanceToSqr(h.getX() + 0.5, h.getY() + 0.5, h.getZ() + 0.5);
	}

	@Override
	public boolean canUse() {
		if (this.fish.reefHome() == null) {
			return false;
		}
		if (this.cooldown > 0) {
			this.cooldown--;
			return false;
		}
		if (this.mob.getRandom().nextInt(this.fish.hasFled() ? 4 : 20) != 0) {
			return false;
		}
		double limit = this.fish.hasFled() ? 1.5 : this.profile.homeRange();
		if (this.homeDistSqr() <= limit * limit) {
			this.fish.setFled(false);
			return false;
		}
		// Stay away from home while the player is still close.
		return FleeHelper.tooClose(this.mob, this.profile.fleeRadius() * 1.3) == null;
	}

	@Override
	public boolean canContinueToUse() {
		return !this.mob.getNavigation().isDone() && this.homeDistSqr() > 1.0
				&& FleeHelper.tooClose(this.mob, this.profile.fleeRadius()) == null;
	}

	@Override
	public void start() {
		BlockPos h = this.fish.reefHome();
		double speed = this.fish.hasFled() ? 1.3 : 1.0;
		if (!this.mob.getNavigation().moveTo(h.getX() + 0.5, h.getY() + 0.5, h.getZ() + 0.5, speed)) {
			// No route: give up for a while instead of re-pathing every tick.
			this.fish.setFled(false);
			this.cooldown = 100;
		}
	}

	@Override
	public void stop() {
		if (this.homeDistSqr() <= 2.25 * 2.25) {
			this.fish.setFled(false);
		}
	}
}
