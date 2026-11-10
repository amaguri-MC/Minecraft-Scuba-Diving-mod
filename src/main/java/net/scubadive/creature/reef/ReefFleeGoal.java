package net.scubadive.creature.reef;

import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.scubadive.creature.FleeHelper;

/** Bolts away from a close player, or darts into nearby coral for species that hide. */
final class ReefFleeGoal extends Goal {
	private final PathfinderMob mob;
	private final ReefCreature fish;
	private final ReefProfile profile;
	private Player threat;
	private BlockPos hideSpot;
	private int repath;

	ReefFleeGoal(PathfinderMob mob, ReefProfile profile) {
		this.mob = mob;
		this.fish = (ReefCreature) mob;
		this.profile = profile;
		this.setFlags(EnumSet.of(Goal.Flag.MOVE));
	}

	@Override
	public boolean canUse() {
		this.threat = FleeHelper.tooClose(this.mob, this.profile.fleeRadius());
		return this.threat != null;
	}

	@Override
	public boolean canContinueToUse() {
		this.threat = FleeHelper.tooClose(this.mob, this.profile.fleeRadius() * 1.5);
		return this.threat != null;
	}

	@Override
	public void start() {
		this.fish.setFled(true);
		this.repath = 0;
		if (this.profile.trait() == ReefProfile.Trait.LURKER) {
			// Groupers retreat into their shelter.
			this.hideSpot = this.fish.reefHome();
		} else {
			this.hideSpot = this.profile.hide() ? this.findCoral() : null;
		}
	}

	@Override
	public void stop() {
		this.threat = null;
		this.hideSpot = null;
		this.mob.getNavigation().stop();
	}

	@Override
	public boolean requiresUpdateEveryTick() {
		return true;
	}

	@Override
	public void tick() {
		if (this.threat == null) {
			return;
		}
		if (this.hideSpot != null) {
			Vec3 c = Vec3.atCenterOf(this.hideSpot);
			if (this.mob.distanceToSqr(c) > 0.5) {
				if (--this.repath <= 0) {
					this.repath = 10;
					this.mob.getNavigation().moveTo(c.x, c.y, c.z, this.profile.fleeSpeed());
				}
			} else {
				// Tucked in: hold still until the player leaves.
				this.mob.getNavigation().stop();
			}
			return;
		}
		if (--this.repath <= 0 || this.mob.getNavigation().isDone()) {
			this.repath = 8 + this.mob.getRandom().nextInt(8);
			this.flee();
		}
	}

	private void flee() {
		Vec3 away = FleeHelper.awayFrom(this.mob, this.threat);
		RandomSource r = this.mob.getRandom();
		for (int i = 0; i < 8; i++) {
			double ang = (r.nextDouble() - 0.5) * (i < 4 ? 1.0 : 2.4);
			double cos = Math.cos(ang);
			double sin = Math.sin(ang);
			double dx = away.x * cos - away.z * sin;
			double dz = away.x * sin + away.z * cos;
			double dist = 4.0 + r.nextDouble() * 3.0;
			double ty = this.mob.getY() + away.y * dist + (r.nextDouble() - 0.5) * 1.5;
			double tx = this.mob.getX() + dx * dist;
			double tz = this.mob.getZ() + dz * dist;
			if (this.mob.level().getFluidState(BlockPos.containing(tx, ty, tz)).is(FluidTags.WATER)) {
				this.mob.getNavigation().moveTo(tx, ty, tz, this.profile.fleeSpeed());
				return;
			}
		}
	}

	/** Water cell just above the nearest coral, or null. */
	private BlockPos findCoral() {
		BlockPos origin = this.mob.blockPosition();
		BlockPos best = null;
		double bestDist = Double.MAX_VALUE;
		for (BlockPos p : BlockPos.betweenClosed(origin.offset(-6, -4, -6), origin.offset(6, 4, 6))) {
			BlockState s = this.mob.level().getBlockState(p);
			if (!(s.is(BlockTags.CORALS) || s.is(BlockTags.CORAL_BLOCKS) || s.is(BlockTags.WALL_CORALS))) {
				continue;
			}
			BlockPos above = p.above();
			if (!this.mob.level().getFluidState(above).is(FluidTags.WATER)) {
				continue;
			}
			double d = origin.distSqr(p);
			if (d < bestDist) {
				bestDist = d;
				best = above.immutable();
			}
		}
		return best;
	}
}
