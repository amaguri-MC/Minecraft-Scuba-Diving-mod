package net.scubadive.creature.bottom;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.scubadive.ScubaDive;
import net.scubadive.creature.ScubaCreatures;
import net.scubadive.creature.spawn.SeaZones;
import net.scubadive.reef.ScubaReefs;

/** Registration for the symbiotic and bottom-dwelling creatures: clownfish, anemone shrimp, goby, moray eel, frogfish. */
public final class BottomDwellers {
	public static final ResourceKey<EntityType<?>> CLOWNFISH_KEY = ResourceKey.create(Registries.ENTITY_TYPE, ScubaDive.id("clownfish"));
	public static final ResourceKey<EntityType<?>> ANEMONE_SHRIMP_KEY = ResourceKey.create(Registries.ENTITY_TYPE, ScubaDive.id("anemone_shrimp"));
	public static final ResourceKey<EntityType<?>> GOBY_KEY = ResourceKey.create(Registries.ENTITY_TYPE, ScubaDive.id("goby"));
	public static final ResourceKey<EntityType<?>> MORAY_EEL_KEY = ResourceKey.create(Registries.ENTITY_TYPE, ScubaDive.id("moray_eel"));
	public static final ResourceKey<EntityType<?>> FROGFISH_KEY = ResourceKey.create(Registries.ENTITY_TYPE, ScubaDive.id("frogfish"));
	public static final ResourceKey<EntityType<?>> OCTOPUS_KEY = ResourceKey.create(Registries.ENTITY_TYPE, ScubaDive.id("octopus"));

	public static final EntityType<Clownfish> CLOWNFISH = FabricEntityType.Builder.createMob(Clownfish::new, MobCategory.AXOLOTLS, mob -> mob
					.defaultAttributes(() -> Mob.createMobAttributes()
							.add(Attributes.MAX_HEALTH, 3.0)
							.add(Attributes.MOVEMENT_SPEED, 0.04))
					.spawnPlacement(SpawnPlacementTypes.IN_WATER, Heightmap.Types.OCEAN_FLOOR, SeaZones.wild((type, level, reason, pos, random) ->
							nearAnemoneFloor(level, pos))))
			.sized(0.3f, 0.2f)
			.clientTrackingRange(8)
			.build(CLOWNFISH_KEY);

	public static final EntityType<AnemoneShrimp> ANEMONE_SHRIMP = FabricEntityType.Builder.createMob(AnemoneShrimp::new, MobCategory.AXOLOTLS, mob -> mob
					.defaultAttributes(() -> Mob.createMobAttributes()
							.add(Attributes.MAX_HEALTH, 1.0)
							.add(Attributes.MOVEMENT_SPEED, 0.006))
					.spawnPlacement(SpawnPlacementTypes.IN_WATER, Heightmap.Types.OCEAN_FLOOR, SeaZones.wild((type, level, reason, pos, random) ->
							nearAnemoneFloor(level, pos))))
			.sized(0.1f, 0.1f)
			.clientTrackingRange(6)
			.build(ANEMONE_SHRIMP_KEY);

	public static final EntityType<Goby> GOBY = FabricEntityType.Builder.createMob(Goby::new, MobCategory.UNDERGROUND_WATER_CREATURE, mob -> mob
					.defaultAttributes(() -> Mob.createMobAttributes()
							.add(Attributes.MAX_HEALTH, 3.0)
							.add(Attributes.MOVEMENT_SPEED, 0.05))
					.spawnPlacement(SpawnPlacementTypes.IN_WATER, Heightmap.Types.OCEAN_FLOOR, SeaZones.wild((type, level, reason, pos, random) ->
							onFloor(level, pos, f -> level.getBlockState(f.below()).is(Blocks.SAND)))))
			.sized(0.22f, 0.14f)
			.clientTrackingRange(6)
			.build(GOBY_KEY);

	public static final EntityType<MorayEel> MORAY_EEL = FabricEntityType.Builder.createMob(MorayEel::new, MobCategory.UNDERGROUND_WATER_CREATURE, mob -> mob
					.defaultAttributes(() -> Mob.createMobAttributes()
							.add(Attributes.MAX_HEALTH, 12.0)
							.add(Attributes.MOVEMENT_SPEED, 0.0))
					.spawnPlacement(SpawnPlacementTypes.IN_WATER, Heightmap.Types.OCEAN_FLOOR, SeaZones.wild((type, level, reason, pos, random) ->
							level.getFluidState(pos).is(FluidTags.WATER) && MorayEel.findCrevice(level, pos, 3) != null)))
			.sized(0.4f, 0.4f)
			.clientTrackingRange(8)
			.build(MORAY_EEL_KEY);

	public static final EntityType<Frogfish> FROGFISH = FabricEntityType.Builder.createMob(Frogfish::new, MobCategory.UNDERGROUND_WATER_CREATURE, mob -> mob
					.defaultAttributes(() -> Mob.createMobAttributes()
							.add(Attributes.MAX_HEALTH, 8.0)
							.add(Attributes.MOVEMENT_SPEED, 0.0))
					.spawnPlacement(SpawnPlacementTypes.IN_WATER, Heightmap.Types.OCEAN_FLOOR, SeaZones.wild((type, level, reason, pos, random) ->
							onFloor(level, pos, f -> isMimicryBed(level, f)))))
			.sized(0.35f, 0.3f)
			.clientTrackingRange(6)
			.build(FROGFISH_KEY);

	public static final EntityType<Octopus> OCTOPUS = FabricEntityType.Builder.createMob(Octopus::new, MobCategory.UNDERGROUND_WATER_CREATURE, mob -> mob
					.defaultAttributes(() -> Mob.createMobAttributes()
							.add(Attributes.MAX_HEALTH, 10.0)
							.add(Attributes.MOVEMENT_SPEED, 0.03))
					.spawnPlacement(SpawnPlacementTypes.IN_WATER, Heightmap.Types.OCEAN_FLOOR, SeaZones.wild((type, level, reason, pos, random) ->
							onFloor(level, pos, f -> Octopus.canSpawnAt(level, f)))))
			.sized(0.6f, 0.4f)
			.clientTrackingRange(8)
			.build(OCTOPUS_KEY);

	private BottomDwellers() {
	}

	public static void init() {
		Registry.register(BuiltInRegistries.ENTITY_TYPE, CLOWNFISH_KEY, CLOWNFISH);
		Registry.register(BuiltInRegistries.ENTITY_TYPE, ANEMONE_SHRIMP_KEY, ANEMONE_SHRIMP);
		Registry.register(BuiltInRegistries.ENTITY_TYPE, GOBY_KEY, GOBY);
		Registry.register(BuiltInRegistries.ENTITY_TYPE, MORAY_EEL_KEY, MORAY_EEL);
		Registry.register(BuiltInRegistries.ENTITY_TYPE, FROGFISH_KEY, FROGFISH);
		Registry.register(BuiltInRegistries.ENTITY_TYPE, OCTOPUS_KEY, OCTOPUS);

		registerSpawnEgg("clownfish_spawn_egg", CLOWNFISH);
		registerSpawnEgg("anemone_shrimp_spawn_egg", ANEMONE_SHRIMP);
		registerSpawnEgg("goby_spawn_egg", GOBY);
		registerSpawnEgg("moray_eel_spawn_egg", MORAY_EEL);
		registerSpawnEgg("frogfish_spawn_egg", FROGFISH);
		registerSpawnEgg("octopus_spawn_egg", OCTOPUS);

		// Natural spawns. Anemone dwellers use the AXOLOTLS cap and floor dwellers the UNDERGROUND_WATER_CREATURE
		// cap (both unused in the open sea), so they no longer crowd the fish out of the 20-mob WATER_AMBIENT cap.
		// Tropical species: warm and lukewarm oceans. The placement rules above do the fine filtering.
		SeaZones.addSpawn(SeaZones.Region.WARM, MobCategory.AXOLOTLS, CLOWNFISH, 10, 2, 3);
		SeaZones.addSpawn(SeaZones.Region.WARM, MobCategory.AXOLOTLS, ANEMONE_SHRIMP, 5, 1, 2);
		SeaZones.addSpawn(SeaZones.Region.WARM, MobCategory.UNDERGROUND_WATER_CREATURE, GOBY, 8, 1, 3);
		SeaZones.addSpawn(SeaZones.Region.WARM, MobCategory.UNDERGROUND_WATER_CREATURE, MORAY_EEL, 4, 1, 1);
		SeaZones.addSpawn(SeaZones.Region.WARM, MobCategory.UNDERGROUND_WATER_CREATURE, FROGFISH, 2, 1, 1);
		// Octopuses: reefs and the temperate sea, on rocky floors only (placement rule).
		SeaZones.addSpawn(SeaZones.Region.WARM, MobCategory.UNDERGROUND_WATER_CREATURE, OCTOPUS, 3, 1, 1);
		SeaZones.addSpawn(SeaZones.Region.TEMPERATE, MobCategory.UNDERGROUND_WATER_CREATURE, OCTOPUS, 6, 1, 1);
	}

	/** Natural floor-dweller check: the floor lies at most 3 cells below (the creature sinks onto it) and passes the test. */
	private static boolean onFloor(LevelReader level, BlockPos pos, java.util.function.Predicate<BlockPos> floorTest) {
		BlockPos f = SeaZones.floorWater(level, pos, 3);
		return f != null && level.getFluidState(f.above()).is(FluidTags.WATER) && floorTest.test(f);
	}

	/** Anemone dwellers: low over the floor (cheap test first) with an anemone within their home range. */
	private static boolean nearAnemoneFloor(LevelReader level, BlockPos pos) {
		return SeaZones.floorWater(level, pos, 4) != null && anemoneNear(level, pos, AnemoneDweller.HOME_RANGE);
	}

	/** True when a sea anemone block lies within the radius (sphere) of the position. */
	public static boolean anemoneNear(LevelReader level, BlockPos pos, int radius) {
		for (BlockPos p : BlockPos.betweenClosed(pos.offset(-radius, -radius / 2, -radius), pos.offset(radius, radius / 2, radius))) {
			if (p.distSqr(pos) <= radius * radius && level.getBlockState(p).is(ScubaCreatures.SEA_ANEMONE)) {
				return true;
			}
		}
		return false;
	}

	/** Frogfish sit on rock or coral (or right beside coral), never on bare sand. */
	private static boolean isMimicryBed(LevelReader level, BlockPos pos) {
		BlockPos below = pos.below();
		BlockState floor = level.getBlockState(below);
		if (!floor.isFaceSturdy(level, below, Direction.UP)) {
			return false;
		}
		if (floor.is(BlockTags.BASE_STONE_OVERWORLD) || floor.is(BlockTags.CORAL_BLOCKS) || isModCoral(floor)) {
			return true;
		}
		for (Direction d : Direction.Plane.HORIZONTAL) {
			BlockState side = level.getBlockState(pos.relative(d));
			if (side.is(BlockTags.CORALS) || side.is(BlockTags.CORAL_BLOCKS) || side.is(BlockTags.BASE_STONE_OVERWORLD) || isModCoral(side)) {
				return true;
			}
		}
		return false;
	}

	private static boolean isModCoral(BlockState state) {
		return state.is(ScubaReefs.TABLE_CORAL) || state.is(ScubaReefs.BRAIN_CORAL)
				|| state.is(ScubaReefs.BRANCH_CORAL) || state.is(ScubaReefs.FAN_CORAL);
	}

	private static void registerSpawnEgg(String path, EntityType<? extends Mob> type) {
		ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, ScubaDive.id(path));
		Registry.register(BuiltInRegistries.ITEM, key, new SpawnEggItem(new Item.Properties().setId(key).spawnEgg(type)));
	}
}
