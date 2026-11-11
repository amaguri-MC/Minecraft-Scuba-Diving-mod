package net.scubadive.creature.critter;

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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

import net.scubadive.ScubaDive;
import net.scubadive.creature.Nudibranch;
import net.scubadive.creature.spawn.SeaZones;

/** Sea stars, the long-spined sea urchin and jellyfish. */
public final class Critters {
	public static final EntityType<Starfish> BLUE_STARFISH = starfish("blue_starfish", 0.45f, 0.08f);
	public static final EntityType<Starfish> CUSHION_STAR = starfish("cushion_star", 0.35f, 0.15f);
	public static final EntityType<Starfish> CROWN_OF_THORNS = starfish("crown_of_thorns", 0.55f, 0.12f);

	public static final EntityType<SeaUrchin> SEA_URCHIN = FabricEntityType.Builder.createMob(SeaUrchin::new, MobCategory.UNDERGROUND_WATER_CREATURE, mob -> mob
					.defaultAttributes(() -> Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 4.0).add(Attributes.MOVEMENT_SPEED, 0.0))
					.spawnPlacement(SpawnPlacementTypes.IN_WATER, Heightmap.Types.OCEAN_FLOOR, SeaZones.wild((type, level, reason, pos, random) ->
							onFloor(level, pos))))
			.sized(0.5f, 0.35f)
			.clientTrackingRange(8)
			.build(key("sea_urchin"));

	public static final EntityType<Jellyfish> MOON_JELLY = jelly("moon_jelly", 0.35f, 0.45f);
	public static final EntityType<Jellyfish> SEA_NETTLE = jelly("sea_nettle", 0.35f, 0.8f);
	public static final EntityType<Jellyfish> SPOTTED_JELLY = jelly("spotted_jelly", 0.4f, 0.5f);
	public static final EntityType<Jellyfish> BOX_JELLYFISH = jelly("box_jellyfish", 0.35f, 0.75f);
	public static final EntityType<Jellyfish> COMB_JELLY = jelly("comb_jelly", 0.25f, 0.35f);
	public static final EntityType<Jellyfish> LIONS_MANE_JELLY = jelly("lions_mane_jelly", 1.0f, 2.0f, 8.0, 5);
	public static final EntityType<Jellyfish> FRIED_EGG_JELLY = jelly("fried_egg_jelly", 0.45f, 0.5f);
	/** Rare giant: bell about two blocks across, heavy oral arms; needs deep open water. */
	public static final EntityType<Jellyfish> ECHIZEN_JELLY = jelly("echizen_jelly", 2.2f, 3.0f, 20.0, 7);
	// Aquarium favourites (update-11).
	public static final EntityType<Jellyfish> INDONESIAN_SEA_NETTLE = jelly("indonesian_sea_nettle", 0.4f, 1.0f, 3.0, 4);
	public static final EntityType<Jellyfish> CANNONBALL_JELLY = jelly("cannonball_jelly", 0.45f, 0.5f);
	public static final EntityType<Jellyfish> FLOWER_HAT_JELLY = jelly("flower_hat_jelly", 0.3f, 0.35f);
	public static final EntityType<Jellyfish> BLUE_BLUBBER_JELLY = jelly("blue_blubber_jelly", 0.45f, 0.6f);
	/** Lies bell-down on sandy or muddy floors. */
	public static final EntityType<Jellyfish> UPSIDE_DOWN_JELLY = FabricEntityType.Builder.createMob(Jellyfish::new, MobCategory.AXOLOTLS, mob -> mob
					.defaultAttributes(() -> Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 3.0).add(Attributes.MOVEMENT_SPEED, 0.01))
					.spawnPlacement(SpawnPlacementTypes.IN_WATER, Heightmap.Types.OCEAN_FLOOR, SeaZones.wild((type, level, reason, pos, random) ->
							onSoftFloor(level, pos))))
			.sized(0.5f, 0.3f)
			.clientTrackingRange(10)
			.build(key("upside_down_jelly"));

	private Critters() {
	}

	private static ResourceKey<EntityType<?>> key(String id) {
		return ResourceKey.create(Registries.ENTITY_TYPE, ScubaDive.id(id));
	}

	private static EntityType<Starfish> starfish(String id, float width, float height) {
		return FabricEntityType.Builder.createMob(Starfish::new, MobCategory.UNDERGROUND_WATER_CREATURE, mob -> mob
						.defaultAttributes(() -> Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 4.0).add(Attributes.MOVEMENT_SPEED, 0.003))
						.spawnPlacement(SpawnPlacementTypes.IN_WATER, Heightmap.Types.OCEAN_FLOOR, SeaZones.wild((type, level, reason, pos, random) ->
								id.equals("crown_of_thorns")
										? onReefFloor(level, pos)
										: onFloor(level, pos))))
				.sized(width, height)
				.clientTrackingRange(8)
				.build(key(id));
	}

	private static EntityType<Jellyfish> jelly(String id, float width, float height) {
		return jelly(id, width, height, 3.0, 3);
	}

	/** waterAbove: how many cells above the spawn position must be water too. */
	private static EntityType<Jellyfish> jelly(String id, float width, float height, double health, int waterAbove) {
		return FabricEntityType.Builder.createMob(Jellyfish::new, MobCategory.AXOLOTLS, mob -> mob
						.defaultAttributes(() -> Mob.createMobAttributes().add(Attributes.MAX_HEALTH, health).add(Attributes.MOVEMENT_SPEED, 0.01))
						.spawnPlacement(SpawnPlacementTypes.IN_WATER, Heightmap.Types.OCEAN_FLOOR, SeaZones.wild((type, level, reason, pos, random) ->
								// The giant is rarer still: most otherwise valid spots are refused.
								openWater(level, pos, waterAbove) && (!id.equals("echizen_jelly") || random.nextInt(3) == 0))))
				.sized(width, height)
				.clientTrackingRange(10)
				.build(key(id));
	}

	/** A sturdy floor at most 3 water cells below the position (the critter sinks onto it). */
	private static boolean onFloor(LevelReader level, BlockPos pos) {
		BlockPos f = SeaZones.floorWater(level, pos, 3);
		return f != null && level.getBlockState(f.below()).isFaceSturdy(level, f.below(), Direction.UP);
	}

	/** Like {@link #onFloor} but on reef rock or coral only. */
	private static boolean onReefFloor(LevelReader level, BlockPos pos) {
		BlockPos f = SeaZones.floorWater(level, pos, 3);
		return f != null && level.getBlockState(f.below()).isFaceSturdy(level, f.below(), Direction.UP)
				&& Nudibranch.isReefFloor(level.getBlockState(f.below()));
	}

	/** Sand or mud floor at most 3 water cells below (upside-down jelly). */
	private static boolean onSoftFloor(LevelReader level, BlockPos pos) {
		BlockPos f = SeaZones.floorWater(level, pos, 3);
		if (f == null) {
			return false;
		}
		BlockState floor = level.getBlockState(f.below());
		return floor.is(BlockTags.SAND) || floor.is(BlockTags.MUD);
	}

	/** A few blocks of open water around the position. */
	private static boolean openWater(LevelReader level, BlockPos pos, int up) {
		for (int dy = 0; dy <= up; dy++) {
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
		register("box_jellyfish", BOX_JELLYFISH);
		register("comb_jelly", COMB_JELLY);
		register("lions_mane_jelly", LIONS_MANE_JELLY);
		register("fried_egg_jelly", FRIED_EGG_JELLY);
		register("echizen_jelly", ECHIZEN_JELLY);
		register("indonesian_sea_nettle", INDONESIAN_SEA_NETTLE);
		register("cannonball_jelly", CANNONBALL_JELLY);
		register("flower_hat_jelly", FLOWER_HAT_JELLY);
		register("blue_blubber_jelly", BLUE_BLUBBER_JELLY);
		register("upside_down_jelly", UPSIDE_DOWN_JELLY);

		// By water temperature. Floor critters use the UNDERGROUND_WATER_CREATURE cap and jellies the AXOLOTLS cap
		// (both free in the open sea), leaving WATER_AMBIENT to the fish.
		SeaZones.addSpawn(SeaZones.Region.WARM, MobCategory.UNDERGROUND_WATER_CREATURE, BLUE_STARFISH, 6, 1, 2);
		SeaZones.addSpawn(SeaZones.Region.WARM, MobCategory.UNDERGROUND_WATER_CREATURE, CROWN_OF_THORNS, 2, 1, 1);
		SeaZones.addSpawn(SeaZones.Region.WARM, MobCategory.UNDERGROUND_WATER_CREATURE, SEA_URCHIN, 6, 2, 4);
		SeaZones.addSpawn(SeaZones.Region.WARM, MobCategory.AXOLOTLS, SPOTTED_JELLY, 4, 2, 3);
		SeaZones.addSpawn(SeaZones.Region.TEMPERATE, MobCategory.UNDERGROUND_WATER_CREATURE, CUSHION_STAR, 6, 1, 2);
		SeaZones.addSpawn(SeaZones.Region.TEMPERATE, MobCategory.AXOLOTLS, MOON_JELLY, 8, 2, 5);
		SeaZones.addSpawn(SeaZones.Region.COLD, MobCategory.UNDERGROUND_WATER_CREATURE, CUSHION_STAR, 8, 1, 3);
		SeaZones.addSpawn(SeaZones.Region.COLD, MobCategory.AXOLOTLS, SEA_NETTLE, 6, 1, 3);
		SeaZones.addSpawn(SeaZones.Region.WARM, MobCategory.AXOLOTLS, BOX_JELLYFISH, 3, 1, 2);
		SeaZones.addSpawn(SeaZones.Region.TEMPERATE, MobCategory.AXOLOTLS, COMB_JELLY, 5, 2, 4);
		SeaZones.addSpawn(SeaZones.Region.COLD, MobCategory.AXOLOTLS, COMB_JELLY, 4, 2, 4);
		SeaZones.addSpawn(SeaZones.Region.TEMPERATE, MobCategory.AXOLOTLS, FRIED_EGG_JELLY, 3, 1, 2);
		SeaZones.addSpawn(SeaZones.Region.COLD, MobCategory.AXOLOTLS, LIONS_MANE_JELLY, 2, 1, 1);
		// The rare giant: weight 1, always alone.
		SeaZones.addSpawn(SeaZones.Region.TEMPERATE, MobCategory.AXOLOTLS, ECHIZEN_JELLY, 1, 1, 1);
		SeaZones.addSpawn(SeaZones.Region.WARM, MobCategory.AXOLOTLS, INDONESIAN_SEA_NETTLE, 3, 1, 2);
		SeaZones.addSpawn(SeaZones.Region.TROPICAL_SHALLOW, MobCategory.AXOLOTLS, UPSIDE_DOWN_JELLY, 3, 2, 5);
		SeaZones.addSpawn(SeaZones.Region.WARM, MobCategory.AXOLOTLS, CANNONBALL_JELLY, 3, 2, 4);
		SeaZones.addSpawn(SeaZones.Region.TEMPERATE, MobCategory.AXOLOTLS, CANNONBALL_JELLY, 3, 2, 4);
		// Flower hat: a little rare, alone or in pairs.
		SeaZones.addSpawn(SeaZones.Region.TEMPERATE, MobCategory.AXOLOTLS, FLOWER_HAT_JELLY, 1, 1, 2);
		// Blue blubber drifts in loose swarms.
		SeaZones.addSpawn(SeaZones.Region.WARM, MobCategory.AXOLOTLS, BLUE_BLUBBER_JELLY, 3, 3, 6);
	}

	private static void register(String id, EntityType<? extends Mob> type) {
		Registry.register(BuiltInRegistries.ENTITY_TYPE, key(id), type);
		ResourceKey<Item> egg = ResourceKey.create(Registries.ITEM, ScubaDive.id(id + "_spawn_egg"));
		Registry.register(BuiltInRegistries.ITEM, egg, new SpawnEggItem(new Item.Properties().setId(egg).spawnEgg(type)));
	}
}
