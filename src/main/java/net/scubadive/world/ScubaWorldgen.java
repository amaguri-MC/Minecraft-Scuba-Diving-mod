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
	public static final Feature<NoneFeatureConfiguration> DEEP_TRENCH = new DeepTrenchFeature(NoneFeatureConfiguration.CODEC);
	public static final Feature<NoneFeatureConfiguration> ANEMONE_PATCH = new AnemonePatchFeature(NoneFeatureConfiguration.CODEC);
	public static final Feature<NoneFeatureConfiguration> SHIPWRECK = new ShipwreckFeature(NoneFeatureConfiguration.CODEC);

	private static ResourceKey<PlacedFeature> placed(String path) {
		return ResourceKey.create(Registries.PLACED_FEATURE, ScubaDive.id(path));
	}

	@SuppressWarnings("unchecked")
	public static void init() {
		Registry.register(BuiltInRegistries.FEATURE, ScubaDive.id("deep_trench"), DEEP_TRENCH);
		Registry.register(BuiltInRegistries.FEATURE, ScubaDive.id("shipwreck"), SHIPWRECK);
		Registry.register(BuiltInRegistries.FEATURE, ScubaDive.id("anemone_patch"), ANEMONE_PATCH);

		// Deep trenches (with basalt columns on their floor) in every deep ocean variant; runs first, before other decoration.
		BiomeModifications.addFeature(BiomeSelectors.includeByKey(Biomes.DEEP_OCEAN, Biomes.DEEP_COLD_OCEAN,
				Biomes.DEEP_LUKEWARM_OCEAN, Biomes.DEEP_FROZEN_OCEAN),
				GenerationStep.Decoration.RAW_GENERATION, placed("deep_trench"));
		TrenchCommand.init();
		// Shipwrecks: plain ocean + deep ocean.
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
