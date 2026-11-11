package net.scubadive.creature.temperate;

import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

/**
 * Sunfish movement: a slow, wide-turning cruise that probes ahead for walls, floor and surface.
 * Every few minutes the fish rises to just below an open surface, lies on its side and drifts there
 * for a while (basking), then dives back down.
 */
final class SunfishSwimGoal extends Goal {
	private static final double CRUISE_SPEED = 0.055;
	private static final double DRIFT_SPEED = 0.008;
	private static final float YAW_STEP = 0.9f;
	private static final float PITCH_STEP = 0.4f;
	private static final int LOOK_AHEAD = 8;
	private static final float HALF_WIDTH = 0.6f;
	private static final float HALF_HEIGHT = 1.2f;
	/** Body centre height above the entity origin. */
	private static final double CENTRE = 1.1;

	private enum State { CRUISE, RISE, BASK }

	private final OceanSunfish fish;
	private State state = State.CRUISE;
	private float wantYaw;
	private float wantPitch;
	private int retarget;
	private int probeTimer;
	private int stuckTimer;
	private int stateTimer;
	private int baskCooldown;
	private double baskY;
	private Vec3 lastPos = Vec3.ZERO;

	SunfishSwimGoal(OceanSunfish fish) {
		this.fish = fish;
		this.baskCooldown = 600 + fish.getRandom().nextInt(1200);
		this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
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
	public boolean requiresUpdateEveryTick() {
		return true;
	}

	@Override
	public void start() {
		this.state = State.CRUISE;
		this.wantYaw = this.fish.getYRot();
		this.wantPitch = 0.0f;
		this.retarget = 0;
		this.probeTimer = 0;
		this.stuckTimer = 0;
		this.lastPos = this.fish.position();
	}

	@Override
	public void stop() {
		this.fish.setBasking(false);
		this.state = State.CRUISE;
	}

	@Override
	public void tick() {
		if (this.baskCooldown > 0) {
			this.baskCooldown--;
		}
		// A hit sends it down at once.
		if (this.state != State.CRUISE && this.fish.hurtTime > 0) {
			this.endBask(1200);
		}
		switch (this.state) {
			case CRUISE -> this.cruise();
			case RISE -> this.rise();
			case BASK -> this.bask();
		}
	}

	private void cruise() {
		RandomSource random = this.fish.getRandom();
		float yaw = this.fish.getYRot();
		float pitch = this.fish.getXRot();

		if (this.baskCooldown <= 0 && random.nextInt(200) == 0 && this.startRise()) {
			return;
		}
		if (--this.retarget <= 0) {
			this.retarget = 100 + random.nextInt(200);
			this.wantYaw = yaw + (random.nextFloat() - 0.5f) * 140.0f;
			this.wantPitch = (random.nextFloat() - 0.5f) * 14.0f;
		}
		if (--this.probeTimer <= 0) {
			this.probeTimer = 4;
			if (!this.clear(yaw, pitch, HALF_HEIGHT)) {
				this.avoid(yaw);
			} else if (!this.clear(this.wantYaw, this.wantPitch, HALF_HEIGHT)) {
				this.wantYaw = yaw;
				this.wantPitch = 0.0f;
			}
		}
		if (++this.stuckTimer >= 40) {
			this.stuckTimer = 0;
			Vec3 now = this.fish.position();
			if (now.distanceToSqr(this.lastPos) < 0.5) {
				this.wantYaw = yaw + 150.0f;
				this.wantPitch = 0.0f;
				this.retarget = 80;
			}
			this.lastPos = now;
		}
		if (this.fish.horizontalCollision) {
			this.wantYaw = yaw + 90.0f;
			this.retarget = 60;
		}
		this.steer(CRUISE_SPEED, true);
	}

	/** Swim up toward the basking depth, keeping clear of walls. */
	private void rise() {
		if (++this.stateTimer > 900 || !this.openSurfaceAbove()) {
			this.endBask(600);
			return;
		}
		float yaw = this.fish.getYRot();
		if (--this.probeTimer <= 0) {
			this.probeTimer = 4;
			if (!this.clear(yaw, 0.0f, 0.0f)) {
				this.wantYaw = yaw + 60.0f + this.fish.getRandom().nextFloat() * 60.0f;
			}
		}
		double dy = this.baskY - this.fish.getY();
		if (dy < 0.15) {
			this.state = State.BASK;
			this.stateTimer = 300 + this.fish.getRandom().nextInt(400);
			this.fish.setBasking(true);
			return;
		}
		this.wantPitch = -18.0f;
		this.steer(CRUISE_SPEED * 0.6, false);
		Vec3 v = this.fish.getDeltaMovement();
		this.fish.setDeltaMovement(v.x, Mth.clamp(dy * 0.05, 0.01, 0.035), v.z);
	}

	/** Lie on the side just under the surface and drift. */
	private void bask() {
		if (--this.stateTimer <= 0 || !this.openSurfaceAbove()) {
			this.endBask(2400 + this.fish.getRandom().nextInt(2400));
			return;
		}
		float yaw = this.fish.getYRot();
		if (--this.retarget <= 0) {
			this.retarget = 80 + this.fish.getRandom().nextInt(80);
			this.wantYaw = yaw + (this.fish.getRandom().nextFloat() - 0.5f) * 60.0f;
		}
		if (!this.clear(yaw, 0.0f, 0.0f)) {
			this.wantYaw = yaw + 120.0f;
		}
		this.wantPitch = 0.0f;
		this.steer(DRIFT_SPEED, false);
		Vec3 v = this.fish.getDeltaMovement();
		this.fish.setDeltaMovement(v.x, Mth.clamp((this.baskY - this.fish.getY()) * 0.05, -0.02, 0.02), v.z);
	}

	private void endBask(int cooldown) {
		this.fish.setBasking(false);
		this.state = State.CRUISE;
		this.baskCooldown = cooldown;
		// Head back down for a while.
		this.wantPitch = 14.0f;
		this.retarget = 100;
	}

	/** Finds open sky above the water column; sets the basking depth. */
	private boolean startRise() {
		Double surface = this.surfaceY();
		if (surface == null) {
			this.baskCooldown = 400;
			return false;
		}
		this.baskY = surface - 0.45 - CENTRE;
		if (this.baskY < this.fish.getY()) {
			// Already shallow: bask right here.
			this.baskY = Math.max(this.fish.getY() - 0.5, this.baskY);
		}
		this.state = State.RISE;
		this.stateTimer = 0;
		return true;
	}

	private boolean openSurfaceAbove() {
		return this.surfaceY() != null;
	}

	/** Y of the water surface above the fish when the cell above the water is air, else null. */
	private Double surfaceY() {
		BlockPos.MutableBlockPos p = BlockPos.containing(this.fish.getX(), this.fish.getY() + CENTRE, this.fish.getZ()).mutable();
		for (int i = 0; i < 40; i++) {
			if (!this.fish.level().getFluidState(p).is(FluidTags.WATER)) {
				return this.fish.level().getBlockState(p).isAir() ? (double) p.getY() : null;
			}
			p.move(0, 1, 0);
		}
		return null;
	}

	/** Turns toward the wanted heading at a limited rate and sets the velocity along it. */
	private void steer(double speed, boolean usePitch) {
		float yaw = this.fish.getYRot();
		float pitch = this.fish.getXRot();
		yaw += Mth.clamp(Mth.wrapDegrees(this.wantYaw - yaw), -YAW_STEP, YAW_STEP);
		pitch += Mth.clamp(this.wantPitch - pitch, -PITCH_STEP, PITCH_STEP);
		pitch = Mth.clamp(pitch, -20.0f, 20.0f);
		if (!usePitch && this.state == State.BASK) {
			pitch = Mth.clamp(pitch, -2.0f, 2.0f);
		}
		this.fish.setYRot(yaw);
		this.fish.setYBodyRot(yaw);
		this.fish.setYHeadRot(yaw);
		this.fish.setXRot(pitch);
		float yawRad = yaw * Mth.DEG_TO_RAD;
		float pitchRad = pitch * Mth.DEG_TO_RAD;
		double cosP = usePitch ? Mth.cos(pitchRad) : 1.0;
		double dy = usePitch ? -Mth.sin(pitchRad) * speed : 0.0;
		this.fish.setDeltaMovement(-Mth.sin(yawRad) * cosP * speed, dy, Mth.cos(yawRad) * cosP * speed);
	}

	/** Sideways first, then climb or dive, finally a U-turn. */
	private void avoid(float yaw) {
		this.retarget = 60;
		for (int i = 1; i <= 8; i++) {
			for (int sign = 1; sign >= -1; sign -= 2) {
				float cand = yaw + sign * i * 22.5f;
				if (this.clear(cand, 0.0f, HALF_HEIGHT)) {
					this.wantYaw = cand;
					this.wantPitch = 0.0f;
					return;
				}
			}
		}
		if (this.clear(yaw, -15.0f, HALF_HEIGHT)) {
			this.wantPitch = -15.0f;
		} else if (this.clear(yaw, 15.0f, HALF_HEIGHT)) {
			this.wantPitch = 15.0f;
		} else {
			this.wantYaw = yaw + 180.0f;
			this.wantPitch = 0.0f;
		}
	}

	/** True when the volume ahead along the heading is all water. */
	private boolean clear(float yaw, float pitch, float halfHeight) {
		float yawRad = yaw * Mth.DEG_TO_RAD;
		float pitchRad = pitch * Mth.DEG_TO_RAD;
		double cosP = Mth.cos(pitchRad);
		double fx = -Mth.sin(yawRad) * cosP;
		double fy = -Mth.sin(pitchRad);
		double fz = Mth.cos(yawRad) * cosP;
		double rx = Mth.cos(yawRad);
		double rz = Mth.sin(yawRad);
		Vec3 origin = this.fish.position().add(0.0, CENTRE, 0.0);
		for (int d = 2; d <= LOOK_AHEAD; d += 2) {
			double x = origin.x + fx * d;
			double y = origin.y + fy * d;
			double z = origin.z + fz * d;
			if (!this.water(x, y, z)
					|| !this.water(x + rx * HALF_WIDTH, y, z + rz * HALF_WIDTH)
					|| !this.water(x - rx * HALF_WIDTH, y, z - rz * HALF_WIDTH)
					|| (halfHeight > 0.0f && (!this.water(x, y + halfHeight, z) || !this.water(x, y - halfHeight, z)))) {
				return false;
			}
		}
		return true;
	}

	private boolean water(double x, double y, double z) {
		return this.fish.level().getFluidState(BlockPos.containing(x, y, z)).is(FluidTags.WATER);
	}
}
