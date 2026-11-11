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
	public static final ResourceKey<EntityType<?>> SEA_BUNNY_KEY = ResourceKey.create(Registries.ENTITY_TYPE, ScubaDive.id("sea_bunny"));
	public static final ResourceKey<EntityType<?>> SPANISH_DANCER_KEY = ResourceKey.create(Registries.ENTITY_TYPE, ScubaDive.id("spanish_dancer"));
	public static final ResourceKey<EntityType<?>> BLUE_DRAGON_KEY = ResourceKey.create(Registries.ENTITY_TYPE, ScubaDive.id("blue_dragon"));
	public static final ResourceKey<EntityType<?>> LEAF_SHEEP_KEY = ResourceKey.create(Registries.ENTITY_TYPE, ScubaDive.id("leaf_sheep"));
	public static final ResourceKey<EntityType<?>> PHYLLIDIA_KEY = ResourceKey.create(Registries.ENTITY_TYPE, ScubaDive.id("phyllidia"));
	public static final ResourceKey<EntityType<?>> THECACERA_KEY = ResourceKey.create(Registries.ENTITY_TYPE, ScubaDive.id("thecacera"));
	public static final ResourceKey<EntityType<?>> BLUE_NUDIBRANCH_KEY = ResourceKey.create(Registries.ENTITY_TYPE, ScubaDive.id("blue_nudibranch"));
	public static final ResourceKey<EntityType<?>> BORNELLA_KEY = ResourceKey.create(Registries.ENTITY_TYPE, ScubaDive.id("bornella"));
	public static final ResourceKey<EntityType<?>> CERATOSOMA_KEY = ResourceKey.create(Registries.ENTITY_TYPE, ScubaDive.id("ceratosoma"));
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

	// The original nudibranch is the white sea slug (Chromodoris orientalis); its id stays for old saves.
	public static final EntityType<Nudibranch> NUDIBRANCH = reefSlug(Nudibranch::new, NUDIBRANCH_KEY, 0.12f, 0.06f, 4.0);

	// More sea slug species: each is its own entity type (its own journal entry), all on the nudibranch base.
	public static final EntityType<Nudibranch> SEA_BUNNY = reefSlug(Nudibranch::new, SEA_BUNNY_KEY, 0.07f, 0.05f, 2.0);
	public static final EntityType<SpanishDancer> SPANISH_DANCER = reefSlug(SpanishDancer::new, SPANISH_DANCER_KEY, 0.36f, 0.08f, 8.0);
	public static final EntityType<Nudibranch> PHYLLIDIA = reefSlug(Nudibranch::new, PHYLLIDIA_KEY, 0.14f, 0.05f, 4.0);
	public static final EntityType<Nudibranch> THECACERA = reefSlug(Nudibranch::new, THECACERA_KEY, 0.08f, 0.06f, 2.0);
	public static final EntityType<Nudibranch> BLUE_NUDIBRANCH = reefSlug(Nudibranch::new, BLUE_NUDIBRANCH_KEY, 0.1f, 0.05f, 3.0);
	public static final EntityType<Bornella> BORNELLA = reefSlug(Bornella::new, BORNELLA_KEY, 0.16f, 0.06f, 3.0);
	public static final EntityType<Nudibranch> CERATOSOMA = reefSlug(Nudibranch::new, CERATOSOMA_KEY, 0.26f, 0.12f, 6.0);

	public static final EntityType<LeafSheep> LEAF_SHEEP = FabricEntityType.Builder.createMob(LeafSheep::new, MobCategory.UNDERGROUND_WATER_CREATURE, mob -> mob
					.defaultAttributes(() -> Mob.createMobAttributes()
							.add(Attributes.MAX_HEALTH, 1.0)
							.add(Attributes.MOVEMENT_SPEED, 0.002))
					.spawnPlacement(SpawnPlacementTypes.IN_WATER, Heightmap.Types.OCEAN_FLOOR, SeaZones.wild((type, level, reason, pos, random) ->
							reefFloorNear(level, pos) || seagrassNear(level, pos))))
			.sized(0.06f, 0.04f)
			.clientTrackingRange(6)
			.build(LEAF_SHEEP_KEY);

	/** Floats at the surface: any water cell will do, it rises to the top after spawning. */
	public static final EntityType<BlueDragon> BLUE_DRAGON = FabricEntityType.Builder.createMob(BlueDragon::new, MobCategory.UNDERGROUND_WATER_CREATURE, mob -> mob
					.defaultAttributes(() -> Mob.createMobAttributes()
							.add(Attributes.MAX_HEALTH, 2.0)
							.add(Attributes.MOVEMENT_SPEED, 0.003))
					.spawnPlacement(SpawnPlacementTypes.IN_WATER, Heightmap.Types.OCEAN_FLOOR, SeaZones.wild((type, level, reason, pos, random) ->
							level.getFluidState(pos).is(FluidTags.WATER) && level.getFluidState(pos.above()).is(FluidTags.WATER))))
			.sized(0.14f, 0.05f)
			.clientTrackingRange(8)
			.build(BLUE_DRAGON_KEY);

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
		Registry.register(BuiltInRegistries.ENTITY_TYPE, SEA_BUNNY_KEY, SEA_BUNNY);
		Registry.register(BuiltInRegistries.ENTITY_TYPE, SPANISH_DANCER_KEY, SPANISH_DANCER);
		Registry.register(BuiltInRegistries.ENTITY_TYPE, BLUE_DRAGON_KEY, BLUE_DRAGON);
		Registry.register(BuiltInRegistries.ENTITY_TYPE, LEAF_SHEEP_KEY, LEAF_SHEEP);
		Registry.register(BuiltInRegistries.ENTITY_TYPE, PHYLLIDIA_KEY, PHYLLIDIA);
		Registry.register(BuiltInRegistries.ENTITY_TYPE, THECACERA_KEY, THECACERA);
		Registry.register(BuiltInRegistries.ENTITY_TYPE, BLUE_NUDIBRANCH_KEY, BLUE_NUDIBRANCH);
		Registry.register(BuiltInRegistries.ENTITY_TYPE, BORNELLA_KEY, BORNELLA);
		Registry.register(BuiltInRegistries.ENTITY_TYPE, CERATOSOMA_KEY, CERATOSOMA);
		Registry.register(BuiltInRegistries.ENTITY_TYPE, PYGMY_SEAHORSE_KEY, PYGMY_SEAHORSE);

		// Spawn eggs
		registerSpawnEgg("nudibranch_spawn_egg", NUDIBRANCH);
		registerSpawnEgg("sea_bunny_spawn_egg", SEA_BUNNY);
		registerSpawnEgg("spanish_dancer_spawn_egg", SPANISH_DANCER);
		registerSpawnEgg("blue_dragon_spawn_egg", BLUE_DRAGON);
		registerSpawnEgg("leaf_sheep_spawn_egg", LEAF_SHEEP);
		registerSpawnEgg("phyllidia_spawn_egg", PHYLLIDIA);
		registerSpawnEgg("thecacera_spawn_egg", THECACERA);
		registerSpawnEgg("blue_nudibranch_spawn_egg", BLUE_NUDIBRANCH);
		registerSpawnEgg("bornella_spawn_egg", BORNELLA);
		registerSpawnEgg("ceratosoma_spawn_egg", CERATOSOMA);
		registerSpawnEgg("pygmy_seahorse_spawn_egg", PYGMY_SEAHORSE);

		// Natural spawns: sea slugs on warm reefs (a few in the temperate sea), the rare pygmy seahorse in warm water.
		// Floor dwellers use the UNDERGROUND_WATER_CREATURE cap and the seahorse the AXOLOTLS cap, so the fish keep
		// the WATER_AMBIENT cap to themselves.
		SeaZones.addSpawn(SeaZones.Region.WARM, MobCategory.UNDERGROUND_WATER_CREATURE, NUDIBRANCH, 10, 1, 3);
		SeaZones.addSpawn(SeaZones.Region.TEMPERATE, MobCategory.UNDERGROUND_WATER_CREATURE, NUDIBRANCH, 3, 1, 2);
		// The other sea slugs share the floor-dweller cap; weights stay low so the reef is not covered in slugs.
		SeaZones.addSpawn(SeaZones.Region.WARM, MobCategory.UNDERGROUND_WATER_CREATURE, SEA_BUNNY, 3, 1, 2);
		SeaZones.addSpawn(SeaZones.Region.TEMPERATE, MobCategory.UNDERGROUND_WATER_CREATURE, SEA_BUNNY, 1, 1, 2);
		SeaZones.addSpawn(SeaZones.Region.WARM, MobCategory.UNDERGROUND_WATER_CREATURE, SPANISH_DANCER, 2, 1, 1);
		SeaZones.addSpawn(SeaZones.Region.WARM, MobCategory.UNDERGROUND_WATER_CREATURE, PHYLLIDIA, 4, 1, 2);
		SeaZones.addSpawn(SeaZones.Region.WARM, MobCategory.UNDERGROUND_WATER_CREATURE, THECACERA, 2, 1, 2);
		SeaZones.addSpawn(SeaZones.Region.TEMPERATE, MobCategory.UNDERGROUND_WATER_CREATURE, THECACERA, 2, 1, 2);
		SeaZones.addSpawn(SeaZones.Region.TROPICAL_SHALLOW, MobCategory.UNDERGROUND_WATER_CREATURE, LEAF_SHEEP, 1, 1, 1);
		// The blue sea slug is the most common slug on Japanese rocky shores, warm or temperate.
		SeaZones.addSpawn(SeaZones.Region.WARM, MobCategory.UNDERGROUND_WATER_CREATURE, BLUE_NUDIBRANCH, 4, 1, 2);
		SeaZones.addSpawn(SeaZones.Region.TEMPERATE, MobCategory.UNDERGROUND_WATER_CREATURE, BLUE_NUDIBRANCH, 5, 1, 3);
		SeaZones.addSpawn(SeaZones.Region.WARM, MobCategory.UNDERGROUND_WATER_CREATURE, BORNELLA, 2, 1, 1);
		SeaZones.addSpawn(SeaZones.Region.WARM, MobCategory.UNDERGROUND_WATER_CREATURE, CERATOSOMA, 2, 1, 1);
		// Blue dragons drift over open warm water (deep lukewarm ocean included), a few together.
		SeaZones.addSpawn(SeaZones.Region.WARM, MobCategory.UNDERGROUND_WATER_CREATURE, BLUE_DRAGON, 2, 1, 3);
		SeaZones.addSpawn(SeaZones.Region.TROPICAL_SHALLOW, MobCategory.AXOLOTLS, PYGMY_SEAHORSE, 2, 1, 1);
	}

	/** Reef rock or coral at most 3 cells below, with water above; the slug settles onto it. */
	private static boolean reefFloorNear(net.minecraft.world.level.LevelReader level, net.minecraft.core.BlockPos pos) {
		net.minecraft.core.BlockPos f = SeaZones.floorWater(level, pos, 3);
		return f != null && level.getFluidState(f.above()).is(FluidTags.WATER)
				&& Nudibranch.isReefFloor(level.getBlockState(f.below()));
	}

	/** Seagrass in a cell at most 3 cells below (leaf sheep graze on it). */
	private static boolean seagrassNear(net.minecraft.world.level.LevelReader level, net.minecraft.core.BlockPos pos) {
		net.minecraft.core.BlockPos f = SeaZones.floorWater(level, pos, 3);
		return f != null && LeafSheep.isSeagrass(level.getBlockState(f));
	}

	/** A reef-crawling sea slug: same spawn rule, cap and behaviour as the original nudibranch. */
	private static <T extends Nudibranch> EntityType<T> reefSlug(EntityType.EntityFactory<T> factory, ResourceKey<EntityType<?>> key,
			float width, float height, double health) {
		return FabricEntityType.Builder.createMob(factory, MobCategory.UNDERGROUND_WATER_CREATURE, mob -> mob
						.defaultAttributes(() -> Mob.createMobAttributes()
								.add(Attributes.MAX_HEALTH, health)
								.add(Attributes.MOVEMENT_SPEED, 0.004))
						.spawnPlacement(SpawnPlacementTypes.IN_WATER, Heightmap.Types.OCEAN_FLOOR, SeaZones.wild((type, level, reason, pos, random) ->
								reefFloorNear(level, pos))))
				.sized(width, height)
				.clientTrackingRange(8)
				.build(key);
	}

	private static void registerSpawnEgg(String path, EntityType<? extends Mob> type) {
		ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, ScubaDive.id(path));
		Registry.register(BuiltInRegistries.ITEM, key, new SpawnEggItem(new Item.Properties().setId(key).spawnEgg(type)));
	}
}
