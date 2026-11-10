package net.scubadive.creature.big;

import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.scubadive.creature.FleeHelper;

/** Rests on the sea floor; slides a few blocks away when a diver gets close, then settles again. */
final class StingrayGoal extends Goal {
	private static final double FLEE_RADIUS = 4.0;
	private final Stingray ray;
	private int fleeTicks;

	StingrayGoal(Stingray ray) {
		this.ray = ray;
		this.setFlags(EnumSet.of(Goal.Flag.MOVE));
	}

	@Override
	public boolean canUse() {
		return this.ray.isInWater();
	}

	@Override
	public boolean canContinueToUse() {
		return this.ray.isInWater();
	}

	@Override
	public boolean requiresUpdateEveryTick() {
		return true;
	}

	@Override
	public void stop() {
		this.fleeTicks = 0;
	}

	@Override
	public void tick() {
		Player threat = FleeHelper.tooClose(this.ray, FLEE_RADIUS);
		if (threat != null && this.fleeTicks <= 0) {
			this.fleeTicks = 20 + this.ray.getRandom().nextInt(15);
			Vec3 away = FleeHelper.awayFrom(this.ray, threat);
			double len = Math.sqrt(away.x * away.x + away.z * away.z);
			if (len > 1.0E-4) {
				double dist = 5.0 + this.ray.getRandom().nextDouble() * 2.0;
				double tx = this.ray.getX() + away.x / len * dist;
				double tz = this.ray.getZ() + away.z / len * dist;
				double ty = this.ray.getY();
				if (this.ray.level().getFluidState(BlockPos.containing(tx, ty, tz)).is(FluidTags.WATER)) {
					this.ray.getMoveControl().setWantedPosition(tx, ty, tz, 1.6);
				}
			}
		}
		if (this.fleeTicks > 0) {
			if (--this.fleeTicks == 0) {
				// Cancel any unfinished slide so the ray really stays put.
				this.ray.getMoveControl().setWantedPosition(this.ray.getX(), this.ray.getY(), this.ray.getZ(), 0.0);
			}
			return;
		}
		// At rest: stop and sink onto the floor.
		this.ray.setSpeed(0.0f);
		Vec3 dm = this.ray.getDeltaMovement();
		if (!this.ray.onGround()) {
			this.ray.setDeltaMovement(dm.x * 0.5, -0.04, dm.z * 0.5);
		} else {
			this.ray.setDeltaMovement(0.0, dm.y, 0.0);
		}
	}
}
