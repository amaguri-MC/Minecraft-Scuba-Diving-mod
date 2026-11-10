package net.scubadive.creature.bottom;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.scubadive.creature.FleeHelper;

/** Orange clownfish that swims within 2 blocks of its anemone and ducks into it when approached. */
public class Clownfish extends AnemoneDweller {
	private static final double ROAM_RADIUS = 1.5;
	private static final double LEASH = 1.9;

	private int hideTimer;
	private int hideCooldown;

	public Clownfish(EntityType<? extends Clownfish> type, Level level) {
		super(type, level);
		this.setNoGravity(true);
	}

	@Override
	public void tick() {
		super.tick();

		if (this.level().isClientSide()) {
			return;
		}

		boolean inWater = this.isInWater();
		this.setNoGravity(inWater);
		if (!inWater) {
			return;
		}

		updateHome();
		if (this.home == null) {
			if (this.isInvisible()) {
				this.setInvisible(false);
			}
			wanderFree(0.03);
			return;
		}

		Vec3 center = Vec3.atCenterOf(this.home);
		Vec3 hideSpot = new Vec3(center.x, this.home.getY() + 0.4, center.z);

		if (this.hideCooldown > 0) {
			this.hideCooldown--;
		}

		// Hiding: dart into the anemone, stay out of sight for a moment, then come back out.
		if (this.hideTimer > 0) {
			this.hideTimer--;
			steer(hideSpot, 0.08);
			if (this.position().distanceTo(hideSpot) < 0.3) {
				this.setInvisible(true);
			}
			if (this.hideTimer == 0) {
				this.setInvisible(false);
				this.hideCooldown = 100 + this.getRandom().nextInt(60);
				this.target = null;
			}
			return;
		}
		if (this.isInvisible()) {
			this.setInvisible(false);
		}
		if (this.hideCooldown <= 0 && FleeHelper.tooClose(this, 2.5) != null) {
			this.hideTimer = 60 + this.getRandom().nextInt(40);
			return;
		}

		// Roaming: pick spots in a small bubble above the anemone.
		if (this.position().distanceTo(center) > LEASH) {
			this.target = hideSpot.add(0, 0.5, 0);
			this.retarget = 30;
		} else if (this.target == null || --this.retarget <= 0 || this.position().distanceToSqr(this.target) < 0.04) {
			this.retarget = 30 + this.getRandom().nextInt(50);
			this.target = pickRoamTarget(center);
		}
		steer(this.target, 0.04);
	}

	private Vec3 pickRoamTarget(Vec3 center) {
		for (int i = 0; i < 4; i++) {
			double angle = this.getRandom().nextDouble() * Math.PI * 2.0;
			double radius = 0.5 + this.getRandom().nextDouble() * (ROAM_RADIUS - 0.5);
			Vec3 t = new Vec3(
					center.x + Math.cos(angle) * radius,
					this.home.getY() + 0.3 + this.getRandom().nextDouble() * 0.9,
					center.z + Math.sin(angle) * radius);
			if (isWater(t)) {
				return t;
			}
		}
		return center.add(0, 0.4, 0);
	}
}
