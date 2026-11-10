package net.scubadive.creature.bottom;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.scubadive.creature.ScubaCreatures;

/** Base for creatures that live on a sea anemone: remembers one anemone as home and steers around it. */
public abstract class AnemoneDweller extends Mob {
	/** Radius in blocks within which an anemone is adopted as home. */
	public static final int HOME_RANGE = 8;

	protected BlockPos home;
	protected Vec3 target;
	protected int retarget;
	private int searchTimer;

	protected AnemoneDweller(EntityType<? extends AnemoneDweller> type, Level level) {
		super(type, level);
	}

	@Override
	public boolean canBreatheUnderwater() {
		return true;
	}

	protected boolean isAnemone(BlockPos pos) {
		return this.level().getBlockState(pos).is(ScubaCreatures.SEA_ANEMONE);
	}

	/** Drops a destroyed home and, now and then, looks for a new one. */
	protected void updateHome() {
		if (this.home != null && !isAnemone(this.home)) {
			this.home = null;
		}
		if (--this.searchTimer > 0) {
			return;
		}
		this.searchTimer = 60 + this.getRandom().nextInt(60);
		if (this.home == null) {
			this.home = findNearestAnemone();
		}
	}

	private BlockPos findNearestAnemone() {
		BlockPos origin = this.blockPosition();
		BlockPos best = null;
		double bestDist = Double.MAX_VALUE;
		for (BlockPos p : BlockPos.betweenClosed(
				origin.offset(-HOME_RANGE, -HOME_RANGE / 2, -HOME_RANGE),
				origin.offset(HOME_RANGE, HOME_RANGE / 2, HOME_RANGE))) {
			double d = p.distSqr(origin);
			if (d <= HOME_RANGE * HOME_RANGE && d < bestDist && isAnemone(p)) {
				bestDist = d;
				best = p.immutable();
			}
		}
		return best;
	}

	/** Eases the velocity toward the point, slowing down on arrival, and turns to face the motion. */
	protected void steer(Vec3 to, double speed) {
		Vec3 d = to.subtract(this.position());
		double len = d.length();
		Vec3 desired = Vec3.ZERO;
		if (len > 0.02) {
			double s = Math.min(speed, len * 0.25);
			desired = d.scale(s / len);
		}
		Vec3 v = this.getDeltaMovement().scale(0.6).add(desired.scale(0.4));
		this.setDeltaMovement(v);
		if (v.x * v.x + v.z * v.z > 1.0E-5) {
			faceTowards(v);
		}
	}

	protected void faceTowards(Vec3 dir) {
		float yaw = (float) Math.toDegrees(Math.atan2(-dir.x, dir.z));
		float y = Mth.approachDegrees(this.getYRot(), yaw, 25.0f);
		this.setYRot(y);
		this.setYBodyRot(y);
		this.setYHeadRot(y);
	}

	/** Lazy drift around the current spot, used when there is no anemone to live on. */
	protected void wanderFree(double speed) {
		if (this.target == null || --this.retarget <= 0) {
			this.retarget = 40 + this.getRandom().nextInt(60);
			Vec3 t = this.position().add(
					(this.getRandom().nextDouble() - 0.5) * 3.0,
					(this.getRandom().nextDouble() - 0.5) * 0.8,
					(this.getRandom().nextDouble() - 0.5) * 3.0);
			this.target = isWater(t) ? t : this.position();
		}
		steer(this.target, speed);
	}

	protected boolean isWater(Vec3 p) {
		return this.level().getFluidState(BlockPos.containing(p)).is(net.minecraft.tags.FluidTags.WATER);
	}
}
