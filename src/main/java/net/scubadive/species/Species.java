package net.scubadive.species;

import net.minecraft.resources.Identifier;

/** Every photographable subject. Display name: lang key "species.scubadive.<id>". */
public enum Species {
	// Vanilla mobs are deliberately not photographable subjects: only this mod's creatures count.
	// scubadive (phase 1)
	SEA_ANEMONE("sea_anemone", "scubadive:sea_anemone", true, 10),
	NUDIBRANCH("nudibranch", "scubadive:nudibranch", false, 40),
	PYGMY_SEAHORSE("pygmy_seahorse", "scubadive:pygmy_seahorse", false, 70),
	// phase 2
	BARRACUDA("barracuda", "scubadive:barracuda", false, 40),
	REEF_SHARK("reef_shark", "scubadive:reef_shark", false, 80),
	HAMMERHEAD_SHARK("hammerhead_shark", "scubadive:hammerhead_shark", false, 150),
	WHALE_SHARK("whale_shark", "scubadive:whale_shark", false, 250),
	BLUE_DAMSEL("blue_damsel", "scubadive:blue_damsel", false, 15),
	CHROMIS("chromis", "scubadive:chromis", false, 18),
	ANTHIAS("anthias", "scubadive:anthias", false, 25),
	BLUE_TANG("blue_tang", "scubadive:blue_tang", false, 30),
	BUTTERFLYFISH("butterflyfish", "scubadive:butterflyfish", false, 35),
	MOORISH_IDOL("moorish_idol", "scubadive:moorish_idol", false, 45),
	LIONFISH("lionfish", "scubadive:lionfish", false, 60),
	CLOWNFISH("clownfish", "scubadive:clownfish", false, 30),
	ANEMONE_SHRIMP("anemone_shrimp", "scubadive:anemone_shrimp", false, 60),
	GOBY("goby", "scubadive:goby", false, 45),
	MORAY_EEL("moray_eel", "scubadive:moray_eel", false, 55),
	FROGFISH("frogfish", "scubadive:frogfish", false, 120),
	// update-5
	PARROTFISH("parrotfish", "scubadive:parrotfish", false, 35),
	BOXFISH("boxfish", "scubadive:boxfish", false, 40),
	TRIGGERFISH("triggerfish", "scubadive:triggerfish", false, 45),
	GROUPER("grouper", "scubadive:grouper", false, 55),
	BLUESTRIPE_SNAPPER("bluestripe_snapper", "scubadive:bluestripe_snapper", false, 25),
	BIGEYE_TREVALLY("bigeye_trevally", "scubadive:bigeye_trevally", false, 50),
	MANTA_RAY("manta_ray", "scubadive:manta_ray", false, 200),
	STINGRAY("stingray", "scubadive:stingray", false, 90),
	OCTOPUS("octopus", "scubadive:octopus", false, 80),
	// critters
	BLUE_STARFISH("blue_starfish", "scubadive:blue_starfish", false, 25),
	CUSHION_STAR("cushion_star", "scubadive:cushion_star", false, 30),
	CROWN_OF_THORNS("crown_of_thorns", "scubadive:crown_of_thorns", false, 60),
	SEA_URCHIN("sea_urchin", "scubadive:sea_urchin", false, 20),
	MOON_JELLY("moon_jelly", "scubadive:moon_jelly", false, 30),
	SEA_NETTLE("sea_nettle", "scubadive:sea_nettle", false, 55),
	SPOTTED_JELLY("spotted_jelly", "scubadive:spotted_jelly", false, 45),
	CLIONE("clione", "scubadive:clione", false, 70),
	// fresh water and cold water
	RAINBOW_TROUT("rainbow_trout", "scubadive:rainbow_trout", false, 25),
	CARP("carp", "scubadive:carp", false, 20),
	CATFISH("catfish", "scubadive:catfish", false, 40),
	BLIND_CAVEFISH("blind_cavefish", "scubadive:blind_cavefish", false, 80),
	CRAYFISH("crayfish", "scubadive:crayfish", false, 30),
	LUMPSUCKER("lumpsucker", "scubadive:lumpsucker", false, 90),
	WOLF_EEL("wolf_eel", "scubadive:wolf_eel", false, 75);

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
