package net.scubadive.creature.reef;

import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;

/** Mandarinfish idling: short slow hops just above the floor, preferring spots tucked against coral. */
final class ReefBottomGoal extends Goal {
	private final PathfinderMob mob;
	private final ReefCreature fish;
	private final ReefProfile profile;
	private int cooldown = 40;

	ReefBottomGoal(PathfinderMob mob, ReefProfile profile) {
		this.mob = mob;
		this.fish = (ReefCreature) mob;
		this.profile = profile;
		this.setFlags(EnumSet.of(Goal.Flag.MOVE));
	}

	@Override
	public boolean canUse() {
		if (this.fish.reefHome() == null || --this.cooldown > 0) {
			return false;
		}
		this.cooldown = 50 + this.mob.getRandom().nextInt(110);
		return true;
	}

	@Override
	public boolean canContinueToUse() {
		return !this.mob.getNavigation().isDone();
	}

	@Override
	public void start() {
		Level level = this.mob.level();
		RandomSource r = this.mob.getRandom();
		BlockPos home = this.fish.reefHome();
		int range = Math.max(1, (int) this.profile.homeRange());
		BlockPos best = null;
		int bestScore = -1;
		for (int i = 0; i < 6; i++) {
			BlockPos p = home.offset(r.nextInt(range * 2 + 1) - range, r.nextInt(3) - 1, r.nextInt(range * 2 + 1) - range);
			BlockPos floor = this.floorWater(level, p);
			if (floor == null) {
				continue;
			}
			int score = r.nextInt(2);
			for (Direction d : Direction.Plane.HORIZONTAL) {
				if (ReefBehavior.isCoral(level.getBlockState(floor.relative(d)))) {
					score += 2;
				}
			}
			if (score > bestScore) {
				bestScore = score;
				best = floor;
			}
		}
		if (best != null) {
			this.mob.getNavigation().moveTo(best.getX() + 0.5, best.getY() + 0.15 + r.nextDouble() * 0.3, best.getZ() + 0.5, 0.6);
		}
	}

	/** Lowest water cell at or below p (within 4 blocks) that rests on a solid floor, or null. */
	private BlockPos floorWater(Level level, BlockPos p) {
		if (!level.isLoaded(p) || !level.getFluidState(p).is(FluidTags.WATER)) {
			return null;
		}
		BlockPos.MutableBlockPos m = p.mutable();
		for (int i = 0; i < 4; i++) {
			BlockPos below = m.below();
			if (level.getBlockState(below).isFaceSturdy(level, below, Direction.UP)) {
				return m.immutable();
			}
			if (!level.getFluidState(below).is(FluidTags.WATER)) {
				return null;
			}
			m.move(Direction.DOWN);
		}
		return null;
	}
}
