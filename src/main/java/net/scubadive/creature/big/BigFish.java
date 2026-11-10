package net.scubadive.creature.big;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.levelgen.Heightmap;
import net.scubadive.ScubaDive;

/** Registration entry for the big fish: sharks, barracuda, bigeye trevally, manta ray and stingray. */
public final class BigFish {
	public static final ResourceKey<EntityType<?>> REEF_SHARK_KEY = ResourceKey.create(Registries.ENTITY_TYPE, ScubaDive.id("reef_shark"));
	public static final ResourceKey<EntityType<?>> HAMMERHEAD_SHARK_KEY = ResourceKey.create(Registries.ENTITY_TYPE, ScubaDive.id("hammerhead_shark"));
	public static final ResourceKey<EntityType<?>> WHALE_SHARK_KEY = ResourceKey.create(Registries.ENTITY_TYPE, ScubaDive.id("whale_shark"));
	public static final ResourceKey<EntityType<?>> BARRACUDA_KEY = ResourceKey.create(Registries.ENTITY_TYPE, ScubaDive.id("barracuda"));

	public static final ResourceKey<EntityType<?>> BIGEYE_TREVALLY_KEY = ResourceKey.create(Registries.ENTITY_TYPE, ScubaDive.id("bigeye_trevally"));
	public static final ResourceKey<EntityType<?>> MANTA_RAY_KEY = ResourceKey.create(Registries.ENTITY_TYPE, ScubaDive.id("manta_ray"));
	public static final ResourceKey<EntityType<?>> STINGRAY_KEY = ResourceKey.create(Registries.ENTITY_TYPE, ScubaDive.id("stingray"));

	private static final ResourceKey<Biome> DEEP_TRENCH = ResourceKey.create(Registries.BIOME, ScubaDive.id("deep_trench"));

	public static final EntityType<ReefShark> REEF_SHARK = FabricEntityType.Builder.createMob(ReefShark::new, MobCategory.WATER_CREATURE, mob -> mob
					.defaultAttributes(ReefShark::sharkAttributes)
					.spawnPlacement(SpawnPlacementTypes.IN_WATER, Heightmap.Types.OCEAN_FLOOR, (type, level, reason, pos, random) ->
							openWater(level, pos, 6)))
			.sized(1.4f, 0.7f)
			.clientTrackingRange(8)
			.build(REEF_SHARK_KEY);

	public static final EntityType<HammerheadShark> HAMMERHEAD_SHARK = FabricEntityType.Builder.createMob(HammerheadShark::new, MobCategory.WATER_CREATURE, mob -> mob
					.defaultAttributes(HammerheadShark::hammerheadAttributes)
					.spawnPlacement(SpawnPlacementTypes.IN_WATER, Heightmap.Types.OCEAN_FLOOR, (type, level, reason, pos, random) ->
							openWater(level, pos, 10) && (reason != EntitySpawnReason.NATURAL || random.nextInt(2) == 0)))
			.sized(2.0f, 1.0f)
			.clientTrackingRange(10)
			.build(HAMMERHEAD_SHARK_KEY);

	public static final EntityType<WhaleShark> WHALE_SHARK = FabricEntityType.Builder.createMob(WhaleShark::new, MobCategory.WATER_CREATURE, mob -> mob
					.defaultAttributes(WhaleShark::whaleSharkAttributes)
					.spawnPlacement(SpawnPlacementTypes.IN_WATER, Heightmap.Types.OCEAN_FLOOR, (type, level, reason, pos, random) ->
							openWater(level, pos, 12) && (reason != EntitySpawnReason.NATURAL || random.nextInt(3) == 0)))
			.sized(5.0f, 3.2f)
			.clientTrackingRange(16)
			.build(WHALE_SHARK_KEY);

	public static final EntityType<Barracuda> BARRACUDA = FabricEntityType.Builder.createMob(Barracuda::new, MobCategory.WATER_CREATURE, mob -> mob
					.defaultAttributes(Barracuda::barracudaAttributes)
					.spawnPlacement(SpawnPlacementTypes.IN_WATER, Heightmap.Types.OCEAN_FLOOR, (type, level, reason, pos, random) ->
							openWater(level, pos, 5)))
			.sized(0.9f, 0.5f)
			.clientTrackingRange(8)
			.build(BARRACUDA_KEY);

	public static final EntityType<BigeyeTrevally> BIGEYE_TREVALLY = FabricEntityType.Builder.createMob(BigeyeTrevally::new, MobCategory.WATER_AMBIENT, mob -> mob
					.defaultAttributes(BigeyeTrevally::trevallyAttributes)
					.spawnPlacement(SpawnPlacementTypes.IN_WATER, Heightmap.Types.OCEAN_FLOOR, (type, level, reason, pos, random) ->
							openWater(level, pos, 5)))
			.sized(0.6f, 0.6f)
			.clientTrackingRange(8)
			.build(BIGEYE_TREVALLY_KEY);

	public static final EntityType<MantaRay> MANTA_RAY = FabricEntityType.Builder.createMob(MantaRay::new, MobCategory.WATER_CREATURE, mob -> mob
					.defaultAttributes(MantaRay::mantaAttributes)
					.spawnPlacement(SpawnPlacementTypes.IN_WATER, Heightmap.Types.OCEAN_FLOOR, (type, level, reason, pos, random) ->
							openWater(level, pos, 8) && (reason != EntitySpawnReason.NATURAL || random.nextInt(2) == 0)))
			.sized(3.6f, 1.2f)
			.clientTrackingRange(12)
			.build(MANTA_RAY_KEY);

	public static final EntityType<Stingray> STINGRAY = FabricEntityType.Builder.createMob(Stingray::new, MobCategory.WATER_AMBIENT, mob -> mob
					.defaultAttributes(Stingray::stingrayAttributes)
					.spawnPlacement(SpawnPlacementTypes.IN_WATER, Heightmap.Types.OCEAN_FLOOR, (type, level, reason, pos, random) ->
							level.getFluidState(pos).is(FluidTags.WATER)
									&& level.getFluidState(pos.above()).is(FluidTags.WATER)
									&& level.getBlockState(pos.below()).is(BlockTags.SAND)))
			.sized(1.4f, 0.4f)
			.clientTrackingRange(8)
			.build(STINGRAY_KEY);

	private BigFish() {
	}

	/** Open water: the spawn cell and {@code depth - 1} cells below it plus one above are all water, and not at the very surface. */
	private static boolean openWater(LevelReader level, BlockPos pos, int depth) {
		if (!level.getFluidState(pos.above()).is(FluidTags.WATER) || pos.getY() > level.getSeaLevel() - 2) {
			return false;
		}
		for (int i = 0; i < depth; i++) {
			if (!level.getFluidState(pos.below(i)).is(FluidTags.WATER)) {
				return false;
			}
		}
		return true;
	}

	public static void init() {
		Registry.register(BuiltInRegistries.ENTITY_TYPE, REEF_SHARK_KEY, REEF_SHARK);
		Registry.register(BuiltInRegistries.ENTITY_TYPE, HAMMERHEAD_SHARK_KEY, HAMMERHEAD_SHARK);
		Registry.register(BuiltInRegistries.ENTITY_TYPE, WHALE_SHARK_KEY, WHALE_SHARK);
		Registry.register(BuiltInRegistries.ENTITY_TYPE, BARRACUDA_KEY, BARRACUDA);

		Registry.register(BuiltInRegistries.ENTITY_TYPE, BIGEYE_TREVALLY_KEY, BIGEYE_TREVALLY);
		Registry.register(BuiltInRegistries.ENTITY_TYPE, MANTA_RAY_KEY, MANTA_RAY);
		Registry.register(BuiltInRegistries.ENTITY_TYPE, STINGRAY_KEY, STINGRAY);

		registerSpawnEgg("reef_shark_spawn_egg", REEF_SHARK);
		registerSpawnEgg("hammerhead_shark_spawn_egg", HAMMERHEAD_SHARK);
		registerSpawnEgg("whale_shark_spawn_egg", WHALE_SHARK);
		registerSpawnEgg("barracuda_spawn_egg", BARRACUDA);
		registerSpawnEgg("bigeye_trevally_spawn_egg", BIGEYE_TREVALLY);
		registerSpawnEgg("manta_ray_spawn_egg", MANTA_RAY);
		registerSpawnEgg("stingray_spawn_egg", STINGRAY);

		// Natural spawns: modest weights; the whale shark and hammerhead are further thinned in their placement rules.
		var reefSeas = BiomeSelectors.includeByKey(Biomes.WARM_OCEAN, Biomes.LUKEWARM_OCEAN, Biomes.DEEP_LUKEWARM_OCEAN, Biomes.DEEP_OCEAN);
		BiomeModifications.addSpawn(reefSeas, MobCategory.WATER_CREATURE, REEF_SHARK, 3, 1, 2);

		var warmSeas = BiomeSelectors.includeByKey(Biomes.WARM_OCEAN, Biomes.LUKEWARM_OCEAN, Biomes.DEEP_LUKEWARM_OCEAN);
		BiomeModifications.addSpawn(warmSeas, MobCategory.WATER_CREATURE, BARRACUDA, 3, 6, 12);

		var deepSeas = BiomeSelectors.includeByKey(Biomes.DEEP_OCEAN, Biomes.DEEP_LUKEWARM_OCEAN);
		BiomeModifications.addSpawn(deepSeas, MobCategory.WATER_CREATURE, HAMMERHEAD_SHARK, 1, 4, 8);
		BiomeModifications.addSpawn(BiomeSelectors.includeByKey(DEEP_TRENCH), MobCategory.WATER_CREATURE, HAMMERHEAD_SHARK, 4, 4, 8);

		var whaleSeas = BiomeSelectors.includeByKey(Biomes.WARM_OCEAN, Biomes.DEEP_LUKEWARM_OCEAN, Biomes.DEEP_OCEAN);
		BiomeModifications.addSpawn(whaleSeas, MobCategory.WATER_CREATURE, WHALE_SHARK, 1, 1, 1);

		// Open-water school of 20-40 (big crowds are the hotspot spawner's job), rare manta, sand-dwelling stingray.
		var openSeas = BiomeSelectors.includeByKey(Biomes.WARM_OCEAN, Biomes.LUKEWARM_OCEAN, Biomes.DEEP_LUKEWARM_OCEAN, Biomes.DEEP_OCEAN, Biomes.OCEAN);
		BiomeModifications.addSpawn(openSeas, MobCategory.WATER_AMBIENT, BIGEYE_TREVALLY, 2, 20, 40);
		var mantaSeas = BiomeSelectors.includeByKey(Biomes.WARM_OCEAN, Biomes.LUKEWARM_OCEAN, Biomes.DEEP_LUKEWARM_OCEAN, Biomes.DEEP_OCEAN);
		BiomeModifications.addSpawn(mantaSeas, MobCategory.WATER_CREATURE, MANTA_RAY, 1, 1, 2);
		var sandSeas = BiomeSelectors.includeByKey(Biomes.WARM_OCEAN, Biomes.LUKEWARM_OCEAN, Biomes.DEEP_LUKEWARM_OCEAN, Biomes.OCEAN);
		BiomeModifications.addSpawn(sandSeas, MobCategory.WATER_AMBIENT, STINGRAY, 4, 1, 2);
	}

	private static void registerSpawnEgg(String path, EntityType<? extends Mob> type) {
		ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, ScubaDive.id(path));
		Registry.register(BuiltInRegistries.ITEM, key, new SpawnEggItem(new Item.Properties().setId(key).spawnEgg(type)));
	}
}
