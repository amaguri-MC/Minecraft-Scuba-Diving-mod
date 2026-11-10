package net.scubadive.creature.big;

import java.util.EnumSet;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

/**
 * Schooling vortex: every fish circles the centroid of the trevally around it, each on its own
 * radius and depth, while the whole school slowly drifts. Needs no leader.
 */
final class SwirlGoal extends Goal {
	private final BigeyeTrevally fish;
	private int cooldown;
	private Vec3 anchor;

	SwirlGoal(BigeyeTrevally fish) {
		this.fish = fish;
		this.setFlags(EnumSet.of(Goal.Flag.MOVE));
	}

	@Override
	public boolean canUse() {
		return this.fish.isInWater();
	}

	@Override
	public boolean canContinueToUse() {
		return this.fish.isInWater();
	}

	@Override
	public void start() {
		this.cooldown = 0;
		this.anchor = this.fish.position();
	}

	@Override
	public void tick() {
		if (--this.cooldown > 0) {
			return;
		}
		this.cooldown = 6 + this.fish.getRandom().nextInt(4);

		List<BigeyeTrevally> mates = this.fish.level().getEntitiesOfClass(BigeyeTrevally.class,
				this.fish.getBoundingBox().inflate(16.0, 8.0, 16.0));
		Vec3 sum = Vec3.ZERO;
		int lowestId = Integer.MAX_VALUE;
		for (BigeyeTrevally m : mates) {
			sum = sum.add(m.position());
			lowestId = Math.min(lowestId, m.getId());
		}
		int n = mates.size();
		long time = this.fish.level().getGameTime();

		// A lone fish orbits its own anchor; a school orbits its centroid.
		Vec3 center = n >= 3 ? sum.scale(1.0 / n) : this.anchor;
		// The school-wide drift seed comes from the lowest id, so all members agree.
		double drift = time * 0.0025 + lowestId * 1.7;
		double cx = center.x + Math.cos(drift) * 2.0;
		double cz = center.z + Math.sin(drift) * 2.0;
		double cy = center.y + Math.sin(time * 0.004 + lowestId) * 1.0;

		Vec3 pos = this.fish.position();
		double theta = Mth.atan2(pos.z - cz, pos.x - cx);
		double radius = 3.0 + Math.floorMod(this.fish.getId() * 31, 5);
		double ang = theta + 0.55;
		double tx = cx + Math.cos(ang) * radius;
		double tz = cz + Math.sin(ang) * radius;
		double ty = cy + (Math.floorMod(this.fish.getId(), 7) - 3) * 0.7;

		if (!this.water(tx, ty, tz) || !this.water((tx + pos.x) * 0.5, (ty + pos.y) * 0.5, (tz + pos.z) * 0.5)) {
			// Open water is not there: head for the school centre instead.
			tx = cx;
			ty = cy;
			tz = cz;
			if (!this.water(tx, ty, tz)) {
				return;
			}
		}
		// Fish move control only steers along an active path, so use navigation (not setWantedPosition).
		this.fish.getNavigation().moveTo(tx, ty, tz, 1.0);
	}

	private boolean water(double x, double y, double z) {
		return this.fish.level().getFluidState(BlockPos.containing(x, y, z)).is(FluidTags.WATER);
	}
}
