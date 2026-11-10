package net.scubadive.world;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.scubadive.ScubaDive;

public final class ScubaWorldgen {
	public static final Feature<NoneFeatureConfiguration> BASALT_COLUMNS = new BasaltColumnsFeature(NoneFeatureConfiguration.CODEC);
	public static final Feature<NoneFeatureConfiguration> SHIPWRECK = new ShipwreckFeature(NoneFeatureConfiguration.CODEC);

	private static ResourceKey<PlacedFeature> placed(String path) {
		return ResourceKey.create(Registries.PLACED_FEATURE, ScubaDive.id(path));
	}

	@SuppressWarnings("unchecked")
	public static void init() {
		Registry.register(BuiltInRegistries.FEATURE, ScubaDive.id("basalt_columns"), BASALT_COLUMNS);
		Registry.register(BuiltInRegistries.FEATURE, ScubaDive.id("shipwreck"), SHIPWRECK);

		// Plain ocean + deep ocean.
		BiomeModifications.addFeature(BiomeSelectors.includeByKey(Biomes.OCEAN, Biomes.DEEP_OCEAN),
				GenerationStep.Decoration.VEGETAL_DECORATION, placed("basalt_columns"));
		BiomeModifications.addFeature(BiomeSelectors.includeByKey(Biomes.OCEAN, Biomes.DEEP_OCEAN),
				GenerationStep.Decoration.SURFACE_STRUCTURES, placed("shipwreck"));
		// Warm + lukewarm oceans: anemone patches (block referenced by id in JSON only).
		BiomeModifications.addFeature(
				BiomeSelectors.includeByKey(Biomes.WARM_OCEAN, Biomes.LUKEWARM_OCEAN, Biomes.DEEP_LUKEWARM_OCEAN),
				GenerationStep.Decoration.VEGETAL_DECORATION, placed("sea_anemone_patch"));
	}

	private ScubaWorldgen() {
	}
}
