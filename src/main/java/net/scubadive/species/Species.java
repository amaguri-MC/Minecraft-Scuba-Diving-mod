package net.scubadive.species;

import net.minecraft.resources.Identifier;

/** Every photographable subject. Display name: lang key "species.scubadive.<id>". */
public enum Species {
	// vanilla
	COD("cod", "minecraft:cod", false, 5),
	SALMON("salmon", "minecraft:salmon", false, 5),
	TROPICAL_FISH("tropical_fish", "minecraft:tropical_fish", false, 10),
	PUFFERFISH("pufferfish", "minecraft:pufferfish", false, 15),
	SQUID("squid", "minecraft:squid", false, 10),
	GLOW_SQUID("glow_squid", "minecraft:glow_squid", false, 25),
	DOLPHIN("dolphin", "minecraft:dolphin", false, 20),
	TURTLE("turtle", "minecraft:turtle", false, 25),
	AXOLOTL("axolotl", "minecraft:axolotl", false, 30),
	GUARDIAN("guardian", "minecraft:guardian", false, 35),
	ELDER_GUARDIAN("elder_guardian", "minecraft:elder_guardian", false, 120),
	// scubadive (phase 1)
	SEA_ANEMONE("sea_anemone", "scubadive:sea_anemone", true, 10),
	NUDIBRANCH("nudibranch", "scubadive:nudibranch", false, 40),
	PYGMY_SEAHORSE("pygmy_seahorse", "scubadive:pygmy_seahorse", false, 70);

	public final String id;
	public final Identifier target;
	public final boolean isBlock;
	public final int basePoints;

	Species(String id, String target, boolean isBlock, int basePoints) {
		this.id = id;
		this.target = Identifier.parse(target);
		this.isBlock = isBlock;
		this.basePoints = basePoints;
	}

	public String langKey() {
		return "species.scubadive." + id;
	}

	public static Species byTarget(Identifier target, boolean isBlock) {
		for (Species s : values()) if (s.isBlock == isBlock && s.target.equals(target)) return s;
		return null;
	}

	public static Species byId(String id) {
		for (Species s : values()) if (s.id.equals(id)) return s;
		return null;
	}
}
