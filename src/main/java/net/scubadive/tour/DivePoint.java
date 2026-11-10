package net.scubadive.tour;

import java.util.List;

/**
 * Tour dive sites. Shared, read-only for all modules (owned by the orchestrator).
 * Spawn tables use entity ids; unknown ids are skipped at runtime.
 */
public enum DivePoint {
	WARM_SEA("warm_sea", 4, 6, 22, List.of(
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
			new Spawn("minecraft:turtle", 1, 1, 1))),
	ICE_FLOE("ice_floe", 6, 8, 28, List.of(
			new Spawn("minecraft:cod", 10, 4, 8), new Spawn("minecraft:salmon", 8, 3, 6),
			new Spawn("minecraft:squid", 6, 2, 4), new Spawn("minecraft:glow_squid", 3, 1, 3),
			new Spawn("scubadive:nudibranch", 8, 2, 4), new Spawn("scubadive:octopus", 3, 1, 1),
			new Spawn("scubadive:grouper", 2, 1, 1), new Spawn("minecraft:dolphin", 1, 1, 2))),
	LAKE("lake", 2, 4, 14, List.of(
			new Spawn("minecraft:salmon", 10, 3, 6), new Spawn("minecraft:axolotl", 4, 1, 2),
			new Spawn("minecraft:tadpole", 4, 2, 5), new Spawn("minecraft:frog", 2, 1, 2),
			new Spawn("scubadive:goby", 8, 2, 4), new Spawn("scubadive:nudibranch", 3, 1, 2))),
	WRECK("wreck", 6, 12, 30, List.of(
			new Spawn("scubadive:barracuda", 6, 6, 12), new Spawn("scubadive:grouper", 6, 1, 2),
			new Spawn("scubadive:moray_eel", 5, 1, 1), new Spawn("scubadive:bigeye_trevally", 4, 12, 24),
			new Spawn("scubadive:reef_shark", 3, 1, 2), new Spawn("scubadive:lionfish", 4, 1, 2),
			new Spawn("scubadive:octopus", 3, 1, 1), new Spawn("scubadive:nudibranch", 6, 2, 3),
			new Spawn("scubadive:frogfish", 2, 1, 1), new Spawn("scubadive:anthias", 5, 8, 14))),
	DROP_OFF("drop_off", 10, 10, 60, List.of(
			new Spawn("scubadive:hammerhead_shark", 4, 3, 6), new Spawn("scubadive:whale_shark", 1, 1, 1),
			new Spawn("scubadive:manta_ray", 2, 1, 2), new Spawn("scubadive:bigeye_trevally", 5, 15, 30),
			new Spawn("scubadive:barracuda", 4, 6, 12), new Spawn("scubadive:reef_shark", 4, 1, 3),
			new Spawn("scubadive:anthias", 8, 10, 16), new Spawn("scubadive:chromis", 6, 8, 14),
			new Spawn("scubadive:triggerfish", 3, 1, 2), new Spawn("scubadive:nudibranch", 4, 1, 3))),
	CENOTE("cenote", 8, 6, 30, List.of(
			new Spawn("scubadive:goby", 6, 1, 2), new Spawn("scubadive:nudibranch", 4, 1, 2),
			new Spawn("minecraft:glow_squid", 4, 1, 2), new Spawn("minecraft:axolotl", 3, 1, 2),
			new Spawn("scubadive:octopus", 1, 1, 1), new Spawn("scubadive:frogfish", 1, 1, 1)));

	public final String id;
	/** Emeralds per diver. */
	public final int cost;
	/** Typical and maximum floor depth in metres below the water surface (for builders and HUD text). */
	public final int shallowDepth;
	public final int maxDepth;
	public final List<Spawn> spawns;

	DivePoint(String id, int cost, int shallowDepth, int maxDepth, List<Spawn> spawns) {
		this.id = id;
		this.cost = cost;
		this.shallowDepth = shallowDepth;
		this.maxDepth = maxDepth;
		this.spawns = spawns;
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
