package net.scubadive.creature;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
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
	private boolean settled;
	private boolean deliberate;
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
	public net.minecraft.world.entity.SpawnGroupData finalizeSpawn(net.minecraft.world.level.ServerLevelAccessor level,
			net.minecraft.world.DifficultyInstance difficulty, net.minecraft.world.entity.EntitySpawnReason reason,
			net.minecraft.world.entity.SpawnGroupData data) {
		// Placed on purpose (spawn egg, command, bucket): stay exactly where the player put it.
		this.deliberate = reason == net.minecraft.world.entity.EntitySpawnReason.SPAWN_ITEM_USE
				|| reason == net.minecraft.world.entity.EntitySpawnReason.COMMAND
				|| reason == net.minecraft.world.entity.EntitySpawnReason.BUCKET
				|| reason == net.minecraft.world.entity.EntitySpawnReason.DISPENSER;
		return super.finalizeSpawn(level, difficulty, reason, data);
	}

	/** Sea slugs live on rock reefs and coral, never on bare sand or mud. */
	public static boolean isReefFloor(BlockState state) {
		return state.is(BlockTags.BASE_STONE_OVERWORLD) || state.is(BlockTags.CORAL_BLOCKS)
				|| state.is(Blocks.COBBLESTONE) || state.is(Blocks.MOSSY_COBBLESTONE) || state.is(Blocks.BASALT)
				|| state.is(Blocks.SMOOTH_BASALT) || state.is(Blocks.PRISMARINE) || state.is(Blocks.MOSSY_STONE_BRICKS)
				|| state.is(net.scubadive.reef.ScubaReefs.BRAIN_CORAL);
	}

	/** Moves a stray slug onto the nearest reef rock or coral within a few blocks (top face, in water). */
	private void settleOnReef() {
		BlockPos here = this.blockPosition();
		if (isReefFloor(this.level().getBlockState(here.below()))) {
			return;
		}
		BlockPos best = null;
		double bestDist = Double.MAX_VALUE;
		for (BlockPos p : BlockPos.betweenClosed(here.offset(-6, -3, -6), here.offset(6, 3, 6))) {
			if (isReefFloor(this.level().getBlockState(p.below()))
					&& this.level().getFluidState(p).is(FluidTags.WATER)) {
				double d = p.distSqr(here);
				if (d < bestDist) {
					bestDist = d;
					best = p.immutable();
				}
			}
		}
		if (best != null) {
			this.setPos(best.getX() + 0.5, best.getY(), best.getZ() + 0.5);
		}
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

		if (!this.settled) {
			this.settled = true;
			if (!this.deliberate) {
				settleOnReef();
			}
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
			if (this.level().getFluidState(ahead).is(FluidTags.WATER)
					&& (this.deliberate || isReefFloor(this.level().getBlockState(ahead.below()))
							|| !isReefFloor(this.level().getBlockState(this.blockPosition().below())))) {
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
