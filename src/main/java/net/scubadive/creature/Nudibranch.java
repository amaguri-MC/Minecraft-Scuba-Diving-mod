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

/**
 * Tiny slow sea slug that crawls along reef rock and coral. Every nudibranch species is its own entity type
 * built on this class; subclasses change where it lives ({@link #isHome}), how it settles after spawning
 * ({@link #settle}) and how it moves ({@link #moveTick}). The colour variant is synced entity data; every
 * species currently has a single look (the original nudibranch is the white sea slug).
 */
public class Nudibranch extends Mob {

	/** Water creatures: the default Mob check refuses positions inside liquid, which would block natural spawns. */
	@Override
	public boolean checkSpawnObstruction(net.minecraft.world.level.LevelReader level) {
		return level.isUnobstructed(this);
	}
	private static final EntityDataAccessor<Integer> VARIANT = SynchedEntityData.defineId(Nudibranch.class, EntityDataSerializers.INT);

	private final int variantCount;
	private int moveTimer;
	private boolean settled;
	private boolean deliberate;
	private boolean crawling;
	protected float heading;

	/** A species with a single look. */
	public Nudibranch(EntityType<? extends Nudibranch> type, Level level) {
		this(type, level, 1);
	}

	public Nudibranch(EntityType<? extends Nudibranch> type, Level level, int variantCount) {
		super(type, level);
		this.variantCount = Math.max(1, variantCount);
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
		this.entityData.set(VARIANT, Math.floorMod(variant, this.variantCount));
	}

	/** True when a player put it here on purpose (spawn egg, command...): it stays where it was placed. */
	protected boolean isDeliberate() {
		return this.deliberate;
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

	@Override
	protected void addAdditionalSaveData(net.minecraft.world.level.storage.ValueOutput output) {
		super.addAdditionalSaveData(output);
		output.putBoolean("Settled", this.settled);
		output.putBoolean("Deliberate", this.deliberate);
	}

	@Override
	protected void readAdditionalSaveData(net.minecraft.world.level.storage.ValueInput input) {
		super.readAdditionalSaveData(input);
		this.settled = input.getBooleanOr("Settled", true);
		this.deliberate = input.getBooleanOr("Deliberate", true);
	}

	/** Sea slugs live on rock reefs and coral, never on bare sand or mud. */
	public static boolean isReefFloor(BlockState state) {
		return state.is(BlockTags.BASE_STONE_OVERWORLD) || state.is(BlockTags.CORAL_BLOCKS)
				|| state.is(Blocks.COBBLESTONE) || state.is(Blocks.MOSSY_COBBLESTONE) || state.is(Blocks.BASALT)
				|| state.is(Blocks.SMOOTH_BASALT) || state.is(Blocks.PRISMARINE) || state.is(Blocks.MOSSY_STONE_BRICKS)
				|| state.is(net.scubadive.reef.ScubaReefs.BRAIN_CORAL);
	}

	/** Whether this species may sit in the water cell at pos (default: on reef rock or coral). */
	protected boolean isHome(BlockPos pos) {
		return isReefFloor(this.level().getBlockState(pos.below()));
	}

	/** Called once after a natural spawn: move to a proper home or leave the world. */
	protected void settle() {
		BlockPos here = this.blockPosition();
		if (isHome(here)) {
			return;
		}
		BlockPos best = null;
		double bestDist = Double.MAX_VALUE;
		for (BlockPos p : BlockPos.betweenClosed(here.offset(-6, -3, -6), here.offset(6, 3, 6))) {
			if (this.level().getFluidState(p).is(FluidTags.WATER) && isHome(p)) {
				double d = p.distSqr(here);
				if (d < bestDist) {
					bestDist = d;
					best = p.immutable();
				}
			}
		}
		if (best != null) {
			this.setPos(best.getX() + 0.5, best.getY(), best.getZ() + 0.5);
		} else {
			// No reef anywhere near (open sand, ice floe, cave floor): a sea slug would not live here.
			this.discard();
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
				settle();
				if (this.isRemoved()) {
					return;
				}
			}
		}

		if (this.entityData.get(VARIANT) < 0) {
			// The UUID is persisted with the entity, so the variant is stable across reloads.
			setVariant(this.getUUID().hashCode());
		}

		if (!this.isInWater()) {
			return;
		}
		moveTick();
	}

	/** Server, in water: one tick of movement. Default: slow crawl in a random direction, staying at home. */
	protected void moveTick() {
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
					&& (this.deliberate || isHome(ahead) || !isHome(this.blockPosition()))) {
				double speed = crawlSpeed();
				Vec3 motion = this.getDeltaMovement();
				this.setDeltaMovement(dx * speed, motion.y, dz * speed);
				this.setYRot(this.heading);
				this.setYHeadRot(this.heading);
			} else {
				this.moveTimer = 0;
			}
		}
	}

	/** Blocks per tick while crawling. */
	protected double crawlSpeed() {
		return 0.004;
	}
}
