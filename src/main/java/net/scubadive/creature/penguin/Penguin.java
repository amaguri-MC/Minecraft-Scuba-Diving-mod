package net.scubadive.creature.penguin;

import java.util.EnumSet;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.control.SmoothSwimmingMoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.RandomSwimmingGoal;
import net.minecraft.world.entity.ai.navigation.AmphibiousPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.Vec3;

/**
 * Penguin (gentoo, rockhopper, cape share this class). On the ice it mostly stands and dozes, looks around,
 * now and then waddles a few steps and steps aside from a running player. Once in a while it goes into the
 * water and swims fast for a while (body horizontal), then climbs back out onto the ice or shore.
 * It never drowns.
 */
public class Penguin extends PathfinderMob {
	/** Client only: 0 = standing pose, 1 = swimming pose (blended over a few ticks). */
	public float swimAmount;
	public float swimAmountO;

	private int waterTicks;
	private int stayInWater;

	public Penguin(EntityType<? extends Penguin> type, Level level) {
		super(type, level);
		this.moveControl = new PenguinMoveControl(this);
		this.lookControl = new PenguinLookControl(this);
		this.setPathfindingMalus(PathType.WATER, 0.0f);
		this.stayInWater = rollStay();
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Mob.createMobAttributes()
				.add(Attributes.MAX_HEALTH, 8.0)
				.add(Attributes.MOVEMENT_SPEED, 0.2);
	}

	private int rollStay() {
		return 500 + this.getRandom().nextInt(1300);
	}

	@Override
	protected PathNavigation createNavigation(Level level) {
		return new AmphibiousPathNavigation(this, level);
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(0, new PanicGoal(this, 1.6));
		this.goalSelector.addGoal(1, new StepAsideGoal(this));
		this.goalSelector.addGoal(2, new LeaveWaterGoal(this));
		this.goalSelector.addGoal(3, new EnterWaterGoal(this));
		this.goalSelector.addGoal(4, new SwimAroundGoal(this));
		this.goalSelector.addGoal(5, new WaddleGoal(this));
		this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 6.0f));
		this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
	}

	@Override
	public boolean canBreatheUnderwater() {
		return true;
	}

	@Override
	public boolean isPushedByFluid() {
		return false;
	}

	@Override
	public boolean canFreeze() {
		return false;
	}

	@Override
	public boolean removeWhenFarAway(double distance) {
		return false;
	}

	@Override
	public int getMaxHeadYRot() {
		return 60;
	}

	@Override
	public float getWalkTargetValue(BlockPos pos, LevelReader level) {
		if (level.getFluidState(pos).is(FluidTags.WATER)) {
			return 4.0f;
		}
		return isIceOrSnow(level.getBlockState(pos.below())) || level.getBlockState(pos).is(Blocks.SNOW) ? 10.0f : 0.0f;
	}

	static boolean isIceOrSnow(BlockState s) {
		return s.is(BlockTags.ICE) || s.is(Blocks.SNOW_BLOCK) || s.is(Blocks.POWDER_SNOW) || s.is(BlockTags.SNOW);
	}

	@Override
	public void tick() {
		super.tick();
		if (this.level().isClientSide()) {
			this.swimAmountO = this.swimAmount;
			boolean swimming = this.isInWater() && !(this.onGround() && !this.isUnderWater());
			this.swimAmount = Mth.approach(this.swimAmount, swimming ? 1.0f : 0.0f, 0.15f);
		}
	}

	@Override
	public void aiStep() {
		super.aiStep();
		if (!this.level().isClientSide()) {
			if (this.isInWater()) {
				if (this.waterTicks == 0) {
					this.stayInWater = rollStay();
				}
				this.waterTicks++;
			} else {
				this.waterTicks = 0;
			}
		}
	}

	/** Fast, gravity-free swimming steered by the move control; hops out where the bank is one block high. */
	@Override
	protected void travelInWater(Vec3 input, double baseGravity, boolean isFalling, double oldY) {
		this.moveRelative(this.getSpeed(), input);
		this.move(MoverType.SELF, this.getDeltaMovement());
		Vec3 v = this.getDeltaMovement().scale(0.9);
		if (this.horizontalCollision && this.isFree(v.x, v.y + 0.6 - this.getY() + oldY, v.z)) {
			v = new Vec3(v.x, 0.3, v.z);
		}
		this.setDeltaMovement(v);
	}

	/** Ground: plain walking (steps and jumps); water: smooth swimming that pitches the body. */
	static final class PenguinMoveControl extends MoveControl<Penguin> {
		private final SmoothSwimmingMoveControl<Penguin> swim;

		PenguinMoveControl(Penguin penguin) {
			super(penguin);
			this.swim = new SmoothSwimmingMoveControl<>(penguin, 85, 10, 0.12f, 1.0f, false);
		}

		@Override
		public void tick() {
			if (this.mob.isInWater()) {
				if (this.operation == MoveControl.Operation.MOVE_TO) {
					this.swim.setWantedPosition(this.wantedX, this.wantedY, this.wantedZ, this.speedModifier);
					this.operation = MoveControl.Operation.WAIT;
				}
				this.swim.tick();
			} else {
				this.swim.setWait();
				super.tick();
			}
		}
	}

	/** In water the head stays in line with the body (the body pitch is the swimming direction). */
	static final class PenguinLookControl extends LookControl {
		PenguinLookControl(Mob mob) {
			super(mob);
		}

		@Override
		public void tick() {
			if (this.mob.isInWater()) {
				this.lookAtCooldown = 0;
				this.mob.setYHeadRot(Mth.approachDegrees(this.mob.yHeadRot, this.mob.yBodyRot, 20.0f));
				if (this.mob.getNavigation().isDone()) {
					this.mob.setXRot(Mth.approachDegrees(this.mob.getXRot(), 0.0f, 3.0f));
				}
				return;
			}
			super.tick();
		}
	}

	/** A running (sprinting) player close by on land: shuffle a few blocks away, nothing more. */
	static final class StepAsideGoal extends Goal {
		private final Penguin penguin;
		private Player player;

		StepAsideGoal(Penguin penguin) {
			this.penguin = penguin;
			this.setFlags(EnumSet.of(Goal.Flag.MOVE));
		}

		@Override
		public boolean canUse() {
			if (this.penguin.isInWater()) {
				return false;
			}
			Player p = this.penguin.level().getNearestPlayer(this.penguin, 4.0);
			if (p == null || p.isSpectator() || !p.isSprinting()) {
				return false;
			}
			Vec3 to = LandRandomPos.getPosAway(this.penguin, 4, 2, p.position());
			if (to == null) {
				return false;
			}
			this.player = p;
			return this.penguin.getNavigation().moveTo(to.x, to.y, to.z, 1.0);
		}

		@Override
		public boolean canContinueToUse() {
			return !this.penguin.getNavigation().isDone();
		}

		@Override
		public void stop() {
			this.player = null;
		}
	}

	/** Rarely: walk to open water nearby and dive in. */
	static final class EnterWaterGoal extends Goal {
		private final Penguin penguin;
		private BlockPos target;

		EnterWaterGoal(Penguin penguin) {
			this.penguin = penguin;
			this.setFlags(EnumSet.of(Goal.Flag.MOVE));
		}

		@Override
		public boolean canUse() {
			if (this.penguin.isInWater() || !this.penguin.onGround()
					|| this.penguin.getRandom().nextInt(reducedTickDelay(900)) != 0) {
				return false;
			}
			this.target = findWater();
			return this.target != null;
		}

		private BlockPos findWater() {
			BlockPos o = this.penguin.blockPosition();
			Level level = this.penguin.level();
			for (int i = 0; i < 48; i++) {
				BlockPos p = o.offset(this.penguin.getRandom().nextInt(21) - 10, this.penguin.getRandom().nextInt(5) - 3,
						this.penguin.getRandom().nextInt(21) - 10);
				if (level.getFluidState(p).is(FluidTags.WATER) && level.getBlockState(p.above()).isAir()) {
					return p;
				}
			}
			return null;
		}

		@Override
		public void start() {
			this.penguin.getNavigation().moveTo(this.target.getX() + 0.5, this.target.getY(), this.target.getZ() + 0.5, 0.9);
		}

		@Override
		public boolean canContinueToUse() {
			return !this.penguin.isInWater() && !this.penguin.getNavigation().isDone();
		}
	}

	/** After a swim, head for a low bank (ice, snow, rock or sand) and climb out. */
	static final class LeaveWaterGoal extends Goal {
		private final Penguin penguin;
		private BlockPos target;
		private int ticks;

		LeaveWaterGoal(Penguin penguin) {
			this.penguin = penguin;
			this.setFlags(EnumSet.of(Goal.Flag.MOVE));
		}

		@Override
		public boolean canUse() {
			if (!this.penguin.isInWater() || this.penguin.waterTicks < this.penguin.stayInWater
					|| this.penguin.getRandom().nextInt(reducedTickDelay(20)) != 0) {
				return false;
			}
			this.target = findBank();
			return this.target != null;
		}

		private BlockPos findBank() {
			BlockPos o = this.penguin.blockPosition();
			Level level = this.penguin.level();
			BlockPos best = null;
			double bestScore = Double.MAX_VALUE;
			for (int i = 0; i < 80; i++) {
				BlockPos p = o.offset(this.penguin.getRandom().nextInt(25) - 12, this.penguin.getRandom().nextInt(6) - 1,
						this.penguin.getRandom().nextInt(25) - 12);
				BlockState below = level.getBlockState(p.below());
				if (level.getFluidState(p).is(FluidTags.WATER) || !level.getFluidState(p.below()).isEmpty()
						|| !level.getBlockState(p).getCollisionShape(level, p).isEmpty()
						|| !level.getBlockState(p.above()).getCollisionShape(level, p.above()).isEmpty()
						|| !below.isFaceSturdy(level, p.below(), Direction.UP)) {
					continue;
				}
				double score = p.distSqr(o) - (isIceOrSnow(below) ? 40.0 : 0.0);
				if (score < bestScore) {
					bestScore = score;
					best = p;
				}
			}
			return best;
		}

		@Override
		public void start() {
			this.ticks = 0;
			this.penguin.getNavigation().moveTo(this.target.getX() + 0.5, this.target.getY(), this.target.getZ() + 0.5, 1.2);
		}

		@Override
		public boolean canContinueToUse() {
			return this.penguin.isInWater() && this.ticks < 400;
		}

		@Override
		public void tick() {
			this.ticks++;
			if (this.penguin.getNavigation().isDone()) {
				// No path found (or it ran out at the edge): steer straight for the bank.
				this.penguin.getMoveControl().setWantedPosition(this.target.getX() + 0.5, this.target.getY() + 0.2,
						this.target.getZ() + 0.5, 1.2);
			}
			if (this.penguin.horizontalCollision) {
				Vec3 m = this.penguin.getDeltaMovement();
				this.penguin.setDeltaMovement(m.x, Math.max(m.y, 0.35), m.z);
			}
		}

		@Override
		public void stop() {
			this.penguin.getNavigation().stop();
		}
	}

	/** Swims about quickly while in the water. */
	static final class SwimAroundGoal extends RandomSwimmingGoal {
		private final Penguin penguin;

		SwimAroundGoal(Penguin penguin) {
			super(penguin, 1.0, 10);
			this.penguin = penguin;
		}

		@Override
		public boolean canUse() {
			return this.penguin.isInWater() && super.canUse();
		}

		@Override
		public boolean canContinueToUse() {
			return this.penguin.isInWater() && super.canContinueToUse();
		}
	}

	/** On land: now and then a few waddling steps, otherwise it just stands there. */
	static final class WaddleGoal extends RandomStrollGoal {
		private final Penguin penguin;

		WaddleGoal(Penguin penguin) {
			super(penguin, 0.6, 320);
			this.penguin = penguin;
		}

		@Override
		public boolean canUse() {
			return !this.penguin.isInWater() && super.canUse();
		}

		@Override
		protected Vec3 getPosition() {
			return LandRandomPos.getPos(this.penguin, 3, 1);
		}
	}
}
