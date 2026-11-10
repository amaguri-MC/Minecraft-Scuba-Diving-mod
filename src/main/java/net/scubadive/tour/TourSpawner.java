package net.scubadive.tour;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.phys.AABB;

import net.scubadive.tour.site.DiveSites;

/** Weighted creature spawning inside one tour site. */
final class TourSpawner {
	static final int MAX_CREATURES = 120;
	private static final int BOAT_AVOID = 8;
	private static final int SCATTER = 3;
	private static final Set<String> BOTTOM = Set.of(
			"scubadive:blue_starfish", "scubadive:cushion_star", "scubadive:crown_of_thorns", "scubadive:sea_urchin",
			"scubadive:nudibranch", "scubadive:octopus", "scubadive:goby", "scubadive:stingray",
			"scubadive:moray_eel", "scubadive:frogfish", "scubadive:pygmy_seahorse",
			"minecraft:axolotl", "minecraft:tadpole", "minecraft:frog");

	private TourSpawner() {
	}

	static AABB siteBox(BlockPos origin) {
		int r = DiveSites.RADIUS;
		return new AABB(origin.getX() - r, -64, origin.getZ() - r, origin.getX() + r + 1, 320, origin.getZ() + r + 1);
	}

	static int count(ServerLevel level, BlockPos origin) {
		return level.getEntitiesOfClass(Mob.class, siteBox(origin)).size();
	}

	/** Spawns up to groups groups; the cap is only checked when respectCap is set. */
	static void spawnGroups(ServerLevel level, BlockPos origin, DivePoint point, int groups, boolean respectCap) {
		RandomSource random = level.getRandom();
		int existing = respectCap ? count(level, origin) : 0;
		for (int g = 0; g < groups; g++) {
			if (respectCap && existing >= MAX_CREATURES) {
				return;
			}
			existing += spawnGroup(level, origin, point, random);
		}
	}

	private static boolean isWaterSource(ServerLevel level, BlockPos pos) {
		var fluid = level.getFluidState(pos);
		return fluid.is(FluidTags.WATER) && fluid.isSource();
	}

	private static int spawnGroup(ServerLevel level, BlockPos origin, DivePoint point, RandomSource random) {
		List<DivePoint.Spawn> usable = new ArrayList<>();
		List<EntityType<?>> types = new ArrayList<>();
		int total = 0;
		for (DivePoint.Spawn s : point.spawns) {
			Identifier id = Identifier.tryParse(s.entityId());
			EntityType<?> type = id == null ? null : BuiltInRegistries.ENTITY_TYPE.getOptional(id).orElse(null);
			if (type != null && s.weight() > 0) {
				usable.add(s);
				types.add(type);
				total += s.weight();
			}
		}
		if (usable.isEmpty()) {
			return 0;
		}
		int roll = random.nextInt(total);
		int index = 0;
		for (; index < usable.size() - 1; index++) {
			roll -= usable.get(index).weight();
			if (roll < 0) {
				break;
			}
		}
		DivePoint.Spawn spawn = usable.get(index);
		EntityType<?> type = types.get(index);
		boolean bottom = BOTTOM.contains(spawn.entityId());

		// Pick a column inside the site and a random water-source block anywhere in it (under ice, in caves too).
		int r = DiveSites.RADIUS - 6;
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
		int cx = 0;
		int cz = 0;
		int cy = 0;
		boolean found = false;
		int minY = Math.max(level.getMinY(), DiveSites.SURFACE_Y - 130);
		for (int attempt = 0; attempt < 12 && !found; attempt++) {
			cx = origin.getX() + random.nextInt(r * 2 + 1) - r;
			cz = origin.getZ() + random.nextInt(r * 2 + 1) - r;
			pos.set(cx, DiveSites.SURFACE_Y - 1, cz);
			if (!level.hasChunkAt(pos)) {
				continue;
			}
			int seen = 0;
			for (int y = DiveSites.SURFACE_Y - 1; y >= minY; y--) {
				if (isWaterSource(level, pos.set(cx, y, cz)) && random.nextInt(++seen) == 0) {
					cy = y;
				}
			}
			found = seen > 0;
		}
		if (!found) {
			return 0;
		}
		int count = spawn.min() + random.nextInt(spawn.max() - spawn.min() + 1);
		SpawnGroupData groupData = null;
		int made = 0;
		for (int i = 0; i < count; i++) {
			int x = cx + random.nextInt(SCATTER * 2 + 1) - SCATTER;
			int z = cz + random.nextInt(SCATTER * 2 + 1) - SCATTER;
			int y = cy + random.nextInt(3) - 1;
			pos.set(x, y, z);
			if (!level.hasChunkAt(pos) || !isWaterSource(level, pos)) {
				continue;
			}
			if (bottom) {
				for (int down = 0; down < 160 && isWaterSource(level, pos.below()); down++) {
					pos.move(0, -1, 0);
				}
			}
			double dx = pos.getX() + 0.5 - origin.getX();
			double dy = pos.getY() + 0.5 - origin.getY();
			double dz = pos.getZ() + 0.5 - origin.getZ();
			if (dx * dx + dy * dy + dz * dz < BOAT_AVOID * BOAT_AVOID) {
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
			made++;
		}
		return made;
	}
}
