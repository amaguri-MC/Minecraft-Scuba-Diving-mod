package net.scubadive.creature.cephalopod;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityType;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.level.levelgen.Heightmap;

import net.scubadive.ScubaDive;
import net.scubadive.creature.spawn.SeaZones;

/** Squid and cuttlefish: the spear squid (schools, temperate to cold seas) and the cuttlefish (reefs, warm to temperate). */
public final class Cephalopods {
	public static final ResourceKey<EntityType<?>> SPEAR_SQUID_KEY = ResourceKey.create(Registries.ENTITY_TYPE, ScubaDive.id("spear_squid"));
	public static final ResourceKey<EntityType<?>> CUTTLEFISH_KEY = ResourceKey.create(Registries.ENTITY_TYPE, ScubaDive.id("cuttlefish"));

	public static final EntityType<SpearSquid> SPEAR_SQUID = FabricEntityType.Builder.createMob(SpearSquid::new, MobCategory.WATER_CREATURE, mob -> mob
					.defaultAttributes(() -> Mob.createMobAttributes()
							.add(Attributes.MAX_HEALTH, 6.0)
							.add(Attributes.MOVEMENT_SPEED, 0.0))
					.spawnPlacement(SpawnPlacementTypes.IN_WATER, Heightmap.Types.OCEAN_FLOOR, SeaZones.wild((type, level, reason, pos, random) ->
							openWater(level, pos))))
			.sized(0.35f, 0.3f)
			.clientTrackingRange(8)
			.build(SPEAR_SQUID_KEY);

	public static final EntityType<Cuttlefish> CUTTLEFISH = FabricEntityType.Builder.createMob(Cuttlefish::new, MobCategory.UNDERGROUND_WATER_CREATURE, mob -> mob
					.defaultAttributes(() -> Mob.createMobAttributes()
							.add(Attributes.MAX_HEALTH, 8.0)
							.add(Attributes.MOVEMENT_SPEED, 0.0))
					.spawnPlacement(SpawnPlacementTypes.IN_WATER, Heightmap.Types.OCEAN_FLOOR, SeaZones.wild((type, level, reason, pos, random) ->
							nearFloor(level, pos))))
			.sized(0.45f, 0.3f)
			.clientTrackingRange(8)
			.build(CUTTLEFISH_KEY);

	private Cephalopods() {
	}

	public static void init() {
		register(SPEAR_SQUID_KEY, SPEAR_SQUID, "spear_squid_spawn_egg");
		register(CUTTLEFISH_KEY, CUTTLEFISH, "cuttlefish_spawn_egg");

		// Spear squid: temperate to cold seas, in schools of 5-12 (water creature cap, like the vanilla squid).
		BiomeModifications.addSpawn(SeaZones.temperate().or(SeaZones.cold()), MobCategory.WATER_CREATURE, SPEAR_SQUID, 4, 5, 12);
		// Cuttlefish: warm to temperate seas near the floor, alone or in a pair (floor-dweller cap).
		BiomeModifications.addSpawn(SeaZones.warm().or(SeaZones.temperate()), MobCategory.UNDERGROUND_WATER_CREATURE, CUTTLEFISH, 4, 1, 2);
	}

	/** Mid-water below the surface: water here and two cells above. */
	private static boolean openWater(LevelReader level, BlockPos pos) {
		if (pos.getY() > level.getSeaLevel() - 3) {
			return false;
		}
		for (int dy = 0; dy <= 2; dy++) {
			if (!level.getFluidState(pos.above(dy)).is(FluidTags.WATER)) {
				return false;
			}
		}
		return true;
	}

	/** Within a few cells of the sea floor, with water above. */
	private static boolean nearFloor(LevelReader level, BlockPos pos) {
		BlockPos f = SeaZones.floorWater(level, pos, 4);
		return f != null && level.getFluidState(f.above()).is(FluidTags.WATER) && pos.getY() < level.getSeaLevel() - 2;
	}

	private static void register(ResourceKey<EntityType<?>> key, EntityType<? extends Mob> type, String egg) {
		Registry.register(BuiltInRegistries.ENTITY_TYPE, key, type);
		ResourceKey<Item> eggKey = ResourceKey.create(Registries.ITEM, ScubaDive.id(egg));
		Registry.register(BuiltInRegistries.ITEM, eggKey, new SpawnEggItem(new Item.Properties().setId(eggKey).spawnEgg(type)));
	}
}
