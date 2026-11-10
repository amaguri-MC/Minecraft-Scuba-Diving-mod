package net.scubadive.creature.critter;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** Long-spined sea urchin (Diadema): sits on rock and sand; stepping on or brushing it hurts. */
public class SeaUrchin extends Mob {

	/** Water creatures: the default Mob check refuses positions inside liquid, which would block natural spawns. */
	@Override
	public boolean checkSpawnObstruction(net.minecraft.world.level.LevelReader level) {
		return level.isUnobstructed(this);
	}
	private final TouchHazard hazard = new TouchHazard(15);

	public SeaUrchin(EntityType<? extends SeaUrchin> type, Level level) {
		super(type, level);
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
		if (this.level().isClientSide()) {
			return;
		}
		for (Player p : this.hazard.touching(this, 0.05)) {
			TouchHazard.prick(this, p, 2.0f);
		}
		Vec3 m = this.getDeltaMovement();
		this.setDeltaMovement(0.0, m.y, 0.0);
	}
}
