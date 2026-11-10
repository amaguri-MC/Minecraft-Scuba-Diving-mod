package net.scubadive.camera;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import net.scubadive.species.Species;

/**
 * Shared (client + server) subject search and scoring.
 * The screen is modelled as a circle of angular radius fov/2 around the view axis;
 * offset r = angle / (fov/2): 0 = dead centre, 1 = screen edge.
 */
public final class CameraLogic {
	public static final double MAX_DIST = 24.0;
	public static final double CENTER_LIMIT = 0.4;
	/** Server-side tolerances (view direction / position can lag the client slightly). */
	public static final double SERVER_MAX_DIST = 25.0;
	public static final double SERVER_CENTER_LIMIT = 0.6;
	public static final int NO_ENTITY = -1;

	private CameraLogic() { }

	public record Shot(Species species, int entityId, BlockPos pos, int score) { }

	/** Client: find the highest scoring subject, or null. */
	public static Shot pickBest(Player player, double fov) {
		Level level = player.level();
		Vec3 eye = player.getEyePosition();
		Vec3 look = player.getViewVector(1.0F);
		Shot best = null;

		List<Entity> nearby = nearby(player, level);
		for (Entity e : nearby) {
			Species s = speciesOf(e);
			if (s == null) continue;
			int score = scoreEntity(player, level, e, s, nearby, fov, MAX_DIST, CENTER_LIMIT, eye, look);
			if (score > 0 && (best == null || score > best.score())) {
				best = new Shot(s, e.getId(), BlockPos.ZERO, score);
			}
		}

		Vec3 end = eye.add(look.scale(MAX_DIST));
		BlockHitResult hit = level.clip(new ClipContext(eye, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
		if (hit.getType() == HitResult.Type.BLOCK) {
			BlockPos bp = hit.getBlockPos();
			Species s = blockSpecies(level, bp);
			if (s != null) {
				int score = scoreBlock(player, level, bp, s, fov, MAX_DIST, CENTER_LIMIT, eye, look);
				if (score > 0 && (best == null || score > best.score())) {
					best = new Shot(s, NO_ENTITY, bp, score);
				}
			}
		}
		return best;
	}

	/** Server: recompute the score of the claimed subject. Returns -1 if the claim is invalid. */
	public static int verify(Player player, Species species, int entityId, BlockPos pos, double fovClaim) {
		double fov = Math.max(30.0, Math.min(110.0, fovClaim));
		Level level = player.level();
		Vec3 eye = player.getEyePosition();
		Vec3 look = player.getViewVector(1.0F);
		if (species.isBlock) {
			if (blockSpecies(level, pos) != species) return -1;
			return scoreBlock(player, level, pos, species, fov, SERVER_MAX_DIST, SERVER_CENTER_LIMIT, eye, look);
		}
		Entity e = level.getEntity(entityId);
		if (e == null || e.isRemoved() || speciesOf(e) != species) return -1;
		return scoreEntity(player, level, e, species, nearby(player, level), fov, SERVER_MAX_DIST, SERVER_CENTER_LIMIT, eye, look);
	}

	private static List<Entity> nearby(Player player, Level level) {
		AABB box = player.getBoundingBox().inflate(SERVER_MAX_DIST);
		return level.getEntities((Entity) null, box, e -> e != player && !e.isRemoved());
	}

	private static Species speciesOf(Entity e) {
		return Species.byTarget(BuiltInRegistries.ENTITY_TYPE.getKey(e.getType()), false);
	}

	private static Species blockSpecies(Level level, BlockPos pos) {
		Identifier id = BuiltInRegistries.BLOCK.getKey(level.getBlockState(pos).getBlock());
		return Species.byTarget(id, true);
	}

	private static int scoreEntity(Player player, Level level, Entity e, Species s, List<Entity> nearby, double fov,
			double maxDist, double maxOffset, Vec3 eye, Vec3 look) {
		Vec3 center = e.getBoundingBox().getCenter();
		double dist = eye.distanceTo(center);
		if (dist > maxDist) return -1;
		double r = offset(eye, look, center, fov);
		if (r > maxOffset) return -1;
		if (!clearLine(player, level, eye, center)) return -1;

		int same = 0;
		for (Entity o : nearby) {
			if (speciesOf(o) != s) continue;
			Vec3 oc = o.getBoundingBox().getCenter();
			if (eye.distanceTo(oc) > maxDist) continue;
			if (offset(eye, look, oc, fov) > 1.0) continue;
			if (!clearLine(player, level, eye, oc)) continue;
			same++;
		}
		double size = Math.max(e.getBbWidth(), e.getBbHeight());
		return computeScore(s.basePoints, r, dist, size, fov, same);
	}

	private static int scoreBlock(Player player, Level level, BlockPos pos, Species s, double fov,
			double maxDist, double maxOffset, Vec3 eye, Vec3 look) {
		Vec3 center = Vec3.atCenterOf(pos);
		double dist = eye.distanceTo(center);
		if (dist > maxDist) return -1;
		if (offset(eye, look, center, fov) > maxOffset) return -1;
		BlockHitResult hit = level.clip(new ClipContext(eye, center, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
		if (hit.getType() == HitResult.Type.BLOCK && !hit.getBlockPos().equals(pos)
				&& hit.getLocation().distanceTo(center) > 0.9) {
			return -1;
		}
		// A block subject is aimed at directly: treat it as dead centre, 1 block in size.
		return computeScore(s.basePoints, 0.0, dist, 1.0, fov, 1);
	}

	/** score = base * centre(1.0-2.0) * size(1.0-1.5, lower when far) * group(1.5 if 3+ of the species on screen). */
	public static int computeScore(int basePoints, double r, double dist, double size, double fov, int sameOnScreen) {
		double center = 1.0 + (1.0 - clamp(r / CENTER_LIMIT, 0.0, 1.0));
		double angular = 2.0 * Math.atan(size / (2.0 * Math.max(dist, 0.5)));
		double frac = angular / Math.toRadians(fov);
		double sizeFactor = 1.0 + 0.5 * clamp(frac / 0.25, 0.0, 1.0);
		if (dist > 16.0) {
			// too far: the size bonus fades out completely by 24 blocks
			sizeFactor = 1.0 + (sizeFactor - 1.0) * clamp(1.0 - (dist - 16.0) / 8.0, 0.0, 1.0);
		}
		double group = sameOnScreen >= 3 ? 1.5 : 1.0;
		return Math.max(1, (int) Math.round(basePoints * center * sizeFactor * group));
	}

	private static double offset(Vec3 eye, Vec3 look, Vec3 target, double fov) {
		Vec3 d = target.subtract(eye);
		double len = d.length();
		if (len < 1.0E-6) return 0.0;
		double cos = clamp(d.dot(look) / len, -1.0, 1.0);
		return Math.acos(cos) / Math.toRadians(fov / 2.0);
	}

	private static boolean clearLine(Player player, Level level, Vec3 from, Vec3 to) {
		BlockHitResult hit = level.clip(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
		return hit.getType() == HitResult.Type.MISS;
	}

	private static double clamp(double v, double lo, double hi) {
		return Math.max(lo, Math.min(hi, v));
	}
}
