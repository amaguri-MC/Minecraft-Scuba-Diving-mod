package net.scubadive.reef;

import java.util.HashMap;
import java.util.Map;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/** Irregular rock mounds (2-6 high, 4-10 wide) on the sea floor, grouped into reef zones. */
public class RockReefFeature extends Feature<NoneFeatureConfiguration> {
	/** Max horizontal reach from the origin; stays inside the 3x3 chunk write area. */
	private static final int REACH = 12;

	public RockReefFeature(Codec<NoneFeatureConfiguration> codec) {
		super(codec);
	}

	@Override
	public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
		WorldGenLevel level = context.level();
		BlockPos origin = context.origin();
		RandomSource random = context.random();

		float zone = ReefNoise.zone(level.getSeed(), origin.getX(), origin.getZ());
		int mounds;
		if (zone > 0.55F) {
			mounds = 3 + random.nextInt(4);
		} else if (zone > 0.4F) {
			mounds = 1 + random.nextInt(3);
		} else {
			mounds = random.nextInt(3) == 0 ? 1 : 0;
		}
		if (mounds == 0) {
			return false;
		}

		Map<Long, Integer> baseFloor = new HashMap<>();
		int placed = 0;
		for (int i = 0; i < mounds; i++) {
			int cx = origin.getX() + random.nextInt(8) + random.nextInt(8) - 7;
			int cz = origin.getZ() + random.nextInt(8) + random.nextInt(8) - 7;
			int rx = 2 + random.nextInt(4); // width 4..10
			int rz = 2 + random.nextInt(4);
			int height = 2 + random.nextInt(5); // 2..6
			placed += mound(level, random, origin, baseFloor, cx, cz, rx, rz, height);
		}
		return placed > 0;
	}

	private static int mound(WorldGenLevel level, RandomSource random, BlockPos origin, Map<Long, Integer> baseFloor,
			int cx, int cz, int rx, int rz, int height) {
		int placed = 0;
		int maxTop = level.getSeaLevel() - 2;
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
		for (int dx = -rx; dx <= rx; dx++) {
			for (int dz = -rz; dz <= rz; dz++) {
				int x = cx + dx;
				int z = cz + dz;
				if (Math.abs(x - origin.getX()) > REACH || Math.abs(z - origin.getZ()) > REACH) {
					continue;
				}
				float nx = dx / (rx + 0.5F);
				float nz = dz / (rz + 0.5F);
				float d = (float) Math.sqrt(nx * nx + nz * nz) + (random.nextFloat() - 0.5F) * 0.4F;
				if (d >= 1.0F) {
					continue;
				}
				int h = Math.round(height * (1.0F - d * d));
				if (random.nextInt(4) == 0) {
					h--;
				}
				if (h < 1) {
					continue;
				}
				long key = BlockPos.asLong(x, 0, z);
				Integer cached = baseFloor.get(key);
				int floor;
				if (cached == null) {
					floor = level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, x, z);
					baseFloor.put(key, floor);
				} else {
					floor = cached;
				}
				int top = Math.min(floor + h, maxTop);
				for (int y = floor; y < top; y++) {
					pos.set(x, y, z);
					if (!level.getBlockState(pos).is(Blocks.WATER)) {
						continue;
					}
					level.setBlock(pos, pickRock(level.getSeed(), x, y, z), 2);
					placed++;
				}
			}
		}
		return placed;
	}

	/** Rock type varies in small 2x2x2 clumps so the mound looks veined, not noisy. */
	private static net.minecraft.world.level.block.state.BlockState pickRock(long seed, int x, int y, int z) {
		float r = ReefNoise.hash((x >> 1) + (y >> 1) * 31, z >> 1, (int) seed);
		Block block;
		if (r < 0.33F) {
			block = Blocks.STONE;
		} else if (r < 0.58F) {
			block = Blocks.ANDESITE;
		} else if (r < 0.73F) {
			block = Blocks.TUFF;
		} else if (r < 0.88F) {
			block = Blocks.COBBLESTONE;
		} else {
			block = Blocks.MOSSY_COBBLESTONE;
		}
		return block.defaultBlockState();
	}
}
