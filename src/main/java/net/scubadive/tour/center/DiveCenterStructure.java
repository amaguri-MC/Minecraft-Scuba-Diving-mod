package net.scubadive.tour.center;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.QuartPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;

import net.scubadive.ScubaDive;

/**
 * Beach dive center as a real structure (findable with /locate structure scubadive:dive_center).
 * The site search runs on the noise heightmaps: dry ground near sea level with open sea 4-8 blocks away
 * in one of the four directions. No suitable spot in the start chunk area means no structure.
 */
public class DiveCenterStructure extends Structure {
	public static final MapCodec<DiveCenterStructure> CODEC = Structure.simpleCodec(DiveCenterStructure::new);
	public static final StructureType<DiveCenterStructure> TYPE = () -> CODEC;
	public static final StructurePieceType.ContextlessType PIECE = DiveCenterPiece::new;

	/** Candidate origins: offsets from the start chunk's middle. */
	private static final int[] OFFSETS = {-12, -6, 0, 6, 12};

	public DiveCenterStructure(StructureSettings settings) {
		super(settings);
	}

	/** Registers the structure type and piece type. Data: worldgen/structure + structure_set dive_center. */
	public static void register() {
		Registry.register(BuiltInRegistries.STRUCTURE_TYPE, ScubaDive.id("dive_center"), TYPE);
		Registry.register(BuiltInRegistries.STRUCTURE_PIECE, ScubaDive.id("dive_center"), PIECE);
	}

	@Override
	public StructureType<?> type() {
		return TYPE;
	}

	/** Noise-stage view of the terrain used by the site search. */
	private record Terrain(ChunkGenerator gen, LevelHeightAccessor height, RandomState random, int sea) {
		int floor(int x, int z) {
			return gen.getBaseHeight(x, z, Heightmap.Types.OCEAN_FLOOR_WG, height, random);
		}

		int surface(int x, int z) {
			return gen.getBaseHeight(x, z, Heightmap.Types.WORLD_SURFACE_WG, height, random);
		}

		/** Water at sea level: the solid ground is below sea level and something (water) is on top of it. */
		boolean isWater(int x, int z) {
			int floor = floor(x, z);
			return floor < sea && surface(x, z) > floor;
		}

		/** Ground top block (or -1 if the column is wet or the ground is below sea level). */
		int dryGround(int x, int z) {
			int floor = floor(x, z);
			if (floor < sea || surface(x, z) != floor) {
				return -1;
			}
			return floor - 1;
		}
	}

	@Override
	protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
		ChunkPos chunk = context.chunkPos();
		Terrain t = new Terrain(context.chunkGenerator(), context.heightAccessor(), context.randomState(),
				context.chunkGenerator().getSeaLevel());

		List<int[]> origins = new ArrayList<>();
		for (int dx : OFFSETS) {
			for (int dz : OFFSETS) {
				origins.add(new int[]{chunk.getMiddleBlockX() + dx, chunk.getMiddleBlockZ() + dz});
			}
		}
		java.util.Random shuffle = new java.util.Random(context.random().nextLong());
		Collections.shuffle(origins, shuffle);
		List<Direction> dirs = new ArrayList<>();
		for (Direction d : Direction.Plane.HORIZONTAL) {
			dirs.add(d);
		}

		for (int[] o : origins) {
			int x = o[0];
			int z = o[1];
			// Cheap biome check first; the same position is checked again by Structure#findValidGenerationPoint.
			if (!context.validBiome().test(context.biomeSource().getNoiseBiome(
					QuartPos.fromBlock(x), QuartPos.fromBlock(t.sea()), QuartPos.fromBlock(z), context.randomState().sampler()))) {
				continue;
			}
			int y0 = t.dryGround(x, z);
			if (y0 < t.sea() || y0 > t.sea() + 3) {
				continue;
			}
			Collections.shuffle(dirs, shuffle);
			for (Direction dir : dirs) {
				DiveCenterPiece.Frame frame = new DiveCenterPiece.Frame(x, z, dir);
				int shore = findShore(t, frame);
				if (shore > 0 && footprintDry(t, frame, y0)) {
					int fy = y0 + 1;
					int pierEnd = Math.min(DiveCenterPiece.MAX_PIER_END, shore + 4);
					BlockPos pos = new BlockPos(x, fy, z);
					return Optional.of(new GenerationStub(pos,
							builder -> builder.addPiece(new DiveCenterPiece(x, z, dir, fy, pierEnd))));
				}
			}
		}
		return Optional.empty();
	}

	/** Distance of the first water column along the pier axis (4-8), or -1 if the spot is unsuitable. */
	private static int findShore(Terrain t, DiveCenterPiece.Frame frame) {
		for (int k = 1; k <= 3; k++) {
			BlockPos p = frame.at(0, k, 0);
			if (t.isWater(p.getX(), p.getZ())) {
				return -1;
			}
		}
		for (int k = 4; k <= 8; k++) {
			BlockPos p = frame.at(0, k, 0);
			if (t.isWater(p.getX(), p.getZ())) {
				// Open water for a few blocks beyond the shore line.
				for (int j = 1; j <= 3; j++) {
					BlockPos q = frame.at(0, k + j, 0);
					if (!t.isWater(q.getX(), q.getZ())) {
						return -1;
					}
				}
				return k;
			}
		}
		return -1;
	}

	/** The hut corners must be dry and not much higher or lower than the origin. */
	private static boolean footprintDry(Terrain t, DiveCenterPiece.Frame frame, int y0) {
		int[][] corners = {{-4, -3}, {4, -3}, {-4, 3}, {4, 3}, {0, -3}, {0, 3}};
		for (int[] c : corners) {
			BlockPos p = frame.at(c[0], c[1], 0);
			int ground = t.dryGround(p.getX(), p.getZ());
			if (ground < 0 || ground < y0 - 2 || ground > y0 + 2) {
				return false;
			}
		}
		return true;
	}
}
