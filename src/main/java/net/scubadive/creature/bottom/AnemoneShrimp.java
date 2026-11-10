package net.scubadive.creature.bottom;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** Tiny shrimp that stays on top of its anemone and only shuffles a few centimetres. */
public class AnemoneShrimp extends AnemoneDweller {
	public AnemoneShrimp(EntityType<? extends AnemoneShrimp> type, Level level) {
		super(type, level);
	}

	@Override
	public void tick() {
		super.tick();

		if (this.level().isClientSide()) {
			return;
		}

		boolean inWater = this.isInWater();
		if (!inWater) {
			this.setNoGravity(false);
			return;
		}

		updateHome();
		// Without an anemone it simply sits on the sea floor.
		this.setNoGravity(this.home != null);
		if (this.home == null) {
			Vec3 m = this.getDeltaMovement();
			this.setDeltaMovement(0.0, m.y, 0.0);
			return;
		}

		Vec3 top = new Vec3(this.home.getX() + 0.5, this.home.getY() + 0.62, this.home.getZ() + 0.5);
		if (this.target == null || --this.retarget <= 0) {
			this.retarget = 80 + this.getRandom().nextInt(160);
			this.target = top.add(
					(this.getRandom().nextDouble() - 0.5) * 0.4,
					(this.getRandom().nextDouble() - 0.5) * 0.1,
					(this.getRandom().nextDouble() - 0.5) * 0.4);
			faceTowards(this.target.subtract(this.position()).add(
					(this.getRandom().nextDouble() - 0.5) * 0.5, 0, (this.getRandom().nextDouble() - 0.5) * 0.5));
		}
		steer(this.target, 0.006);
	}
}
