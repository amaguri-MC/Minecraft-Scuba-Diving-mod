package net.scubadive.world;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/** A cluster of hexagon-ish basalt pillars of varying height standing on the sea floor. */
public class BasaltColumnsFeature extends Feature<NoneFeatureConfiguration> {
	public BasaltColumnsFeature(Codec<NoneFeatureConfiguration> codec) {
		super(codec);
	}

	@Override
	public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
		WorldGenLevel level = context.level();
		BlockPos origin = context.origin();
		RandomSource random = context.random();

		if (!level.getFluidState(origin).is(FluidTags.WATER)) {
			return false;
		}

		int radius = 3 + random.nextInt(3); // 3..5
		int maxTop = level.getSeaLevel() - 3; // keep the tops under water
		int placed = 0;
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

		for (int dx = -radius; dx <= radius; dx++) {
			for (int dz = -radius; dz <= radius; dz++) {
				// hexagon-like footprint: square clipped at the corners
				int ax = Math.abs(dx);
				int az = Math.abs(dz);
				if (ax + az > radius + radius / 2) {
					continue;
				}
				if (random.nextInt(5) == 0) {
					continue; // ragged edge
				}

				int x = origin.getX() + dx;
				int z = origin.getZ() + dz;
				int floor = level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, x, z); // first free block above the floor
				pos.set(x, floor, z);
				if (!level.getFluidState(pos).is(FluidTags.WATER)) {
					continue;
				}

				int dist = Math.max(ax, az);
				int height = 2 + random.nextInt(5) - dist / 2; // taller in the middle, uneven
				if (height < 1) {
					height = 1;
				}
				int top = Math.min(floor + height - 1, maxTop);
				if (top < floor) {
					continue;
				}

				for (int y = floor; y <= top; y++) {
					pos.set(x, y, z);
					if (!level.getFluidState(pos).is(FluidTags.WATER)) {
						break;
					}
					level.setBlock(pos, pickState(random, y == top), 2);
				}
				placed++;
			}
		}
		return placed > 0;
	}

	private static BlockState pickState(RandomSource random, boolean isTop) {
		if (isTop && random.nextInt(2) == 0) {
			return Blocks.POLISHED_BASALT.defaultBlockState();
		}
		return random.nextInt(10) < 7 ? Blocks.BASALT.defaultBlockState() : Blocks.SMOOTH_BASALT.defaultBlockState();
	}
}
