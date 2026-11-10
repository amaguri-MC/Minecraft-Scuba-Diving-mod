package net.scubadive.creature.bottom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.scubadive.creature.FleeHelper;

/** Skittish sand goby: rests on the sand and vanishes into it the moment a diver gets close. */
public class Goby extends Mob {
	private static final double ALERT_RADIUS = 3.0;

	private int hideTimer;
	private int dartTimer = 60;
	private int dartTicks;
	private double dartX;
	private double dartZ;

	public Goby(EntityType<? extends Goby> type, Level level) {
		super(type, level);
	}

	@Override
	public boolean canBreatheUnderwater() {
		return true;
	}

	@Override
	public void tick() {
		super.tick();

		if (!(this.level() instanceof ServerLevel serverLevel)) {
			return;
		}

		// Stay hidden while a diver is near, and a little while after they leave.
		// Hide only when a diver comes very close, or rushes in; a calm, still photographer can watch it.
		net.minecraft.world.entity.player.Player near = FleeHelper.tooClose(this, ALERT_RADIUS);
		boolean startled = near != null && (near.distanceTo(this) < 1.6
				|| near.getKnownMovement().horizontalDistance() > 0.09);
		if (startled) {
			if (this.hideTimer <= 0) {
				serverLevel.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.SAND.defaultBlockState()),
						this.getX(), this.getY() + 0.05, this.getZ(), 14, 0.1, 0.02, 0.1, 0.04);
			}
			this.hideTimer = Math.max(this.hideTimer, 30);
		}

		Vec3 motion = this.getDeltaMovement();
		if (this.hideTimer > 0) {
			this.hideTimer--;
			this.setInvisible(true);
			this.setDeltaMovement(0.0, motion.y, 0.0);
			if (this.hideTimer == 0) {
				this.setInvisible(false);
			}
			return;
		}
		if (this.isInvisible()) {
			this.setInvisible(false);
		}

		if (this.dartTicks > 0) {
			this.dartTicks--;
			BlockPos ahead = BlockPos.containing(this.getX() + this.dartX * 6.0, this.getY(), this.getZ() + this.dartZ * 6.0);
			if (this.level().getBlockState(ahead.below()).is(Blocks.SAND) && this.level().getFluidState(ahead).is(net.minecraft.tags.FluidTags.WATER)) {
				this.setDeltaMovement(this.dartX, motion.y, this.dartZ);
			} else {
				this.dartTicks = 0;
			}
			return;
		}
		this.setDeltaMovement(0.0, motion.y, 0.0);

		// Rarely scoot a short way along the sand.
		if (--this.dartTimer <= 0) {
			this.dartTimer = 120 + this.getRandom().nextInt(240);
			float heading = this.getRandom().nextFloat() * 360.0f;
			double rad = Math.toRadians(heading);
			this.dartX = -Math.sin(rad) * 0.05;
			this.dartZ = Math.cos(rad) * 0.05;
			this.dartTicks = 4 + this.getRandom().nextInt(6);
			this.setYRot(heading);
			this.setYBodyRot(heading);
			this.setYHeadRot(heading);
		}
	}
}
