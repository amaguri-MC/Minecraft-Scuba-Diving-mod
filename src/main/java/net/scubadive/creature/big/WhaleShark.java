package net.scubadive.creature.big;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;

/** Whale shark: 10+ blocks long, very slow, turns in wide arcs and is completely harmless. */
public class WhaleShark extends GliderFish {
	public WhaleShark(EntityType<? extends WhaleShark> type, Level level) {
		super(type, level);
	}

	public static AttributeSupplier.Builder whaleSharkAttributes() {
		return Mob.createMobAttributes()
				.add(Attributes.MAX_HEALTH, 60.0)
				.add(Attributes.MOVEMENT_SPEED, 0.35);
	}

	@Override
	protected Goal createCruiseGoal() {
		// 0.10 blocks/tick, 0.6 deg/tick (about 9 block turning radius), probe 14 blocks ahead.
		return new CruiseGoal(this, 0.10, 0.6f, 0.4f, 14, 2.6f, 1.6f);
	}
}
