package net.scubadive.species;

import net.minecraft.resources.Identifier;

/** Every photographable subject. Display name: lang key "species.scubadive.<id>". */
public enum Species {
	// Vanilla mobs are not photographable subjects, except the five whose look this mod renews
	// (client/creature/vanilla): they are listed under their vanilla entity ids.
	TURTLE("turtle", "minecraft:turtle", false, 90, Category.BIG, Hint.REEF, 100),
	DOLPHIN("dolphin", "minecraft:dolphin", false, 110, Category.BIG, Hint.OPEN, 250),
	NAUTILUS("nautilus", "minecraft:nautilus", false, 120, Category.CEPHALOPOD, Hint.DEEP, 20),
	PUFFERFISH("pufferfish", "minecraft:pufferfish", false, 40, Category.REEF_FISH, Hint.REEF, 25),
	SQUID("squid", "minecraft:squid", false, 45, Category.CEPHALOPOD, Hint.TEMPERATE, 45),
	// scubadive (phase 1)
	SEA_ANEMONE("sea_anemone", "scubadive:sea_anemone", true, 10, Category.INVERT, Hint.ANEMONE, 30),
	NUDIBRANCH("nudibranch", "scubadive:nudibranch", false, 40, Category.NUDIBRANCH, Hint.REEF, 4),
	PYGMY_SEAHORSE("pygmy_seahorse", "scubadive:pygmy_seahorse", false, 70, Category.BOTTOM, Hint.SEA_FAN, 2),
	// phase 2
	BARRACUDA("barracuda", "scubadive:barracuda", false, 40, Category.BIG, Hint.OPEN, 150),
	REEF_SHARK("reef_shark", "scubadive:reef_shark", false, 80, Category.BIG, Hint.REEF, 160),
	HAMMERHEAD_SHARK("hammerhead_shark", "scubadive:hammerhead_shark", false, 150, Category.BIG, Hint.OPEN, 400),
	WHALE_SHARK("whale_shark", "scubadive:whale_shark", false, 250, Category.BIG, Hint.OPEN, 1200),
	BLUE_DAMSEL("blue_damsel", "scubadive:blue_damsel", false, 15, Category.REEF_FISH, Hint.REEF, 7),
	CHROMIS("chromis", "scubadive:chromis", false, 18, Category.REEF_FISH, Hint.REEF, 8),
	ANTHIAS("anthias", "scubadive:anthias", false, 25, Category.REEF_FISH, Hint.DROP_OFF, 10),
	BLUE_TANG("blue_tang", "scubadive:blue_tang", false, 30, Category.REEF_FISH, Hint.REEF, 25),
	BUTTERFLYFISH("butterflyfish", "scubadive:butterflyfish", false, 35, Category.REEF_FISH, Hint.REEF, 15),
	MOORISH_IDOL("moorish_idol", "scubadive:moorish_idol", false, 45, Category.REEF_FISH, Hint.REEF, 20),
	LIONFISH("lionfish", "scubadive:lionfish", false, 60, Category.REEF_FISH, Hint.REEF, 35),
	CLOWNFISH("clownfish", "scubadive:clownfish", false, 30, Category.REEF_FISH, Hint.ANEMONE, 10),
	ANEMONE_SHRIMP("anemone_shrimp", "scubadive:anemone_shrimp", false, 60, Category.INVERT, Hint.ANEMONE, 2),
	GOBY("goby", "scubadive:goby", false, 45, Category.BOTTOM, Hint.SAND, 6),
	MORAY_EEL("moray_eel", "scubadive:moray_eel", false, 55, Category.BOTTOM, Hint.ROCK, 150),
	FROGFISH("frogfish", "scubadive:frogfish", false, 120, Category.BOTTOM, Hint.REEF, 15),
	// update-5
	PARROTFISH("parrotfish", "scubadive:parrotfish", false, 35, Category.REEF_FISH, Hint.REEF, 50),
	BOXFISH("boxfish", "scubadive:boxfish", false, 40, Category.REEF_FISH, Hint.REEF, 25),
	TRIGGERFISH("triggerfish", "scubadive:triggerfish", false, 45, Category.REEF_FISH, Hint.REEF, 40),
	GROUPER("grouper", "scubadive:grouper", false, 55, Category.REEF_FISH, Hint.ROCK, 80),
	BLUESTRIPE_SNAPPER("bluestripe_snapper", "scubadive:bluestripe_snapper", false, 25, Category.REEF_FISH, Hint.REEF, 30),
	BIGEYE_TREVALLY("bigeye_trevally", "scubadive:bigeye_trevally", false, 50, Category.BIG, Hint.DROP_OFF, 70),
	MANTA_RAY("manta_ray", "scubadive:manta_ray", false, 200, Category.BIG, Hint.OPEN, 450),
	STINGRAY("stingray", "scubadive:stingray", false, 90, Category.BIG, Hint.SAND, 120),
	OCTOPUS("octopus", "scubadive:octopus", false, 80, Category.CEPHALOPOD, Hint.ROCK, 60),
	// critters
	BLUE_STARFISH("blue_starfish", "scubadive:blue_starfish", false, 25, Category.INVERT, Hint.REEF, 30),
	CUSHION_STAR("cushion_star", "scubadive:cushion_star", false, 30, Category.INVERT, Hint.TEMPERATE, 25),
	CROWN_OF_THORNS("crown_of_thorns", "scubadive:crown_of_thorns", false, 60, Category.INVERT, Hint.REEF, 40),
	SEA_URCHIN("sea_urchin", "scubadive:sea_urchin", false, 20, Category.INVERT, Hint.ROCK, 20),
	MOON_JELLY("moon_jelly", "scubadive:moon_jelly", false, 30, Category.JELLY, Hint.TEMPERATE, 25),
	SEA_NETTLE("sea_nettle", "scubadive:sea_nettle", false, 55, Category.JELLY, Hint.COLD, 50),
	SPOTTED_JELLY("spotted_jelly", "scubadive:spotted_jelly", false, 45, Category.JELLY, Hint.REEF, 40),
	CLIONE("clione", "scubadive:clione", false, 70, Category.JELLY, Hint.ICE, 3),
	// fresh water and cold water
	RAINBOW_TROUT("rainbow_trout", "scubadive:rainbow_trout", false, 25, Category.FRESH, Hint.RIVER, 50),
	CARP("carp", "scubadive:carp", false, 20, Category.FRESH, Hint.LAKE, 60),
	CATFISH("catfish", "scubadive:catfish", false, 40, Category.FRESH, Hint.LAKE, 80),
	BLIND_CAVEFISH("blind_cavefish", "scubadive:blind_cavefish", false, 80, Category.FRESH, Hint.CAVE, 9),
	CRAYFISH("crayfish", "scubadive:crayfish", false, 30, Category.FRESH, Hint.RIVER, 10),
	LUMPSUCKER("lumpsucker", "scubadive:lumpsucker", false, 90, Category.COLD, Hint.COLD, 15),
	WOLF_EEL("wolf_eel", "scubadive:wolf_eel", false, 75, Category.COLD, Hint.COLD, 200),
	// cephalopods
	SPEAR_SQUID("spear_squid", "scubadive:spear_squid", false, 45, Category.CEPHALOPOD, Hint.TEMPERATE, 40),
	CUTTLEFISH("cuttlefish", "scubadive:cuttlefish", false, 70, Category.CEPHALOPOD, Hint.TEMPERATE, 30),
	// update-10: more fish, jellies and nudibranchs
	YELLOW_TANG("yellow_tang", "scubadive:yellow_tang", false, 30, Category.REEF_FISH, Hint.REEF, 20),
	CLEANER_WRASSE("cleaner_wrasse", "scubadive:cleaner_wrasse", false, 35, Category.REEF_FISH, Hint.REEF, 10),
	MANDARINFISH("mandarinfish", "scubadive:mandarinfish", false, 110, Category.BOTTOM, Hint.REEF, 6),
	NAPOLEON_WRASSE("napoleon_wrasse", "scubadive:napoleon_wrasse", false, 160, Category.BIG, Hint.DROP_OFF, 150),
	GARDEN_EEL("garden_eel", "scubadive:garden_eel", false, 70, Category.BOTTOM, Hint.SAND, 40),
	SARDINE("sardine", "scubadive:sardine", false, 20, Category.COLD, Hint.TEMPERATE, 20),
	RED_SEA_BREAM("red_sea_bream", "scubadive:red_sea_bream", false, 40, Category.COLD, Hint.TEMPERATE, 60),
	STRIPED_BEAKFISH("striped_beakfish", "scubadive:striped_beakfish", false, 45, Category.COLD, Hint.ROCK, 50),
	OCEAN_SUNFISH("ocean_sunfish", "scubadive:ocean_sunfish", false, 180, Category.BIG, Hint.OPEN, 250),
	ARCTIC_CHAR("arctic_char", "scubadive:arctic_char", false, 50, Category.FRESH, Hint.LAKE, 45),
	BOX_JELLYFISH("box_jellyfish", "scubadive:box_jellyfish", false, 70, Category.JELLY, Hint.REEF, 25),
	COMB_JELLY("comb_jelly", "scubadive:comb_jelly", false, 50, Category.JELLY, Hint.TEMPERATE, 8),
	LIONS_MANE_JELLY("lions_mane_jelly", "scubadive:lions_mane_jelly", false, 90, Category.JELLY, Hint.COLD, 100),
	FRIED_EGG_JELLY("fried_egg_jelly", "scubadive:fried_egg_jelly", false, 60, Category.JELLY, Hint.TEMPERATE, 35),
	ECHIZEN_JELLY("echizen_jelly", "scubadive:echizen_jelly", false, 220, Category.JELLY, Hint.OPEN, 200),
	SEA_BUNNY("sea_bunny", "scubadive:sea_bunny", false, 80, Category.NUDIBRANCH, Hint.REEF, 2),
	SPANISH_DANCER("spanish_dancer", "scubadive:spanish_dancer", false, 90, Category.NUDIBRANCH, Hint.REEF, 30),
	BLUE_DRAGON("blue_dragon", "scubadive:blue_dragon", false, 100, Category.NUDIBRANCH, Hint.SURFACE, 3),
	LEAF_SHEEP("leaf_sheep", "scubadive:leaf_sheep", false, 120, Category.NUDIBRANCH, Hint.REEF, 1),
	PHYLLIDIA("phyllidia", "scubadive:phyllidia", false, 40, Category.NUDIBRANCH, Hint.ROCK, 5),
	THECACERA("thecacera", "scubadive:thecacera", false, 70, Category.NUDIBRANCH, Hint.REEF, 2),
	// update-11: more nudibranchs and aquarium jellies
	BLUE_NUDIBRANCH("blue_nudibranch", "scubadive:blue_nudibranch", false, 35, Category.NUDIBRANCH, Hint.ROCK, 3),
	BORNELLA("bornella", "scubadive:bornella", false, 90, Category.NUDIBRANCH, Hint.REEF, 6),
	CERATOSOMA("ceratosoma", "scubadive:ceratosoma", false, 60, Category.NUDIBRANCH, Hint.REEF, 12),
	INDONESIAN_SEA_NETTLE("indonesian_sea_nettle", "scubadive:indonesian_sea_nettle", false, 60, Category.JELLY, Hint.OPEN, 30),
	UPSIDE_DOWN_JELLY("upside_down_jelly", "scubadive:upside_down_jelly", false, 55, Category.JELLY, Hint.SAND, 20),
	CANNONBALL_JELLY("cannonball_jelly", "scubadive:cannonball_jelly", false, 50, Category.JELLY, Hint.TEMPERATE, 20),
	FLOWER_HAT_JELLY("flower_hat_jelly", "scubadive:flower_hat_jelly", false, 85, Category.JELLY, Hint.TEMPERATE, 15),
	BLUE_BLUBBER_JELLY("blue_blubber_jelly", "scubadive:blue_blubber_jelly", false, 50, Category.JELLY, Hint.OPEN, 35),
	GENTOO_PENGUIN("gentoo_penguin", "scubadive:gentoo_penguin", false, 70, Category.COLD, Hint.ICE, 75),
	ROCKHOPPER_PENGUIN("rockhopper_penguin", "scubadive:rockhopper_penguin", false, 80, Category.COLD, Hint.ROCK, 55),
	CAPE_PENGUIN("cape_penguin", "scubadive:cape_penguin", false, 75, Category.COLD, Hint.TEMPERATE, 65);

	/** Journal tab a species is listed under. */
	public enum Category {
		REEF_FISH, BIG, BOTTOM, CEPHALOPOD, NUDIBRANCH, JELLY, INVERT, COLD, FRESH;

		public String langKey() {
			return "species.scubadive.category." + name().toLowerCase(java.util.Locale.ROOT);
		}
	}

	/** Where to look: shown as a hint for species not photographed yet. */
	public enum Hint {
		REEF, ANEMONE, SEA_FAN, SAND, ROCK, DROP_OFF, OPEN, DEEP, TEMPERATE, KELP, COLD, ICE, SURFACE, RIVER, LAKE, CAVE;

		public String langKey() {
			return "species.scubadive.habitat." + name().toLowerCase(java.util.Locale.ROOT);
		}
	}

	public final String id;
	public final Identifier target;
	public final boolean isBlock;
	public final int basePoints;
	public final Category category;
	public final Hint habitat;
	/** Typical adult length in centimetres (journal hint). */
	public final int lengthCm;

	Species(String id, String target, boolean isBlock, int basePoints, Category category, Hint habitat, int lengthCm) {
		this.id = id;
		this.target = Identifier.parse(target);
		this.isBlock = isBlock;
		this.basePoints = basePoints;
		this.category = category;
		this.habitat = habitat;
		this.lengthCm = lengthCm;
	}

	public String langKey() {
		return "species.scubadive." + id;
	}

	/** Rarity 1-5 from the photo points (rarer species are worth more). */
	public int rarity() {
		if (basePoints < 30) return 1;
		if (basePoints < 50) return 2;
		if (basePoints < 80) return 3;
		if (basePoints < 150) return 4;
		return 5;
	}

	/** Rarity as stars, e.g. ★★★☆☆. */
	public String rarityStars() {
		return "★".repeat(rarity()) + "☆".repeat(5 - rarity());
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
