package net.scubadive.creature.reef;

import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;

/** Grouper idling: an occasional slow shuffle within a block or two of its shelter. */
final class ReefLurkGoal extends Goal {
	private final PathfinderMob mob;
	private final ReefCreature fish;
	private int cooldown = 60;

	ReefLurkGoal(PathfinderMob mob) {
		this.mob = mob;
		this.fish = (ReefCreature) mob;
		this.setFlags(EnumSet.of(Goal.Flag.MOVE));
	}

	@Override
	public boolean canUse() {
		if (this.fish.reefHome() == null || --this.cooldown > 0) {
			return false;
		}
		this.cooldown = 80 + this.mob.getRandom().nextInt(140);
		return true;
	}

	@Override
	public boolean canContinueToUse() {
		return !this.mob.getNavigation().isDone();
	}

	@Override
	public void start() {
		RandomSource r = this.mob.getRandom();
		BlockPos h = this.fish.reefHome();
		double x = h.getX() + 0.5 + (r.nextDouble() - 0.5) * 3.0;
		double y = h.getY() + 0.5 + (r.nextDouble() - 0.5) * 1.5;
		double z = h.getZ() + 0.5 + (r.nextDouble() - 0.5) * 3.0;
		if (this.mob.level().getFluidState(BlockPos.containing(x, y, z)).is(FluidTags.WATER)) {
			this.mob.getNavigation().moveTo(x, y, z, 0.6);
		}
	}
}
