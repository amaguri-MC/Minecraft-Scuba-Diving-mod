package net.scubadive.creature.bottom;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import net.scubadive.creature.FleeHelper;
import net.scubadive.creature.reef.ReefFish;

/**
 * Garden eel: lives in a sand burrow and never leaves it. The upper body sways out of the sand facing
 * the current; when a diver comes within a few blocks it sinks into the sand and comes back out once
 * they have moved away. Natural spawns found a colony of 5-12 eels about a block apart.
 */
public class GardenEel extends Mob {
	public static final double ALERT_RADIUS = 4.0;
	private static final EntityDataAccessor<Boolean> HIDDEN = SynchedEntityData.defineId(GardenEel.class, EntityDataSerializers.BOOLEAN);

	private Vec3 anchor;
	private float currentYaw = Float.NaN;
	private boolean colonyPending;
	private int hideTimer;
	private int reactDelay = -1;
	/** Client-side retraction 0 (out) .. 1 (in the sand), smoothed for rendering. */
	private float retract;
	private float retractO;

	public GardenEel(EntityType<? extends GardenEel> type, Level level) {
		super(type, level);
		this.setNoGravity(true);
	}

	/** Water creatures: the default Mob check refuses positions inside liquid, which would block natural spawns. */
	@Override
	public boolean checkSpawnObstruction(LevelReader level) {
		return level.isUnobstructed(this);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(HIDDEN, false);
	}

	public boolean isHidden() {
		return this.entityData.get(HIDDEN);
	}

	/** Smoothed retraction for the renderer (0 = out, 1 = fully in the sand). */
	public float getRetract(float partialTick) {
		return this.retractO + (this.retract - this.retractO) * partialTick;
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
	public boolean isPushedByFluid() {
		return false;
	}

	@Override
	public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason,
			SpawnGroupData data) {
		this.colonyPending = reason == EntitySpawnReason.NATURAL || reason == EntitySpawnReason.CHUNK_GENERATION;
		return super.finalizeSpawn(level, difficulty, reason, data);
	}

	@Override
	protected void addAdditionalSaveData(ValueOutput output) {
		super.addAdditionalSaveData(output);
		// Chunk-generated eels are saved before their first tick: keep the colony request.
		output.putBoolean("ColonyPending", this.colonyPending);
	}

	@Override
	protected void readAdditionalSaveData(ValueInput input) {
		super.readAdditionalSaveData(input);
		this.colonyPending = input.getBooleanOr("ColonyPending", false);
	}

	@Override
	public void tick() {
		super.tick();
		if (!(this.level() instanceof ServerLevel level)) {
			// Ducks in fast, creeps back out slowly.
			this.retractO = this.retract;
			float target = this.isHidden() ? 1.0f : 0.0f;
			float step = target > this.retract ? 0.3f : 0.04f;
			this.retract = target > this.retract ? Math.min(target, this.retract + step) : Math.max(target, this.retract - step);
			return;
		}
		if (this.anchor == null) {
			this.settle();
		}
		this.setPos(this.anchor.x, this.anchor.y, this.anchor.z);
		this.setDeltaMovement(Vec3.ZERO);
		this.setYRot(this.currentYaw);
		this.setYBodyRot(this.currentYaw);
		this.setYHeadRot(this.currentYaw);
		if (this.colonyPending) {
			this.colonyPending = false;
			this.spawnColony(level);
		}

		Player near = FleeHelper.tooClose(this, ALERT_RADIUS);
		if (near != null) {
			if (!this.isHidden()) {
				// Each eel reacts a moment apart, so the colony ripples down into the sand.
				if (this.reactDelay < 0) {
					this.reactDelay = this.getRandom().nextInt(6);
				} else if (this.reactDelay-- == 0) {
					this.entityData.set(HIDDEN, true);
					level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.SAND.defaultBlockState()),
							this.getX(), this.getY() + 0.05, this.getZ(), 6, 0.06, 0.02, 0.06, 0.02);
					this.playSound(SoundEvents.SAND_HIT, 0.25f, 1.6f);
				}
			}
			this.hideTimer = 40 + this.getRandom().nextInt(40);
		} else if (this.isHidden()) {
			this.reactDelay = -1;
			if (--this.hideTimer <= 0) {
				this.entityData.set(HIDDEN, false);
			}
		} else {
			this.reactDelay = -1;
		}
	}

	/** Sinks onto the floor below (spawn eggs may place it in open water) and picks the current's direction. */
	private void settle() {
		BlockPos.MutableBlockPos p = this.blockPosition().mutable();
		for (int i = 0; i < 8; i++) {
			BlockPos below = p.below();
			if (this.level().getBlockState(below).isFaceSturdy(this.level(), below, Direction.UP)) {
				break;
			}
			p.move(Direction.DOWN);
		}
		this.anchor = new Vec3(this.getX(), p.getY(), this.getZ());
		if (Float.isNaN(this.currentYaw)) {
			// Neighbouring eels share a current direction (8-block cells).
			long h = (p.getX() >> 3) * 341873128712L + (p.getZ() >> 3) * 132897987541L;
			this.currentYaw = (float) Math.floorMod(h, 360L) + (this.getRandom().nextFloat() - 0.5f) * 16.0f;
		}
	}

	/** Natural spawns grow into a colony: 4-11 more eels on the sand around, roughly a block apart. */
	private void spawnColony(ServerLevel level) {
		if (!level.getEntitiesOfClass(GardenEel.class, this.getBoundingBox().inflate(6.0), e -> e != this).isEmpty()) {
			return;
		}
		RandomSource r = this.getRandom();
		int want = 3 + r.nextInt(5);
		BlockPos origin = this.blockPosition();
		List<BlockPos> cells = new ArrayList<>();
		for (int ring = 1; ring <= 3; ring++) {
			List<BlockPos> ringCells = new ArrayList<>();
			for (int dx = -ring; dx <= ring; dx++) {
				for (int dz = -ring; dz <= ring; dz++) {
					if (Math.max(Math.abs(dx), Math.abs(dz)) == ring) {
						ringCells.add(origin.offset(dx, 0, dz));
					}
				}
			}
			Collections.shuffle(ringCells, new java.util.Random(r.nextLong()));
			cells.addAll(ringCells);
		}
		int made = 0;
		for (BlockPos cell : cells) {
			if (made >= want) {
				break;
			}
			if (r.nextInt(4) == 0) {
				continue; // leave gaps so the colony looks natural
			}
			BlockPos floor = sandFloor(level, cell);
			if (floor == null) {
				continue;
			}
			GardenEel eel = ReefFish.GARDEN_EEL.create(level, EntitySpawnReason.NATURAL);
			if (eel == null) {
				continue;
			}
			double x = floor.getX() + 0.5 + (r.nextDouble() - 0.5) * 0.3;
			double z = floor.getZ() + 0.5 + (r.nextDouble() - 0.5) * 0.3;
			eel.currentYaw = this.currentYaw + (r.nextFloat() - 0.5f) * 20.0f;
			eel.snapTo(x, floor.getY(), z, eel.currentYaw, 0.0f);
			if (!level.noCollision(eel)) {
				eel.discard();
				continue;
			}
			eel.finalizeSpawn(level, level.getCurrentDifficultyAt(floor), EntitySpawnReason.NATURAL, null);
			eel.colonyPending = false;
			level.addFreshEntity(eel);
			made++;
		}
	}

	/** Water cell resting on sand within a block above or two below the given cell, or null. */
	public static BlockPos sandFloor(LevelReader level, BlockPos cell) {
		for (int dy = 1; dy >= -2; dy--) {
			BlockPos p = cell.above(dy);
			if (level.getFluidState(p).is(FluidTags.WATER) && level.getFluidState(p.above()).is(FluidTags.WATER)
					&& level.getBlockState(p.below()).is(BlockTags.SAND)) {
				return p;
			}
		}
		return null;
	}
}
