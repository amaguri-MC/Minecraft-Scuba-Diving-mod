package net.scubadive.creature.reef;

import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.scubadive.creature.FleeHelper;

/** Parrotfish swim up to a bit of coral and nibble it (puffs of coral particles, no block damage). */
final class ReefGrazeGoal extends Goal {
	private final PathfinderMob mob;
	private final ReefCreature fish;
	private final ReefProfile profile;
	private BlockPos coral;
	private Direction face;
	private Vec3 mouth;
	private int bites;
	private int age;
	private int repath;

	ReefGrazeGoal(PathfinderMob mob, ReefProfile profile) {
		this.mob = mob;
		this.fish = (ReefCreature) mob;
		this.profile = profile;
		this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
	}

	@Override
	public boolean canUse() {
		if (this.fish.hasFled() || this.mob.getRandom().nextInt(60) != 0) {
			return false;
		}
		if (FleeHelper.tooClose(this.mob, this.profile.fleeRadius() * 1.3) != null) {
			return false;
		}
		return this.pickCoral();
	}

	@Override
	public boolean canContinueToUse() {
		return this.bites < 6 && this.age < 240 && !this.fish.hasFled()
				&& FleeHelper.tooClose(this.mob, this.profile.fleeRadius()) == null
				&& this.coral != null && this.stillCoral();
	}

	@Override
	public void start() {
		this.bites = 0;
		this.age = 0;
		this.repath = 0;
	}

	@Override
	public void stop() {
		this.mob.getNavigation().stop();
		this.coral = null;
	}

	@Override
	public boolean requiresUpdateEveryTick() {
		return true;
	}

	@Override
	public void tick() {
		this.age++;
		if (this.mouth == null || this.coral == null) {
			return;
		}
		this.mob.getLookControl().setLookAt(this.mouth.x + this.face.getStepX(), this.mouth.y, this.mouth.z + this.face.getStepZ());
		if (this.mob.distanceToSqr(this.mouth) > 0.6) {
			if (--this.repath <= 0) {
				this.repath = 12;
				this.mob.getNavigation().moveTo(this.mouth.x, this.mouth.y, this.mouth.z, 0.9);
			}
			return;
		}
		this.mob.getNavigation().stop();
		if (this.age % 12 == 0) {
			this.bites++;
			BlockState state = this.mob.level().getBlockState(this.coral);
			this.mob.playSound(SoundEvents.CORAL_BLOCK_HIT, 0.35f, 1.5f);
			if (this.mob.level() instanceof ServerLevel serverLevel) {
				Vec3 c = Vec3.atCenterOf(this.coral);
				serverLevel.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, state),
						c.x + this.face.getStepX() * 0.5, c.y + this.face.getStepY() * 0.5, c.z + this.face.getStepZ() * 0.5,
						5, 0.12, 0.12, 0.12, 0.04);
			}
		}
	}

	private boolean stillCoral() {
		BlockState s = this.mob.level().getBlockState(this.coral);
		return s.is(BlockTags.CORALS) || s.is(BlockTags.CORAL_BLOCKS);
	}

	/** Samples a few spots around the fish for coral with open water on one side. */
	private boolean pickCoral() {
		RandomSource r = this.mob.getRandom();
		BlockPos origin = this.mob.blockPosition();
		for (int i = 0; i < 24; i++) {
			BlockPos p = origin.offset(r.nextInt(13) - 6, r.nextInt(7) - 3, r.nextInt(13) - 6);
			BlockState s = this.mob.level().getBlockState(p);
			if (!(s.is(BlockTags.CORAL_BLOCKS) || s.is(BlockTags.CORALS))) {
				continue;
			}
			for (Direction d : Direction.values()) {
				BlockPos n = p.relative(d);
				if (this.mob.level().getFluidState(n).is(FluidTags.WATER) && this.mob.level().getBlockState(n).getCollisionShape(this.mob.level(), n).isEmpty()) {
					this.coral = p.immutable();
					this.face = d;
					this.mouth = Vec3.atCenterOf(n);
					return true;
				}
			}
		}
		return false;
	}
}
