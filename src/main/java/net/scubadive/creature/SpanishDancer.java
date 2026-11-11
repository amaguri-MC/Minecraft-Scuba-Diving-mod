package net.scubadive.creature;

import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

/**
 * Spanish dancer (Hexabranchus): a big red slug that crawls the reef like the others, but every so often
 * lifts off and swims slowly through the water, flexing its body and flapping its frilled mantle,
 * then sinks back down. The swim bout is synced so the model can dance.
 */
public class SpanishDancer extends Nudibranch {
	private static final EntityDataAccessor<Boolean> DANCING = SynchedEntityData.defineId(SpanishDancer.class, EntityDataSerializers.BOOLEAN);
	private static final int SWIM_MIN = 100;
	private static final int SWIM_SPREAD = 80;
	private static final int REST_MIN = 600;
	private static final int REST_SPREAD = 900;
	private static final double SWIM_SPEED = 0.025;

	private int swimTicks;
	private int restTicks = REST_MIN / 2;

	public SpanishDancer(EntityType<? extends SpanishDancer> type, Level level) {
		super(type, level);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(DANCING, false);
	}

	/** True while swimming through the water. */
	public boolean isDancing() {
		return this.entityData.get(DANCING);
	}

	@Override
	protected void readAdditionalSaveData(net.minecraft.world.level.storage.ValueInput input) {
		super.readAdditionalSaveData(input);
		// A bout saved mid-swim is not resumed: let it sink back to the reef.
		this.setNoGravity(false);
	}

	@Override
	protected void moveTick() {
		if (this.swimTicks > 0) {
			swimTick();
			return;
		}
		if (--this.restTicks <= 0) {
			this.swimTicks = SWIM_MIN + this.getRandom().nextInt(SWIM_SPREAD);
			this.heading = this.getRandom().nextFloat() * 360.0f;
			this.setNoGravity(true);
			this.entityData.set(DANCING, true);
			return;
		}
		super.moveTick();
	}

	private void swimTick() {
		this.swimTicks--;
		// Gentle wandering turn while swimming.
		this.heading += (float) Math.sin(this.tickCount * 0.05) * 1.5f;
		double rad = Math.toRadians(this.heading);
		double dx = -Math.sin(rad);
		double dz = Math.cos(rad);
		BlockPos ahead = BlockPos.containing(this.getX() + dx * 0.8, this.getY() + 0.2, this.getZ() + dz * 0.8);
		if (!this.level().getFluidState(ahead).is(FluidTags.WATER)) {
			this.heading += 90.0f + this.getRandom().nextFloat() * 180.0f;
			dx = 0.0;
			dz = 0.0;
		}
		// Rise during the first part of the bout (never out of the water), drift down at the end.
		boolean waterAbove = this.level().getFluidState(this.blockPosition().above()).is(FluidTags.WATER);
		double up = this.swimTicks > 50 ? (waterAbove ? 0.008 : 0.0) : -0.006;
		// The undulating body gives a slight bob.
		up += Math.sin(this.tickCount * 0.3) * 0.003;
		this.setDeltaMovement(dx * SWIM_SPEED, up, dz * SWIM_SPEED);
		this.setYRot(this.heading);
		this.setYHeadRot(this.heading);
		this.yBodyRot = this.heading;
		if (this.swimTicks <= 0) {
			this.setNoGravity(false);
			this.entityData.set(DANCING, false);
			this.restTicks = REST_MIN + this.getRandom().nextInt(REST_SPREAD);
		}
	}

	@Override
	protected double crawlSpeed() {
		return 0.006;
	}
}
