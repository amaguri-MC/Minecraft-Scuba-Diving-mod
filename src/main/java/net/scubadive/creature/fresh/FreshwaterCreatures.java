package net.scubadive.creature.fresh;

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
 * Fresh-water creatures of rivers, lakes, swamps and flooded caves: rainbow trout, carp, catfish,
 * the eyeless cave fish and the crayfish. The fish reuse the reef fish behaviour with their own profiles.
 */
public final class FreshwaterCreatures {
	//                                                                        id                flee  speed home school hide
	public static final ReefProfile TROUT_PROFILE = new ReefProfile("rainbow_trout", 4.5, 2.0, 10.0, 1, false, ReefProfile.Trait.NONE, 0.0);
	/** Carp cruise slowly and are not very shy. */
	public static final ReefProfile CARP_PROFILE = new ReefProfile("carp", 3.0, 1.3, 8.0, 1, false, ReefProfile.Trait.NONE, 0.35);
	/** Catfish lie still in a sheltered spot on the bottom (lurker). */
	public static final ReefProfile CATFISH_PROFILE = new ReefProfile("catfish", 2.5, 1.2, 2.5, 1, false, ReefProfile.Trait.LURKER, 0.3);
	public static final ReefProfile CAVEFISH_PROFILE = new ReefProfile("blind_cavefish", 2.5, 1.7, 6.0, 12, false, ReefProfile.Trait.SCHOOLING, 0.0);

	public static final ResourceKey<EntityType<?>> CRAYFISH_KEY = key("crayfish");

	public static final EntityType<ReefLoneFish> RAINBOW_TROUT = lone(TROUT_PROFILE, MobCategory.WATER_AMBIENT, 0.35f, 0.25f, 3.0,
			(type, level, reason, pos, random) -> openWater(level, pos));
	public static final EntityType<ReefLoneFish> CARP = lone(CARP_PROFILE, MobCategory.WATER_AMBIENT, 0.5f, 0.38f, 4.0,
			(type, level, reason, pos, random) -> openWater(level, pos));
	public static final EntityType<ReefLoneFish> CATFISH = lone(CATFISH_PROFILE, MobCategory.UNDERGROUND_WATER_CREATURE, 0.6f, 0.3f, 8.0,
			(type, level, reason, pos, random) -> onFloor(level, pos));
	public static final EntityType<ReefSchoolFish> BLIND_CAVEFISH = cavefish();

	public static final EntityType<Crayfish> CRAYFISH = FabricEntityType.Builder.createMob(Crayfish::new, MobCategory.UNDERGROUND_WATER_CREATURE, mob -> mob
					.defaultAttributes(() -> Mob.createMobAttributes()
							.add(Attributes.MAX_HEALTH, 4.0)
							.add(Attributes.MOVEMENT_SPEED, 0.02))
					.spawnPlacement(SpawnPlacementTypes.IN_WATER, Heightmap.Types.OCEAN_FLOOR, SeaZones.wild((type, level, reason, pos, random) ->
							onFloor(level, pos))))
			.sized(0.3f, 0.15f)
			.clientTrackingRange(6)
			.build(CRAYFISH_KEY);

	private FreshwaterCreatures() {
	}

	private static ResourceKey<EntityType<?>> key(String id) {
		return ResourceKey.create(Registries.ENTITY_TYPE, ScubaDive.id(id));
	}

	private static EntityType<ReefLoneFish> lone(ReefProfile p, MobCategory category, float w, float h, double health,
			SpawnPlacements.SpawnPredicate<ReefLoneFish> rule) {
		EntityType.EntityFactory<ReefLoneFish> factory = (t, level) -> new ReefLoneFish(t, level, p);
		return FabricEntityType.Builder.createMob(factory, category, mob -> mob
						.defaultAttributes(() -> {
							var attributes = AbstractFish.createAttributes().add(Attributes.MAX_HEALTH, health);
							if (p.moveSpeed() > 0.0) {
								attributes.add(Attributes.MOVEMENT_SPEED, p.moveSpeed());
							}
							return attributes;
						})
						.spawnPlacement(SpawnPlacementTypes.IN_WATER, Heightmap.Types.OCEAN_FLOOR, SeaZones.wild(rule)))
				.sized(w, h)
				.clientTrackingRange(8)
				.build(key(p.id()));
	}

	private static EntityType<ReefSchoolFish> cavefish() {
		EntityType.EntityFactory<ReefSchoolFish> factory = (t, level) -> new ReefSchoolFish(t, level, CAVEFISH_PROFILE);
		return FabricEntityType.Builder.createMob(factory, MobCategory.UNDERGROUND_WATER_CREATURE, mob -> mob
						.defaultAttributes(AbstractFish::createAttributes)
						.spawnPlacement(SpawnPlacementTypes.IN_WATER, Heightmap.Types.OCEAN_FLOOR, SeaZones.wild((type, level, reason, pos, random) ->
								inDarkCave(level, pos))))
				.sized(0.2f, 0.15f)
				.clientTrackingRange(8)
				.build(key(CAVEFISH_PROFILE.id()));
	}

	/** Water here and above. */
	private static boolean openWater(LevelReader level, BlockPos pos) {
		return level.getFluidState(pos).is(FluidTags.WATER) && level.getFluidState(pos.above()).is(FluidTags.WATER);
	}

	/** A sturdy bottom at most 3 water cells below. */
	private static boolean onFloor(LevelReader level, BlockPos pos) {
		BlockPos f = SeaZones.floorWater(level, pos, 3);
		return f != null && level.getBlockState(f.below()).isFaceSturdy(level, f.below(), Direction.UP);
	}

	/** Flooded cave: water well below sea level with no view of the sky. */
	private static boolean inDarkCave(LevelReader level, BlockPos pos) {
		return openWater(level, pos) && pos.getY() < level.getSeaLevel() - 8 && !level.canSeeSky(pos);
	}

	public static void init() {
		register(RAINBOW_TROUT, TROUT_PROFILE.id());
		register(CARP, CARP_PROFILE.id());
		register(CATFISH, CATFISH_PROFILE.id());
		register(BLIND_CAVEFISH, CAVEFISH_PROFILE.id());
		register(CRAYFISH, "crayfish");

		// Fresh water only (rivers, frozen rivers, swamps; lakes elsewhere are filled by the hotspot spawner).
		SeaZones.addSpawn(SeaZones.Region.RIVER, MobCategory.WATER_AMBIENT, RAINBOW_TROUT, 8, 1, 3);
		SeaZones.addSpawn(SeaZones.Region.FRESH, MobCategory.WATER_AMBIENT, CARP, 6, 2, 4);
		SeaZones.addSpawn(SeaZones.Region.FRESH, MobCategory.UNDERGROUND_WATER_CREATURE, CATFISH, 3, 1, 1);
		SeaZones.addSpawn(SeaZones.Region.FRESH, MobCategory.UNDERGROUND_WATER_CREATURE, CRAYFISH, 6, 1, 2);
		SeaZones.addSpawn(SeaZones.Region.CAVES, MobCategory.UNDERGROUND_WATER_CREATURE, BLIND_CAVEFISH, 4, 4, 8);
	}

	private static void register(EntityType<? extends Mob> type, String id) {
		Registry.register(BuiltInRegistries.ENTITY_TYPE, key(id), type);
		ResourceKey<Item> egg = ResourceKey.create(Registries.ITEM, ScubaDive.id(id + "_spawn_egg"));
		Registry.register(BuiltInRegistries.ITEM, egg, new SpawnEggItem(new Item.Properties().setId(egg).spawnEgg(type)));
	}
}
