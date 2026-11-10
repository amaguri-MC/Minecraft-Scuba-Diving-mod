package net.scubadive.creature.reef;

import java.util.EnumSet;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.scubadive.creature.FleeHelper;

/** Triggerfish territory: charges toward a diver that comes close, hovers in front of them, then goes home. Never bites. */
final class ReefChaseGoal extends Goal {
	private static final double RANGE = 5.0;
	private static final double HOVER = 1.7;
	private final PathfinderMob mob;
	private final ReefCreature fish;
	private Player target;
	private int age;
	private int cooldown;
	private int repath;

	ReefChaseGoal(PathfinderMob mob, ReefProfile profile) {
		this.mob = mob;
		this.fish = (ReefCreature) mob;
		this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
	}

	@Override
	public boolean canUse() {
		if (this.cooldown > 0) {
			this.cooldown--;
			return false;
		}
		if (this.fish.hasFled()) {
			return false;
		}
		Player p = FleeHelper.tooClose(this.mob, RANGE);
		if (p == null || this.mob.distanceToSqr(p) < HOVER * HOVER) {
			return false;
		}
		this.target = p;
		return true;
	}

	@Override
	public boolean canContinueToUse() {
		return this.age < 100 && this.target != null && this.target.isAlive() && !this.target.isSpectator()
				&& this.mob.distanceToSqr(this.target) < 9.0 * 9.0 && !this.fish.hasFled();
	}

	@Override
	public void start() {
		this.age = 0;
		this.repath = 0;
	}

	@Override
	public void stop() {
		this.target = null;
		this.mob.getNavigation().stop();
		this.cooldown = 160 + this.mob.getRandom().nextInt(120);
	}

	@Override
	public boolean requiresUpdateEveryTick() {
		return true;
	}

	@Override
	public void tick() {
		this.age++;
		if (this.target == null) {
			return;
		}
		this.mob.getLookControl().setLookAt(this.target, 30.0f, 30.0f);
		if (this.mob.distanceToSqr(this.target) > HOVER * HOVER) {
			if (--this.repath <= 0) {
				this.repath = 8;
				this.mob.getNavigation().moveTo(this.target, 1.5);
			}
		} else {
			// Hovering in the diver's face is the whole threat.
			this.mob.getNavigation().stop();
		}
	}
}
