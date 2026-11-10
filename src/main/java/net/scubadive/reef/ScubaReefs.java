package net.scubadive.reef;

import java.util.function.Function;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.shapes.Shapes;
import net.scubadive.ScubaDive;

/** Reefs, coral blocks and their worldgen. Owner: reef module. */
public final class ScubaReefs {
	public static final Block TABLE_CORAL = block("table_coral", props -> new ReefPlantBlock(props,
			Shapes.or(Block.box(5.0, 0.0, 5.0, 11.0, 6.0, 11.0), Block.box(0.0, 6.0, 0.0, 16.0, 9.0, 16.0)), true),
			BlockBehaviour.Properties.of()
					.mapColor(MapColor.COLOR_ORANGE)
					.noOcclusion()
					.strength(0.5F)
					.sound(SoundType.CORAL_BLOCK)
					.pushReaction(PushReaction.DESTROY));

	public static final Block BRAIN_CORAL = block("brain_coral", Block::new,
			BlockBehaviour.Properties.of()
					.mapColor(MapColor.COLOR_PINK)
					.strength(1.5F, 3.0F)
					.sound(SoundType.CORAL_BLOCK));

	public static final Block BRANCH_CORAL = block("branch_coral", props -> new ReefPlantBlock(props,
			Block.box(2.0, 0.0, 2.0, 14.0, 15.0, 14.0), false),
			BlockBehaviour.Properties.of()
					.mapColor(MapColor.COLOR_MAGENTA)
					.noCollision()
					.noOcclusion()
					.instabreak()
					.sound(SoundType.CORAL_BLOCK)
					.pushReaction(PushReaction.DESTROY));

	public static final Block FAN_CORAL = block("fan_coral", FanCoralBlock::new,
			BlockBehaviour.Properties.of()
					.mapColor(MapColor.COLOR_YELLOW)
					.noCollision()
					.noOcclusion()
					.instabreak()
					.sound(SoundType.CORAL_BLOCK)
					.pushReaction(PushReaction.DESTROY));

	public static final Feature<NoneFeatureConfiguration> ROCK_REEF = new RockReefFeature(NoneFeatureConfiguration.CODEC);
	public static final Feature<NoneFeatureConfiguration> CORAL_GARDEN = new CoralGardenFeature(NoneFeatureConfiguration.CODEC);

	private ScubaReefs() {
	}

	private static Block block(String path, Function<BlockBehaviour.Properties, Block> factory, BlockBehaviour.Properties props) {
		ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, ScubaDive.id(path));
		return factory.apply(props.setId(key));
	}

	private static void register(String path, Block block) {
		Registry.register(BuiltInRegistries.BLOCK, ScubaDive.id(path), block);
		ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, ScubaDive.id(path));
		Registry.register(BuiltInRegistries.ITEM, itemKey,
				new BlockItem(block, new Item.Properties().setId(itemKey).useBlockDescriptionPrefix()));
	}

	private static ResourceKey<PlacedFeature> placed(String path) {
		return ResourceKey.create(Registries.PLACED_FEATURE, ScubaDive.id(path));
	}

	public static void init() {
		register("table_coral", TABLE_CORAL);
		register("brain_coral", BRAIN_CORAL);
		register("branch_coral", BRANCH_CORAL);
		register("fan_coral", FAN_CORAL);

		Registry.register(BuiltInRegistries.FEATURE, ScubaDive.id("reef_rocks"), ROCK_REEF);
		Registry.register(BuiltInRegistries.FEATURE, ScubaDive.id("coral_garden"), CORAL_GARDEN);

		// Rock mounds on the floor of plain, cold and warm oceans (before vegetation so corals can sit on them).
		BiomeModifications.addFeature(
				BiomeSelectors.includeByKey(Biomes.OCEAN, Biomes.DEEP_OCEAN, Biomes.COLD_OCEAN, Biomes.DEEP_COLD_OCEAN,
						Biomes.WARM_OCEAN, Biomes.LUKEWARM_OCEAN, Biomes.DEEP_LUKEWARM_OCEAN),
				GenerationStep.Decoration.LOCAL_MODIFICATIONS, placed("reef_rocks"));
		// Dense coral colonies in warm oceans only.
		BiomeModifications.addFeature(
				BiomeSelectors.includeByKey(Biomes.WARM_OCEAN, Biomes.LUKEWARM_OCEAN, Biomes.DEEP_LUKEWARM_OCEAN),
				GenerationStep.Decoration.VEGETAL_DECORATION, placed("coral_garden"));
	}
}
