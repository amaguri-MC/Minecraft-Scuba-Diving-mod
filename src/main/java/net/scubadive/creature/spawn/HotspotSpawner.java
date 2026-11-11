package net.scubadive.creature.spawn;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.animal.fish.WaterAnimal;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.scubadive.ScubaDive;
import net.scubadive.tour.TourApi;

/**
 * Dense fish spawning around reefs and other hotspots, and a lighter background of life in the open sea.
 * Tables depend on the water temperature of the biome (see {@link SeaZones}), like the biome spawns.
 * Cheap by design: a few random probes per player every couple of seconds; the tour dimension is skipped
 * (the tour spawner fills each site itself).
 */
public final class HotspotSpawner {
	// Tuning constants
	private static final int INTERVAL_TICKS = 40;
	private static final int PROBES_PER_PLAYER = 4;
	private static final int MIN_RANGE = 20;
	private static final int MAX_RANGE = 40;
	private static final int VERTICAL_RANGE = 16;
	private static final int MAX_FLOOR_SEARCH = 24;
	private static final int MARKER_RADIUS = 3;
	private static final int FISH_CAP = 72;
	private static final double CAP_RADIUS = 32.0;
	/** Large or rare animals: at most one group of the kind around the spot. */
	private static final double SOLO_RADIUS = 48.0;
	private static final double MIN_PLAYER_DISTANCE = 8.0;
	private static final int SCATTER = 3;
	private static final int SAND_MIN = 12;
	private static final int ROCK_MIN = 4;
	private static final int PLACE_TRIES = 3;
	/** Placed on the sea floor. */
	private static final Set<String> BOTTOM_DWELLERS = Set.of(
			"scubadive:nudibranch", "scubadive:octopus", "scubadive:goby", "scubadive:stingray",
			"scubadive:moray_eel", "scubadive:frogfish",
			"scubadive:blue_starfish", "scubadive:cushion_star", "scubadive:crown_of_thorns", "scubadive:sea_urchin",
			"scubadive:crayfish", "scubadive:catfish", "scubadive:lumpsucker", "minecraft:tadpole",
			"scubadive:sea_bunny", "scubadive:spanish_dancer", "scubadive:leaf_sheep", "scubadive:phyllidia", "scubadive:thecacera", "scubadive:garden_eel");
	/** Their own placement rule must pass (anemone close by, right kind of floor, crevice...). */
	private static final Set<String> CHECKED = Set.of(
			"scubadive:nudibranch", "scubadive:octopus", "scubadive:goby", "scubadive:stingray",
			"scubadive:moray_eel", "scubadive:frogfish", "scubadive:pygmy_seahorse",
			"scubadive:blue_starfish", "scubadive:cushion_star", "scubadive:crown_of_thorns", "scubadive:sea_urchin",
			"scubadive:clownfish", "scubadive:anemone_shrimp", "scubadive:wolf_eel", "scubadive:crayfish",
			"scubadive:catfish", "scubadive:lumpsucker",
			"scubadive:sea_bunny", "scubadive:spanish_dancer", "scubadive:leaf_sheep", "scubadive:phyllidia", "scubadive:thecacera");
	private static final Set<String> SOLO = Set.of(
			"scubadive:whale_shark", "scubadive:manta_ray", "scubadive:hammerhead_shark", "scubadive:reef_shark",
			"scubadive:frogfish", "scubadive:pygmy_seahorse", "scubadive:barracuda", "scubadive:wolf_eel",
			"scubadive:spanish_dancer", "scubadive:leaf_sheep", "scubadive:mandarinfish", "scubadive:napoleon_wrasse",
			"scubadive:ocean_sunfish", "scubadive:echizen_jelly", "scubadive:lions_mane_jelly");

	private static final Identifier ANEMONE = ScubaDive.id("sea_anemone");

	/** One spawn table row: entity id, weight, group size range. */
	private record Entry(String id, int weight, int minGroup, int maxGroup) {
	}

	private static Entry e(String id, int weight, int min, int max) {
		return new Entry(id, weight, min, max);
	}

	private enum Hotspot {
		WRECK, ANEMONE, REEF, ROCK, SAND, TRENCH,
		/** Plain floor (gravel, mud, clay...) or no floor in reach: mid-water life. */
		OPEN
	}

	// ---- warm and lukewarm water: coral reef and tropical life ----
	private static final List<Entry> WARM_REEF = List.of(
			e("scubadive:blue_damsel", 10, 6, 12), e("scubadive:chromis", 10, 8, 16), e("scubadive:anthias", 10, 8, 16),
			e("scubadive:blue_tang", 6, 2, 4), e("scubadive:butterflyfish", 5, 2, 2), e("scubadive:moorish_idol", 3, 1, 2),
			e("scubadive:lionfish", 3, 1, 2), e("scubadive:parrotfish", 5, 1, 3), e("scubadive:boxfish", 3, 1, 1),
			e("scubadive:triggerfish", 3, 1, 2), e("scubadive:bluestripe_snapper", 5, 10, 18),
			e("minecraft:tropical_fish", 6, 3, 6), e("scubadive:nudibranch", 10, 2, 4), e("scubadive:pygmy_seahorse", 2, 1, 1),
			e("scubadive:octopus", 2, 1, 1), e("scubadive:cuttlefish", 2, 1, 2), e("scubadive:blue_starfish", 4, 1, 2), e("scubadive:crown_of_thorns", 2, 1, 1),
			e("scubadive:sea_urchin", 4, 2, 4), e("scubadive:spotted_jelly", 2, 2, 3), e("scubadive:frogfish", 1, 1, 1),
			e("scubadive:yellow_tang", 5, 2, 4), e("scubadive:cleaner_wrasse", 3, 1, 1), e("scubadive:mandarinfish", 1, 1, 1),
			e("scubadive:sea_bunny", 3, 1, 2), e("scubadive:thecacera", 2, 1, 2), e("scubadive:spanish_dancer", 1, 1, 1),
			e("scubadive:leaf_sheep", 1, 1, 1), e("scubadive:box_jellyfish", 1, 1, 2));
	private static final List<Entry> ANEMONE_TABLE = List.of(
			e("scubadive:clownfish", 24, 2, 4), e("scubadive:anemone_shrimp", 6, 1, 3), e("scubadive:nudibranch", 4, 1, 2));
	private static final List<Entry> WARM_ROCK = List.of(
			e("scubadive:grouper", 4, 1, 2), e("scubadive:moray_eel", 5, 1, 1), e("scubadive:goby", 3, 1, 3),
			e("scubadive:lionfish", 3, 1, 2), e("scubadive:blue_tang", 4, 2, 4), e("scubadive:triggerfish", 2, 1, 2),
			e("scubadive:nudibranch", 8, 2, 4), e("scubadive:octopus", 3, 1, 1), e("scubadive:spear_squid", 3, 5, 10), e("scubadive:sea_urchin", 4, 2, 4),
			e("scubadive:frogfish", 1, 1, 1), e("scubadive:phyllidia", 4, 1, 2), e("scubadive:cleaner_wrasse", 2, 1, 1));
	private static final List<Entry> WARM_SAND = List.of(
			e("scubadive:stingray", 3, 1, 1), e("scubadive:goby", 6, 1, 3), e("scubadive:blue_starfish", 3, 1, 2),
			e("minecraft:tropical_fish", 4, 3, 6), e("scubadive:bluestripe_snapper", 2, 8, 14),
			e("scubadive:garden_eel", 3, 1, 1));
	private static final List<Entry> WARM_OPEN = List.of(
			e("scubadive:bluestripe_snapper", 4, 8, 14), e("scubadive:anthias", 3, 6, 10), e("scubadive:bigeye_trevally", 3, 10, 20),
			e("minecraft:tropical_fish", 6, 3, 6), e("scubadive:spotted_jelly", 3, 2, 3), e("scubadive:barracuda", 2, 3, 6),
			e("scubadive:reef_shark", 1, 1, 1), e("scubadive:napoleon_wrasse", 1, 1, 1), e("scubadive:blue_dragon", 2, 2, 4),
			e("scubadive:box_jellyfish", 2, 1, 2));

	// ---- ordinary ocean: temperate life ----
	private static final List<Entry> TEMPERATE_ROCK = List.of(
			e("scubadive:grouper", 6, 1, 2), e("scubadive:octopus", 5, 1, 1), e("scubadive:nudibranch", 3, 1, 2),
			e("scubadive:cushion_star", 4, 1, 2), e("minecraft:cod", 6, 3, 6), e("scubadive:bigeye_trevally", 2, 8, 14),
			e("scubadive:striped_beakfish", 4, 1, 1), e("scubadive:red_sea_bream", 4, 2, 5), e("scubadive:sea_bunny", 1, 1, 2),
			e("scubadive:thecacera", 2, 1, 2));
	private static final List<Entry> TEMPERATE_SAND = List.of(
			e("scubadive:cushion_star", 4, 1, 2), e("scubadive:stingray", 2, 1, 1), e("minecraft:cod", 5, 3, 6),
			e("scubadive:bigeye_trevally", 3, 8, 14), e("scubadive:moon_jelly", 2, 2, 4),
			e("scubadive:red_sea_bream", 3, 2, 5));
	private static final List<Entry> TEMPERATE_OPEN = List.of(
			e("scubadive:bigeye_trevally", 6, 12, 24), e("scubadive:moon_jelly", 6, 3, 6), e("minecraft:cod", 6, 3, 6),
			e("scubadive:grouper", 2, 1, 1), e("minecraft:squid", 2, 1, 3),
			e("scubadive:sardine", 5, 15, 30), e("scubadive:ocean_sunfish", 1, 1, 1), e("scubadive:comb_jelly", 3, 2, 4),
			e("scubadive:fried_egg_jelly", 2, 1, 2), e("scubadive:echizen_jelly", 1, 1, 1));

	// ---- cold and frozen oceans: cold-water life ----
	private static final List<Entry> COLD_FLOOR = List.of(
			e("scubadive:cushion_star", 6, 1, 3), e("minecraft:cod", 6, 3, 6), e("minecraft:salmon", 4, 3, 5),
			e("scubadive:sea_nettle", 2, 1, 2), e("scubadive:clione", 5, 2, 4), e("scubadive:lumpsucker", 3, 1, 1),
			e("scubadive:wolf_eel", 2, 1, 1), e("scubadive:comb_jelly", 3, 2, 4));
	private static final List<Entry> COLD_OPEN = List.of(
			e("minecraft:cod", 8, 3, 6), e("minecraft:salmon", 6, 3, 6), e("scubadive:sea_nettle", 4, 1, 3),
			e("scubadive:clione", 8, 2, 5), e("minecraft:squid", 2, 1, 3),
			e("scubadive:lions_mane_jelly", 1, 1, 1), e("scubadive:comb_jelly", 3, 2, 4));

	// ---- deep water and wrecks: big fish ----
	private static final List<Entry> DEEP = List.of(
			e("scubadive:hammerhead_shark", 3, 2, 5), e("scubadive:bigeye_trevally", 6, 12, 24), e("scubadive:manta_ray", 2, 1, 1),
			e("scubadive:reef_shark", 2, 1, 2), e("scubadive:whale_shark", 1, 1, 1), e("minecraft:glow_squid", 4, 2, 4),
			e("scubadive:ocean_sunfish", 1, 1, 1));
	private static final List<Entry> COLD_DEEP = List.of(
			e("minecraft:cod", 6, 4, 8), e("minecraft:salmon", 4, 3, 6), e("scubadive:clione", 6, 3, 6),
			e("scubadive:sea_nettle", 3, 1, 3), e("scubadive:wolf_eel", 1, 1, 1));

	// ---- fresh water: rivers, lakes, swamps and flooded caves ----
	private static final List<Entry> FRESH_FLOOR = List.of(
			e("scubadive:crayfish", 6, 1, 2), e("scubadive:catfish", 3, 1, 1), e("scubadive:carp", 4, 2, 4),
			e("scubadive:rainbow_trout", 4, 1, 3), e("minecraft:salmon", 2, 2, 4), e("minecraft:tadpole", 2, 2, 4));
	private static final List<Entry> FRESH_OPEN = List.of(
			e("scubadive:rainbow_trout", 6, 1, 3), e("scubadive:carp", 5, 2, 4), e("minecraft:salmon", 3, 2, 4));
	private static final List<Entry> FRESH_CAVE = List.of(
			e("scubadive:blind_cavefish", 6, 6, 12), e("scubadive:crayfish", 2, 1, 2), e("minecraft:glow_squid", 3, 1, 3),
			e("minecraft:axolotl", 2, 1, 2));
	private static final List<Entry> WRECK_TABLE = List.of(
			e("scubadive:barracuda", 6, 3, 6), e("scubadive:bigeye_trevally", 6, 12, 24), e("scubadive:grouper", 5, 1, 2),
			e("scubadive:reef_shark", 3, 1, 2), e("scubadive:moray_eel", 3, 1, 1));

	/** The table for a hotspot in a water-temperature zone. */
	private static List<Entry> table(Hotspot hotspot, SeaZones.Zone zone, boolean deepBiome, boolean cave) {
		if (zone == SeaZones.Zone.FRESH) {
			// Lakes, rivers, swamps and caves: fresh-water life only, whatever the bottom looks like.
			if (cave) {
				return FRESH_CAVE;
			}
			return hotspot == Hotspot.OPEN ? FRESH_OPEN : FRESH_FLOOR;
		}
		return switch (hotspot) {
			case ANEMONE -> ANEMONE_TABLE;
			case WRECK -> zone == SeaZones.Zone.COLD ? COLD_FLOOR : WRECK_TABLE;
			case TRENCH -> zone == SeaZones.Zone.COLD ? COLD_DEEP : DEEP;
			case REEF -> switch (zone) {
				case WARM -> WARM_REEF;
				case TEMPERATE -> TEMPERATE_ROCK;
				case COLD, FRESH -> COLD_FLOOR;
			};
			case ROCK -> switch (zone) {
				case WARM -> WARM_ROCK;
				case TEMPERATE -> TEMPERATE_ROCK;
				case COLD, FRESH -> COLD_FLOOR;
			};
			case SAND -> switch (zone) {
				case WARM -> WARM_SAND;
				case TEMPERATE -> TEMPERATE_SAND;
				case COLD, FRESH -> COLD_FLOOR;
			};
			case OPEN -> switch (zone) {
				case WARM -> deepBiome ? DEEP : WARM_OPEN;
				case TEMPERATE -> deepBiome ? DEEP : TEMPERATE_OPEN;
				case COLD, FRESH -> deepBiome ? COLD_DEEP : COLD_OPEN;
			};
		};
	}

	private HotspotSpawner() {
	}

	public static void init() {
		// Vanilla biome spawns that contradict the habitat table are removed here (no extra init line needed).
		SeaZones.cleanVanillaSpawns();
		ServerTickEvents.END_LEVEL_TICK.register(HotspotSpawner::tick);
	}

	private static void tick(ServerLevel level) {
		if (level.dimension().equals(TourApi.DIMENSION)) {
			return;
		}
		long time = level.getGameTime();
		for (ServerPlayer player : level.players()) {
			// Stagger players so they do not all probe on the same tick
			if ((time + player.getId()) % INTERVAL_TICKS != 0 || player.isSpectator()) {
				continue;
			}
			for (int i = 0; i < PROBES_PER_PLAYER; i++) {
				probe(level, player);
			}
		}
	}

	private static void probe(ServerLevel level, ServerPlayer player) {
		RandomSource random = level.getRandom();
		double angle = random.nextDouble() * Math.PI * 2.0;
		double dist = MIN_RANGE + random.nextDouble() * (MAX_RANGE - MIN_RANGE);
		int x = (int) Math.floor(player.getX() + Math.cos(angle) * dist);
		int z = (int) Math.floor(player.getZ() + Math.sin(angle) * dist);
		int y = (int) Math.floor(player.getY()) + random.nextInt(VERTICAL_RANGE * 2 + 1) - VERTICAL_RANGE;
		y = Math.max(level.getMinY() + 1, Math.min(level.getMaxY() - 1, y));

		BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos(x, y, z);
		// Never touch unloaded chunks: the marker scan and the scatter reach a few blocks sideways.
		int reach = Math.max(MARKER_RADIUS, SCATTER) + 1;
		if (!level.hasChunksAt(x - reach, level.getMinY(), z - reach, x + reach, level.getMaxY(), z + reach) || !isWaterSource(level, cursor)) {
			return;
		}
		BlockPos probe = cursor.immutable();
		// Find the seabed below the probe
		int steps = 0;
		while (steps < MAX_FLOOR_SEARCH && isWaterSource(level, cursor.below())) {
			cursor.move(0, -1, 0);
			steps++;
		}
		boolean hasFloor = steps < MAX_FLOOR_SEARCH && !level.getBlockState(cursor.below()).isAir();
		BlockPos floorWater = cursor.immutable();

		var biome = level.getBiome(probe);
		SeaZones.Zone zone = SeaZones.zoneOf(biome);
		boolean deepBiome = SeaZones.isDeep(biome);
		Hotspot hotspot;
		if (hasFloor) {
			hotspot = classify(level, floorWater);
		} else {
			// Deep open water with no floor in reach: pelagic life only, never right under the surface.
			if (probe.getY() > level.getSeaLevel() - 4) {
				return;
			}
			hotspot = Hotspot.OPEN;
			deepBiome = deepBiome || steps >= MAX_FLOOR_SEARCH;
		}
		// The open sea gets a thinner background than the hotspots.
		if (hotspot == Hotspot.OPEN && random.nextInt(3) != 0) {
			return;
		}
		BlockPos center = hasFloor ? floorWater : probe;
		int room = FISH_CAP - countFish(level, center);
		if (room <= 0) {
			return;
		}
		boolean cave = !level.canSeeSky(probe);
		List<Entry> table = table(hotspot, zone, deepBiome, cave);
		// Mid-water species of the open sea swim at the probe depth (at least two cells above the floor).
		BlockPos midWater = probe;
		if (hasFloor && probe.getY() < floorWater.getY() + 2) {
			midWater = floorWater.above(2);
		}
		// Only species whose habitat matches this water (see Habitat), deep-water species only in deep water.
		java.util.EnumSet<Habitat> here = java.util.EnumSet.of(zone.habitat);
		if (deepBiome) {
			here.add(Habitat.DEEP);
		}
		spawnGroup(level, player, hotspot == Hotspot.OPEN ? midWater : center, table, here, random, room);
	}

	private static boolean isWaterSource(ServerLevel level, BlockPos pos) {
		var fluid = level.getFluidState(pos);
		return fluid.is(FluidTags.WATER) && fluid.isSource();
	}

	/** Looks at blocks around the point and returns the strongest matching hotspot (OPEN when nothing stands out). */
	private static Hotspot classify(ServerLevel level, BlockPos center) {
		int coral = 0;
		int anemone = 0;
		int rock = 0;
		int sand = 0;
		int wreck = 0;
		BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
		for (int dx = -MARKER_RADIUS; dx <= MARKER_RADIUS; dx++) {
			for (int dy = -MARKER_RADIUS; dy <= MARKER_RADIUS; dy++) {
				for (int dz = -MARKER_RADIUS; dz <= MARKER_RADIUS; dz++) {
					p.setWithOffset(center, dx, dy, dz);
					BlockState state = level.getBlockState(p);
					if (state.isAir()) {
						continue;
					}
					Block block = state.getBlock();
					if (state.is(Blocks.WARPED_PLANKS)) {
						wreck++;
					} else if (isAnemone(block)) {
						anemone++;
					} else if (isCoral(state, block)) {
						coral++;
					} else if (isRock(state)) {
						if (level.getFluidState(p.above()).is(FluidTags.WATER)) {
							rock++;
						}
					} else if (state.is(Blocks.SAND) || state.is(Blocks.RED_SAND)) {
						sand++;
					}
				}
			}
		}
		if (wreck > 0) {
			return Hotspot.WRECK;
		}
		if (anemone > 0) {
			return Hotspot.ANEMONE;
		}
		if (coral > 0) {
			return Hotspot.REEF;
		}
		if (level.getBiome(center).is(SeaZones.DEEP_TRENCH)) {
			return Hotspot.TRENCH;
		}
		if (rock >= ROCK_MIN) {
			return Hotspot.ROCK;
		}
		if (sand >= SAND_MIN) {
			return Hotspot.SAND;
		}
		return Hotspot.OPEN;
	}

	private static boolean isAnemone(Block block) {
		return BuiltInRegistries.BLOCK.getKey(block).equals(ANEMONE);
	}

	private static boolean isCoral(BlockState state, Block block) {
		if (state.is(BlockTags.CORALS) || state.is(BlockTags.CORAL_BLOCKS)) {
			return true;
		}
		Identifier key = BuiltInRegistries.BLOCK.getKey(block);
		if (!ScubaDive.MOD_ID.equals(key.getNamespace())) {
			return false;
		}
		String path = key.getPath();
		return path.equals("table_coral") || path.equals("brain_coral") || path.equals("branch_coral") || path.equals("fan_coral");
	}

	private static boolean isRock(BlockState state) {
		return state.is(Blocks.STONE) || state.is(Blocks.ANDESITE) || state.is(Blocks.TUFF) || state.is(Blocks.MOSSY_COBBLESTONE)
				|| state.is(Blocks.GRANITE) || state.is(Blocks.DIORITE) || state.is(Blocks.COBBLESTONE);
	}

	/** Counts scubadive and vanilla fish near the point. */
	private static int countFish(ServerLevel level, BlockPos center) {
		AABB box = new AABB(center).inflate(CAP_RADIUS);
		return level.getEntitiesOfClass(Mob.class, box, HotspotSpawner::isFish).size();
	}

	private static boolean isFish(Mob mob) {
		if (mob instanceof WaterAnimal) {
			return true;
		}
		return ScubaDive.MOD_ID.equals(BuiltInRegistries.ENTITY_TYPE.getKey(mob.getType()).getNamespace());
	}

	/** Extra 1-in-n thinning for the rarest animals so they stay a lucky find. */
	private static int thinning(String id) {
		return switch (id) {
			case "scubadive:whale_shark" -> 6;
			case "scubadive:manta_ray" -> 3;
			case "scubadive:frogfish", "scubadive:pygmy_seahorse" -> 2;
			default -> 1;
		};
	}

	private static void spawnGroup(ServerLevel level, ServerPlayer player, BlockPos center, List<Entry> table, Set<Habitat> here,
			RandomSource random, int room) {
		// Resolve the table at runtime; unknown ids are ignored
		List<Entry> usable = new ArrayList<>();
		List<EntityType<?>> types = new ArrayList<>();
		int total = 0;
		for (Entry entry : table) {
			EntityType<?> type = lookup(entry.id());
			if (type != null && Habitat.fits(entry.id(), here)) {
				usable.add(entry);
				types.add(type);
				total += entry.weight();
			}
		}
		if (usable.isEmpty()) {
			return;
		}
		int roll = random.nextInt(total);
		int index = 0;
		for (; index < usable.size() - 1; index++) {
			roll -= usable.get(index).weight();
			if (roll < 0) {
				break;
			}
		}
		Entry entry = usable.get(index);
		EntityType<?> type = types.get(index);
		if (random.nextInt(thinning(entry.id())) != 0) {
			return;
		}
		if (SOLO.contains(entry.id()) && !level.getEntitiesOfClass(Entity.class, new AABB(center).inflate(SOLO_RADIUS), e -> e.getType() == type).isEmpty()) {
			return;
		}
		int count = Math.min(room, entry.minGroup() + random.nextInt(entry.maxGroup() - entry.minGroup() + 1));
		boolean bottom = BOTTOM_DWELLERS.contains(entry.id());
		boolean checked = CHECKED.contains(entry.id());

		SpawnGroupData groupData = null;
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
		for (int i = 0; i < count; i++) {
			boolean placed = false;
			for (int attempt = 0; attempt < PLACE_TRIES && !placed; attempt++) {
				pos.setWithOffset(center, random.nextInt(SCATTER * 2 + 1) - SCATTER, random.nextInt(3), random.nextInt(SCATTER * 2 + 1) - SCATTER);
				if (!level.hasChunkAt(pos) || !isWaterSource(level, pos)) {
					continue;
				}
				if (bottom) {
					// Sea-floor species are placed on the floor, not in mid-water above a slope.
					for (int down = 0; down < 8 && isWaterSource(level, pos.below()); down++) {
						pos.move(0, -1, 0);
					}
				}
				if (player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) < MIN_PLAYER_DISTANCE * MIN_PLAYER_DISTANCE) {
					continue;
				}
				// Picky species only where their own rule allows (e.g. clownfish next to an anemone, gobies on sand).
				if (checked && !SpawnPlacements.checkSpawnRules(type, level, EntitySpawnReason.NATURAL, pos, random)) {
					continue;
				}
				placed = true;
			}
			if (!placed) {
				continue;
			}
			Entity entity = type.create(level, EntitySpawnReason.NATURAL);
			if (!(entity instanceof Mob mob)) {
				if (entity != null) {
					entity.discard();
				}
				continue;
			}
			mob.snapTo(pos.getX() + 0.5, pos.getY() + 0.1, pos.getZ() + 0.5, random.nextFloat() * 360.0f, 0.0f);
			if (!level.noCollision(mob)) {
				mob.discard();
				continue;
			}
			groupData = mob.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), EntitySpawnReason.NATURAL, groupData);
			level.addFreshEntity(mob);
		}
	}

	private static EntityType<?> lookup(String id) {
		Identifier identifier = Identifier.tryParse(id);
		if (identifier == null) {
			return null;
		}
		// getOptional avoids the default-entry fallback of the entity type registry
		return BuiltInRegistries.ENTITY_TYPE.getOptional(identifier).orElse(null);
	}
}
