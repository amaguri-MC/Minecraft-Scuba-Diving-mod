package net.scubadive.creature.critter;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Drifting jellyfish: pulses upward, sinks slowly, drifts. Touching the tentacles stings (poison, for some species
 * also nausea). Each species has its own swimming style: the box jelly swims actively, the comb jelly glides
 * without pulsing, the giant Echizen jelly pulses slowly and heavily.
 */
public class Jellyfish extends Mob {

	/**
	 * Swimming style. pulse: ticks between bell pulses (0 = glides on its comb rows, no pulse); thrust: upward kick
	 * per pulse; push: forward kick per pulse; sink: constant sinking per tick; drift: forward drift per tick;
	 * turn: ticks between heading changes; turnDeg: largest heading change.
	 */
	record Style(int pulse, double thrust, double push, double sink, double drift, int turn, float turnDeg) {
	}

	private static final Style DRIFTER = new Style(50, 0.035, 0.0, 0.0015, 0.0008, 200, 90.0f);
	private static final Style BOX = new Style(18, 0.022, 0.03, 0.0012, 0.0015, 80, 120.0f);
	private static final Style COMB = new Style(0, 0.0, 0.0, 0.0002, 0.0012, 120, 70.0f);
	private static final Style LIONS_MANE = new Style(64, 0.04, 0.004, 0.0016, 0.0007, 240, 60.0f);
	private static final Style ECHIZEN = new Style(90, 0.045, 0.006, 0.0014, 0.0006, 300, 45.0f);

	/** Water creatures: the default Mob check refuses positions inside liquid, which would block natural spawns. */
	@Override
	public boolean checkSpawnObstruction(net.minecraft.world.level.LevelReader level) {
		return level.isUnobstructed(this);
	}
	private final TouchHazard hazard = new TouchHazard(30);
	private float drift;
	private int lastBounce;

	public Jellyfish(EntityType<? extends Jellyfish> type, Level level) {
		super(type, level);
		this.setNoGravity(true);
		this.drift = level.getRandom().nextFloat() * 360.0f;
	}

	private Style style() {
		EntityType<?> t = this.getType();
		if (t == Critters.BOX_JELLYFISH) {
			return BOX;
		}
		if (t == Critters.COMB_JELLY) {
			return COMB;
		}
		if (t == Critters.LIONS_MANE_JELLY) {
			return LIONS_MANE;
		}
		if (t == Critters.ECHIZEN_JELLY) {
			return ECHIZEN;
		}
		return DRIFTER;
	}

	/** Stings a touching player according to the species. The comb jelly has no stinging cells. */
	private void sting(Player p) {
		EntityType<?> t = this.getType();
		if (t == Critters.COMB_JELLY) {
			return;
		}
		if (t == Critters.BOX_JELLYFISH) {
			TouchHazard.sting(p, 80, 1, 140);
		} else if (t == Critters.LIONS_MANE_JELLY) {
			TouchHazard.sting(p, 70, 0, 40);
		} else if (t == Critters.SEA_NETTLE) {
			TouchHazard.sting(p, 50, 0);
		} else if (t == Critters.ECHIZEN_JELLY) {
			TouchHazard.sting(p, 30, 0);
		} else if (t == Critters.FRIED_EGG_JELLY) {
			TouchHazard.sting(p, 15, 0);
		} else {
			TouchHazard.sting(p, 25, 0);
		}
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
			sting(p);
		}
		if (!this.isInWater()) {
			this.setNoGravity(false);
			return;
		}
		this.setNoGravity(true);
		Style s = style();
		Vec3 m = this.getDeltaMovement();
		double rad = Math.toRadians(this.drift);
		double fx = -Math.sin(rad);
		double fz = Math.cos(rad);
		double vy = m.y * 0.94 - s.sink();
		double vx = m.x * 0.9 + fx * s.drift();
		double vz = m.z * 0.9 + fz * s.drift();
		if (s.pulse() > 0) {
			if ((this.tickCount + this.getId() * 7) % s.pulse() == 0) {
				vy += s.thrust();
				vx += fx * s.push();
				vz += fz * s.push();
			}
		} else {
			// Comb rows beat all the time: a smooth, slowly wandering glide.
			vy += Math.sin((this.tickCount + this.getId() * 13) * 0.02) * 0.0009;
		}
		if (this.tickCount % s.turn() == 0) {
			this.drift += (this.getRandom().nextFloat() - 0.5f) * s.turnDeg();
		}
		BlockPos above = BlockPos.containing(this.getX(), this.getBoundingBox().maxY + 0.3, this.getZ());
		if (!this.level().getFluidState(above).is(FluidTags.WATER) && vy > 0) {
			vy = -0.01;
		}
		if (this.horizontalCollision && this.tickCount - this.lastBounce > 40) {
			this.lastBounce = this.tickCount;
			this.drift += 180.0f;
		}
		this.setDeltaMovement(vx, vy, vz);
	}
}
