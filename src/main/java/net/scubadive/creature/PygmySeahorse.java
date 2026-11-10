package net.scubadive.creature;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** Very small, rare seahorse that hangs in the water and barely drifts. */
public class PygmySeahorse extends Mob {

	/** Water creatures: the default Mob check refuses positions inside liquid, which would block natural spawns. */
	@Override
	public boolean checkSpawnObstruction(net.minecraft.world.level.LevelReader level) {
		return level.isUnobstructed(this);
	}
	private int driftTimer;
	private Vec3 drift = Vec3.ZERO;

	public PygmySeahorse(EntityType<? extends PygmySeahorse> type, Level level) {
		super(type, level);
		this.setNoGravity(true);
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

		boolean inWater = this.isInWater();
		// Out of water it falls like anything else.
		this.setNoGravity(inWater);

		if (!inWater) {
			return;
		}

		if (--this.driftTimer <= 0) {
			this.driftTimer = 80 + this.getRandom().nextInt(120);
			this.drift = new Vec3(
					(this.getRandom().nextDouble() - 0.5) * 0.008,
					(this.getRandom().nextDouble() - 0.5) * 0.004,
					(this.getRandom().nextDouble() - 0.5) * 0.008);
		}

		BlockPos target = BlockPos.containing(
				this.getX() + this.drift.x * 40, this.getY() + this.drift.y * 40 + 0.1, this.getZ() + this.drift.z * 40);
		if (this.level().getFluidState(target).is(FluidTags.WATER)) {
			this.setDeltaMovement(this.drift);
		} else {
			this.drift = Vec3.ZERO;
			this.setDeltaMovement(Vec3.ZERO);
		}
	}
}
