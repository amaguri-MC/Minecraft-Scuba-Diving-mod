package net.scubadive.creature;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

/**
 * Bornella: a slender orange slug with finger-like cerata. It crawls the reef, and now and then lifts off and
 * swims in short bouts by bending its body from side to side (the Spanish dancer's swim, shorter and more often).
 */
public class Bornella extends SpanishDancer {
	public Bornella(EntityType<? extends Bornella> type, Level level) {
		super(type, level);
	}

	@Override
	protected int swimTime() {
		return 60 + this.getRandom().nextInt(60);
	}

	@Override
	protected int restTime() {
		return 400 + this.getRandom().nextInt(600);
	}

	@Override
	protected double swimSpeed() {
		return 0.02;
	}

	@Override
	protected double crawlSpeed() {
		return 0.005;
	}
}
