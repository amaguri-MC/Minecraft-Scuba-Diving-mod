package net.scubadive.creature.big;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;

/** Manta ray: a rare, harmless wide glider that flaps its wings. */
public class MantaRay extends GliderFish {
	public MantaRay(EntityType<? extends MantaRay> type, Level level) {
		super(type, level);
	}

	public static AttributeSupplier.Builder mantaAttributes() {
		return Mob.createMobAttributes()
				.add(Attributes.MAX_HEALTH, 30.0)
				.add(Attributes.MOVEMENT_SPEED, 0.5);
	}

	@Override
	protected Goal createCruiseGoal() {
		return new CruiseGoal(this, 0.13, 1.3f, 0.7f, 10, 2.2f, 0.8f);
	}
}
