package net.scubadive.creature.fresh;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.phys.Vec3;
import net.scubadive.creature.FleeHelper;

/**
 * Crayfish: walks along the bottom of rivers and lakes in short bursts, pausing in between. When a diver
 * comes close it flips its tail and shoots backwards, away from the diver.
 */
public class Crayfish extends Mob {
	private static final double WALK_SPEED = 0.018;
	private static final double FLEE_RADIUS = 2.5;

	private int moveTimer;
	private boolean walking;
	private float heading;
	private int fleeCooldown;

	public Crayfish(EntityType<? extends Crayfish> type, Level level) {
		super(type, level);
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
		if (this.level().isClientSide() || !this.isInWater()) {
			return;
		}
		Vec3 m = this.getDeltaMovement();
		if (this.fleeCooldown > 0) {
			this.fleeCooldown--;
		}
		Player p = FleeHelper.tooClose(this, FLEE_RADIUS);
		if (p != null && this.fleeCooldown <= 0) {
			// Tail flip: a quick backwards dart, keeping the claws towards the diver.
			Vec3 away = FleeHelper.awayFrom(this, p);
			this.setDeltaMovement(away.x * 0.22, 0.06, away.z * 0.22);
			this.heading = (float) Math.toDegrees(Math.atan2(away.x, -away.z));
			this.fleeCooldown = 40;
			this.walking = false;
			this.moveTimer = 40;
			turnTo(this.heading, 90.0f);
			return;
		}

		if (--this.moveTimer <= 0) {
			this.moveTimer = 30 + this.getRandom().nextInt(90);
			this.walking = this.getRandom().nextInt(2) == 0;
			if (this.walking) {
				this.heading += (this.getRandom().nextFloat() - 0.5f) * 160.0f;
			}
		}
		if (this.walking && this.onGround()) {
			double rad = Math.toRadians(this.heading);
			double dx = -Math.sin(rad);
			double dz = Math.cos(rad);
			BlockPos ahead = BlockPos.containing(this.getX() + dx * 0.5, this.getY() + 0.1, this.getZ() + dz * 0.5);
			if (this.level().getFluidState(ahead).is(FluidTags.WATER)) {
				this.setDeltaMovement(dx * WALK_SPEED, m.y, dz * WALK_SPEED);
				turnTo(this.heading, 12.0f);
				return;
			}
			this.walking = false;
		}
		if (this.onGround()) {
			this.setDeltaMovement(0.0, m.y, 0.0);
		}
	}

	private void turnTo(float yaw, float step) {
		float y = Mth.approachDegrees(this.getYRot(), yaw, step);
		this.setYRot(y);
		this.setYBodyRot(y);
		this.setYHeadRot(y);
	}
}
