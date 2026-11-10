package net.scubadive.creature;

import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** Tiny slow sea slug that crawls along the sea floor. Colour variant is synced entity data (0 blue, 1 orange, 2 black/white). */
public class Nudibranch extends Mob {
	public static final int VARIANT_COUNT = 3;
	private static final EntityDataAccessor<Integer> VARIANT = SynchedEntityData.defineId(Nudibranch.class, EntityDataSerializers.INT);

	private int moveTimer;
	private boolean crawling;
	private float heading;

	public Nudibranch(EntityType<? extends Nudibranch> type, Level level) {
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
			// The UUID is persisted with the entity, so the variant is stable across reloads.
			setVariant(this.getUUID().hashCode());
		}

		if (!this.isInWater()) {
			return;
		}

		if (--this.moveTimer <= 0) {
			this.moveTimer = 60 + this.getRandom().nextInt(140);
			this.crawling = this.getRandom().nextInt(2) == 0;
			this.heading = this.getRandom().nextFloat() * 360.0f;
		}

		if (this.crawling) {
			double rad = Math.toRadians(this.heading);
			double dx = -Math.sin(rad);
			double dz = Math.cos(rad);
			BlockPos ahead = BlockPos.containing(this.getX() + dx * 0.6, this.getY() + 0.1, this.getZ() + dz * 0.6);
			if (this.level().getFluidState(ahead).is(FluidTags.WATER)) {
				double speed = 0.004;
				Vec3 motion = this.getDeltaMovement();
				this.setDeltaMovement(dx * speed, motion.y, dz * speed);
				this.setYRot(this.heading);
				this.setYHeadRot(this.heading);
			} else {
				this.moveTimer = 0;
			}
		}
	}
}
