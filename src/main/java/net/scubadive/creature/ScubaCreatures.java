package net.scubadive.creature;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityType;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.scubadive.ScubaDive;

/** Server/common registration for sea creatures and the sea anemone block. */
public final class ScubaCreatures {
	public static final ResourceKey<Block> SEA_ANEMONE_KEY = ResourceKey.create(Registries.BLOCK, ScubaDive.id("sea_anemone"));
	public static final ResourceKey<EntityType<?>> NUDIBRANCH_KEY = ResourceKey.create(Registries.ENTITY_TYPE, ScubaDive.id("nudibranch"));
	public static final ResourceKey<EntityType<?>> PYGMY_SEAHORSE_KEY = ResourceKey.create(Registries.ENTITY_TYPE, ScubaDive.id("pygmy_seahorse"));

	public static final Block SEA_ANEMONE = new SeaAnemoneBlock(BlockBehaviour.Properties.of()
			.mapColor(MapColor.COLOR_PURPLE)
			.noCollision()
			.instabreak()
			.noOcclusion()
			.sound(SoundType.WET_GRASS)
			.pushReaction(PushReaction.DESTROY)
			.lightLevel(state -> 3)
			.setId(SEA_ANEMONE_KEY));

	public static final EntityType<Nudibranch> NUDIBRANCH = FabricEntityType.Builder.createMob(Nudibranch::new, MobCategory.WATER_AMBIENT, mob -> mob
					.defaultAttributes(() -> Mob.createMobAttributes()
							.add(Attributes.MAX_HEALTH, 4.0)
							.add(Attributes.MOVEMENT_SPEED, 0.004))
					.spawnPlacement(SpawnPlacementTypes.IN_WATER, Heightmap.Types.OCEAN_FLOOR, (type, level, reason, pos, random) ->
							level.getFluidState(pos).is(FluidTags.WATER)
									&& level.getFluidState(pos.above()).is(FluidTags.WATER)
									&& level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP)))
			.sized(0.12f, 0.06f)
			.clientTrackingRange(8)
			.build(NUDIBRANCH_KEY);

	public static final EntityType<PygmySeahorse> PYGMY_SEAHORSE = FabricEntityType.Builder.createMob(PygmySeahorse::new, MobCategory.WATER_AMBIENT, mob -> mob
					.defaultAttributes(() -> Mob.createMobAttributes()
							.add(Attributes.MAX_HEALTH, 2.0)
							.add(Attributes.MOVEMENT_SPEED, 0.01))
					.spawnPlacement(SpawnPlacementTypes.IN_WATER, Heightmap.Types.OCEAN_FLOOR, (type, level, reason, pos, random) ->
							level.getFluidState(pos).is(FluidTags.WATER)
									&& level.getFluidState(pos.above()).is(FluidTags.WATER)
									&& level.getFluidState(pos.below()).is(FluidTags.WATER)))
			.sized(0.12f, 0.32f)
			.clientTrackingRange(6)
			.build(PYGMY_SEAHORSE_KEY);

	private ScubaCreatures() {
	}

	public static void init() {
		// Block + block item
		Registry.register(BuiltInRegistries.BLOCK, SEA_ANEMONE_KEY, SEA_ANEMONE);
		ResourceKey<Item> anemoneItemKey = ResourceKey.create(Registries.ITEM, ScubaDive.id("sea_anemone"));
		Registry.register(BuiltInRegistries.ITEM, anemoneItemKey,
				new BlockItem(SEA_ANEMONE, new Item.Properties().setId(anemoneItemKey).useBlockDescriptionPrefix()));

		// Entities
		Registry.register(BuiltInRegistries.ENTITY_TYPE, NUDIBRANCH_KEY, NUDIBRANCH);
		Registry.register(BuiltInRegistries.ENTITY_TYPE, PYGMY_SEAHORSE_KEY, PYGMY_SEAHORSE);

		// Spawn eggs
		registerSpawnEgg("nudibranch_spawn_egg", NUDIBRANCH);
		registerSpawnEgg("pygmy_seahorse_spawn_egg", PYGMY_SEAHORSE);

		// Natural spawns: warm and lukewarm oceans; the pygmy seahorse is rare.
		var warmSeas = BiomeSelectors.includeByKey(Biomes.WARM_OCEAN, Biomes.LUKEWARM_OCEAN, Biomes.DEEP_LUKEWARM_OCEAN);
		BiomeModifications.addSpawn(warmSeas, MobCategory.WATER_AMBIENT, NUDIBRANCH, 8, 1, 2);
		BiomeModifications.addSpawn(warmSeas, MobCategory.WATER_AMBIENT, PYGMY_SEAHORSE, 1, 1, 1);
	}

	private static void registerSpawnEgg(String path, EntityType<? extends Mob> type) {
		ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, ScubaDive.id(path));
		Registry.register(BuiltInRegistries.ITEM, key, new SpawnEggItem(new Item.Properties().setId(key).spawnEgg(type)));
	}
}
