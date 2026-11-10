package net.scubadive.creature.critter;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** Drifting jellyfish: pulses upward, sinks slowly, drifts. Touching the tentacles stings briefly (poison). */
public class Jellyfish extends Mob {

	/** Water creatures: the default Mob check refuses positions inside liquid, which would block natural spawns. */
	@Override
	public boolean checkSpawnObstruction(net.minecraft.world.level.LevelReader level) {
		return level.isUnobstructed(this);
	}
	private final TouchHazard hazard = new TouchHazard(30);
	private float drift;

	public Jellyfish(EntityType<? extends Jellyfish> type, Level level) {
		super(type, level);
		this.setNoGravity(true);
		this.drift = level.getRandom().nextFloat() * 360.0f;
	}

	/** Sting length in ticks for this species. */
	private int stingTicks() {
		if (this.getType() == Critters.SEA_NETTLE) {
			return 50;
		}
		return 25;
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
		for (Player p : this.hazard.touching(this, 0.05)) {
			TouchHazard.sting(p, stingTicks(), 0);
		}
		if (!this.isInWater()) {
			this.setNoGravity(false);
			return;
		}
		this.setNoGravity(true);
		Vec3 m = this.getDeltaMovement();
		double vy = m.y * 0.94 - 0.0015;
		int phase = (this.tickCount + this.getId() * 7) % 50;
		if (phase == 0) {
			vy += 0.035;
		}
		if (this.tickCount % 200 == 0) {
			this.drift += (this.getRandom().nextFloat() - 0.5f) * 90.0f;
		}
		BlockPos above = BlockPos.containing(this.getX(), this.getBoundingBox().maxY + 0.3, this.getZ());
		if (!this.level().getFluidState(above).is(FluidTags.WATER) && vy > 0) {
			vy = -0.01;
		}
		double rad = Math.toRadians(this.drift);
		double vx = m.x * 0.9 - Math.sin(rad) * 0.0008;
		double vz = m.z * 0.9 + Math.cos(rad) * 0.0008;
		this.setDeltaMovement(vx, vy, vz);
	}
}
