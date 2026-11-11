package net.scubadive.creature.reef;

import java.util.EnumSet;
import java.util.List;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.fish.AbstractFish;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Cleaner wrasse: picks a big fish or a diver close by, swims over and circles it tightly for a few
 * seconds, darting in now and then as if picking off parasites. Harmless.
 */
final class ReefCleanGoal extends Goal {
	private static final double SEARCH = 7.0;
	/** Fish at least this wide count as clients (lionfish, grouper, parrotfish, sharks, napoleon...). */
	private static final float CLIENT_WIDTH = 0.34f;
	private final PathfinderMob mob;
	private final ReefCreature fish;
	private LivingEntity client;
	private int age;
	private int duration;
	private int cooldown = 40;
	private int repath;
	private float angle;
	private float spin;

	ReefCleanGoal(PathfinderMob mob) {
		this.mob = mob;
		this.fish = (ReefCreature) mob;
		this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
	}

	@Override
	public boolean canUse() {
		if (this.cooldown > 0) {
			this.cooldown--;
			return false;
		}
		if (this.fish.hasFled() || this.mob.getRandom().nextInt(20) != 0) {
			return false;
		}
		this.client = this.findClient();
		return this.client != null;
	}

	private LivingEntity findClient() {
		AABB box = this.mob.getBoundingBox().inflate(SEARCH);
		LivingEntity best = null;
		double bestDist = Double.MAX_VALUE;
		List<Player> players = this.mob.level().getEntitiesOfClass(Player.class, box,
				p -> p.isAlive() && !p.isSpectator() && p.isInWater());
		List<AbstractFish> fishes = this.mob.level().getEntitiesOfClass(AbstractFish.class, box,
				f -> f != this.mob && f.isAlive() && f.isInWater() && f.getBbWidth() >= CLIENT_WIDTH && f.getType() != this.mob.getType());
		for (LivingEntity e : players) {
			double d = this.mob.distanceToSqr(e) * 0.7; // divers are slightly preferred
			if (d < bestDist) {
				bestDist = d;
				best = e;
			}
		}
		for (LivingEntity e : fishes) {
			double d = this.mob.distanceToSqr(e);
			if (d < bestDist) {
				bestDist = d;
				best = e;
			}
		}
		return best;
	}

	@Override
	public boolean canContinueToUse() {
		return this.age < this.duration && this.client != null && this.client.isAlive() && this.client.isInWater()
				&& !(this.client instanceof Player p && p.isSpectator())
				&& this.mob.distanceToSqr(this.client) < 10.0 * 10.0 && !this.fish.hasFled();
	}

	@Override
	public void start() {
		this.age = 0;
		this.repath = 0;
		this.duration = 60 + this.mob.getRandom().nextInt(80);
		this.angle = this.mob.getRandom().nextFloat() * (float) (Math.PI * 2);
		this.spin = this.mob.getRandom().nextBoolean() ? 0.09f : -0.09f;
	}

	@Override
	public void stop() {
		this.client = null;
		this.mob.getNavigation().stop();
		this.cooldown = 160 + this.mob.getRandom().nextInt(240);
	}

	@Override
	public boolean requiresUpdateEveryTick() {
		return true;
	}

	@Override
	public void tick() {
		this.age++;
		if (this.client == null) {
			return;
		}
		Vec3 c = this.client.getBoundingBox().getCenter();
		this.angle += this.spin;
		// Mostly circles just outside the body; every second or so darts in for a "bite".
		boolean dart = (this.age / 10) % 3 == 2;
		double r = this.client.getBbWidth() * 0.5 + (dart ? 0.12 : 0.4);
		double bob = Math.sin(this.age * 0.2) * this.client.getBbHeight() * 0.3;
		double tx = c.x + Math.cos(this.angle) * r;
		double ty = c.y + bob;
		double tz = c.z + Math.sin(this.angle) * r;
		this.mob.getLookControl().setLookAt(c.x, c.y, c.z);
		if (--this.repath <= 0) {
			this.repath = 4;
			this.mob.getNavigation().moveTo(tx, ty, tz, dart ? 1.6 : 1.3);
		}
	}
}
