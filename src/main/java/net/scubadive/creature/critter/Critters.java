package net.scubadive.creature.critter;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityType;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.levelgen.Heightmap;

import net.scubadive.ScubaDive;
import net.scubadive.creature.Nudibranch;

/** Sea stars, the long-spined sea urchin and jellyfish. */
public final class Critters {
	public static final EntityType<Starfish> BLUE_STARFISH = starfish("blue_starfish", 0.45f, 0.08f);
	public static final EntityType<Starfish> CUSHION_STAR = starfish("cushion_star", 0.35f, 0.15f);
	public static final EntityType<Starfish> CROWN_OF_THORNS = starfish("crown_of_thorns", 0.55f, 0.12f);

	public static final EntityType<SeaUrchin> SEA_URCHIN = FabricEntityType.Builder.createMob(SeaUrchin::new, MobCategory.WATER_AMBIENT, mob -> mob
					.defaultAttributes(() -> Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 4.0).add(Attributes.MOVEMENT_SPEED, 0.0))
					.spawnPlacement(SpawnPlacementTypes.IN_WATER, Heightmap.Types.OCEAN_FLOOR, (type, level, reason, pos, random) ->
							onFloor(level, pos)))
			.sized(0.5f, 0.35f)
			.clientTrackingRange(8)
			.build(key("sea_urchin"));

	public static final EntityType<Jellyfish> MOON_JELLY = jelly("moon_jelly", 0.35f, 0.45f);
	public static final EntityType<Jellyfish> SEA_NETTLE = jelly("sea_nettle", 0.35f, 0.8f);
	public static final EntityType<Jellyfish> SPOTTED_JELLY = jelly("spotted_jelly", 0.4f, 0.5f);

	private Critters() {
	}

	private static ResourceKey<EntityType<?>> key(String id) {
		return ResourceKey.create(Registries.ENTITY_TYPE, ScubaDive.id(id));
	}

	private static EntityType<Starfish> starfish(String id, float width, float height) {
		return FabricEntityType.Builder.createMob(Starfish::new, MobCategory.WATER_AMBIENT, mob -> mob
						.defaultAttributes(() -> Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 4.0).add(Attributes.MOVEMENT_SPEED, 0.003))
						.spawnPlacement(SpawnPlacementTypes.IN_WATER, Heightmap.Types.OCEAN_FLOOR, (type, level, reason, pos, random) ->
								id.equals("crown_of_thorns")
										? onFloor(level, pos) && Nudibranch.isReefFloor(level.getBlockState(pos.below()))
										: onFloor(level, pos)))
				.sized(width, height)
				.clientTrackingRange(8)
				.build(key(id));
	}

	private static EntityType<Jellyfish> jelly(String id, float width, float height) {
		return FabricEntityType.Builder.createMob(Jellyfish::new, MobCategory.WATER_AMBIENT, mob -> mob
						.defaultAttributes(() -> Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 3.0).add(Attributes.MOVEMENT_SPEED, 0.01))
						.spawnPlacement(SpawnPlacementTypes.IN_WATER, Heightmap.Types.OCEAN_FLOOR, (type, level, reason, pos, random) ->
								openWater(level, pos)))
				.sized(width, height)
				.clientTrackingRange(10)
				.build(key(id));
	}

	/** Water at the position with a sturdy floor right below. */
	private static boolean onFloor(LevelReader level, BlockPos pos) {
		return level.getFluidState(pos).is(FluidTags.WATER)
				&& level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP);
	}

	/** A few blocks of open water around the position. */
	private static boolean openWater(LevelReader level, BlockPos pos) {
		for (int dy = 0; dy <= 3; dy++) {
			if (!level.getFluidState(pos.above(dy)).is(FluidTags.WATER)) {
				return false;
			}
		}
		return true;
	}

	public static void init() {
		register("blue_starfish", BLUE_STARFISH);
		register("cushion_star", CUSHION_STAR);
		register("crown_of_thorns", CROWN_OF_THORNS);
		register("sea_urchin", SEA_URCHIN);
		register("moon_jelly", MOON_JELLY);
		register("sea_nettle", SEA_NETTLE);
		register("spotted_jelly", SPOTTED_JELLY);

		var warm = BiomeSelectors.includeByKey(Biomes.WARM_OCEAN, Biomes.LUKEWARM_OCEAN, Biomes.DEEP_LUKEWARM_OCEAN);
		var temperate = BiomeSelectors.includeByKey(Biomes.OCEAN, Biomes.DEEP_OCEAN, Biomes.LUKEWARM_OCEAN, Biomes.DEEP_LUKEWARM_OCEAN);
		var cool = BiomeSelectors.includeByKey(Biomes.OCEAN, Biomes.DEEP_OCEAN, Biomes.COLD_OCEAN, Biomes.DEEP_COLD_OCEAN);
		BiomeModifications.addSpawn(warm, MobCategory.WATER_AMBIENT, BLUE_STARFISH, 6, 1, 2);
		BiomeModifications.addSpawn(temperate, MobCategory.WATER_AMBIENT, CUSHION_STAR, 4, 1, 2);
		BiomeModifications.addSpawn(warm, MobCategory.WATER_AMBIENT, CROWN_OF_THORNS, 2, 1, 1);
		BiomeModifications.addSpawn(warm, MobCategory.WATER_AMBIENT, SEA_URCHIN, 6, 2, 5);
		BiomeModifications.addSpawn(temperate, MobCategory.WATER_AMBIENT, MOON_JELLY, 4, 3, 6);
		BiomeModifications.addSpawn(cool, MobCategory.WATER_AMBIENT, SEA_NETTLE, 2, 1, 3);
		BiomeModifications.addSpawn(warm, MobCategory.WATER_AMBIENT, SPOTTED_JELLY, 3, 2, 4);
	}

	private static void register(String id, EntityType<? extends Mob> type) {
		Registry.register(BuiltInRegistries.ENTITY_TYPE, key(id), type);
		ResourceKey<Item> egg = ResourceKey.create(Registries.ITEM, ScubaDive.id(id + "_spawn_egg"));
		Registry.register(BuiltInRegistries.ITEM, egg, new SpawnEggItem(new Item.Properties().setId(egg).spawnEgg(type)));
	}
}
