package net.scubadive.world;

import com.mojang.serialization.Codec;
import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.scubadive.ScubaDive;

/**
 * Runs once per chunk (origin = chunk corner) and carves the part of a deep trench that lies inside that
 * chunk. The trench shape is a pure function of world coordinates (TrenchField), so chunks join seamlessly.
 * Never writes outside its own 16x16 chunk. Also re-biomes the carved area and places basalt columns.
 */
public class DeepTrenchFeature extends Feature<NoneFeatureConfiguration> {
	public static final ResourceKey<Biome> DEEP_TRENCH = ResourceKey.create(Registries.BIOME, ScubaDive.id("deep_trench"));

	private static final int NONE = Integer.MIN_VALUE;
	private static final int CENTRE_Y = -40;
	private static final int MIN_BOTTOM = -56;

	private static final BlockState WATER = Blocks.WATER.defaultBlockState();

	public DeepTrenchFeature(Codec<NoneFeatureConfiguration> codec) {
		super(codec);
	}

	@Override
	public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
		WorldGenLevel level = context.level();
		int minX = context.origin().getX() & ~15;
		int minZ = context.origin().getZ() & ~15;
		long seed = level.getSeed();

		// Trench-ness for the chunk plus a 1-block border (used to find exposed wall faces).
		double[][] ts = new double[18][18];
		boolean any = false;
		for (int i = 0; i < 18; i++) {
			for (int j = 0; j < 18; j++) {
				double t = TrenchField.shape(seed, minX - 1 + i + 0.5, minZ - 1 + j + 0.5);
				ts[i][j] = t;
				if (t > 0.0 && i >= 1 && i <= 16 && j >= 1 && j <= 16) {
					any = true;
				}
			}
		}
		if (!any) {
			return false;
		}
		// Leave chunks touched by structures (ocean monuments, ruins, wrecks) alone.
		if (level.getLevel().structureManager().hasAnyStructureAt(new BlockPos(minX + 8, CENTRE_Y, minZ + 8))) {
			return false;
		}

		int seaLevel = level.getSeaLevel();
		int[][] bottoms = new int[16][16];
		int[][] cellCarved = new int[4][4];
		int[][] cellTop = new int[4][4];
		for (int[] row : bottoms) {
			java.util.Arrays.fill(row, NONE);
		}
		for (int[] row : cellTop) {
			java.util.Arrays.fill(row, NONE);
		}
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
		boolean carvedAny = false;

		for (int dx = 0; dx < 16; dx++) {
			for (int dz = 0; dz < 16; dz++) {
				double t = ts[dx + 1][dz + 1];
				if (t <= 0.0) {
					continue;
				}
				int x = minX + dx;
				int z = minZ + dz;
				int floorTop = level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, x, z) - 1; // top solid block
				int depthBelowSea = seaLevel - floorTop;
				double fade = smooth((depthBelowSea - 18) / 10.0);
				if (fade <= 0.0) {
					continue;
				}
				pos.set(x, floorTop, z);
				Holder<Biome> biome = level.getBiome(pos);
				if (!biome.is(BiomeTags.IS_DEEP_OCEAN) && !biome.is(DEEP_TRENCH)) {
					continue;
				}
				int centre = Math.max(MIN_BOTTOM, Math.min(CENTRE_Y, floorTop - 45))
						+ (int) Math.round(TrenchField.floorNoise(seed, x, z) * 2.5);
				int fullDepth = floorTop - centre;
				if (fullDepth <= 2) {
					continue;
				}
				int bottom = floorTop - (int) Math.round(fullDepth * TrenchField.profile(t) * fade);
				if (bottom >= floorTop) {
					continue;
				}

				// Exposed wall height towards the lowest of the four neighbours (estimated from the border grid).
				int extra = 0;
				int[][] nb = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
				for (int[] d : nb) {
					double tn = ts[dx + 1 + d[0]][dz + 1 + d[1]];
					int nBottom = floorTop - (int) Math.round(fullDepth * TrenchField.profile(tn) * fade);
					extra = Math.max(extra, bottom - nBottom);
				}
				extra = Math.min(extra, 48);

				for (int y = floorTop; y > bottom; y--) {
					pos.set(x, y, z);
					level.setBlock(pos, WATER, 2);
				}
				double flat = TrenchField.profile(t) * fade;
				for (int y = bottom; y >= bottom - extra - 1 && y > level.getMinY() + 4; y--) {
					pos.set(x, y, z);
					BlockState old = level.getBlockState(pos);
					if (old.isAir() || old.is(Blocks.WATER) || old.is(Blocks.BEDROCK)) {
						continue;
					}
					level.setBlock(pos, material(seed, x, y, z, y == bottom && flat >= 0.92), 2);
				}
				bottoms[dx][dz] = bottom;
				cellCarved[dx >> 2][dz >> 2]++;
				cellTop[dx >> 2][dz >> 2] = Math.max(cellTop[dx >> 2][dz >> 2], floorTop);
				carvedAny = true;
			}
		}
		if (!carvedAny) {
			return false;
		}

		BasaltColumnsFeature.place(level, minX, minZ, seed, bottoms);

		// Re-biome the carved area (same mechanism as /fillbiome).
		ChunkAccess chunk = level.getChunk(minX >> 4, minZ >> 4);
		Holder<Biome> trench = level.registryAccess().lookupOrThrow(Registries.BIOME).getOrThrow(DEEP_TRENCH);
		int baseQx = minX >> 2;
		int baseQz = minZ >> 2;
		chunk.fillBiomesFromNoise((qx, qy, qz, sampler) -> {
			int cx = qx - baseQx;
			int cz = qz - baseQz;
			if (cx >= 0 && cx < 4 && cz >= 0 && cz < 4 && cellCarved[cx][cz] >= 8 && (qy << 2) <= cellTop[cx][cz]) {
				return trench;
			}
			return chunk.getNoiseBiome(qx, qy, qz);
		}, Climate.empty());
		chunk.markUnsaved();

		Heightmap.primeHeightmaps(chunk, EnumSet.of(Heightmap.Types.OCEAN_FLOOR_WG, Heightmap.Types.WORLD_SURFACE_WG,
				Heightmap.Types.OCEAN_FLOOR, Heightmap.Types.WORLD_SURFACE));
		return true;
	}

	private static double smooth(double v) {
		v = Math.max(0.0, Math.min(1.0, v));
		return v * v * (3.0 - 2.0 * v);
	}

	private static BlockState material(long seed, int x, int y, int z, boolean floor) {
		long h = seed ^ (x * 0x9E3779B97F4A7C15L) ^ (y * 0xC2B2AE3D27D4EB4FL) ^ (z * 0x165667B19E3779F9L);
		h ^= h >>> 31;
		h *= 0xFF51AFD7ED558CCDL;
		h ^= h >>> 29;
		int r = (int) ((h >>> 8) % 100);
		if (floor) {
			if (r < 40) {
				return Blocks.GRAVEL.defaultBlockState();
			} else if (r < 70) {
				return Blocks.MUD.defaultBlockState();
			} else if (r < 90) {
				return Blocks.BASALT.defaultBlockState();
			}
			return Blocks.SMOOTH_BASALT.defaultBlockState();
		}
		if (r < 45) {
			return Blocks.DEEPSLATE.defaultBlockState();
		} else if (r < 70) {
			return Blocks.BASALT.defaultBlockState();
		} else if (r < 95) {
			return Blocks.BLACKSTONE.defaultBlockState();
		}
		return Blocks.TUFF.defaultBlockState();
	}
}
