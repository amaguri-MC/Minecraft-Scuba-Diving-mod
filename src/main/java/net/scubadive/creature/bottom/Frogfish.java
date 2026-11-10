package net.scubadive.creature.bottom;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** Camouflaged frogfish that sits motionless on rock or coral. Colour variant is synced (0 grey, 1 yellow, 2 red). */
public class Frogfish extends Mob {

	/** Water creatures: the default Mob check refuses positions inside liquid, which would block natural spawns. */
	@Override
	public boolean checkSpawnObstruction(net.minecraft.world.level.LevelReader level) {
		return level.isUnobstructed(this);
	}
	public static final int VARIANT_COUNT = 3;
	private static final EntityDataAccessor<Integer> VARIANT = SynchedEntityData.defineId(Frogfish.class, EntityDataSerializers.INT);

	private boolean oriented;

	public Frogfish(EntityType<? extends Frogfish> type, Level level) {
		super(type, level);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(VARIANT, -1);
	}

	/** Returns the colour variant, 0 until the server has assigned one. */
	public int getVariant() {
		return Math.max(0, this.entityData.get(VARIANT));
	}

	public void setVariant(int variant) {
		this.entityData.set(VARIANT, Math.floorMod(variant, VARIANT_COUNT));
	}

	@Override
	public boolean canBreatheUnderwater() {
		return true;
	}

	@Override
	public void tick() {
		super.tick();

		if (this.level().isClientSide()) {
			return;
		}

		if (this.entityData.get(VARIANT) < 0) {
			setVariant(this.getUUID().hashCode());
		}
		if (!this.oriented) {
			this.oriented = true;
			float yaw = this.getRandom().nextFloat() * 360.0f;
			this.setYRot(yaw);
			this.setYBodyRot(yaw);
			this.setYHeadRot(yaw);
		}

		// It never swims; it just sinks onto the bottom and stays put.
		Vec3 motion = this.getDeltaMovement();
		this.setDeltaMovement(0.0, motion.y, 0.0);
	}
}
