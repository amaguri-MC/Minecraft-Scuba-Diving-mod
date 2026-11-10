package net.scubadive.creature.reef;

import java.util.EnumSet;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.fish.AbstractSchoolingFish;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Keeps schooling reef fish in a school at all times. Followers hold a fixed slot around the
 * leader (about 0.9 blocks apart) and copy its heading; a fish with no school within 12 blocks
 * swims to the nearest school of its species within 32 blocks. Searches run every 1-2 seconds.
 */
final class ReefSchoolGoal extends Goal {
	private static final double JOIN_RADIUS = 12.0;
	private static final double SEEK_RADIUS = 32.0;
	private static final double SPACING = 0.9;
	private final ReefSchoolFish fish;
	private ReefSchoolFish target;
	private int scan;
	private int age;
	private int repath;

	ReefSchoolGoal(ReefSchoolFish fish) {
		this.fish = fish;
		this.scan = fish.getRandom().nextInt(20);
		this.setFlags(EnumSet.of(Goal.Flag.MOVE));
	}

	@Override
	public boolean canUse() {
		if (this.fish.reefLeader() != null) {
			return true;
		}
		if (this.fish.hasFollowers() || --this.scan > 0) {
			return false;
		}
		this.scan = 20 + this.fish.getRandom().nextInt(20);
		return this.regroup();
	}

	@Override
	public boolean canContinueToUse() {
		if (this.fish.reefLeader() != null) {
			return true;
		}
		return this.target != null && this.target.isAlive() && this.age < 300
				&& this.fish.distanceToSqr(this.target) > 10.0 * 10.0;
	}

	@Override
	public void start() {
		this.age = 0;
		this.repath = 0;
	}

	@Override
	public void stop() {
		if (this.target != null) {
			this.fish.getNavigation().stop();
		}
		this.target = null;
	}

	@Override
	public boolean requiresUpdateEveryTick() {
		return true;
	}

	@Override
	public void tick() {
		this.age++;
		AbstractSchoolingFish leader = this.fish.reefLeader();
		if (leader != null) {
			this.follow(leader);
		} else if (this.target != null && --this.repath <= 0) {
			this.repath = 10;
			this.fish.getNavigation().moveTo(this.target, 1.3);
		}
	}

	private void follow(AbstractSchoolingFish leader) {
		this.fish.setFled(false);
		// Stable slot per fish on a lattice behind and beside the leader.
		int cell = Math.floorMod(this.fish.getId() * 31, 90);
		double side = (cell % 5 - 2) * SPACING;
		double back = ((cell / 5) % 6 - 1) * SPACING;
		double up = (cell / 30 - 1) * 0.6;
		double yaw = Math.toRadians(leader.getYRot());
		double fx = -Math.sin(yaw);
		double fz = Math.cos(yaw);
		double tx = leader.getX() + Math.cos(yaw) * side - fx * back;
		double tz = leader.getZ() + Math.sin(yaw) * side - fz * back;
		double ty = leader.getY() + up;
		double dist = this.fish.distanceToSqr(tx, ty, tz);
		if (dist > 0.45 * 0.45) {
			if (--this.repath <= 0) {
				this.repath = 4;
				double speed = Math.max(1.0, Math.min(2.2, 1.0 + Math.sqrt(dist) * 0.4));
				if (!this.fish.getNavigation().moveTo(tx, ty, tz, speed)) {
					this.fish.getNavigation().moveTo(leader, 1.2);
				}
			}
		} else {
			// In place: stop and line up with the leader so the school swims the same way.
			this.fish.getNavigation().stop();
			this.fish.setYRot(leader.getYRot());
			this.fish.setYBodyRot(leader.getYRot());
			this.fish.setYHeadRot(leader.getYRot());
		}
	}

	/** Joins a school within 12 blocks (returns false), or starts swimming to a far one (returns true). */
	private boolean regroup() {
		AABB box = this.fish.getBoundingBox().inflate(JOIN_RADIUS);
		List<ReefSchoolFish> near = this.fish.level().getEntitiesOfClass(ReefSchoolFish.class, box,
				o -> o != this.fish && o.isAlive() && o.reefProfile() == this.fish.reefProfile());
		if (!near.isEmpty()) {
			this.join(near);
			return this.fish.reefLeader() != null;
		}
		List<ReefSchoolFish> far = this.fish.level().getEntitiesOfClass(ReefSchoolFish.class, this.fish.getBoundingBox().inflate(SEEK_RADIUS),
				o -> o != this.fish && o.isAlive() && o.reefProfile() == this.fish.reefProfile()
						&& (o.canBeFollowed() || !o.isFollower()));
		ReefSchoolFish best = null;
		double bestDist = Double.MAX_VALUE;
		Vec3 me = this.fish.position();
		for (ReefSchoolFish o : far) {
			double d = o.position().distanceToSqr(me);
			if (d < bestDist) {
				bestDist = d;
				best = o;
			}
		}
		this.target = best;
		return best != null;
	}

	private void join(List<ReefSchoolFish> near) {
		AbstractSchoolingFish leader = null;
		for (ReefSchoolFish o : near) {
			if (o.canBeFollowed()) {
				leader = o;
				break;
			}
		}
		if (leader == null) {
			// Only loners around: the lowest id leads so everybody agrees.
			leader = this.fish;
			for (ReefSchoolFish o : near) {
				if (!o.isFollower() && o.getId() < leader.getId()) {
					leader = o;
				}
			}
		}
		if (leader == this.fish) {
			this.fish.addFollowers(near.stream().filter(o -> !o.isFollower() && !o.hasFollowers()));
		} else if (!leader.isFollower()) {
			leader.addFollowers(Stream.of(this.fish));
		}
	}
}
