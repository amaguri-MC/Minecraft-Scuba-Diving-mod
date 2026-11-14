package net.scubadive.creature.spawn;

import java.util.List;
import java.util.function.Predicate;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectionContext;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.biome.v1.ModificationPhase;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.scubadive.ScubaDive;
import net.scubadive.tour.TourApi;

/**
 * Water-temperature zones used by every natural spawn table (biome spawns and the hotspot spawner).
 * Warm/lukewarm = coral reef and tropical life, ocean = temperate, cold/frozen = cold-water life,
 * deep variants additionally host the big pelagic fish.
 */
public final class SeaZones {
	public enum Zone {
		WARM(Habitat.TROPICAL), TEMPERATE(Habitat.TEMPERATE), COLD(Habitat.COLD), FRESH(Habitat.FRESH);

		public final Habitat habitat;

		Zone(Habitat habitat) {
			this.habitat = habitat;
		}
	}

	/** Biome groups for natural spawns, each tied to the habitat a creature must have to be added there. */
	public enum Region {
		/** Warm and lukewarm oceans. */
		WARM(Habitat.TROPICAL),
		/** Warm and lukewarm ocean, not the deep variant. */
		TROPICAL_SHALLOW(Habitat.TROPICAL),
		TEMPERATE(Habitat.TEMPERATE),
		COLD(Habitat.COLD),
		/** Deep ocean, deep lukewarm ocean and the trench (not the cold or frozen deep). */
		DEEP(Habitat.DEEP),
		TRENCH(Habitat.DEEP),
		/** Rivers, frozen rivers and swamps. */
		FRESH(Habitat.FRESH),
		/** Rivers and frozen rivers only (running water). */
		RIVER(Habitat.FRESH),
		/** Land biomes of the overworld: their flooded caves and aquifers (cave dwellers). */
		CAVES(Habitat.FRESH);

		public final Habitat habitat;

		Region(Habitat habitat) {
			this.habitat = habitat;
		}

		Predicate<BiomeSelectionContext> selector() {
			return switch (this) {
				case WARM -> warm();
				case TROPICAL_SHALLOW -> tropical();
				case TEMPERATE -> temperate();
				case COLD -> cold();
				case DEEP -> deepWarm();
				case TRENCH -> BiomeSelectors.includeByKey(DEEP_TRENCH);
				case FRESH -> BiomeSelectors.includeByKey(FRESH_KEYS);
				case RIVER -> BiomeSelectors.includeByKey(Biomes.RIVER, Biomes.FROZEN_RIVER);
				case CAVES -> BiomeSelectors.foundInOverworld().and(BiomeSelectors.tag(BiomeTags.IS_OCEAN).negate())
						.and(BiomeSelectors.tag(BiomeTags.IS_RIVER).negate());
			};
		}
	}

	public static final ResourceKey<Biome> DEEP_TRENCH = ResourceKey.create(Registries.BIOME, ScubaDive.id("deep_trench"));

	private static final List<ResourceKey<Biome>> WARM_KEYS = List.of(Biomes.WARM_OCEAN, Biomes.LUKEWARM_OCEAN, Biomes.DEEP_LUKEWARM_OCEAN);
	private static final List<ResourceKey<Biome>> TEMPERATE_KEYS = List.of(Biomes.OCEAN, Biomes.DEEP_OCEAN, DEEP_TRENCH);
	private static final List<ResourceKey<Biome>> COLD_KEYS = List.of(Biomes.COLD_OCEAN, Biomes.DEEP_COLD_OCEAN,
			Biomes.FROZEN_OCEAN, Biomes.DEEP_FROZEN_OCEAN);
	private static final List<ResourceKey<Biome>> FRESH_KEYS = List.of(Biomes.RIVER, Biomes.FROZEN_RIVER, Biomes.SWAMP,
			Biomes.MANGROVE_SWAMP);
	private static final List<ResourceKey<Biome>> DEEP_KEYS = List.of(Biomes.DEEP_OCEAN, Biomes.DEEP_LUKEWARM_OCEAN,
			Biomes.DEEP_COLD_OCEAN, DEEP_TRENCH);

	private SeaZones() {
	}

	/** Warm and lukewarm oceans (coral reefs, tropical life). */
	public static Predicate<BiomeSelectionContext> warm() {
		return BiomeSelectors.includeByKey(WARM_KEYS);
	}

	/** Plain warm ocean only (the clearest, shallowest reef water). */
	public static Predicate<BiomeSelectionContext> tropical() {
		return BiomeSelectors.includeByKey(Biomes.WARM_OCEAN, Biomes.LUKEWARM_OCEAN);
	}

	/** Ordinary ocean and deep ocean (temperate). */
	public static Predicate<BiomeSelectionContext> temperate() {
		return BiomeSelectors.includeByKey(TEMPERATE_KEYS);
	}

	/** Cold and frozen oceans. */
	public static Predicate<BiomeSelectionContext> cold() {
		return BiomeSelectors.includeByKey(COLD_KEYS);
	}

	/** Deep oceans (not frozen) and the trench: big pelagic fish. */
	public static Predicate<BiomeSelectionContext> deep() {
		return BiomeSelectors.includeByKey(DEEP_KEYS);
	}

	/** Deep oceans that are warm enough for tropical giants (whale shark, manta). */
	public static Predicate<BiomeSelectionContext> deepWarm() {
		return BiomeSelectors.includeByKey(Biomes.DEEP_LUKEWARM_OCEAN, Biomes.DEEP_OCEAN, DEEP_TRENCH);
	}

	public static boolean isDeep(Holder<Biome> biome) {
		for (ResourceKey<Biome> key : DEEP_KEYS) {
			if (biome.is(key)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Adds a natural biome spawn, but only when the creature's {@link Habitat} includes the region's habitat
	 * (a mismatch is logged and skipped, so no table can put a creature into the wrong water).
	 */
	public static void addSpawn(Region region, MobCategory category, EntityType<?> type, int weight, int min, int max) {
		String id = BuiltInRegistries.ENTITY_TYPE.getKey(type).toString();
		if (!Habitat.fits(id, region.habitat)) {
			ScubaDive.LOGGER.warn("Spawn of {} in {} skipped: not its habitat {}", id, region, Habitat.of(id));
			return;
		}
		BiomeModifications.addSpawn(region.selector(), category, type, weight, min, max);
	}

	/** Removes vanilla spawns that break the habitat table (cod on warm reefs, squid in rivers, tropical fish in fresh water...). */
	public static void cleanVanillaSpawns() {
		BiomeModifications.create(ScubaDive.id("habitat_cleanup"))
				.add(ModificationPhase.REMOVALS, warm(), ctx -> ctx.getMobSpawnSettings().removeSpawnsOfEntityType(net.minecraft.world.entity.EntityTypes.COD))
				.add(ModificationPhase.REMOVALS, cold(), ctx -> ctx.getMobSpawnSettings().removeSpawnsOfEntityType(net.minecraft.world.entity.EntityTypes.NAUTILUS))
				.add(ModificationPhase.REMOVALS, BiomeSelectors.includeByKey(Biomes.RIVER, Biomes.FROZEN_RIVER),
						ctx -> ctx.getMobSpawnSettings().removeSpawnsOfEntityType(net.minecraft.world.entity.EntityTypes.SQUID))
				.add(ModificationPhase.REMOVALS, BiomeSelectors.includeByKey(Biomes.MANGROVE_SWAMP, Biomes.LUSH_CAVES),
						ctx -> ctx.getMobSpawnSettings().removeSpawnsOfEntityType(net.minecraft.world.entity.EntityTypes.TROPICAL_FISH));
	}

	/** Zone of a biome: the oceans by water temperature, every other biome (rivers, lakes, swamps, caves) is fresh water. */
	public static Zone zoneOf(Holder<Biome> biome) {
		for (ResourceKey<Biome> key : WARM_KEYS) {
			if (biome.is(key)) {
				return Zone.WARM;
			}
		}
		for (ResourceKey<Biome> key : COLD_KEYS) {
			if (biome.is(key)) {
				return Zone.COLD;
			}
		}
		for (ResourceKey<Biome> key : TEMPERATE_KEYS) {
			if (biome.is(key)) {
				return Zone.TEMPERATE;
			}
		}
		return Zone.FRESH;
	}

	/**
	 * The water cell resting on the sea floor in the column at or below pos, at most maxDown cells down,
	 * or null. Lets floor dwellers accept a natural spawn point a little above the bottom (they settle down).
	 */
	public static BlockPos floorWater(LevelReader level, BlockPos pos, int maxDown) {
		if (!level.getFluidState(pos).is(FluidTags.WATER)) {
			return null;
		}
		BlockPos.MutableBlockPos p = pos.mutable();
		for (int i = 0; i <= maxDown; i++) {
			p.setY(pos.getY() - i - 1);
			if (!level.getFluidState(p).is(FluidTags.WATER)) {
				p.setY(pos.getY() - i);
				return p.immutable();
			}
		}
		return null;
	}

	/**
	 * Wraps a spawn placement rule so vanilla natural spawning never puts the creature into the tour dimension
	 * (its whole map is one warm-ocean biome; the tour spawner fills each site from its own table instead), and so
	 * a rule that reads too far during chunk generation refuses the spawn instead of crashing world generation.
	 */
	public static <T extends Entity> SpawnPlacements.SpawnPredicate<T> wild(SpawnPlacements.SpawnPredicate<T> rule) {
		return (type, level, reason, pos, random) -> {
			if ((reason == EntitySpawnReason.NATURAL || reason == EntitySpawnReason.CHUNK_GENERATION)
					&& level.getLevel().dimension().equals(TourApi.DIMENSION)) {
				return false;
			}
			try {
				return rule.test(type, level, reason, pos, random);
			} catch (IllegalStateException e) {
				// A rule that looks too far during chunk generation reads outside the generation region; vanilla
				// throws "Requested chunk unavailable". Treat it as "no spawn here" instead of crashing the world.
				return false;
			}
		};
	}
}
