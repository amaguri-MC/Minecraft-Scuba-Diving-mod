package net.scubadive.creature.bottom;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import net.scubadive.creature.ScubaCreatures;

/** Base for creatures that live on a sea anemone: remembers one anemone as home and steers around it. */
public abstract class AnemoneDweller extends Mob {

	/** Water creatures: the default Mob check refuses positions inside liquid, which would block natural spawns. */
	@Override
	public boolean checkSpawnObstruction(net.minecraft.world.level.LevelReader level) {
		return level.isUnobstructed(this);
	}
	/** Radius in blocks within which an anemone is adopted as home. */
	public static final int HOME_RANGE = 8;

	/** Home searches without result before a wild dweller gives up and vanishes (about 20-40 s). */
	private static final int HOMELESS_LIMIT = 20;

	protected BlockPos home;
	protected Vec3 target;
	protected int retarget;
	private int searchTimer;
	/** Placed on purpose (spawn egg, bucket, command): never removed for lack of an anemone. */
	private boolean deliberate;
	/** The one-time "is there an anemone near where I appeared" check has run. */
	private boolean anemoneChecked;
	private int homelessSearches;

	protected AnemoneDweller(EntityType<? extends AnemoneDweller> type, Level level) {
		super(type, level);
	}

	@Override
	public boolean canBreatheUnderwater() {
		return true;
	}

	@Override
	public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason,
			SpawnGroupData data) {
		// Only wild spawns must live on an anemone; anything a player placed stays where it was put.
		this.deliberate = reason != EntitySpawnReason.NATURAL && reason != EntitySpawnReason.CHUNK_GENERATION
				&& reason != EntitySpawnReason.STRUCTURE;
		this.anemoneChecked = false;
		return super.finalizeSpawn(level, difficulty, reason, data);
	}

	@Override
	protected void addAdditionalSaveData(ValueOutput output) {
		super.addAdditionalSaveData(output);
		output.putBoolean("Deliberate", this.deliberate);
		output.putBoolean("AnemoneChecked", this.anemoneChecked);
	}

	@Override
	protected void readAdditionalSaveData(ValueInput input) {
		super.readAdditionalSaveData(input);
		// Older saves lack the flags: treat them as wild and check once, which clears strays left by earlier versions.
		this.deliberate = input.getBooleanOr("Deliberate", true);
		this.anemoneChecked = input.getBooleanOr("AnemoneChecked", false);
	}

	protected boolean isAnemone(BlockPos pos) {
		return this.level().getBlockState(pos).is(ScubaCreatures.SEA_ANEMONE);
	}

	/**
	 * Drops a destroyed home and, now and then, looks for a new one. A wild dweller that appears with no
	 * anemone within {@link #HOME_RANGE}, or stays homeless for long, is removed.
	 */
	protected void updateHome() {
		if (!this.anemoneChecked) {
			this.anemoneChecked = true;
			this.home = findNearestAnemone();
			if (this.home == null && !this.deliberate) {
				this.discard();
				return;
			}
		}
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
		if (this.home != null) {
			this.homelessSearches = 0;
		} else if (!this.deliberate && ++this.homelessSearches >= HOMELESS_LIMIT) {
			this.discard();
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
