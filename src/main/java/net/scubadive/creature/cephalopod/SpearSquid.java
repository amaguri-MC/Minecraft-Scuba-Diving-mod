package net.scubadive.creature.cephalopod;

import java.util.List;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import net.scubadive.creature.FleeHelper;

/**
 * Spear squid (yari-ika): slender, translucent squid that cruises in schools of 5-12, all lined up on the same
 * heading. When a diver comes close, the whole school jets backwards together with a little ink.
 * At night the school drifts up into shallower water.
 */
public class SpearSquid extends Cephalopod {
	private static final double SCHOOL_RADIUS = 6.0;
	private static final double ALERT_RADIUS = 4.0;
	private static final double SNEAK_ALERT_RADIUS = 2.2;
	private static final double CRUISE_SPEED = 0.055;

	private int wanderTimer;
	private int schoolTimer;
	private Vec3 cohesion = Vec3.ZERO;
	private Vec3 separation = Vec3.ZERO;

	public SpearSquid(EntityType<? extends SpearSquid> type, Level level) {
		super(type, level);
		this.schoolTimer = level.getRandom().nextInt(10);
	}

	@Override
	protected void swim(ServerLevel level) {
		if (this.startleCooldown <= 0) {
			Player p = FleeHelper.tooClose(this, ALERT_RADIUS);
			if (p != null && (!p.isShiftKeyDown() || p.distanceToSqr(this) < SNEAK_ALERT_RADIUS * SNEAK_ALERT_RADIUS)) {
				alarmSchool(level, p.position());
				return;
			}
		}

		// Schooling: line up with the neighbours' heading, keep together, but not too close.
		if (--this.schoolTimer <= 0) {
			this.schoolTimer = 10;
			updateSchool();
		}
		// The school's heading wanders slowly; every squid nudges it a little and alignment spreads it.
		if (--this.wanderTimer <= 0) {
			this.wanderTimer = 80 + this.getRandom().nextInt(160);
			this.heading += (this.getRandom().nextFloat() - 0.5f) * 50.0f;
		}
		Vec3 ahead = dirOf(this.heading);
		if (!isWaterAt(this.getX() + ahead.x * 2.0, this.getY() + 0.1, this.getZ() + ahead.z * 2.0)) {
			// Wall or shore ahead: turn round (the neighbours follow through alignment).
			this.heading += 120.0f + this.getRandom().nextFloat() * 60.0f;
			ahead = dirOf(this.heading);
		}
		Vec3 steer = ahead.add(this.cohesion.scale(0.35)).add(this.separation.scale(0.8));
		Vec3 flat = new Vec3(steer.x, 0.0, steer.z);
		Vec3 dir = flat.lengthSqr() < 1.0E-6 ? ahead : flat.normalize();

		// Depth: stay off the floor and below the surface; come up shallower at night.
		boolean night = level.isDarkOutside();
		int depth = depthBelowSurface(16);
		int floor = heightAboveFloor(8);
		int minDepth = night ? 2 : 4;
		int maxDepth = night ? 5 : 12;
		double vy = 0.0;
		if (depth < minDepth) {
			vy = -0.012;
		} else if (depth > maxDepth && floor > 2) {
			vy = 0.008;
		}
		if (floor < 2) {
			vy = 0.012;
		}

		// Water drag takes about 20 % per tick, so this settles at the cruise speed.
		Vec3 m = this.getDeltaMovement();
		this.setDeltaMovement(m.x * 0.75 + dir.x * CRUISE_SPEED * 0.4, m.y * 0.75 + vy * 0.4, m.z * 0.75 + dir.z * CRUISE_SPEED * 0.4);
		turnTowards(yawOf(dir), 5.0f);
	}

	private void updateSchool() {
		List<SpearSquid> school = this.level().getEntitiesOfClass(SpearSquid.class, this.getBoundingBox().inflate(SCHOOL_RADIUS),
				s -> s != this && s.isAlive());
		this.cohesion = Vec3.ZERO;
		this.separation = Vec3.ZERO;
		if (school.isEmpty()) {
			return;
		}
		double sx = 0.0;
		double sz = 0.0;
		Vec3 center = Vec3.ZERO;
		Vec3 push = Vec3.ZERO;
		for (SpearSquid s : school) {
			Vec3 h = dirOf(s.heading);
			sx += h.x;
			sz += h.z;
			center = center.add(s.position());
			Vec3 d = this.position().subtract(s.position());
			double dist = d.length();
			if (dist < 0.9 && dist > 1.0E-4) {
				push = push.add(d.scale((0.9 - dist) / dist));
			}
		}
		// Alignment: blend the own heading towards the school's mean heading.
		Vec3 own = dirOf(this.heading);
		float mean = yawOf(new Vec3(sx + own.x, 0.0, sz + own.z));
		this.heading = this.heading + Mth.wrapDegrees(mean - this.heading) * 0.5f;
		center = center.scale(1.0 / school.size());
		Vec3 toCenter = center.subtract(this.position());
		this.cohesion = toCenter.lengthSqr() > 4.0 ? toCenter.normalize() : Vec3.ZERO;
		this.separation = push;
	}

	/** One squid noticed the diver: the whole school jets off backwards together, in one direction. */
	private void alarmSchool(ServerLevel level, Vec3 threat) {
		List<SpearSquid> school = level.getEntitiesOfClass(SpearSquid.class, this.getBoundingBox().inflate(SCHOOL_RADIUS + 2.0),
				SpearSquid::isAlive);
		Vec3 center = Vec3.ZERO;
		for (SpearSquid s : school) {
			center = center.add(s.position());
		}
		center = school.isEmpty() ? this.position() : center.scale(1.0 / school.size());
		Vec3 away = center.subtract(threat);
		away = new Vec3(away.x, away.y * 0.2, away.z);
		Vec3 dir = away.lengthSqr() < 1.0E-6 ? awayFrom(threat) : away.normalize();
		for (SpearSquid s : school) {
			if (s.isInWater() && s.jetTicks <= 0) {
				// Only a little ink: a few squid squirt, the rest just jet.
				s.startJet(level, dir, 16, 0.28, s.getRandom().nextInt(3) == 0 ? 6 : 0);
			}
		}
	}
}
