package net.scubadive.creature.reef;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.FollowFlockLeaderGoal;
import net.minecraft.world.entity.ai.goal.RandomSwimmingGoal;
import net.minecraft.world.level.Level;

/** Goal wiring shared by the schooling and solitary reef fish classes. */
final class ReefBehavior {
	private ReefBehavior() {
	}

	/** Replaces vanilla's fixed-distance player avoidance with the species' own alertness. */
	static <T extends PathfinderMob & ReefCreature> void addGoals(T mob, ReefProfile profile) {
		mob.getGoalSelector().removeAllGoals(g -> g instanceof AvoidEntityGoal<?>);
		mob.getGoalSelector().addGoal(1, new ReefFleeGoal(mob, profile));
		mob.getGoalSelector().addGoal(3, new ReefHomeGoal(mob, profile));
		switch (profile.trait()) {
			case GRAZER -> mob.getGoalSelector().addGoal(3, new ReefGrazeGoal(mob, profile));
			case TERRITORIAL -> mob.getGoalSelector().addGoal(2, new ReefChaseGoal(mob, profile));
			case LURKER -> {
				// Replace the roaming swim goal with a tiny shuffle around the shelter.
				mob.getGoalSelector().removeAllGoals(g -> g instanceof RandomSwimmingGoal);
				mob.getGoalSelector().addGoal(4, new ReefLurkGoal(mob));
			}
			case SCHOOLING -> {
				if (mob instanceof ReefSchoolFish school) {
					// Own school logic replaces vanilla's loose 8-block flock goal.
					mob.getGoalSelector().removeAllGoals(g -> g instanceof FollowFlockLeaderGoal);
					mob.getGoalSelector().addGoal(2, new ReefSchoolGoal(school));
				}
			}
			default -> {
			}
		}
	}

	/** Water cell near the fish with the most solid neighbours (a ceiling counts double), or its own cell. */
	static BlockPos findShelter(PathfinderMob mob) {
		Level level = mob.level();
		BlockPos origin = mob.blockPosition();
		BlockPos best = origin;
		int bestScore = 0;
		for (BlockPos p : BlockPos.betweenClosed(origin.offset(-8, -4, -8), origin.offset(8, 3, 8))) {
			if (!level.isLoaded(p) || !level.getFluidState(p).is(FluidTags.WATER)) {
				continue;
			}
			int score = 0;
			for (Direction d : Direction.values()) {
				BlockPos n = p.relative(d);
				if (level.getBlockState(n).isFaceSturdy(level, n, d.getOpposite())) {
					score += d == Direction.UP ? 2 : 1;
				}
			}
			if (score > bestScore || (score == bestScore && score > 0 && origin.distSqr(p) < origin.distSqr(best))) {
				bestScore = score;
				best = p.immutable();
			}
		}
		return bestScore >= 3 ? best : origin;
	}
}
