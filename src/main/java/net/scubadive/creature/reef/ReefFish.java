package net.scubadive.creature.reef;

import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityType;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.entity.animal.fish.AbstractFish;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.scubadive.ScubaDive;
import net.scubadive.creature.spawn.SeaZones;

/**
 * Coral reef fish: registration, spawn rules and spawn eggs.
 * Alertness differs per species (see {@link ReefProfile}); schooling species use the vanilla
 * school logic, solitary ones wander near a home spot.
 */
public final class ReefFish {
	//                                                       id               flee  speed home school hide
	public static final ReefProfile BLUE_DAMSEL_PROFILE = new ReefProfile("blue_damsel", 4.5, 1.9, 6.0, 8, false, ReefProfile.Trait.SCHOOLING, 0.0);
	public static final ReefProfile CHROMIS_PROFILE = new ReefProfile("chromis", 7.0, 2.2, 2.5, 10, true, ReefProfile.Trait.SCHOOLING, 0.0);
	public static final ReefProfile ANTHIAS_PROFILE = new ReefProfile("anthias", 7.0, 2.2, 6.0, 10, false, ReefProfile.Trait.SCHOOLING, 0.0);
	public static final ReefProfile BLUE_TANG_PROFILE = new ReefProfile("blue_tang", 5.0, 1.8, 7.0, 1, false);
	public static final ReefProfile BUTTERFLYFISH_PROFILE = new ReefProfile("butterflyfish", 4.5, 1.8, 5.0, 2, false);
	public static final ReefProfile MOORISH_IDOL_PROFILE = new ReefProfile("moorish_idol", 5.5, 1.7, 7.0, 1, false);
	public static final ReefProfile LIONFISH_PROFILE = new ReefProfile("lionfish", 1.8, 1.0, 5.0, 1, false);
	public static final ReefProfile PARROTFISH_PROFILE = new ReefProfile("parrotfish", 5.0, 1.8, 7.0, 1, false, ReefProfile.Trait.GRAZER, 0.0);
	public static final ReefProfile BOXFISH_PROFILE = new ReefProfile("boxfish", 3.5, 1.3, 5.0, 1, false, ReefProfile.Trait.NONE, 0.28);
	public static final ReefProfile TRIGGERFISH_PROFILE = new ReefProfile("triggerfish", 1.1, 1.6, 6.0, 1, false, ReefProfile.Trait.TERRITORIAL, 0.0);
	public static final ReefProfile GROUPER_PROFILE = new ReefProfile("grouper", 3.0, 1.4, 2.5, 1, false, ReefProfile.Trait.LURKER, 0.4);
	public static final ReefProfile BLUESTRIPE_SNAPPER_PROFILE = new ReefProfile("bluestripe_snapper", 6.0, 2.0, 8.0, 20, false, ReefProfile.Trait.SCHOOLING, 0.0);

	private static final List<Entry> ENTRIES = new ArrayList<>();

	// Hitbox height = 2 * body centre offset * model scale / 16 (see ReefFishRenderer).
	public static final EntityType<ReefSchoolFish> BLUE_DAMSEL = school(BLUE_DAMSEL_PROFILE, 0.2f, 0.16f, true, 6, 5, 8);
	public static final EntityType<ReefSchoolFish> CHROMIS = school(CHROMIS_PROFILE, 0.2f, 0.15f, true, 5, 6, 10);
	public static final EntityType<ReefSchoolFish> ANTHIAS = school(ANTHIAS_PROFILE, 0.22f, 0.18f, false, 10, 6, 10);
	public static final EntityType<ReefLoneFish> BLUE_TANG = lone(BLUE_TANG_PROFILE, 0.25f, 0.24f, false, 7, 2, 4);
	public static final EntityType<ReefSchoolFish> BUTTERFLYFISH = school(BUTTERFLYFISH_PROFILE, 0.25f, 0.23f, false, 7, 2, 2);
	public static final EntityType<ReefLoneFish> MOORISH_IDOL = lone(MOORISH_IDOL_PROFILE, 0.25f, 0.23f, false, 5, 1, 2);
	public static final EntityType<ReefLoneFish> LIONFISH = lone(LIONFISH_PROFILE, 0.45f, 0.4f, false, 2, 1, 1);
	public static final EntityType<ReefLoneFish> PARROTFISH = lone(PARROTFISH_PROFILE, 0.35f, 0.3f, false, 5, 1, 3);
	public static final EntityType<ReefLoneFish> BOXFISH = lone(BOXFISH_PROFILE, 0.25f, 0.22f, false, 3, 1, 1);
	public static final EntityType<ReefLoneFish> TRIGGERFISH = lone(TRIGGERFISH_PROFILE, 0.3f, 0.3f, false, 3, 1, 2);
	public static final EntityType<ReefLoneFish> GROUPER = lone(GROUPER_PROFILE, 0.5f, 0.38f, false, 2, 1, 1);
	public static final EntityType<ReefSchoolFish> BLUESTRIPE_SNAPPER = school(BLUESTRIPE_SNAPPER_PROFILE, 0.22f, 0.18f, false, 5, 8, 14);

	private record Entry(ReefProfile profile, EntityType<? extends Mob> type, int weight, int min, int max) {
	}

	private ReefFish() {
	}

	private static EntityType<ReefSchoolFish> school(ReefProfile p, float w, float h, boolean needCoral, int weight, int min, int max) {
		EntityType<ReefSchoolFish> type = build(p, (t, level) -> new ReefSchoolFish(t, level, p), w, h, needCoral);
		ENTRIES.add(new Entry(p, type, weight, min, max));
		return type;
	}

	private static EntityType<ReefLoneFish> lone(ReefProfile p, float w, float h, boolean needCoral, int weight, int min, int max) {
		EntityType<ReefLoneFish> type = build(p, (t, level) -> new ReefLoneFish(t, level, p), w, h, needCoral);
		ENTRIES.add(new Entry(p, type, weight, min, max));
		return type;
	}

	private static <T extends Mob> EntityType<T> build(ReefProfile p, EntityType.EntityFactory<T> factory, float w, float h, boolean needCoral) {
		return FabricEntityType.Builder.createMob(factory, MobCategory.WATER_AMBIENT, mob -> mob
						.defaultAttributes(() -> {
							var attributes = AbstractFish.createAttributes();
							if (p.moveSpeed() > 0.0) {
								attributes.add(Attributes.MOVEMENT_SPEED, p.moveSpeed());
							}
							return attributes;
						})
						.spawnPlacement(SpawnPlacementTypes.IN_WATER, Heightmap.Types.OCEAN_FLOOR, SeaZones.wild((type, level, reason, pos, random) ->
								canSpawnAt(level, pos, needCoral))))
				.sized(w, h)
				.clientTrackingRange(8)
				.build(ResourceKey.create(Registries.ENTITY_TYPE, ScubaDive.id(p.id())));
	}

	/** Open shallow-ish water; damselfish and chromis additionally want coral close by. */
	private static boolean canSpawnAt(LevelAccessor level, BlockPos pos, boolean needCoral) {
		if (!level.getFluidState(pos).is(FluidTags.WATER) || !level.getFluidState(pos.above()).is(FluidTags.WATER)) {
			return false;
		}
		int sea = level.getSeaLevel();
		if (pos.getY() < sea - 24 || pos.getY() > sea - 2) {
			return false;
		}
		return !needCoral || nearCoral(level, pos);
	}

	private static boolean nearCoral(LevelAccessor level, BlockPos pos) {
		for (BlockPos p : BlockPos.betweenClosed(pos.offset(-4, -2, -4), pos.offset(4, 3, 4))) {
			BlockState s = level.getBlockState(p);
			if (s.is(BlockTags.CORALS) || s.is(BlockTags.CORAL_BLOCKS) || s.is(BlockTags.WALL_CORALS)) {
				return true;
			}
		}
		return false;
	}

	public static void init() {
		// Coral reef fish live in warm and lukewarm water; the grouper also in the temperate sea.
		for (Entry e : ENTRIES) {
			ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE, ScubaDive.id(e.profile().id()));
			Registry.register(BuiltInRegistries.ENTITY_TYPE, key, e.type());

			ResourceKey<Item> eggKey = ResourceKey.create(Registries.ITEM, ScubaDive.id(e.profile().id() + "_spawn_egg"));
			Registry.register(BuiltInRegistries.ITEM, eggKey, new SpawnEggItem(new Item.Properties().setId(eggKey).spawnEgg(e.type())));

			SeaZones.addSpawn(SeaZones.Region.WARM, MobCategory.WATER_AMBIENT, e.type(), e.weight(), e.min(), e.max());
		}
		SeaZones.addSpawn(SeaZones.Region.TEMPERATE, MobCategory.WATER_AMBIENT, GROUPER, 5, 1, 1);
	}
}
