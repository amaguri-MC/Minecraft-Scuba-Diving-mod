package net.scubadive.creature.bottom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.scubadive.creature.FleeHelper;
import net.scubadive.reef.ScubaReefs;

/**
 * Common octopus. Crawls slowly over the sea floor, sometimes tucks into a rock crevice to rest, and takes the
 * colour of the block underfoot (synced camo index). When a diver gets close or it is hurt it shows its true
 * red-brown, squirts ink (briefly blinding nearby players) and jets backwards away from the threat.
 */
public class Octopus extends Mob {

	/** Water creatures: the default Mob check refuses positions inside liquid, which would block natural spawns. */
	@Override
	public boolean checkSpawnObstruction(net.minecraft.world.level.LevelReader level) {
		return level.isUnobstructed(this);
	}
	/** Camo indices: 0 natural red-brown, 1 sand, 2 stone, 3 coral. */
	public static final int CAMO_COUNT = 4;
	private static final EntityDataAccessor<Integer> CAMO = SynchedEntityData.defineId(Octopus.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Boolean> JETTING = SynchedEntityData.defineId(Octopus.class, EntityDataSerializers.BOOLEAN);

	private static final double ALERT_RADIUS = 2.5;
	private static final double SNEAK_ALERT_RADIUS = 1.5;
	private static final double CAMO_BREAK_RADIUS = 5.0;
	private static final int JET_TICKS = 14;
	private static final double CRAWL_SPEED = 0.03;

	private enum Mode { WANDER, GOTO_CREVICE, REST }

	private Mode mode = Mode.WANDER;
	private int jetTicks;
	private Vec3 jetDir = Vec3.ZERO;
	private int startleCooldown;
	private int camoTimer;
	private int walkTicks;
	private int idleTicks = 20;
	private float heading;
	private int restCooldown = 600;
	private int modeTicks;
	private Vec3 creviceSpot;
	private float creviceYaw;

	public Octopus(EntityType<? extends Octopus> type, Level level) {
		super(type, level);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(CAMO, 0);
		builder.define(JETTING, false);
	}

	public int getCamo() {
		return Mth.clamp(this.entityData.get(CAMO), 0, CAMO_COUNT - 1);
	}

	public boolean isJetting() {
		return this.entityData.get(JETTING);
	}

	@Override
	public boolean canBreatheUnderwater() {
		return true;
	}

	@Override
	public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
		boolean hurt = super.hurtServer(level, source, amount);
		if (hurt && this.isAlive()) {
			Entity attacker = source.getEntity();
			startle(level, attacker != null ? attacker.position() : this.position().add(this.getRandom().nextDouble() - 0.5, 0.0, this.getRandom().nextDouble() - 0.5));
		}
		return hurt;
	}

	@Override
	public void tick() {
		super.tick();

		if (!(this.level() instanceof ServerLevel serverLevel)) {
			return;
		}
		if (this.startleCooldown > 0) {
			this.startleCooldown--;
		}

		updateCamo();

		if (this.jetTicks > 0) {
			jet();
			return;
		}
		this.entityData.set(JETTING, false);

		if (this.startleCooldown <= 0) {
			Player p = FleeHelper.tooClose(this, ALERT_RADIUS);
			if (p != null && (!p.isShiftKeyDown() || p.distanceToSqr(this) < SNEAK_ALERT_RADIUS * SNEAK_ALERT_RADIUS)) {
				startle(serverLevel, p.position());
				return;
			}
		}

		switch (this.mode) {
			case REST -> rest();
			case GOTO_CREVICE -> gotoCrevice();
			default -> wander();
		}
	}

	// ---- startle and jet -------------------------------------------------

	private void startle(ServerLevel level, Vec3 threat) {
		if (!level.getFluidState(this.blockPosition()).is(FluidTags.WATER)) {
			return;
		}
		Vec3 d = this.position().subtract(threat);
		Vec3 away = new Vec3(d.x, d.y * 0.3, d.z);
		this.jetDir = away.lengthSqr() < 1.0E-6 ? new Vec3(1.0, 0.0, 0.0) : away.normalize();
		this.jetTicks = JET_TICKS;
		this.startleCooldown = 100;
		this.mode = Mode.WANDER;
		this.creviceSpot = null;
		this.walkTicks = 0;
		this.idleTicks = 40;
		this.restCooldown = 300 + this.getRandom().nextInt(400);
		this.entityData.set(JETTING, true);
		this.entityData.set(CAMO, 0);
		this.camoTimer = 40;

		// Ink cloud, and a short blindness for anyone right in it.
		level.sendParticles(ParticleTypes.SQUID_INK, this.getX(), this.getY() + 0.2, this.getZ(), 40, 0.25, 0.2, 0.25, 0.06);
		this.playSound(SoundEvents.SQUID_SQUIRT, 1.0f, 1.0f);
		for (Player p : level.players()) {
			if (!p.isSpectator() && p.distanceToSqr(this) <= 2.5 * 2.5) {
				p.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 20 + this.getRandom().nextInt(21), 0, false, false));
			}
		}
		// It faces the threat and shoots away tail first.
		face(Mth.wrapDegrees((float) Math.toDegrees(Math.atan2(d.x, -d.z))));
	}

	private void jet() {
		if (this.jetTicks % 4 == 0 && this.level() instanceof ServerLevel level) {
			level.sendParticles(ParticleTypes.SQUID_INK, this.getX(), this.getY() + 0.2, this.getZ(), 6, 0.15, 0.1, 0.15, 0.02);
		}
		double speed = 0.08 + 0.3 * this.jetTicks / JET_TICKS;
		Vec3 motion = this.jetDir.scale(speed);
		double dy = motion.y + 0.03;
		BlockPos ahead = BlockPos.containing(this.getX() + motion.x * 3.0, this.getY() + 0.2 + dy * 3.0, this.getZ() + motion.z * 3.0);
		if (!this.level().getFluidState(ahead).is(FluidTags.WATER)) {
			dy = Math.min(dy, 0.0);
		}
		this.setDeltaMovement(motion.x, dy, motion.z);
		this.jetTicks--;
	}

	// ---- idle behaviour --------------------------------------------------

	private void wander() {
		this.restCooldown--;
		if (this.restCooldown <= 0) {
			MorayEel.Crevice c = MorayEel.findCrevice(this.level(), this.blockPosition(), 8);
			if (c != null) {
				int[][] dirs = {{0, 1}, {-1, 0}, {0, -1}, {1, 0}};
				int[] d = dirs[c.dir()];
				this.creviceSpot = new Vec3(c.pos().getX() + 0.5, c.pos().getY(), c.pos().getZ() + 0.5);
				this.creviceYaw = (float) Math.toDegrees(Math.atan2(-d[0], d[1]));
				this.mode = Mode.GOTO_CREVICE;
				this.modeTicks = 0;
				return;
			}
			this.restCooldown = 400;
		}

		if (this.walkTicks > 0) {
			this.walkTicks--;
			crawl(this.heading);
		} else if (this.idleTicks > 0) {
			this.idleTicks--;
			halt();
		} else if (this.getRandom().nextInt(3) != 0) {
			this.heading = this.getRandom().nextFloat() * 360.0f;
			this.walkTicks = 40 + this.getRandom().nextInt(80);
		} else {
			this.idleTicks = 30 + this.getRandom().nextInt(80);
		}
	}

	private void gotoCrevice() {
		if (this.creviceSpot == null || ++this.modeTicks > 240) {
			this.mode = Mode.WANDER;
			this.restCooldown = 600;
			return;
		}
		double dx = this.creviceSpot.x - this.getX();
		double dz = this.creviceSpot.z - this.getZ();
		if (dx * dx + dz * dz < 0.04) {
			this.mode = Mode.REST;
			this.modeTicks = 300 + this.getRandom().nextInt(400);
			face(this.creviceYaw);
			return;
		}
		crawl((float) Math.toDegrees(Math.atan2(-dx, dz)));
		if (this.creviceSpot.y > this.getY() + 0.5) {
			Vec3 m = this.getDeltaMovement();
			this.setDeltaMovement(m.x, Math.max(m.y, 0.1), m.z);
		}
	}

	private void rest() {
		halt();
		face(this.creviceYaw);
		if (--this.modeTicks <= 0) {
			this.mode = Mode.WANDER;
			this.creviceSpot = null;
			this.restCooldown = 900 + this.getRandom().nextInt(900);
		}
	}

	/** Slow crawl towards a heading in degrees, climbing small ledges and turning back at open water edges. */
	private void crawl(float yaw) {
		face(Mth.rotLerp(0.2f, this.getYRot(), yaw));
		double rad = Math.toRadians(yaw);
		double dx = -Math.sin(rad) * CRAWL_SPEED;
		double dz = Math.cos(rad) * CRAWL_SPEED;
		BlockPos ahead = BlockPos.containing(this.getX() + dx * 12.0, this.getY() + 0.1, this.getZ() + dz * 12.0);
		if (!this.level().getFluidState(ahead).is(FluidTags.WATER) && this.mode == Mode.WANDER) {
			this.walkTicks = 0;
			halt();
			return;
		}
		double dy = this.getDeltaMovement().y;
		if (this.horizontalCollision) {
			dy = Math.max(dy, 0.12); // haul itself up onto the rock
		}
		this.setDeltaMovement(dx, dy, dz);
	}

	private void halt() {
		Vec3 m = this.getDeltaMovement();
		this.setDeltaMovement(0.0, m.y, 0.0);
	}

	private void face(float yaw) {
		this.setYRot(yaw);
		this.setYBodyRot(yaw);
		this.setYHeadRot(yaw);
	}

	// ---- camouflage ------------------------------------------------------

	private void updateCamo() {
		if (--this.camoTimer > 0) {
			return;
		}
		this.camoTimer = 10;
		int camo = 0;
		if (this.jetTicks <= 0 && FleeHelper.tooClose(this, CAMO_BREAK_RADIUS) == null) {
			camo = camoFor(this.level().getBlockState(BlockPos.containing(this.getX(), this.getBoundingBox().minY - 0.2, this.getZ())),
					this.level().getBlockState(this.blockPosition()));
		}
		if (camo != this.entityData.get(CAMO)) {
			this.entityData.set(CAMO, camo);
		}
	}

	private static int camoFor(BlockState floor, BlockState feet) {
		if (isCoral(floor) || isCoral(feet)) {
			return 3;
		}
		if (floor.is(BlockTags.SAND)) {
			return 1;
		}
		if (isRock(floor)) {
			return 2;
		}
		return 0;
	}

	private static boolean isRock(BlockState s) {
		return s.is(BlockTags.BASE_STONE_OVERWORLD) || s.is(Blocks.COBBLESTONE) || s.is(Blocks.MOSSY_COBBLESTONE) || s.is(Blocks.GRAVEL);
	}

	private static boolean isCoral(BlockState s) {
		return s.is(BlockTags.CORAL_BLOCKS) || s.is(BlockTags.CORALS) || s.is(BlockTags.WALL_CORALS)
				|| s.is(ScubaReefs.TABLE_CORAL) || s.is(ScubaReefs.BRAIN_CORAL)
				|| s.is(ScubaReefs.BRANCH_CORAL) || s.is(ScubaReefs.FAN_CORAL);
	}

	// ---- spawning ---------------------------------------------------------

	/** Rocky or coral sea floor: rock/coral/gravel underfoot, or coral or rock right beside. */
	public static boolean canSpawnAt(LevelReader level, BlockPos pos) {
		if (!level.getFluidState(pos).is(FluidTags.WATER) || !level.getFluidState(pos.above()).is(FluidTags.WATER)) {
			return false;
		}
		BlockPos below = pos.below();
		BlockState floor = level.getBlockState(below);
		if (!floor.isFaceSturdy(level, below, Direction.UP)) {
			return false;
		}
		if (isRock(floor) || isCoral(floor)) {
			return true;
		}
		for (Direction d : Direction.Plane.HORIZONTAL) {
			BlockState side = level.getBlockState(pos.relative(d));
			if (isCoral(side) || isRock(side)) {
				return true;
			}
		}
		return false;
	}
}
