package net.scubadive.creature.cephalopod;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.phys.Vec3;

/**
 * Shared base for the squid and the cuttlefish: a free-swimming water mob steered directly in {@link #tick()}
 * (like the clione and the octopus), with a tail-first escape jet.
 */
public abstract class Cephalopod extends Mob {
	private static final EntityDataAccessor<Boolean> JETTING = SynchedEntityData.defineId(Cephalopod.class, EntityDataSerializers.BOOLEAN);

	/** Heading in degrees (Minecraft yaw: 0 = +Z). */
	protected float heading;
	protected int jetTicks;
	private int jetLength = 1;
	private double jetSpeed;
	private Vec3 jetDir = Vec3.ZERO;
	protected int startleCooldown;

	protected Cephalopod(EntityType<? extends Cephalopod> type, Level level) {
		super(type, level);
		this.setNoGravity(true);
		this.heading = level.getRandom().nextFloat() * 360.0f;
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(JETTING, false);
	}

	public boolean isJetting() {
		return this.entityData.get(JETTING);
	}

	/** Water creatures: the default Mob check refuses positions inside liquid, which would block natural spawns. */
	@Override
	public boolean checkSpawnObstruction(LevelReader level) {
		return level.isUnobstructed(this);
	}

	@Override
	public boolean canBreatheUnderwater() {
		return true;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return SoundEvents.SQUID_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.SQUID_DEATH;
	}

	@Override
	public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
		boolean hurt = super.hurtServer(level, source, amount);
		if (hurt && this.isAlive() && this.isInWater()) {
			Entity attacker = source.getEntity();
			Vec3 from = attacker != null ? attacker.position() : this.position().add(this.getRandom().nextDouble() - 0.5, 0.0, this.getRandom().nextDouble() - 0.5);
			onAttacked(level, from);
		}
		return hurt;
	}

	/** Reaction to being hit (by default: jet away with a puff of ink). */
	protected void onAttacked(ServerLevel level, Vec3 from) {
		startJet(level, awayFrom(from), 14, 0.32, 16);
	}

	@Override
	public void tick() {
		super.tick();
		if (!(this.level() instanceof ServerLevel level)) {
			return;
		}
		if (!this.isInWater()) {
			this.setNoGravity(false);
			this.jetTicks = 0;
			this.entityData.set(JETTING, false);
			return;
		}
		this.setNoGravity(true);
		if (this.startleCooldown > 0) {
			this.startleCooldown--;
		}
		if (this.jetTicks > 0) {
			jet();
			return;
		}
		this.entityData.set(JETTING, false);
		swim(level);
	}

	/** Normal behaviour for one server tick in water (not jetting). */
	protected abstract void swim(ServerLevel level);

	// ---- escape jet ---------------------------------------------------------------------------------

	/** Horizontal-ish unit vector pointing from {@code threat} to this animal. */
	protected Vec3 awayFrom(Vec3 threat) {
		Vec3 d = this.position().subtract(threat);
		Vec3 v = new Vec3(d.x, d.y * 0.3, d.z);
		return v.lengthSqr() < 1.0E-6 ? new Vec3(1.0, 0.0, 0.0) : v.normalize();
	}

	/**
	 * Shoots off along {@code dir}, mantle first: the body keeps facing back towards the threat.
	 *
	 * @param ink number of ink particles (0 for none)
	 */
	protected void startJet(ServerLevel level, Vec3 dir, int ticks, double speed, int ink) {
		this.jetDir = dir;
		this.jetTicks = ticks;
		this.jetLength = Math.max(1, ticks);
		this.jetSpeed = speed;
		this.startleCooldown = 80 + this.getRandom().nextInt(40);
		this.entityData.set(JETTING, true);
		face(yawOf(dir.scale(-1.0)));
		this.heading = yawOf(dir);
		if (ink > 0) {
			level.sendParticles(ParticleTypes.SQUID_INK, this.getX(), this.getY() + 0.1, this.getZ(), ink, 0.15, 0.1, 0.15, 0.04);
		}
		this.playSound(SoundEvents.SQUID_SQUIRT, 0.6f, 1.2f + this.getRandom().nextFloat() * 0.2f);
	}

	private void jet() {
		double speed = 0.04 + this.jetSpeed * this.jetTicks / this.jetLength;
		Vec3 motion = this.jetDir.scale(speed);
		double dy = motion.y;
		if (!isWaterAt(this.getX() + motion.x * 3.0, this.getY() + 0.1 + dy * 3.0, this.getZ() + motion.z * 3.0)) {
			dy = Math.min(dy, 0.0);
		}
		this.setDeltaMovement(motion.x, dy, motion.z);
		this.jetTicks--;
	}

	// ---- helpers ------------------------------------------------------------------------------------

	protected boolean isWaterAt(double x, double y, double z) {
		return this.level().getFluidState(BlockPos.containing(x, y, z)).is(FluidTags.WATER);
	}

	/** Water cells above this mob before the surface (or anything that is not water), up to max. */
	protected int depthBelowSurface(int max) {
		BlockPos.MutableBlockPos p = this.blockPosition().mutable();
		for (int i = 1; i <= max; i++) {
			p.move(0, 1, 0);
			if (!this.level().getFluidState(p).is(FluidTags.WATER)) {
				return i - 1;
			}
		}
		return max;
	}

	/** Water cells below this mob before the floor, up to max. */
	protected int heightAboveFloor(int max) {
		BlockPos.MutableBlockPos p = this.blockPosition().mutable();
		for (int i = 1; i <= max; i++) {
			p.move(0, -1, 0);
			if (!this.level().getFluidState(p).is(FluidTags.WATER)) {
				return i - 1;
			}
		}
		return max;
	}

	/** Unit horizontal vector of a yaw. */
	protected static Vec3 dirOf(float yaw) {
		double rad = Math.toRadians(yaw);
		return new Vec3(-Math.sin(rad), 0.0, Math.cos(rad));
	}

	protected static float yawOf(Vec3 v) {
		return Mth.wrapDegrees((float) Math.toDegrees(Math.atan2(-v.x, v.z)));
	}

	/** Turns the body towards a yaw, at most maxStep degrees per tick. */
	protected void turnTowards(float yaw, float maxStep) {
		face(Mth.approachDegrees(this.getYRot(), yaw, maxStep));
	}

	protected void face(float yaw) {
		this.setYRot(yaw);
		this.setYBodyRot(yaw);
		this.setYHeadRot(yaw);
	}
}
