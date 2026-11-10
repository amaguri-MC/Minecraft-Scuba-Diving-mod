package net.scubadive.creature;

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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.scubadive.ScubaDive;
import net.scubadive.creature.spawn.SeaZones;

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

	public static final EntityType<Nudibranch> NUDIBRANCH = FabricEntityType.Builder.createMob(Nudibranch::new, MobCategory.UNDERGROUND_WATER_CREATURE, mob -> mob
					.defaultAttributes(() -> Mob.createMobAttributes()
							.add(Attributes.MAX_HEALTH, 4.0)
							.add(Attributes.MOVEMENT_SPEED, 0.004))
					.spawnPlacement(SpawnPlacementTypes.IN_WATER, Heightmap.Types.OCEAN_FLOOR, SeaZones.wild((type, level, reason, pos, random) ->
							reefFloorNear(level, pos))))
			.sized(0.12f, 0.06f)
			.clientTrackingRange(8)
			.build(NUDIBRANCH_KEY);

	public static final EntityType<PygmySeahorse> PYGMY_SEAHORSE = FabricEntityType.Builder.createMob(PygmySeahorse::new, MobCategory.AXOLOTLS, mob -> mob
					.defaultAttributes(() -> Mob.createMobAttributes()
							.add(Attributes.MAX_HEALTH, 2.0)
							.add(Attributes.MOVEMENT_SPEED, 0.01))
					.spawnPlacement(SpawnPlacementTypes.IN_WATER, Heightmap.Types.OCEAN_FLOOR, SeaZones.wild((type, level, reason, pos, random) ->
							level.getFluidState(pos).is(FluidTags.WATER)
									&& level.getFluidState(pos.above()).is(FluidTags.WATER)
									&& level.getFluidState(pos.below()).is(FluidTags.WATER))))
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

		// Natural spawns: sea slugs on warm reefs (a few in the temperate sea), the rare pygmy seahorse in warm water.
		// Floor dwellers use the UNDERGROUND_WATER_CREATURE cap and the seahorse the AXOLOTLS cap, so the fish keep
		// the WATER_AMBIENT cap to themselves.
		SeaZones.addSpawn(SeaZones.Region.WARM, MobCategory.UNDERGROUND_WATER_CREATURE, NUDIBRANCH, 10, 1, 3);
		SeaZones.addSpawn(SeaZones.Region.TEMPERATE, MobCategory.UNDERGROUND_WATER_CREATURE, NUDIBRANCH, 3, 1, 2);
		SeaZones.addSpawn(SeaZones.Region.TROPICAL_SHALLOW, MobCategory.AXOLOTLS, PYGMY_SEAHORSE, 2, 1, 1);
	}

	/** Reef rock or coral at most 3 cells below, with water above; the slug settles onto it. */
	private static boolean reefFloorNear(net.minecraft.world.level.LevelReader level, net.minecraft.core.BlockPos pos) {
		net.minecraft.core.BlockPos f = SeaZones.floorWater(level, pos, 3);
		return f != null && level.getFluidState(f.above()).is(FluidTags.WATER)
				&& Nudibranch.isReefFloor(level.getBlockState(f.below()));
	}

	private static void registerSpawnEgg(String path, EntityType<? extends Mob> type) {
		ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, ScubaDive.id(path));
		Registry.register(BuiltInRegistries.ITEM, key, new SpawnEggItem(new Item.Properties().setId(key).spawnEgg(type)));
	}
}
