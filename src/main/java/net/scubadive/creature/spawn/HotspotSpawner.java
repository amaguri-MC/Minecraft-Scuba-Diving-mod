package net.scubadive.creature.spawn;

import java.util.ArrayList;
import java.util.List;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.animal.fish.WaterAnimal;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.scubadive.ScubaDive;

/**
 * Dense fish spawning around reefs and other hotspots. Owner: spawn module (G4).
 * Cheap by design: a few random probes per player every couple of seconds.
 */
public final class HotspotSpawner {
	// Tuning constants
	private static final int INTERVAL_TICKS = 40;
	private static final int PROBES_PER_PLAYER = 3;
	private static final int MIN_RANGE = 24;
	private static final int MAX_RANGE = 48;
	private static final int VERTICAL_RANGE = 16;
	private static final int MAX_FLOOR_SEARCH = 24;
	private static final int MARKER_RADIUS = 3;
	private static final int FISH_CAP = 60;
	private static final double CAP_RADIUS = 32.0;
	private static final double MIN_PLAYER_DISTANCE = 8.0;
	private static final int SCATTER = 3;
	private static final int SAND_MIN = 12;
	private static final int ROCK_MIN = 4;
	private static final java.util.Set<String> BOTTOM_DWELLERS = java.util.Set.of(
			"scubadive:nudibranch", "scubadive:octopus", "scubadive:goby", "scubadive:stingray",
			"scubadive:moray_eel", "scubadive:frogfish", "scubadive:pygmy_seahorse");

	private static final ResourceKey<Biome> DEEP_TRENCH = ResourceKey.create(Registries.BIOME, ScubaDive.id("deep_trench"));
	private static final Identifier ANEMONE = ScubaDive.id("sea_anemone");

	/** One spawn table row: entity id, weight, group size range. */
	private record Entry(String id, int weight, int minGroup, int maxGroup) {
	}

	private enum Hotspot {
		REEF(List.of(
				new Entry("scubadive:blue_damsel", 10, 6, 12),
				new Entry("scubadive:chromis", 10, 8, 16),
				new Entry("scubadive:anthias", 10, 8, 16),
				new Entry("scubadive:blue_tang", 6, 2, 4),
				new Entry("scubadive:butterflyfish", 5, 1, 3),
				new Entry("scubadive:moorish_idol", 3, 1, 2),
				new Entry("scubadive:lionfish", 3, 1, 2),
				new Entry("scubadive:parrotfish", 5, 1, 3),
				new Entry("scubadive:boxfish", 3, 1, 1),
				new Entry("scubadive:triggerfish", 3, 1, 2),
				new Entry("scubadive:bluestripe_snapper", 5, 10, 18),
				new Entry("minecraft:tropical_fish", 8, 3, 6),
				new Entry("scubadive:nudibranch", 12, 2, 4),
				new Entry("scubadive:pygmy_seahorse", 2, 1, 1),
				new Entry("scubadive:octopus", 2, 1, 1))),
		ANEMONE(List.of(
				new Entry("scubadive:clownfish", 24, 2, 4),
				new Entry("scubadive:anemone_shrimp", 5, 1, 3),
				new Entry("scubadive:nudibranch", 4, 1, 2))),
		ROCK(List.of(
				new Entry("scubadive:grouper", 6, 1, 2),
				new Entry("scubadive:moray_eel", 4, 1, 1),
				new Entry("scubadive:goby", 4, 1, 3),
				new Entry("scubadive:lionfish", 3, 1, 2),
				new Entry("minecraft:cod", 6, 2, 5),
				new Entry("scubadive:nudibranch", 10, 2, 4),
				new Entry("scubadive:octopus", 3, 1, 1))),
		TRENCH(List.of(
				new Entry("scubadive:hammerhead_shark", 4, 2, 5),
				new Entry("scubadive:bigeye_trevally", 6, 12, 24),
				new Entry("scubadive:manta_ray", 2, 1, 1),
				new Entry("minecraft:glow_squid", 6, 2, 4))),
		WRECK(List.of(
				new Entry("scubadive:barracuda", 6, 3, 6),
				new Entry("scubadive:bigeye_trevally", 6, 12, 24),
				new Entry("scubadive:grouper", 5, 1, 2),
				new Entry("scubadive:reef_shark", 3, 1, 2))),
		SAND(List.of(
				new Entry("scubadive:stingray", 3, 1, 1),
				new Entry("scubadive:goby", 6, 1, 3)));

		final List<Entry> entries;

		Hotspot(List<Entry> entries) {
			this.entries = entries;
		}
	}

	private HotspotSpawner() {
	}

	public static void init() {
		ServerTickEvents.END_LEVEL_TICK.register(HotspotSpawner::tick);
	}

	private static void tick(ServerLevel level) {
		long time = level.getGameTime();
		for (ServerPlayer player : level.players()) {
			// Stagger players so they do not all probe on the same tick
			if ((time + player.getId()) % INTERVAL_TICKS != 0 || player.isSpectator()) {
				continue;
			}
			for (int i = 0; i < PROBES_PER_PLAYER; i++) {
				probe(level, player);
			}
		}
	}

	private static void probe(ServerLevel level, ServerPlayer player) {
		RandomSource random = level.getRandom();
		double angle = random.nextDouble() * Math.PI * 2.0;
		double dist = MIN_RANGE + random.nextDouble() * (MAX_RANGE - MIN_RANGE);
		int x = (int) Math.floor(player.getX() + Math.cos(angle) * dist);
		int z = (int) Math.floor(player.getZ() + Math.sin(angle) * dist);
		int y = (int) Math.floor(player.getY()) + random.nextInt(VERTICAL_RANGE * 2 + 1) - VERTICAL_RANGE;
		y = Math.max(level.getMinY() + 1, Math.min(level.getMaxY() - 1, y));

		BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos(x, y, z);
		// Never touch unloaded chunks: the marker scan and the scatter reach a few blocks sideways.
		int reach = Math.max(MARKER_RADIUS, SCATTER) + 1;
		if (!level.hasChunksAt(x - reach, level.getMinY(), z - reach, x + reach, level.getMaxY(), z + reach) || !isWaterSource(level, cursor)) {
			return;
		}
		// Find the seabed below the probe
		int steps = 0;
		while (steps < MAX_FLOOR_SEARCH && isWaterSource(level, cursor.below())) {
			cursor.move(0, -1, 0);
			steps++;
		}
		if (steps >= MAX_FLOOR_SEARCH || level.getBlockState(cursor.below()).isAir()) {
			return;
		}
		BlockPos floorWater = cursor.immutable();

		Hotspot hotspot = classify(level, floorWater);
		if (hotspot == null) {
			return;
		}
		int room = FISH_CAP - countFish(level, floorWater);
		if (room <= 0) {
			return;
		}
		spawnGroup(level, player, floorWater, hotspot, random, room);
	}

	private static boolean isWaterSource(ServerLevel level, BlockPos pos) {
		var fluid = level.getFluidState(pos);
		return fluid.is(FluidTags.WATER) && fluid.isSource();
	}

	/** Looks at blocks around the point and returns the strongest matching hotspot, or null. */
	private static Hotspot classify(ServerLevel level, BlockPos center) {
		int coral = 0;
		int anemone = 0;
		int rock = 0;
		int sand = 0;
		int wreck = 0;
		BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
		for (int dx = -MARKER_RADIUS; dx <= MARKER_RADIUS; dx++) {
			for (int dy = -MARKER_RADIUS; dy <= MARKER_RADIUS; dy++) {
				for (int dz = -MARKER_RADIUS; dz <= MARKER_RADIUS; dz++) {
					p.setWithOffset(center, dx, dy, dz);
					BlockState state = level.getBlockState(p);
					if (state.isAir()) {
						continue;
					}
					Block block = state.getBlock();
					if (state.is(Blocks.WARPED_PLANKS)) {
						wreck++;
					} else if (isAnemone(block)) {
						anemone++;
					} else if (isCoral(state, block)) {
						coral++;
					} else if (isRock(state)) {
						if (level.getFluidState(p.above()).is(FluidTags.WATER)) {
							rock++;
						}
					} else if (state.is(Blocks.SAND) || state.is(Blocks.RED_SAND)) {
						sand++;
					}
				}
			}
		}
		if (wreck > 0) {
			return Hotspot.WRECK;
		}
		if (anemone > 0) {
			return Hotspot.ANEMONE;
		}
		if (coral > 0) {
			return Hotspot.REEF;
		}
		var biome = level.getBiome(center);
		if (biome.is(DEEP_TRENCH)) {
			return Hotspot.TRENCH;
		}
		if (rock >= ROCK_MIN) {
			return Hotspot.ROCK;
		}
		if (sand >= SAND_MIN) {
			return Hotspot.SAND;
		}
		return null;
	}

	private static boolean isAnemone(Block block) {
		return BuiltInRegistries.BLOCK.getKey(block).equals(ANEMONE);
	}

	private static boolean isCoral(BlockState state, Block block) {
		if (state.is(BlockTags.CORALS) || state.is(BlockTags.CORAL_BLOCKS)) {
			return true;
		}
		Identifier key = BuiltInRegistries.BLOCK.getKey(block);
		if (!ScubaDive.MOD_ID.equals(key.getNamespace())) {
			return false;
		}
		String path = key.getPath();
		return path.equals("table_coral") || path.equals("brain_coral") || path.equals("branch_coral") || path.equals("fan_coral");
	}

	private static boolean isRock(BlockState state) {
		return state.is(Blocks.STONE) || state.is(Blocks.ANDESITE) || state.is(Blocks.TUFF) || state.is(Blocks.MOSSY_COBBLESTONE);
	}

	/** Counts scubadive and vanilla fish near the point. */
	private static int countFish(ServerLevel level, BlockPos center) {
		AABB box = new AABB(center).inflate(CAP_RADIUS);
		return level.getEntitiesOfClass(Mob.class, box, HotspotSpawner::isFish).size();
	}

	private static boolean isFish(Mob mob) {
		if (mob instanceof WaterAnimal) {
			return true;
		}
		return ScubaDive.MOD_ID.equals(BuiltInRegistries.ENTITY_TYPE.getKey(mob.getType()).getNamespace());
	}

	private static void spawnGroup(ServerLevel level, ServerPlayer player, BlockPos center, Hotspot hotspot, RandomSource random, int room) {
		// Resolve the table at runtime; unknown ids are ignored
		List<Entry> usable = new ArrayList<>();
		List<EntityType<?>> types = new ArrayList<>();
		int total = 0;
		for (Entry entry : hotspot.entries) {
			EntityType<?> type = lookup(entry.id());
			if (type != null) {
				usable.add(entry);
				types.add(type);
				total += entry.weight();
			}
		}
		if (usable.isEmpty()) {
			return;
		}
		int roll = random.nextInt(total);
		int index = 0;
		for (; index < usable.size() - 1; index++) {
			roll -= usable.get(index).weight();
			if (roll < 0) {
				break;
			}
		}
		Entry entry = usable.get(index);
		EntityType<?> type = types.get(index);
		int count = Math.min(room, entry.minGroup() + random.nextInt(entry.maxGroup() - entry.minGroup() + 1));
		boolean bottom = BOTTOM_DWELLERS.contains(entry.id());

		SpawnGroupData groupData = null;
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
		for (int i = 0; i < count; i++) {
			pos.setWithOffset(center, random.nextInt(SCATTER * 2 + 1) - SCATTER, random.nextInt(3), random.nextInt(SCATTER * 2 + 1) - SCATTER);
			if (!level.hasChunkAt(pos) || !isWaterSource(level, pos)) {
				continue;
			}
			if (bottom) {
				// Sea-floor species are placed on the floor, not in mid-water above a slope.
				for (int down = 0; down < 8 && isWaterSource(level, pos.below()); down++) {
					pos.move(0, -1, 0);
				}
			}
			if (player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) < MIN_PLAYER_DISTANCE * MIN_PLAYER_DISTANCE) {
				continue;
			}
			Entity entity = type.create(level, EntitySpawnReason.NATURAL);
			if (!(entity instanceof Mob mob)) {
				if (entity != null) {
					entity.discard();
				}
				continue;
			}
			mob.snapTo(pos.getX() + 0.5, pos.getY() + 0.1, pos.getZ() + 0.5, random.nextFloat() * 360.0f, 0.0f);
			if (!level.noCollision(mob)) {
				mob.discard();
				continue;
			}
			groupData = mob.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), EntitySpawnReason.NATURAL, groupData);
			level.addFreshEntity(mob);
		}
	}

	private static EntityType<?> lookup(String id) {
		Identifier identifier = Identifier.tryParse(id);
		if (identifier == null) {
			return null;
		}
		// getOptional avoids the default-entry fallback of the entity type registry
		return BuiltInRegistries.ENTITY_TYPE.getOptional(identifier).orElse(null);
	}
}
