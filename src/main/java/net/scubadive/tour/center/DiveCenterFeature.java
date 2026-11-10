package net.scubadive.tour.center;

import java.util.ArrayList;
import java.util.List;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

/**
 * Feature version of the dive center, kept only for /place feature scubadive:dive_center (not in any biome).
 * World generation uses {@link DiveCenterStructure}; the building itself is {@link DiveCenterPiece#build}.
 */
public class DiveCenterFeature extends Feature<NoneFeatureConfiguration> {
	private static final int MAX_PIER_END = DiveCenterPiece.MAX_PIER_END;

	public DiveCenterFeature(Codec<NoneFeatureConfiguration> codec) {
		super(codec);
	}

	/** Position and orientation of one hut. */
	private record Frame(BlockPos origin, Direction sea, Direction right) {
		BlockPos at(int u, int v, int y) {
			return new BlockPos(
					origin.getX() + right.getStepX() * u + sea.getStepX() * v,
					y,
					origin.getZ() + right.getStepZ() * u + sea.getStepZ() * v);
		}
	}

	@Override
	public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
		WorldGenLevel level = context.level();
		BlockPos origin = context.origin();
		RandomSource random = context.random();
		int sea = level.getSeaLevel();

		// Dry ground at about sea level under the origin.
		int y0 = origin.getY() - 1;
		if (y0 < sea || y0 > sea + 3 || !isDry(level, origin.getX(), origin.getZ(), sea)) {
			return false;
		}

		// Pick a direction that reaches open water soon.
		List<Direction> dirs = new ArrayList<>();
		for (Direction d : Direction.Plane.HORIZONTAL) {
			dirs.add(d);
		}
		java.util.Collections.shuffle(dirs, new java.util.Random(random.nextLong()));
		Frame frame = null;
		int shore = -1;
		for (Direction dir : dirs) {
			Frame candidate = new Frame(origin, dir, dir.getClockWise());
			int k = findShore(level, candidate, sea);
			if (k > 0 && footprintDry(level, candidate, sea, y0)) {
				frame = candidate;
				shore = k;
				break;
			}
		}
		if (frame == null) {
			return false;
		}
		int fy = y0 + 1;
		int pierEnd = Math.min(MAX_PIER_END, shore + 4);

		DiveCenterPiece.build(level, new DiveCenterPiece.Frame(origin.getX(), origin.getZ(), frame.sea()), fy, pierEnd,
				BoundingBox.infinite());
		return true;
	}

	// ---- site checks --------------------------------------------------------------------------------

	private static boolean isWater(WorldGenLevel level, int x, int z, int sea) {
		return level.getFluidState(new BlockPos(x, sea - 1, z)).is(FluidTags.WATER);
	}

	/** Dry land: no water at sea level in this column and the surface is not water. */
	private static boolean isDry(WorldGenLevel level, int x, int z, int sea) {
		return !isWater(level, x, z, sea)
				&& level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, x, z) == level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z);
	}

	/** Distance (in blocks from the origin) of the first water column along the pier axis, or -1 if unsuitable. */
	private int findShore(WorldGenLevel level, Frame frame, int sea) {
		for (int k = 0; k <= 3; k++) {
			BlockPos p = frame.at(0, k, 0);
			if (isWater(level, p.getX(), p.getZ(), sea)) {
				return -1;
			}
		}
		for (int k = 4; k <= 8; k++) {
			BlockPos p = frame.at(0, k, 0);
			if (isWater(level, p.getX(), p.getZ(), sea)) {
				// Open water for a few blocks beyond the shore line.
				for (int j = 1; j <= 3; j++) {
					BlockPos q = frame.at(0, k + j, 0);
					if (!isWater(level, q.getX(), q.getZ(), sea)) {
						return -1;
					}
				}
				return k;
			}
		}
		return -1;
	}

	/** The hut corners must be dry and not much higher or lower than the origin. */
	private static boolean footprintDry(WorldGenLevel level, Frame frame, int sea, int y0) {
		int[][] corners = {{-4, -3}, {4, -3}, {-4, 3}, {4, 3}, {0, -3}, {0, 3}};
		for (int[] c : corners) {
			BlockPos p = frame.at(c[0], c[1], 0);
			if (!isDry(level, p.getX(), p.getZ(), sea)) {
				return false;
			}
			int ground = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, p.getX(), p.getZ()) - 1;
			if (ground < y0 - 2 || ground > y0 + 2) {
				return false;
			}
		}
		return true;
	}
}
