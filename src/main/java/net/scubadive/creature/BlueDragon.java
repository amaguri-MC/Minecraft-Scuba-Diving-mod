package net.scubadive.creature;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Blue dragon (Glaucus atlanticus): an ocean drifter of warm open seas. It floats belly-up just under the
 * surface, holding itself there with a swallowed air bubble, and slowly drifts with the wind.
 */
public class BlueDragon extends Nudibranch {
	/** How far below the top of the water the body floats. */
	private static final double FLOAT_DEPTH = 0.12;
	private static final int MAX_SCAN = 64;

	private int surfaceY = Integer.MIN_VALUE;
	private int scanTimer;
	private int driftTimer;

	public BlueDragon(EntityType<? extends BlueDragon> type, Level level) {
		super(type, level);
	}

	/** First cell above the water column at pos (the cell the surface touches), or MIN_VALUE when too deep. */
	private int findSurface(BlockPos pos) {
		BlockPos.MutableBlockPos p = pos.mutable();
		for (int i = 0; i < MAX_SCAN; i++) {
			if (!this.level().getFluidState(p).is(FluidTags.WATER)) {
				return i == 0 ? Integer.MIN_VALUE : p.getY();
			}
			p.move(0, 1, 0);
		}
		return Integer.MIN_VALUE;
	}

	@Override
	protected void settle() {
		int top = findSurface(this.blockPosition());
		if (top == Integer.MIN_VALUE) {
			// No surface in reach (deep water, under a roof): it cannot float here.
			this.discard();
			return;
		}
		this.setPos(this.getX(), top - FLOAT_DEPTH - this.getBbHeight(), this.getZ());
	}

	@Override
	protected void moveTick() {
		this.setNoGravity(true);
		if (--this.scanTimer <= 0) {
			this.scanTimer = 20;
			this.surfaceY = findSurface(this.blockPosition());
		}
		double vy;
		if (this.surfaceY == Integer.MIN_VALUE) {
			vy = 0.02; // lost the surface: rise until it is found
		} else {
			double target = this.surfaceY - FLOAT_DEPTH - this.getBbHeight();
			vy = Math.max(-0.03, Math.min(0.03, (target - this.getY()) * 0.2));
		}

		if (--this.driftTimer <= 0) {
			this.driftTimer = 100 + this.getRandom().nextInt(200);
			this.heading += (this.getRandom().nextFloat() - 0.5f) * 90.0f;
		}
		double rad = Math.toRadians(this.heading);
		double dx = -Math.sin(rad);
		double dz = Math.cos(rad);
		BlockPos ahead = BlockPos.containing(this.getX() + dx * 0.5, this.getY(), this.getZ() + dz * 0.5);
		double speed = 0.003;
		if (!this.level().getFluidState(ahead).is(FluidTags.WATER)) {
			this.heading += 180.0f;
			speed = 0.0;
		}
		Vec3 m = new Vec3(dx * speed, vy, dz * speed);
		this.setDeltaMovement(m);
		// Drifters spin slowly as they go.
		this.setYRot(this.getYRot() + 0.3f);
		this.yBodyRot = this.getYRot();
		this.setYHeadRot(this.getYRot());
	}
}
