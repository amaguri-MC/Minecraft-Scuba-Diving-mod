package net.scubadive.creature.temperate;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.fish.AbstractFish;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.levelgen.Heightmap;
import net.scubadive.ScubaDive;
import net.scubadive.creature.reef.ReefLoneFish;
import net.scubadive.creature.reef.ReefProfile;
import net.scubadive.creature.reef.ReefSchoolFish;
import net.scubadive.creature.spawn.SeaZones;

/**
 * Temperate-sea, open-ocean and cold fresh-water fish: sardine, red sea bream, striped beakfish,
 * ocean sunfish and arctic char. The small fish reuse the reef fish behaviour with their own profiles.
 */
public final class TemperateFish {
	//                                                                          id                  flee  speed home  school hide
	/** Sardines: big tight schools that roam the open mid-water. */
	public static final ReefProfile SARDINE_PROFILE = new ReefProfile("sardine", 6.0, 2.2, 16.0, 30, false, ReefProfile.Trait.SCHOOLING, 0.0);
	/** Red sea bream: small loose groups over rock and sand. */
	public static final ReefProfile RED_SEA_BREAM_PROFILE = new ReefProfile("red_sea_bream", 5.0, 1.8, 8.0, 5, false, ReefProfile.Trait.SCHOOLING, 0.0);
	/** Striped beakfish: a solitary, bold rock dweller that sees off divers near its spot. */
	public static final ReefProfile STRIPED_BEAKFISH_PROFILE = new ReefProfile("striped_beakfish", 1.2, 1.6, 5.0, 1, false, ReefProfile.Trait.TERRITORIAL, 0.0);
	/** Arctic char: a wary, solitary cold-water salmonid. */
	public static final ReefProfile ARCTIC_CHAR_PROFILE = new ReefProfile("arctic_char", 4.5, 2.0, 10.0, 1, false, ReefProfile.Trait.NONE, 0.0);

	public static final ResourceKey<EntityType<?>> OCEAN_SUNFISH_KEY = key("ocean_sunfish");

	// Hitbox height = 2 * body centre offset * model scale / 16 (see TemperateFishModels).
	public static final EntityType<ReefSchoolFish> SARDINE = school(SARDINE_PROFILE, 0.2f, 0.15f, 2.0,
			(type, level, reason, pos, random) -> midWater(level, pos));
	public static final EntityType<ReefSchoolFish> RED_SEA_BREAM = school(RED_SEA_BREAM_PROFILE, 0.4f, 0.35f, 5.0,
			(type, level, reason, pos, random) -> nearFloor(level, pos, false));
	public static final EntityType<ReefLoneFish> STRIPED_BEAKFISH = lone(STRIPED_BEAKFISH_PROFILE, 0.4f, 0.36f, 6.0,
			(type, level, reason, pos, random) -> nearFloor(level, pos, true));
	public static final EntityType<ReefLoneFish> ARCTIC_CHAR = lone(ARCTIC_CHAR_PROFILE, 0.3f, 0.2f, 4.0,
			(type, level, reason, pos, random) -> coldFreshWater(level, pos));

	public static final EntityType<OceanSunfish> OCEAN_SUNFISH = FabricEntityType.Builder.createMob(OceanSunfish::new, MobCategory.WATER_CREATURE, mob -> mob
					.defaultAttributes(OceanSunfish::sunfishAttributes)
					.spawnPlacement(SpawnPlacementTypes.IN_WATER, Heightmap.Types.OCEAN_FLOOR, SeaZones.wild((type, level, reason, pos, random) ->
							deepWater(level, pos) && (reason != EntitySpawnReason.NATURAL || random.nextInt(2) == 0))))
			.sized(1.1f, 2.2f)
			.clientTrackingRange(12)
			.build(OCEAN_SUNFISH_KEY);

	private TemperateFish() {
	}

	private static ResourceKey<EntityType<?>> key(String id) {
		return ResourceKey.create(Registries.ENTITY_TYPE, ScubaDive.id(id));
	}

	private static EntityType<ReefSchoolFish> school(ReefProfile p, float w, float h, double health,
			SpawnPlacements.SpawnPredicate<ReefSchoolFish> rule) {
		EntityType.EntityFactory<ReefSchoolFish> factory = (t, level) -> new ReefSchoolFish(t, level, p);
		return FabricEntityType.Builder.createMob(factory, MobCategory.WATER_AMBIENT, mob -> mob
						.defaultAttributes(() -> AbstractFish.createAttributes().add(Attributes.MAX_HEALTH, health))
						.spawnPlacement(SpawnPlacementTypes.IN_WATER, Heightmap.Types.OCEAN_FLOOR, SeaZones.wild(rule)))
				.sized(w, h)
				.clientTrackingRange(8)
				.build(key(p.id()));
	}

	private static EntityType<ReefLoneFish> lone(ReefProfile p, float w, float h, double health,
			SpawnPlacements.SpawnPredicate<ReefLoneFish> rule) {
		EntityType.EntityFactory<ReefLoneFish> factory = (t, level) -> new ReefLoneFish(t, level, p);
		return FabricEntityType.Builder.createMob(factory, MobCategory.WATER_AMBIENT, mob -> mob
						.defaultAttributes(() -> AbstractFish.createAttributes().add(Attributes.MAX_HEALTH, health))
						.spawnPlacement(SpawnPlacementTypes.IN_WATER, Heightmap.Types.OCEAN_FLOOR, SeaZones.wild(rule)))
				.sized(w, h)
				.clientTrackingRange(8)
				.build(key(p.id()));
	}

	private static boolean water(LevelReader level, BlockPos pos) {
		return level.getFluidState(pos).is(FluidTags.WATER);
	}

	/** Open mid-water: below the surface, with water two cells above and three below. */
	private static boolean midWater(LevelReader level, BlockPos pos) {
		if (pos.getY() > level.getSeaLevel() - 3) {
			return false;
		}
		for (int dy = -3; dy <= 2; dy++) {
			if (!water(level, pos.above(dy))) {
				return false;
			}
		}
		return true;
	}

	/** Mid-water a little above a sturdy floor (at most 6 cells down); rockOnly refuses sand. */
	private static boolean nearFloor(LevelReader level, BlockPos pos, boolean rockOnly) {
		if (pos.getY() > level.getSeaLevel() - 3 || !water(level, pos) || !water(level, pos.above())) {
			return false;
		}
		BlockPos f = SeaZones.floorWater(level, pos, 6);
		if (f == null) {
			return false;
		}
		BlockPos below = f.below();
		if (!level.getBlockState(below).isFaceSturdy(level, below, Direction.UP)) {
			return false;
		}
		return !rockOnly || !level.getBlockState(below).is(BlockTags.SAND);
	}

	/** Deep open water: eight cells of water below, three above, well under the surface. */
	private static boolean deepWater(LevelReader level, BlockPos pos) {
		if (pos.getY() > level.getSeaLevel() - 5) {
			return false;
		}
		for (int dy = -8; dy <= 3; dy++) {
			if (!water(level, pos.above(dy))) {
				return false;
			}
		}
		return true;
	}

	/** Fresh water in a cold biome (frozen rivers, lakes in snowy and taiga lands), open to the sky. */
	private static boolean coldFreshWater(LevelReader level, BlockPos pos) {
		if (!water(level, pos) || !water(level, pos.above())) {
			return false;
		}
		if (level.getBiome(pos).value().getBaseTemperature() >= 0.35f) {
			return false;
		}
		return level.canSeeSkyFromBelowWater(pos);
	}

	public static void init() {
		register(SARDINE, SARDINE_PROFILE.id());
		register(RED_SEA_BREAM, RED_SEA_BREAM_PROFILE.id());
		register(STRIPED_BEAKFISH, STRIPED_BEAKFISH_PROFILE.id());
		register(ARCTIC_CHAR, ARCTIC_CHAR_PROFILE.id());
		register(OCEAN_SUNFISH, "ocean_sunfish");

		// Temperate sea: sardine schools in mid-water, sea bream over the floor, beakfish by the rocks.
		SeaZones.addSpawn(SeaZones.Region.TEMPERATE, MobCategory.WATER_AMBIENT, SARDINE, 4, 15, 30);
		SeaZones.addSpawn(SeaZones.Region.TEMPERATE, MobCategory.WATER_AMBIENT, RED_SEA_BREAM, 4, 2, 5);
		SeaZones.addSpawn(SeaZones.Region.TEMPERATE, MobCategory.WATER_AMBIENT, STRIPED_BEAKFISH, 3, 1, 1);
		// Sunfish: rare, in the temperate and deep open ocean (halved again in its placement rule).
		SeaZones.addSpawn(SeaZones.Region.TEMPERATE, MobCategory.WATER_CREATURE, OCEAN_SUNFISH, 1, 1, 1);
		SeaZones.addSpawn(SeaZones.Region.DEEP, MobCategory.WATER_CREATURE, OCEAN_SUNFISH, 1, 1, 1);
		// Arctic char: rivers (the placement rule keeps only cold ones) and lakes in cold land biomes.
		SeaZones.addSpawn(SeaZones.Region.RIVER, MobCategory.WATER_AMBIENT, ARCTIC_CHAR, 6, 1, 3);
		SeaZones.addSpawn(SeaZones.Region.CAVES, MobCategory.WATER_AMBIENT, ARCTIC_CHAR, 2, 1, 2);
	}

	private static void register(EntityType<? extends Mob> type, String id) {
		Registry.register(BuiltInRegistries.ENTITY_TYPE, key(id), type);
		ResourceKey<Item> egg = ResourceKey.create(Registries.ITEM, ScubaDive.id(id + "_spawn_egg"));
		Registry.register(BuiltInRegistries.ITEM, egg, new SpawnEggItem(new Item.Properties().setId(egg).spawnEgg(type)));
	}
}
