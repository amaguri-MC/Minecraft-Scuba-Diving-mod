package net.scubadive.creature.bottom;

import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.phys.Vec3;

/**
 * Moray eel that sits in a rock crevice with its head out, gaping. Neutral: it never moves, but
 * bites back at whoever hurt it while they are in reach.
 */
public class MorayEel extends Mob {

	/** Water creatures: the default Mob check refuses positions inside liquid, which would block natural spawns. */
	@Override
	public boolean checkSpawnObstruction(net.minecraft.world.level.LevelReader level) {
		return level.isUnobstructed(this);
	}
	private static final EntityDataAccessor<Boolean> ANGRY = SynchedEntityData.defineId(MorayEel.class, EntityDataSerializers.BOOLEAN);
	/** Horizontal directions as {dx, dz}: south, west, north, east. */
	private static final int[][] DIRS = {{0, 1}, {-1, 0}, {0, -1}, {1, 0}};
	private static final double BITE_REACH_SQR = 1.7 * 1.7;
	private static final float BITE_DAMAGE = 4.0f;

	private Vec3 anchor;
	private float restYaw;
	private LivingEntity grudge;
	private int angerTicks;
	private int biteCooldown;

	public MorayEel(EntityType<? extends MorayEel> type, Level level) {
		super(type, level);
		this.setNoGravity(true);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(ANGRY, false);
	}

	public boolean isAngry() {
		return this.entityData.get(ANGRY);
	}

	@Override
	public boolean canBreatheUnderwater() {
		return true;
	}

	@Override
	public boolean isPushable() {
		return false;
	}

	@Override
	public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
		boolean hurt = super.hurtServer(level, source, amount);
		if (hurt && source.getEntity() instanceof LivingEntity attacker && attacker != this) {
			this.grudge = attacker;
			this.angerTicks = 400;
		}
		return hurt;
	}

	@Override
	public void tick() {
		super.tick();

		if (!(this.level() instanceof ServerLevel serverLevel)) {
			return;
		}

		if (this.anchor == null) {
			settle();
		}
		// Hold the spot: no drifting, no knockback.
		this.setNoGravity(true);
		this.setDeltaMovement(Vec3.ZERO);
		if (this.position().distanceToSqr(this.anchor) > 1.0E-4) {
			this.setPos(this.anchor.x, this.anchor.y, this.anchor.z);
		}

		if (this.biteCooldown > 0) {
			this.biteCooldown--;
		}
		if (this.angerTicks > 0) {
			this.angerTicks--;
		}
		if (this.grudge != null && (!this.grudge.isAlive() || this.grudge.isRemoved() || this.angerTicks <= 0)) {
			this.grudge = null;
			this.angerTicks = 0;
		}
		this.entityData.set(ANGRY, this.grudge != null);

		float yaw = this.restYaw;
		if (this.grudge != null) {
			double dx = this.grudge.getX() - this.getX();
			double dz = this.grudge.getZ() - this.getZ();
			float toTarget = (float) Math.toDegrees(Math.atan2(-dx, dz));
			yaw = this.restYaw + Mth.clamp(Mth.wrapDegrees(toTarget - this.restYaw), -55.0f, 55.0f);

			double rad = Math.toRadians(this.restYaw);
			Vec3 head = this.position().add(-Math.sin(rad) * 0.5, 0.2, Math.cos(rad) * 0.5);
			if (this.biteCooldown <= 0 && this.grudge.getBoundingBox().distanceToSqr(head) <= BITE_REACH_SQR) {
				this.biteCooldown = 24;
				this.playSound(SoundEvents.TROPICAL_FISH_HURT, 1.0f, 0.6f);
				this.grudge.hurtServer(serverLevel, this.damageSources().mobAttack(this), BITE_DAMAGE);
			}
		}
		this.setYRot(yaw);
		this.setYBodyRot(yaw);
		this.setYHeadRot(yaw);
	}

	/** Picks the nearest crevice as the resting spot; stays where it is if there is none. */
	private void settle() {
		Crevice c = findCrevice(this.level(), this.blockPosition(), 4);
		if (c != null) {
			int[] d = DIRS[c.dir];
			this.anchor = new Vec3(c.pos.getX() + 0.5 + d[0] * 0.3, c.pos.getY() + 0.15, c.pos.getZ() + 0.5 + d[1] * 0.3);
			this.restYaw = (float) Math.toDegrees(Math.atan2(-d[0], d[1]));
		} else {
			this.anchor = this.position();
			this.restYaw = this.getYRot();
		}
		this.setPos(this.anchor.x, this.anchor.y, this.anchor.z);
	}

	/** A water cell enclosed by rock on most sides; {@code dir} indexes the open side the head points out of. */
	public record Crevice(BlockPos pos, int dir) {
	}

	private static boolean solid(LevelReader level, BlockPos p) {
		return level.getBlockState(p).isCollisionShapeFullBlock(level, p);
	}

	private static boolean open(LevelReader level, BlockPos p) {
		return level.getFluidState(p).is(FluidTags.WATER) && !solid(level, p);
	}

	/** Returns the crevice at exactly this cell, or null. */
	public static Crevice creviceAt(LevelReader level, BlockPos p) {
		if (!open(level, p)) {
			return null;
		}
		int vertical = (solid(level, p.above()) ? 1 : 0) + (solid(level, p.below()) ? 1 : 0);
		int bestDir = -1;
		int bestCount = 0;
		for (int i = 0; i < 4; i++) {
			int dx = DIRS[i][0];
			int dz = DIRS[i][1];
			if (!open(level, p.offset(dx, 0, dz)) || !solid(level, p.offset(-dx, 0, -dz))) {
				continue;
			}
			int sides = (solid(level, p.offset(-dz, 0, dx)) ? 1 : 0) + (solid(level, p.offset(dz, 0, -dx)) ? 1 : 0);
			int count = 1 + vertical + sides;
			if (count > bestCount) {
				bestCount = count;
				bestDir = i;
			}
		}
		return bestCount >= 4 ? new Crevice(p.immutable(), bestDir) : null;
	}

	/** Nearest crevice within the radius, or null. */
	public static Crevice findCrevice(LevelReader level, BlockPos origin, int radius) {
		Crevice best = null;
		double bestDist = Double.MAX_VALUE;
		for (BlockPos p : BlockPos.betweenClosed(origin.offset(-radius, -2, -radius), origin.offset(radius, 2, radius))) {
			double d = p.distSqr(origin);
			if (d >= bestDist) {
				continue;
			}
			Crevice c = creviceAt(level, p);
			if (c != null) {
				best = c;
				bestDist = d;
			}
		}
		return best;
	}
}
