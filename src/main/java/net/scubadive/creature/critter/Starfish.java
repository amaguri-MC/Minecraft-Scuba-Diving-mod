package net.scubadive.creature.critter;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** A sea star lying on the floor and creeping very slowly. Crown-of-thorns ones prick on touch. */
public class Starfish extends Mob {
	private final TouchHazard hazard = new TouchHazard(20);
	private int moveTimer;
	private float heading;
	private boolean creeping;

	public Starfish(EntityType<? extends Starfish> type, Level level) {
		super(type, level);
	}

	public boolean isSpiny() {
		return this.getType() == Critters.CROWN_OF_THORNS;
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
	public void tick() {
		super.tick();
		if (this.level().isClientSide() || !this.isInWater()) {
			return;
		}
		if (isSpiny()) {
			for (Player p : this.hazard.touching(this, 0.05)) {
				TouchHazard.prick(this, p, 2.0f);
			}
		}
		if (--this.moveTimer <= 0) {
			this.moveTimer = 200 + this.getRandom().nextInt(400);
			this.creeping = this.getRandom().nextInt(3) == 0;
			this.heading = this.getRandom().nextFloat() * 360.0f;
		}
		Vec3 m = this.getDeltaMovement();
		if (this.creeping && this.onGround()) {
			double rad = Math.toRadians(this.heading);
			double dx = -Math.sin(rad);
			double dz = Math.cos(rad);
			BlockPos ahead = BlockPos.containing(this.getX() + dx * 0.5, this.getY() + 0.1, this.getZ() + dz * 0.5);
			if (this.level().getFluidState(ahead).is(FluidTags.WATER)) {
				this.setDeltaMovement(dx * 0.003, m.y, dz * 0.003);
				return;
			}
			this.creeping = false;
		}
		this.setDeltaMovement(0.0, m.y, 0.0);
	}
}
