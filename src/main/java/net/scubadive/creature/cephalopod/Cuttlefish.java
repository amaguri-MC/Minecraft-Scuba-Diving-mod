package net.scubadive.creature.cephalopod;

import java.util.List;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import net.scubadive.creature.FleeHelper;

/**
 * Cuttlefish (mongou-ika): a broad, flat cuttlefish that hovers alone or in a pair just above the reef floor,
 * rippling the fin around its mantle. Its skin pattern drifts all the time (client side). When a diver comes close
 * it stops, flashes a black-and-white zebra pattern for a moment, then jets away.
 */
public class Cuttlefish extends Cephalopod {
	private static final EntityDataAccessor<Boolean> DISPLAY = SynchedEntityData.defineId(Cuttlefish.class, EntityDataSerializers.BOOLEAN);

	private static final double ALERT_RADIUS = 3.5;
	private static final double SNEAK_ALERT_RADIUS = 2.0;
	private static final int DISPLAY_TICKS = 18;
	private static final double DRIFT_SPEED = 0.025;

	private int displayTicks;
	private Vec3 threat = Vec3.ZERO;
	private int modeTimer;
	private boolean drifting;

	public Cuttlefish(EntityType<? extends Cuttlefish> type, Level level) {
		super(type, level);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(DISPLAY, false);
	}

	/** True while the zebra warning display is shown. */
	public boolean isDisplaying() {
		return this.entityData.get(DISPLAY);
	}

	@Override
	protected void onAttacked(ServerLevel level, Vec3 from) {
		this.displayTicks = 0;
		this.entityData.set(DISPLAY, false);
		startJet(level, awayFrom(from), 14, 0.3, 14);
	}

	@Override
	protected void swim(ServerLevel level) {
		// Warning display: hold still facing the diver, then shoot away.
		if (this.displayTicks > 0) {
			this.displayTicks--;
			turnTowards(yawOf(this.threat.subtract(this.position())), 20.0f);
			Vec3 m = this.getDeltaMovement();
			this.setDeltaMovement(m.x * 0.5, m.y * 0.5, m.z * 0.5);
			if (this.displayTicks == 0) {
				this.entityData.set(DISPLAY, false);
				startJet(level, awayFrom(this.threat), 12, 0.22, 3);
			}
			return;
		}
		if (this.startleCooldown <= 0) {
			Player p = FleeHelper.tooClose(this, ALERT_RADIUS);
			if (p != null && (!p.isShiftKeyDown() || p.distanceToSqr(this) < SNEAK_ALERT_RADIUS * SNEAK_ALERT_RADIUS)) {
				this.threat = p.position();
				this.displayTicks = DISPLAY_TICKS;
				this.entityData.set(DISPLAY, true);
				return;
			}
		}

		// Alternate between hovering in place and drifting slowly along the floor.
		if (--this.modeTimer <= 0) {
			this.drifting = !this.drifting;
			this.modeTimer = this.drifting ? 60 + this.getRandom().nextInt(100) : 40 + this.getRandom().nextInt(120);
			if (this.drifting) {
				this.heading += (this.getRandom().nextFloat() - 0.5f) * 140.0f;
			}
		}
		Vec3 dir = dirOf(this.heading);
		if (!isWaterAt(this.getX() + dir.x * 1.5, this.getY() + 0.1, this.getZ() + dir.z * 1.5)) {
			this.heading += 150.0f + this.getRandom().nextFloat() * 60.0f;
			dir = dirOf(this.heading);
		}
		// A partner nearby: keep within a few blocks of it.
		List<Cuttlefish> near = level.getEntitiesOfClass(Cuttlefish.class, this.getBoundingBox().inflate(8.0), c -> c != this && c.isAlive());
		if (!near.isEmpty()) {
			Vec3 to = near.get(0).position().subtract(this.position());
			if (to.horizontalDistanceSqr() > 9.0) {
				Vec3 pull = new Vec3(to.x, 0.0, to.z).normalize();
				dir = dir.add(pull.scale(0.6)).normalize();
				this.heading = yawOf(dir);
				this.drifting = true;
			}
		}
		double speed = this.drifting ? DRIFT_SPEED : 0.0;

		// Hover about one to two blocks above the floor, with a slow bob.
		int floor = heightAboveFloor(6);
		double vy = Math.sin((this.tickCount + this.getId() * 13) * 0.05) * 0.003;
		if (floor > 2) {
			vy -= 0.01;
		} else if (floor < 1) {
			vy += 0.008;
		}
		if (depthBelowSurface(3) < 1) {
			vy = Math.min(vy, -0.006);
		}

		Vec3 m = this.getDeltaMovement();
		this.setDeltaMovement(m.x * 0.75 + dir.x * speed * 0.4, m.y * 0.75 + vy * 0.4, m.z * 0.75 + dir.z * speed * 0.4);
		turnTowards(this.heading, 3.0f);
	}
}
