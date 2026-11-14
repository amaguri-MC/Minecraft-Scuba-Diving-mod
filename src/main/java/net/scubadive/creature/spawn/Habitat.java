package net.scubadive.creature.spawn;

import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Where each creature lives. Every spawn path follows this table: biome spawns ({@link SeaZones#addSpawn}),
 * the hotspot spawner and the tour sites. A creature may only appear where one of its habitats is present;
 * unlisted ids never spawn through these paths.
 */
public enum Habitat {
	/** Salt water, warm: coral reefs and tropical seas. */
	TROPICAL,
	/** Salt water, temperate: the ordinary ocean. */
	TEMPERATE,
	/** Salt water, cold: cold and frozen oceans, ice floes. */
	COLD,
	/** Salt water, deep: deep oceans, the trench, drop-off walls (big pelagic fish). */
	DEEP,
	/** Fresh water: rivers, lakes, swamps, caves and cenotes. */
	FRESH;

	private static final Map<String, Set<Habitat>> TABLE = new HashMap<>();

	static {
		// ---- vanilla (turtle, dolphin, nautilus, pufferfish, squid have a renewed look and are in the journal) ----
		put("minecraft:cod", COLD, TEMPERATE);
		put("minecraft:salmon", COLD, TEMPERATE, FRESH);
		put("minecraft:tropical_fish", TROPICAL);
		put("minecraft:pufferfish", TROPICAL);
		put("minecraft:turtle", TROPICAL);
		put("minecraft:dolphin", TROPICAL, TEMPERATE, COLD);   // COLD: the ice-floe tour's rare pod
		put("minecraft:squid", TEMPERATE, COLD);
		put("minecraft:nautilus", TROPICAL, DEEP);
		put("minecraft:glow_squid", DEEP, FRESH);
		put("minecraft:axolotl", FRESH);
		put("minecraft:frog", FRESH);
		put("minecraft:tadpole", FRESH);
		// ---- tropical reef ----
		for (String id : new String[] {"blue_damsel", "chromis", "anthias", "blue_tang", "butterflyfish", "moorish_idol",
				"lionfish", "parrotfish", "boxfish", "triggerfish", "bluestripe_snapper", "clownfish", "anemone_shrimp",
				"goby", "moray_eel", "frogfish", "pygmy_seahorse", "blue_starfish", "crown_of_thorns", "sea_urchin",
				"spotted_jelly", "barracuda"}) {
			put("scubadive:" + id, TROPICAL);
		}
		put("scubadive:nudibranch", TROPICAL, TEMPERATE);
		put("scubadive:octopus", TROPICAL, TEMPERATE);
		put("scubadive:spear_squid", TEMPERATE, COLD);
		put("scubadive:cuttlefish", TROPICAL, TEMPERATE);
		put("scubadive:stingray", TROPICAL, TEMPERATE);
		put("scubadive:grouper", TROPICAL, TEMPERATE);
		put("scubadive:bigeye_trevally", TROPICAL, TEMPERATE, DEEP);
		put("scubadive:reef_shark", TROPICAL, DEEP);
		put("scubadive:manta_ray", TROPICAL, DEEP);
		put("scubadive:whale_shark", TROPICAL, DEEP);
		put("scubadive:hammerhead_shark", DEEP);
		// ---- temperate ----
		put("scubadive:moon_jelly", TEMPERATE);
		put("scubadive:cushion_star", TEMPERATE, COLD);
		// ---- cold ----
		put("scubadive:sea_nettle", COLD);
		put("scubadive:clione", COLD);
		put("scubadive:lumpsucker", COLD);
		put("scubadive:wolf_eel", COLD);
		// ---- fresh water ----
		put("scubadive:rainbow_trout", FRESH);
		put("scubadive:carp", FRESH);
		put("scubadive:catfish", FRESH);
		put("scubadive:blind_cavefish", FRESH);
		put("scubadive:crayfish", FRESH);
		// ---- update-10 / 11 ----
		put("scubadive:blue_nudibranch", TROPICAL, TEMPERATE);
		put("scubadive:bornella", TROPICAL);
		put("scubadive:ceratosoma", TROPICAL);
		put("scubadive:indonesian_sea_nettle", TROPICAL);
		put("scubadive:upside_down_jelly", TROPICAL);
		put("scubadive:cannonball_jelly", TROPICAL, TEMPERATE);
		put("scubadive:flower_hat_jelly", TEMPERATE);
		put("scubadive:blue_blubber_jelly", TROPICAL);
		put("scubadive:yellow_tang", TROPICAL);
		put("scubadive:cleaner_wrasse", TROPICAL);
		put("scubadive:mandarinfish", TROPICAL);
		put("scubadive:napoleon_wrasse", TROPICAL);
		put("scubadive:garden_eel", TROPICAL);
		put("scubadive:sardine", TEMPERATE);
		put("scubadive:red_sea_bream", TEMPERATE);
		put("scubadive:striped_beakfish", TEMPERATE);
		put("scubadive:ocean_sunfish", TEMPERATE, DEEP);
		put("scubadive:arctic_char", FRESH);
		put("scubadive:box_jellyfish", TROPICAL);
		put("scubadive:comb_jelly", TEMPERATE, COLD);
		put("scubadive:lions_mane_jelly", COLD);
		put("scubadive:fried_egg_jelly", TEMPERATE);
		put("scubadive:echizen_jelly", TEMPERATE);
		put("scubadive:sea_bunny", TROPICAL, TEMPERATE);
		put("scubadive:spanish_dancer", TROPICAL);
		put("scubadive:blue_dragon", TROPICAL);
		put("scubadive:leaf_sheep", TROPICAL);
		put("scubadive:phyllidia", TROPICAL);
		put("scubadive:thecacera", TROPICAL, TEMPERATE);
	}

	private static void put(String id, Habitat first, Habitat... rest) {
		TABLE.put(id, EnumSet.of(first, rest));
	}

	/** Habitats of a creature id ("namespace:path"); empty when unlisted. */
	public static Set<Habitat> of(String id) {
		Set<Habitat> s = TABLE.get(id);
		return s == null ? EnumSet.noneOf(Habitat.class) : s;
	}

	/** True when the creature lives in at least one of the given habitats. */
	public static boolean fits(String id, Collection<Habitat> where) {
		for (Habitat h : of(id)) {
			if (where.contains(h)) {
				return true;
			}
		}
		return false;
	}

	/** True when the creature lives in this habitat. */
	public static boolean fits(String id, Habitat where) {
		return of(id).contains(where);
	}
}
