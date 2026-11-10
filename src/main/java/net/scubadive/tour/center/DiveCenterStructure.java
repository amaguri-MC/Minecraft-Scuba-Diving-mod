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
	private static final int[] OFFSETS = {-16, -8, 0, 8, 16};

	/** Cost counters for the site search (read by the automated client game test). */
	public static final java.util.concurrent.atomic.AtomicLong SEARCH_CALLS = new java.util.concurrent.atomic.AtomicLong();
	public static final java.util.concurrent.atomic.AtomicLong SEARCH_NANOS = new java.util.concurrent.atomic.AtomicLong();
	public static final java.util.concurrent.atomic.AtomicLong HEIGHT_SAMPLES = new java.util.concurrent.atomic.AtomicLong();

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

	/** Most noise height samples one site search may take (each is a noise column evaluation, the expensive part). */
	static final int SAMPLE_BUDGET = 96;

	/** Thrown when the search runs out of its sample budget. */
	private static final class OutOfBudget extends RuntimeException {
		OutOfBudget() {
			super(null, null, false, false);
		}
	}

	/**
	 * Noise-stage view of the terrain used by the site search. Each column is sampled once (cached) and the
	 * whole search is capped at {@link #SAMPLE_BUDGET} samples so a start chunk can never stall world generation.
	 */
	private static final class Terrain {
		private final ChunkGenerator gen;
		private final LevelHeightAccessor height;
		private final RandomState random;
		private final int sea;
		private final it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap cache = new it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap();
		private int samples;

		Terrain(ChunkGenerator gen, LevelHeightAccessor height, RandomState random, int sea) {
			this.gen = gen;
			this.height = height;
			this.random = random;
			this.sea = sea;
		}

		int sea() {
			return sea;
		}

		/** Top of the solid ground (first block above it). Water sits on top when this is below sea level. */
		int floor(int x, int z) {
			long key = ((long) x << 32) ^ (z & 0xFFFFFFFFL);
			if (cache.containsKey(key)) {
				return cache.get(key);
			}
			if (samples >= SAMPLE_BUDGET) {
				throw new OutOfBudget();
			}
			samples++;
			HEIGHT_SAMPLES.incrementAndGet();
			int floor = gen.getBaseHeight(x, z, Heightmap.Types.OCEAN_FLOOR_WG, height, random);
			cache.put(key, floor);
			return floor;
		}

		/** Sea water at this column: the ground is below sea level. */
		boolean isWater(int x, int z) {
			return floor(x, z) < sea;
		}

		/** Ground top block (or -1 if the column is under water). */
		int dryGround(int x, int z) {
			int floor = floor(x, z);
			return floor < sea ? -1 : floor - 1;
		}
	}

	@Override
	protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
		long start = System.nanoTime();
		try {
			return search(context);
		} catch (OutOfBudget e) {
			return Optional.empty();
		} finally {
			SEARCH_CALLS.incrementAndGet();
			SEARCH_NANOS.addAndGet(System.nanoTime() - start);
		}
	}

	private Optional<GenerationStub> search(GenerationContext context) {
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
			if (y0 < t.sea() || y0 > t.sea() + 5) {
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

	/** Distance of the first water column along the pier axis (3-9), or -1 if the spot is unsuitable. */
	private static int findShore(Terrain t, DiveCenterPiece.Frame frame) {
		for (int k = 1; k <= 2; k++) {
			BlockPos p = frame.at(0, k, 0);
			if (t.isWater(p.getX(), p.getZ())) {
				return -1;
			}
		}
		for (int k = 3; k <= 9; k++) {
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
			if (ground < 0 || ground < y0 - 3 || ground > y0 + 3) {
				return false;
			}
		}
		return true;
	}
}
