package net.scubadive.tour;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import net.scubadive.creature.spawn.Habitat;

/**
 * Tour dive sites. Shared, read-only for all modules (owned by the orchestrator).
 * Spawn tables use entity ids; unknown ids are skipped at runtime.
 */
public enum DivePoint {
	// Tropical reef: coral and anemone life. Rare: whale shark or manta passing over the sand.
	WARM_SEA("warm_sea", 4, 6, 22, EnumSet.of(Habitat.TROPICAL), List.of(
			new Spawn("scubadive:blue_damsel", 10, 6, 12), new Spawn("scubadive:chromis", 10, 8, 16),
			new Spawn("scubadive:anthias", 10, 8, 16), new Spawn("scubadive:blue_tang", 6, 2, 5),
			new Spawn("scubadive:butterflyfish", 6, 2, 3), new Spawn("scubadive:moorish_idol", 4, 1, 3),
			new Spawn("scubadive:lionfish", 3, 1, 2), new Spawn("scubadive:parrotfish", 5, 1, 3),
			new Spawn("scubadive:boxfish", 3, 1, 1), new Spawn("scubadive:triggerfish", 3, 1, 2),
			new Spawn("scubadive:bluestripe_snapper", 5, 10, 18), new Spawn("scubadive:clownfish", 8, 2, 4),
			new Spawn("scubadive:anemone_shrimp", 4, 1, 3), new Spawn("scubadive:nudibranch", 10, 2, 4),
			new Spawn("scubadive:pygmy_seahorse", 3, 1, 1), new Spawn("scubadive:frogfish", 1, 1, 1),
			new Spawn("scubadive:octopus", 2, 1, 1), new Spawn("scubadive:reef_shark", 2, 1, 2),
			new Spawn("scubadive:moray_eel", 3, 1, 1), new Spawn("scubadive:goby", 4, 1, 3),
			new Spawn("scubadive:stingray", 2, 1, 1), new Spawn("minecraft:tropical_fish", 6, 3, 6),
			new Spawn("minecraft:turtle", 1, 1, 1), new Spawn("scubadive:blue_starfish", 4, 1, 2),
			new Spawn("scubadive:crown_of_thorns", 2, 1, 1), new Spawn("scubadive:sea_urchin", 4, 2, 4),
			new Spawn("scubadive:spotted_jelly", 3, 2, 4), new Spawn("minecraft:pufferfish", 2, 1, 2)),
			List.of(new Spawn("scubadive:whale_shark", 1, 1, 1), new Spawn("scubadive:manta_ray", 2, 1, 2))),
	// Cold sea under the floe: cold-water species only, with many cliones and lumpsuckers. Rare: a wolf eel
	// out of its crevice, or a pod of dolphins under the ice.
	ICE_FLOE("ice_floe", 6, 8, 28, EnumSet.of(Habitat.COLD), List.of(
			new Spawn("minecraft:cod", 10, 4, 8), new Spawn("minecraft:salmon", 8, 3, 6),
			new Spawn("scubadive:clione", 10, 2, 5), new Spawn("scubadive:lumpsucker", 6, 1, 2),
			new Spawn("minecraft:squid", 4, 2, 4), new Spawn("scubadive:sea_nettle", 5, 1, 3),
			new Spawn("scubadive:cushion_star", 6, 1, 3)),
			List.of(new Spawn("scubadive:wolf_eel", 3, 1, 1), new Spawn("minecraft:dolphin", 1, 2, 3))),
	// Fresh-water lake: trout, carp, catfish on the bottom, crayfish, frogs. Rare: a big gathering of axolotls.
	LAKE("lake", 2, 4, 14, EnumSet.of(Habitat.FRESH), List.of(
			new Spawn("scubadive:rainbow_trout", 10, 1, 3), new Spawn("scubadive:carp", 8, 2, 4),
			new Spawn("minecraft:salmon", 4, 2, 4), new Spawn("scubadive:catfish", 3, 1, 1),
			new Spawn("scubadive:crayfish", 6, 1, 3), new Spawn("minecraft:tadpole", 4, 2, 5),
			new Spawn("minecraft:frog", 2, 1, 2), new Spawn("minecraft:axolotl", 2, 1, 2)),
			List.of(new Spawn("minecraft:axolotl", 1, 3, 5))),
	// Wreck on sand: predators and crevice dwellers around the timbers. Rare: hammerheads.
	WRECK("wreck", 6, 12, 30, EnumSet.of(Habitat.TROPICAL, Habitat.TEMPERATE, Habitat.DEEP), List.of(
			new Spawn("scubadive:barracuda", 6, 6, 12), new Spawn("scubadive:grouper", 6, 1, 2),
			new Spawn("scubadive:moray_eel", 5, 1, 1), new Spawn("scubadive:bigeye_trevally", 4, 12, 24),
			new Spawn("scubadive:reef_shark", 3, 1, 2), new Spawn("scubadive:lionfish", 4, 1, 2),
			new Spawn("scubadive:octopus", 3, 1, 1), new Spawn("scubadive:nudibranch", 6, 2, 3),
			new Spawn("scubadive:frogfish", 2, 1, 1), new Spawn("scubadive:anthias", 5, 8, 14),
			new Spawn("scubadive:sea_urchin", 3, 2, 4), new Spawn("scubadive:moon_jelly", 3, 3, 6),
			new Spawn("scubadive:stingray", 2, 1, 1)),
			List.of(new Spawn("scubadive:hammerhead_shark", 1, 2, 4))),
	// Warm reef shelf above a deep wall: reef fish on top, pelagic schools and sharks off the edge.
	// Rare: whale shark or manta along the wall.
	DROP_OFF("drop_off", 10, 10, 60, EnumSet.of(Habitat.TROPICAL, Habitat.DEEP), List.of(
			new Spawn("scubadive:hammerhead_shark", 3, 3, 6), new Spawn("scubadive:bigeye_trevally", 6, 15, 30),
			new Spawn("scubadive:barracuda", 4, 6, 12), new Spawn("scubadive:reef_shark", 4, 1, 3),
			new Spawn("scubadive:anthias", 8, 10, 16), new Spawn("scubadive:chromis", 6, 8, 14),
			new Spawn("scubadive:triggerfish", 3, 1, 2), new Spawn("scubadive:nudibranch", 4, 1, 3),
			new Spawn("scubadive:blue_tang", 3, 2, 4), new Spawn("scubadive:butterflyfish", 3, 2, 2),
			new Spawn("scubadive:clownfish", 3, 2, 3), new Spawn("scubadive:moray_eel", 2, 1, 1),
			new Spawn("scubadive:spotted_jelly", 3, 2, 4), new Spawn("minecraft:turtle", 1, 1, 1)),
			List.of(new Spawn("scubadive:whale_shark", 1, 1, 1), new Spawn("scubadive:manta_ray", 2, 1, 2))),
	// Fresh-water cavern: eyeless cave fish in the dark, crayfish, axolotls, glow squid. Rare: a big school
	// of blind cave fish.
	CENOTE("cenote", 8, 6, 30, EnumSet.of(Habitat.FRESH), List.of(
			new Spawn("scubadive:blind_cavefish", 8, 4, 8), new Spawn("scubadive:crayfish", 4, 1, 2),
			new Spawn("minecraft:glow_squid", 4, 1, 2), new Spawn("minecraft:axolotl", 3, 1, 2),
			new Spawn("scubadive:catfish", 2, 1, 1), new Spawn("minecraft:tadpole", 1, 2, 3)),
			List.of(new Spawn("scubadive:blind_cavefish", 1, 12, 18)));

	public final String id;
	/** Emeralds per diver. */
	public final int cost;
	/** Typical and maximum floor depth in metres below the water surface (for builders and HUD text). */
	public final int shallowDepth;
	public final int maxDepth;
	/** Kinds of water at this site; the tour spawner only lets in species living there (see Habitat). */
	public final Set<Habitat> habitats;
	public final List<Spawn> spawns;
	/** Rare visitors: shown once or twice per session, a few minutes in (see TourSpawner). */
	public final List<Spawn> rares;

	DivePoint(String id, int cost, int shallowDepth, int maxDepth, Set<Habitat> habitats, List<Spawn> spawns, List<Spawn> rares) {
		this.habitats = habitats;
		this.id = id;
		this.cost = cost;
		this.shallowDepth = shallowDepth;
		this.maxDepth = maxDepth;
		this.spawns = spawns;
		this.rares = rares;
	}

	public String langKey() {
		return "tour.scubadive.point." + id;
	}

	public static DivePoint byId(String id) {
		for (DivePoint p : values()) {
			if (p.id.equals(id)) {
				return p;
			}
		}
		return null;
	}

	public record Spawn(String entityId, int weight, int min, int max) {
	}
}
