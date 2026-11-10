package net.scubadive.creature.cold;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.fish.AbstractFish;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.levelgen.Heightmap;
import net.scubadive.ScubaDive;
import net.scubadive.creature.bottom.MorayEel;
import net.scubadive.creature.reef.ReefLoneFish;
import net.scubadive.creature.reef.ReefProfile;
import net.scubadive.creature.spawn.SeaZones;

/** Cold-water creatures: the clione (sea angel), the lumpsucker and the wolf eel. */
public final class ColdCreatures {
	public static final ResourceKey<EntityType<?>> CLIONE_KEY = ResourceKey.create(Registries.ENTITY_TYPE, ScubaDive.id("clione"));

	public static final EntityType<Clione> CLIONE = FabricEntityType.Builder.createMob(Clione::new, MobCategory.AXOLOTLS, mob -> mob
					.defaultAttributes(() -> Mob.createMobAttributes()
							.add(Attributes.MAX_HEALTH, 2.0)
							.add(Attributes.MOVEMENT_SPEED, 0.01))
					.spawnPlacement(SpawnPlacementTypes.IN_WATER, Heightmap.Types.OCEAN_FLOOR, SeaZones.wild((type, level, reason, pos, random) ->
							openWater(level, pos))))
			.sized(0.1f, 0.15f)
			.clientTrackingRange(6)
			.build(CLIONE_KEY);

	public static final ResourceKey<EntityType<?>> LUMPSUCKER_KEY = ResourceKey.create(Registries.ENTITY_TYPE, ScubaDive.id("lumpsucker"));
	public static final ResourceKey<EntityType<?>> WOLF_EEL_KEY = ResourceKey.create(Registries.ENTITY_TYPE, ScubaDive.id("wolf_eel"));

	/** Tiny round fish that clings to one rock and barely leaves it (lurker: stays at its shelter). */
	public static final ReefProfile LUMPSUCKER_PROFILE = new ReefProfile("lumpsucker", 1.6, 0.9, 1.5, 1, false, ReefProfile.Trait.LURKER, 0.12);

	public static final EntityType<ReefLoneFish> LUMPSUCKER = buildLumpsucker();

	/** Wolf eel: a big-faced cold-water eel that lives in a rock crevice like the moray (same behaviour). */
	public static final EntityType<MorayEel> WOLF_EEL = FabricEntityType.Builder.createMob(MorayEel::new, MobCategory.UNDERGROUND_WATER_CREATURE, mob -> mob
					.defaultAttributes(() -> Mob.createMobAttributes()
							.add(Attributes.MAX_HEALTH, 16.0)
							.add(Attributes.MOVEMENT_SPEED, 0.0))
					.spawnPlacement(SpawnPlacementTypes.IN_WATER, Heightmap.Types.OCEAN_FLOOR, SeaZones.wild((type, level, reason, pos, random) ->
							level.getFluidState(pos).is(FluidTags.WATER) && MorayEel.findCrevice(level, pos, 3) != null)))
			.sized(0.5f, 0.5f)
			.clientTrackingRange(8)
			.build(WOLF_EEL_KEY);

	private static EntityType<ReefLoneFish> buildLumpsucker() {
		EntityType.EntityFactory<ReefLoneFish> factory = (t, level) -> new ReefLoneFish(t, level, LUMPSUCKER_PROFILE);
		return FabricEntityType.Builder.createMob(factory, MobCategory.UNDERGROUND_WATER_CREATURE, mob -> mob
						.defaultAttributes(() -> AbstractFish.createAttributes()
								.add(Attributes.MAX_HEALTH, 2.0)
								.add(Attributes.MOVEMENT_SPEED, LUMPSUCKER_PROFILE.moveSpeed()))
						.spawnPlacement(SpawnPlacementTypes.IN_WATER, Heightmap.Types.OCEAN_FLOOR, SeaZones.wild((type, level, reason, pos, random) ->
								onRock(level, pos))))
				.sized(0.12f, 0.12f)
				.clientTrackingRange(6)
				.build(LUMPSUCKER_KEY);
	}

	/** A sturdy floor (rock, gravel) at most 3 water cells below, with water above. */
	private static boolean onRock(LevelReader level, BlockPos pos) {
		BlockPos f = SeaZones.floorWater(level, pos, 3);
		return f != null && level.getFluidState(f.above()).is(FluidTags.WATER)
				&& level.getBlockState(f.below()).isFaceSturdy(level, f.below(), Direction.UP)
				&& !level.getBlockState(f.below()).is(BlockTags.SAND);
	}

	private ColdCreatures() {
	}

	/** Mid-water below the surface (or the ice): water here and two cells above. */
	private static boolean openWater(LevelReader level, BlockPos pos) {
		if (pos.getY() > level.getSeaLevel() - 2) {
			return false;
		}
		for (int dy = 0; dy <= 2; dy++) {
			if (!level.getFluidState(pos.above(dy)).is(FluidTags.WATER)) {
				return false;
			}
		}
		return true;
	}

	public static void init() {
		register(CLIONE_KEY, CLIONE, "clione_spawn_egg");
		register(LUMPSUCKER_KEY, LUMPSUCKER, "lumpsucker_spawn_egg");
		register(WOLF_EEL_KEY, WOLF_EEL, "wolf_eel_spawn_egg");

		// Cold and frozen oceans only, in small loose gatherings. Uses the AXOLOTLS cap like the jellyfish,
		// so it does not compete with the cod and salmon for WATER_AMBIENT.
		SeaZones.addSpawn(SeaZones.Region.COLD, MobCategory.AXOLOTLS, CLIONE, 8, 2, 4);
		// Lumpsuckers on rocky floors, wolf eels in rock crevices (floor-dweller cap).
		SeaZones.addSpawn(SeaZones.Region.COLD, MobCategory.UNDERGROUND_WATER_CREATURE, LUMPSUCKER, 5, 1, 1);
		SeaZones.addSpawn(SeaZones.Region.COLD, MobCategory.UNDERGROUND_WATER_CREATURE, WOLF_EEL, 3, 1, 1);
	}

	private static void register(ResourceKey<EntityType<?>> key, EntityType<? extends Mob> type, String egg) {
		Registry.register(BuiltInRegistries.ENTITY_TYPE, key, type);
		ResourceKey<Item> eggKey = ResourceKey.create(Registries.ITEM, ScubaDive.id(egg));
		Registry.register(BuiltInRegistries.ITEM, eggKey, new SpawnEggItem(new Item.Properties().setId(eggKey).spawnEgg(type)));
	}
}
