package net.scubadive.creature.big;

import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

/**
 * Slow free-swimming glide for very large fish. Steers yaw and pitch at a limited rate (wide turns)
 * and probes the water ahead so the fish turns away from the sea floor, walls and the surface
 * before it touches them.
 */
final class CruiseGoal extends Goal {
	private final Mob mob;
	private final double speed;
	private final float yawStep;
	private final float pitchStep;
	private final int lookAhead;
	private final float halfWidth;
	private final float halfHeight;

	private float wantYaw;
	private float wantPitch;
	private int retarget;
	private int probeTimer;
	private int stuckTimer;
	private Vec3 lastPos = Vec3.ZERO;

	/**
	 * @param speed     blocks per tick
	 * @param yawStep   max turn per tick, degrees
	 * @param pitchStep max pitch change per tick, degrees
	 * @param lookAhead probe distance in blocks
	 */
	CruiseGoal(Mob mob, double speed, float yawStep, float pitchStep, int lookAhead, float halfWidth, float halfHeight) {
		this.mob = mob;
		this.speed = speed;
		this.yawStep = yawStep;
		this.pitchStep = pitchStep;
		this.lookAhead = lookAhead;
		this.halfWidth = halfWidth;
		this.halfHeight = halfHeight;
		this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
	}

	@Override
	public boolean canUse() {
		return this.mob.isInWater();
	}

	@Override
	public boolean canContinueToUse() {
		return this.mob.isInWater();
	}

	@Override
	public boolean requiresUpdateEveryTick() {
		return true;
	}

	@Override
	public void start() {
		this.wantYaw = this.mob.getYRot();
		this.wantPitch = 0.0f;
		this.retarget = 0;
		this.probeTimer = 0;
		this.stuckTimer = 0;
		this.lastPos = this.mob.position();
	}

	@Override
	public void tick() {
		RandomSource random = this.mob.getRandom();
		float yaw = this.mob.getYRot();
		float pitch = this.mob.getXRot();

		// Occasionally pick a new lazy course.
		if (--this.retarget <= 0) {
			this.retarget = 120 + random.nextInt(200);
			this.wantYaw = yaw + (random.nextFloat() - 0.5f) * 120.0f;
			this.wantPitch = (random.nextFloat() - 0.5f) * 16.0f;
		}

		// Probe ahead a few times a second; steer away when the way is not open water.
		if (--this.probeTimer <= 0) {
			this.probeTimer = 4;
			if (!this.clear(yaw, pitch)) {
				this.avoid(yaw);
			} else if (!this.clear(this.wantYaw, this.wantPitch)) {
				// The lazy course leads into a wall: cancel it.
				this.wantYaw = yaw;
				this.wantPitch = 0.0f;
			}
		}

		// Stuck check (pressed against something): about-face.
		if (++this.stuckTimer >= 40) {
			this.stuckTimer = 0;
			Vec3 now = this.mob.position();
			if (now.distanceToSqr(this.lastPos) < 1.0) {
				this.wantYaw = yaw + 150.0f;
				this.wantPitch = 0.0f;
				this.retarget = 80;
			}
			this.lastPos = now;
		}
		if (this.mob.horizontalCollision) {
			this.wantYaw = yaw + 90.0f;
			this.retarget = 60;
		}

		yaw += Mth.clamp(Mth.wrapDegrees(this.wantYaw - yaw), -this.yawStep, this.yawStep);
		pitch += Mth.clamp(this.wantPitch - pitch, -this.pitchStep, this.pitchStep);
		pitch = Mth.clamp(pitch, -25.0f, 25.0f);

		this.mob.setYRot(yaw);
		this.mob.setYBodyRot(yaw);
		this.mob.setYHeadRot(yaw);
		this.mob.setXRot(pitch);

		float yawRad = yaw * 0.017453292f;
		float pitchRad = pitch * 0.017453292f;
		double cosP = Mth.cos(pitchRad);
		this.mob.setDeltaMovement(-Mth.sin(yawRad) * cosP * this.speed, -Mth.sin(pitchRad) * this.speed, Mth.cos(yawRad) * cosP * this.speed);
	}

	/** Look for a clear heading: sideways first, then climb or dive, finally a U-turn. */
	private void avoid(float yaw) {
		this.retarget = 60;
		for (int i = 1; i <= 8; i++) {
			for (int sign = 1; sign >= -1; sign -= 2) {
				float cand = yaw + sign * i * 22.5f;
				if (this.clear(cand, 0.0f)) {
					this.wantYaw = cand;
					this.wantPitch = 0.0f;
					return;
				}
			}
		}
		if (this.clear(yaw, -20.0f)) {
			this.wantYaw = yaw;
			this.wantPitch = -20.0f;
		} else if (this.clear(yaw, 20.0f)) {
			this.wantYaw = yaw;
			this.wantPitch = 20.0f;
		} else {
			this.wantYaw = yaw + 180.0f;
			this.wantPitch = 0.0f;
		}
	}

	/** True when the volume ahead along the given heading is all water. */
	private boolean clear(float yaw, float pitch) {
		float yawRad = yaw * 0.017453292f;
		float pitchRad = pitch * 0.017453292f;
		double cosP = Mth.cos(pitchRad);
		double fx = -Mth.sin(yawRad) * cosP;
		double fy = -Mth.sin(pitchRad);
		double fz = Mth.cos(yawRad) * cosP;
		double rx = Mth.cos(yawRad);
		double rz = Mth.sin(yawRad);
		Vec3 origin = this.mob.position().add(0.0, this.mob.getBbHeight() * 0.5, 0.0);
		int step = Math.max(2, this.lookAhead / 4);
		for (int d = step; d <= this.lookAhead; d += step) {
			double x = origin.x + fx * d;
			double y = origin.y + fy * d;
			double z = origin.z + fz * d;
			if (!this.water(x, y, z)
					|| !this.water(x + rx * this.halfWidth, y, z + rz * this.halfWidth)
					|| !this.water(x - rx * this.halfWidth, y, z - rz * this.halfWidth)
					|| !this.water(x, y + this.halfHeight, z)
					|| !this.water(x, y - this.halfHeight, z)) {
				return false;
			}
		}
		return true;
	}

	private boolean water(double x, double y, double z) {
		return this.mob.level().getFluidState(BlockPos.containing(x, y, z)).is(FluidTags.WATER);
	}
}
