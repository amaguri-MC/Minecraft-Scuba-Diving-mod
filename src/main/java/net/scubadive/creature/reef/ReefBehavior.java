package net.scubadive.creature.reef;

import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;

/** Goal wiring shared by the schooling and solitary reef fish classes. */
final class ReefBehavior {
	private ReefBehavior() {
	}

	/** Replaces vanilla's fixed-distance player avoidance with the species' own alertness. */
	static <T extends PathfinderMob & ReefCreature> void addGoals(T mob, ReefProfile profile) {
		mob.getGoalSelector().removeAllGoals(g -> g instanceof AvoidEntityGoal<?>);
		mob.getGoalSelector().addGoal(1, new ReefFleeGoal(mob, profile));
		mob.getGoalSelector().addGoal(3, new ReefHomeGoal(mob, profile));
	}
}
