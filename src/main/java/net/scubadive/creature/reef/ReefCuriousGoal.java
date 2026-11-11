package net.scubadive.creature.reef;

import java.util.EnumSet;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;

/** Napoleon wrasse: every so often glides over to a diver, stops a few blocks away and eyes them. */
final class ReefCuriousGoal extends Goal {
	private static final double NOTICE = 12.0;
	private static final double KEEP = 2.8;
	private final PathfinderMob mob;
	private final ReefCreature fish;
	private Player diver;
	private int age;
	private int duration;
	private int cooldown = 100;
	private int repath;

	ReefCuriousGoal(PathfinderMob mob) {
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
		if (this.fish.hasFled() || this.mob.getRandom().nextInt(40) != 0) {
			return false;
		}
		Player p = this.mob.level().getNearestPlayer(this.mob, NOTICE);
		if (p == null || p.isSpectator() || !p.isInWater()) {
			return false;
		}
		this.diver = p;
		return true;
	}

	@Override
	public boolean canContinueToUse() {
		return this.age < this.duration && this.diver != null && this.diver.isAlive() && !this.diver.isSpectator()
				&& this.mob.distanceToSqr(this.diver) < (NOTICE + 4.0) * (NOTICE + 4.0) && !this.fish.hasFled();
	}

	@Override
	public void start() {
		this.age = 0;
		this.repath = 0;
		this.duration = 120 + this.mob.getRandom().nextInt(100);
	}

	@Override
	public void stop() {
		this.diver = null;
		this.mob.getNavigation().stop();
		this.cooldown = 500 + this.mob.getRandom().nextInt(400);
	}

	@Override
	public boolean requiresUpdateEveryTick() {
		return true;
	}

	@Override
	public void tick() {
		this.age++;
		if (this.diver == null) {
			return;
		}
		this.mob.getLookControl().setLookAt(this.diver, 10.0f, 10.0f);
		if (this.mob.distanceToSqr(this.diver) > KEEP * KEEP) {
			if (--this.repath <= 0) {
				this.repath = 15;
				this.mob.getNavigation().moveTo(this.diver, 0.7);
			}
		} else {
			this.mob.getNavigation().stop();
		}
	}
}
