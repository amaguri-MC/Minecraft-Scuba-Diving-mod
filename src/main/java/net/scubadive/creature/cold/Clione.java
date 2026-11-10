package net.scubadive.creature.cold;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.phys.Vec3;

/**
 * Clione ("sea angel"): a tiny translucent pteropod that hangs upright in cold water and rows slowly
 * with its two wing-like parapodia. Drifts along a lazily changing heading, rising a little with each
 * flutter and sinking gently in between.
 */
public class Clione extends Mob {
	private float heading;
	private int turnTimer;

	public Clione(EntityType<? extends Clione> type, Level level) {
		super(type, level);
		this.setNoGravity(true);
		this.heading = level.getRandom().nextFloat() * 360.0f;
	}

	/** Water creatures: the default Mob check refuses positions inside liquid, which would block natural spawns. */
	@Override
	public boolean checkSpawnObstruction(LevelReader level) {
		return level.isUnobstructed(this);
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
		if (!this.isInWater()) {
			this.setNoGravity(false);
			return;
		}
		this.setNoGravity(true);

		if (--this.turnTimer <= 0) {
			this.turnTimer = 60 + this.getRandom().nextInt(120);
			this.heading += (this.getRandom().nextFloat() - 0.5f) * 120.0f;
		}
		double rad = Math.toRadians(this.heading);
		double dx = -Math.sin(rad);
		double dz = Math.cos(rad);
		// Turn away from walls and the ice above instead of bumping into them.
		BlockPos ahead = BlockPos.containing(this.getX() + dx * 0.8, this.getY() + 0.1, this.getZ() + dz * 0.8);
		if (!this.level().getFluidState(ahead).is(FluidTags.WATER)) {
			this.heading += 150.0f + this.getRandom().nextFloat() * 60.0f;
			dx = -dx;
			dz = -dz;
		}

		Vec3 m = this.getDeltaMovement();
		// Each wing stroke (matching the client flap) gives a little lift; it sinks very slowly in between.
		float stroke = Mth.sin((this.tickCount + this.getId() * 5) * 0.35f);
		double vy = m.y * 0.85 + (stroke > 0.9f ? 0.006 : -0.0012);
		BlockPos above = BlockPos.containing(this.getX(), this.getBoundingBox().maxY + 0.4, this.getZ());
		if (vy > 0 && !this.level().getFluidState(above).is(FluidTags.WATER)) {
			vy = -0.008;
		}
		double speed = 0.012;
		this.setDeltaMovement(m.x * 0.85 + dx * speed * 0.15, vy, m.z * 0.85 + dz * speed * 0.15);

		float yaw = Mth.approachDegrees(this.getYRot(), this.heading, 4.0f);
		this.setYRot(yaw);
		this.setYBodyRot(yaw);
		this.setYHeadRot(yaw);
	}
}
