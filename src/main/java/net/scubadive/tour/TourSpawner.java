package net.scubadive.tour;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.phys.AABB;

import net.scubadive.creature.ScubaCreatures;
import net.scubadive.creature.spawn.Habitat;
import net.scubadive.tour.site.DiveSites;

/**
 * Weighted creature spawning inside one tour site. Anemone dwellers are put next to an anemone of the site,
 * and each session gets one or two rare visitors from {@link DivePoint#rares}, a few minutes into the dive.
 */
final class TourSpawner {
	static final int MAX_CREATURES = 120;
	private static final int BOAT_AVOID = 8;
	private static final int SCATTER = 3;
	/** First rare visitor 2-4 minutes after the start, a possible second one 3-6 minutes after that. */
	private static final int RARE_FIRST_MIN = 20 * 120;
	private static final int RARE_FIRST_SPREAD = 20 * 120;
	private static final int RARE_NEXT_MIN = 20 * 180;
	private static final int RARE_NEXT_SPREAD = 20 * 180;
	/** Rare visitors appear at this horizontal distance from a diver, so they swim into view. */
	private static final int RARE_DIST_MIN = 12;
	private static final int RARE_DIST_SPREAD = 8;
	private static final int ANEMONE_SEARCH_COLUMNS = 24;
	private static final Set<String> BOTTOM = Set.of(
			"scubadive:blue_starfish", "scubadive:cushion_star", "scubadive:crown_of_thorns", "scubadive:sea_urchin",
			"scubadive:nudibranch", "scubadive:octopus", "scubadive:goby", "scubadive:stingray",
			"scubadive:moray_eel", "scubadive:frogfish", "scubadive:pygmy_seahorse",
			"scubadive:crayfish", "scubadive:catfish", "scubadive:lumpsucker", "scubadive:wolf_eel",
			"minecraft:axolotl", "minecraft:tadpole", "minecraft:frog");
	/** Live in the dark only. */
	private static final Set<String> DARK = Set.of("scubadive:blind_cavefish");
	/** Only live on an anemone (they leave the world otherwise, see AnemoneDweller). */
	private static final Set<String> ANEMONE_DWELLERS = Set.of("scubadive:clownfish", "scubadive:anemone_shrimp");

	/** Rare-visitor schedule per session, keyed by the site origin. */
	private static final Map<Long, RareSchedule> RARES = new HashMap<>();

	private static final class RareSchedule {
		long nextAt;
		int left;
	}

	private TourSpawner() {
	}

	static AABB siteBox(BlockPos origin) {
		int r = DiveSites.RADIUS;
		return new AABB(origin.getX() - r, -64, origin.getZ() - r, origin.getX() + r + 1, 320, origin.getZ() + r + 1);
	}

	static int count(ServerLevel level, BlockPos origin) {
		return level.getEntitiesOfClass(Mob.class, siteBox(origin)).size();
	}

	/**
	 * Spawns up to groups groups; the cap is only checked when respectCap is set. The uncapped call is the
	 * initial fill at the start of a dive and (re)starts the rare-visitor schedule; capped calls are the
	 * periodic top-ups and bring in a rare visitor when one is due.
	 */
	static void spawnGroups(ServerLevel level, BlockPos origin, DivePoint point, int groups, boolean respectCap) {
		RandomSource random = level.getRandom();
		if (!respectCap) {
			scheduleRares(level, origin, point, random);
		} else {
			tickRares(level, origin, point, random);
		}
		int existing = respectCap ? count(level, origin) : 0;
		for (int g = 0; g < groups; g++) {
			if (respectCap && existing >= MAX_CREATURES) {
				return;
			}
			existing += spawnGroup(level, origin, point, random);
		}
	}

	// ---- rare visitors ----

	private static void scheduleRares(ServerLevel level, BlockPos origin, DivePoint point, RandomSource random) {
		RareSchedule s = new RareSchedule();
		s.left = point.rares.isEmpty() ? 0 : 1 + (random.nextInt(3) == 0 ? 1 : 0);
		s.nextAt = level.getGameTime() + RARE_FIRST_MIN + random.nextInt(RARE_FIRST_SPREAD);
		RARES.put(origin.asLong(), s);
	}

	private static void tickRares(ServerLevel level, BlockPos origin, DivePoint point, RandomSource random) {
		RareSchedule s = RARES.get(origin.asLong());
		if (s == null || s.left <= 0 || level.getGameTime() < s.nextAt) {
			return;
		}
		Pick pick = pick(point.rares, point, random);
		if (pick == null) {
			s.left = 0;
			return;
		}
		BlockPos at = nearDiver(level, origin, random);
		if (at == null) {
			// Nobody in the water right now: try again a little later.
			s.nextAt = level.getGameTime() + 200;
			return;
		}
		if (spawnAt(level, origin, pick, at.getX(), at.getY(), at.getZ(), random) > 0) {
			s.left--;
			s.nextAt = level.getGameTime() + RARE_NEXT_MIN + random.nextInt(RARE_NEXT_SPREAD);
		} else {
			s.nextAt = level.getGameTime() + 100;
		}
	}

	/** A water cell some distance from a random diver of this site, at about the diver's depth. */
	private static BlockPos nearDiver(ServerLevel level, BlockPos origin, RandomSource random) {
		AABB box = siteBox(origin);
		List<ServerPlayer> divers = new ArrayList<>();
		for (ServerPlayer p : level.players()) {
			if (!p.isSpectator() && p.isInWater() && box.contains(p.position())) {
				divers.add(p);
			}
		}
		if (divers.isEmpty()) {
			return null;
		}
		ServerPlayer diver = divers.get(random.nextInt(divers.size()));
		int r = DiveSites.RADIUS - 6;
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
		for (int attempt = 0; attempt < 8; attempt++) {
			double angle = random.nextDouble() * Math.PI * 2.0;
			double dist = RARE_DIST_MIN + random.nextInt(RARE_DIST_SPREAD + 1);
			int x = (int) Math.floor(diver.getX() + Math.cos(angle) * dist);
			int z = (int) Math.floor(diver.getZ() + Math.sin(angle) * dist);
			if (Math.abs(x - origin.getX()) > r || Math.abs(z - origin.getZ()) > r) {
				continue;
			}
			int baseY = (int) Math.floor(diver.getY());
			// Nearest water cell to the diver's depth in that column.
			for (int dy = 0; dy <= 6; dy++) {
				for (int sign = -1; sign <= 1; sign += 2) {
					pos.set(x, baseY + dy * sign, z);
					if (level.hasChunkAt(pos) && isWaterSource(level, pos) && pos.getY() < DiveSites.SURFACE_Y - 1) {
						return pos.immutable();
					}
				}
			}
		}
		return null;
	}

	// ---- regular groups ----

	private static boolean isWaterSource(ServerLevel level, BlockPos pos) {
		var fluid = level.getFluidState(pos);
		return fluid.is(FluidTags.WATER) && fluid.isSource();
	}

	private record Pick(DivePoint.Spawn spawn, EntityType<?> type) {
	}

	/** Weighted pick among the table rows whose species lives in the site's water (see Habitat). */
	private static Pick pick(List<DivePoint.Spawn> table, DivePoint point, RandomSource random) {
		List<DivePoint.Spawn> usable = new ArrayList<>();
		List<EntityType<?>> types = new ArrayList<>();
		int total = 0;
		for (DivePoint.Spawn s : table) {
			Identifier id = Identifier.tryParse(s.entityId());
			EntityType<?> type = id == null ? null : BuiltInRegistries.ENTITY_TYPE.getOptional(id).orElse(null);
			if (type != null && s.weight() > 0 && Habitat.fits(s.entityId(), point.habitats)) {
				usable.add(s);
				types.add(type);
				total += s.weight();
			}
		}
		if (usable.isEmpty()) {
			return null;
		}
		int roll = random.nextInt(total);
		int index = 0;
		for (; index < usable.size() - 1; index++) {
			roll -= usable.get(index).weight();
			if (roll < 0) {
				break;
			}
		}
		return new Pick(usable.get(index), types.get(index));
	}

	private static int spawnGroup(ServerLevel level, BlockPos origin, DivePoint point, RandomSource random) {
		Pick pick = pick(point.spawns, point, random);
		if (pick == null) {
			return 0;
		}
		if (ANEMONE_DWELLERS.contains(pick.spawn().entityId())) {
			BlockPos anemone = findAnemone(level, origin, random);
			return anemone == null ? 0 : spawnAt(level, origin, pick, anemone.getX(), anemone.getY(), anemone.getZ(), random);
		}

		// Pick a column inside the site and a random water-source block anywhere in it (under ice, in caves too).
		// Cave dwellers only take cells that cannot see the sky (the dark part of the cenote).
		boolean dark = DARK.contains(pick.spawn().entityId());
		int r = DiveSites.RADIUS - 6;
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
		int cx = 0;
		int cz = 0;
		int cy = 0;
		boolean found = false;
		int minY = Math.max(level.getMinY(), DiveSites.SURFACE_Y - 130);
		for (int attempt = 0; attempt < (dark ? 24 : 12) && !found; attempt++) {
			cx = origin.getX() + random.nextInt(r * 2 + 1) - r;
			cz = origin.getZ() + random.nextInt(r * 2 + 1) - r;
			pos.set(cx, DiveSites.SURFACE_Y - 1, cz);
			if (!level.hasChunkAt(pos)) {
				continue;
			}
			int seen = 0;
			for (int y = DiveSites.SURFACE_Y - 1; y >= minY; y--) {
				if (isWaterSource(level, pos.set(cx, y, cz)) && (!dark || !level.canSeeSky(pos)) && random.nextInt(++seen) == 0) {
					cy = y;
				}
			}
			found = seen > 0;
		}
		if (!found) {
			return 0;
		}
		return spawnAt(level, origin, pick, cx, cy, cz, random);
	}

	/** A sea anemone block of the site (searched in a few random columns), or null if none was hit. */
	private static BlockPos findAnemone(ServerLevel level, BlockPos origin, RandomSource random) {
		int r = DiveSites.RADIUS - 6;
		int minY = Math.max(level.getMinY(), DiveSites.SURFACE_Y - 130);
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
		for (int attempt = 0; attempt < ANEMONE_SEARCH_COLUMNS; attempt++) {
			int x = origin.getX() + random.nextInt(r * 2 + 1) - r;
			int z = origin.getZ() + random.nextInt(r * 2 + 1) - r;
			pos.set(x, DiveSites.SURFACE_Y - 1, z);
			if (!level.hasChunkAt(pos)) {
				continue;
			}
			// Walk down through the water (anemones are waterlogged) until the first solid floor.
			for (int y = DiveSites.SURFACE_Y - 1; y >= minY; y--) {
				pos.setY(y);
				if (level.getBlockState(pos).is(ScubaCreatures.SEA_ANEMONE)) {
					return pos.immutable();
				}
				if (!level.getFluidState(pos).is(FluidTags.WATER) && y < DiveSites.SURFACE_Y - 2) {
					break;
				}
			}
		}
		return null;
	}

	private static int spawnAt(ServerLevel level, BlockPos origin, Pick pick, int cx, int cy, int cz, RandomSource random) {
		DivePoint.Spawn spawn = pick.spawn();
		EntityType<?> type = pick.type();
		boolean bottom = BOTTOM.contains(spawn.entityId());
		boolean onAnemone = ANEMONE_DWELLERS.contains(spawn.entityId());
		int scatter = onAnemone ? 1 : SCATTER;
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
		int count = spawn.min() + random.nextInt(spawn.max() - spawn.min() + 1);
		SpawnGroupData groupData = null;
		int made = 0;
		for (int i = 0; i < count; i++) {
			int x = cx + random.nextInt(scatter * 2 + 1) - scatter;
			int z = cz + random.nextInt(scatter * 2 + 1) - scatter;
			int y = onAnemone ? cy + random.nextInt(2) : cy + random.nextInt(3) - 1;
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
