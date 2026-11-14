package net.scubadive.creature.penguin;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.QuartPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.scubadive.ScubaDive;
import net.scubadive.creature.spawn.SeaZones;

/**
 * Penguins: gentoo (frozen seas and snowy beaches, on ice and snow), rockhopper (cold rocky shores and ice)
 * and cape (temperate beaches and rocky shores). Land creatures: CREATURE cap, spawned on the ground.
 */
public final class Penguins {
	public static final ResourceKey<EntityType<?>> GENTOO_KEY = ResourceKey.create(Registries.ENTITY_TYPE, ScubaDive.id("gentoo_penguin"));
	public static final ResourceKey<EntityType<?>> ROCKHOPPER_KEY = ResourceKey.create(Registries.ENTITY_TYPE, ScubaDive.id("rockhopper_penguin"));
	public static final ResourceKey<EntityType<?>> CAPE_KEY = ResourceKey.create(Registries.ENTITY_TYPE, ScubaDive.id("cape_penguin"));

	/**
	 * On the ground, like vanilla ON_GROUND, except that ice counts as ground (vanilla ice only lets polar
	 * bears spawn on it): a solid top or any ice below, and room for the bird.
	 */
	public static final SpawnPlacementType ON_ICE_OR_GROUND = (level, pos, type) -> {
		if (!level.getWorldBorder().isWithinBounds(pos)) {
			return false;
		}
		BlockPos below = pos.below();
		BlockState ground = level.getBlockState(below);
		boolean standable = ground.is(BlockTags.ICE) || ground.isFaceSturdy(level, below, Direction.UP);
		return standable
				&& NaturalSpawner.isValidEmptySpawnBlock(level, pos, level.getBlockState(pos), level.getFluidState(pos), type)
				&& NaturalSpawner.isValidEmptySpawnBlock(level, pos.above(), level.getBlockState(pos.above()),
						level.getFluidState(pos.above()), type);
	};

	public static final EntityType<Penguin> GENTOO = build(GENTOO_KEY, 0.45f, 0.8f, Penguins::gentooGround);
	public static final EntityType<Penguin> ROCKHOPPER = build(ROCKHOPPER_KEY, 0.36f, 0.66f, Penguins::rockhopperGround);
	public static final EntityType<Penguin> CAPE = build(CAPE_KEY, 0.41f, 0.74f, Penguins::capeGround);

	private Penguins() {
	}

	private interface Ground {
		boolean ok(LevelReader level, BlockPos pos);
	}

	private static EntityType<Penguin> build(ResourceKey<EntityType<?>> key, float width, float height, Ground ground) {
		return FabricEntityType.Builder.createMob(Penguin::new, MobCategory.CREATURE, mob -> mob
						.defaultAttributes(Penguin::createAttributes)
						.spawnPlacement(ON_ICE_OR_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
								SeaZones.<Penguin>wild((type, level, reason, pos, random) ->
										level.getRawBrightness(pos, 0) > 8 && ground.ok(level, pos))))
				.sized(width, height)
				.clientTrackingRange(10)
				.build(key);
	}

	public static void init() {
		register(GENTOO_KEY, GENTOO, "gentoo_penguin_spawn_egg");
		register(ROCKHOPPER_KEY, ROCKHOPPER, "rockhopper_penguin_spawn_egg");
		register(CAPE_KEY, CAPE, "cape_penguin_spawn_egg");

		// Land biomes along the sea are not SeaZones regions, so these go straight to the biome table.
		BiomeModifications.addSpawn(BiomeSelectors.includeByKey(Biomes.FROZEN_OCEAN, Biomes.DEEP_FROZEN_OCEAN, Biomes.SNOWY_BEACH),
				MobCategory.CREATURE, GENTOO, 8, 2, 5);
		BiomeModifications.addSpawn(BiomeSelectors.includeByKey(Biomes.STONY_SHORE, Biomes.SNOWY_BEACH, Biomes.FROZEN_OCEAN),
				MobCategory.CREATURE, ROCKHOPPER, 6, 2, 4);
		BiomeModifications.addSpawn(BiomeSelectors.includeByKey(Biomes.BEACH, Biomes.STONY_SHORE),
				MobCategory.CREATURE, CAPE, 6, 2, 5);
	}

	private static void register(ResourceKey<EntityType<?>> key, EntityType<Penguin> type, String egg) {
		Registry.register(BuiltInRegistries.ENTITY_TYPE, key, type);
		ResourceKey<Item> eggKey = ResourceKey.create(Registries.ITEM, ScubaDive.id(egg));
		Registry.register(BuiltInRegistries.ITEM, eggKey, new SpawnEggItem(new Item.Properties().setId(eggKey).spawnEgg(type)));
	}

	// ---------------------------------------------------------------- where each species stands

	private static boolean iceOrSnow(LevelReader level, BlockPos pos) {
		return Penguin.isIceOrSnow(level.getBlockState(pos.below())) || level.getBlockState(pos).is(Blocks.SNOW);
	}

	private static boolean rocky(BlockState s) {
		return s.is(BlockTags.BASE_STONE_OVERWORLD) || s.is(Blocks.GRAVEL) || s.is(Blocks.COBBLESTONE)
				|| s.is(Blocks.MOSSY_COBBLESTONE);
	}

	/** Gentoo: on ice or snow only. */
	private static boolean gentooGround(LevelReader level, BlockPos pos) {
		return iceOrSnow(level, pos);
	}

	/** Rockhopper: ice or snow, or bare rock on a shore that lies by a cold sea (or in snow). */
	private static boolean rockhopperGround(LevelReader level, BlockPos pos) {
		if (iceOrSnow(level, pos)) {
			return true;
		}
		return rocky(level.getBlockState(pos.below())) && coldNearby(level, pos);
	}

	/** Cape: sand, gravel or rock on a shore that is not by a cold sea. */
	private static boolean capeGround(LevelReader level, BlockPos pos) {
		BlockState s = level.getBlockState(pos.below());
		return (s.is(BlockTags.SAND) || rocky(s)) && !coldNearby(level, pos);
	}

	/** A cold or frozen ocean, or a snowy biome, within about 32 blocks. */
	private static boolean coldNearby(LevelReader level, BlockPos pos) {
		for (int dx = -32; dx <= 32; dx += 16) {
			for (int dz = -32; dz <= 32; dz += 16) {
				BlockPos at = pos.offset(dx, 0, dz);
				// The biome straight from the biome source, without touching any chunk: during chunk generation a
				// chunk read outside the generation region crashes world generation ("Requested chunk unavailable").
				Holder<Biome> b = level.getUncachedNoiseBiome(QuartPos.fromBlock(at.getX()), QuartPos.fromBlock(at.getY()),
						QuartPos.fromBlock(at.getZ()));
				if (SeaZones.zoneOf(b) == SeaZones.Zone.COLD || b.is(Biomes.SNOWY_BEACH) || b.is(Biomes.SNOWY_PLAINS)
						|| b.is(Biomes.SNOWY_TAIGA) || b.is(Biomes.ICE_SPIKES) || b.is(Biomes.FROZEN_RIVER)) {
					return true;
				}
			}
		}
		return false;
	}
}
