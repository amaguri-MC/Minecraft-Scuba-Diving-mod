package net.scubadive.world;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/** Fills a dense disc of waterlogged sea anemones on sand/gravel/rock around the origin (replaces vanilla random_patch, removed in 26.2). */
public class AnemonePatchFeature extends Feature<NoneFeatureConfiguration> {
	private static final Identifier ANEMONE = Identifier.fromNamespaceAndPath("scubadive", "sea_anemone");

	public AnemonePatchFeature(Codec<NoneFeatureConfiguration> codec) {
		super(codec);
	}

	@Override
	public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
		WorldGenLevel level = context.level();
		BlockPos origin = context.origin();
		RandomSource random = context.random();
		Block block = BuiltInRegistries.BLOCK.getValue(ANEMONE);
		if (block == Blocks.AIR) {
			return false;
		}
		BlockState state = block.defaultBlockState();
		if (state.hasProperty(BlockStateProperties.WATERLOGGED)) {
			state = state.setValue(BlockStateProperties.WATERLOGGED, true);
		}

		int placed = 0;
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
		// Fill a disc: almost solid in the middle, thinning out towards the rim (60..120 anemones).
		int radius = 6 + random.nextInt(3); // 6..8, stays well inside the 3x3 chunk write area
		for (int dx = -radius; dx <= radius; dx++) {
			for (int dz = -radius; dz <= radius; dz++) {
				float dist = (float) Math.sqrt(dx * dx + dz * dz);
				if (dist > radius) {
					continue;
				}
				float chance = 1.1F - 0.7F * dist / radius;
				if (random.nextFloat() >= chance) {
					continue;
				}
				int x = origin.getX() + dx;
				int z = origin.getZ() + dz;
				int y = level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, x, z);
				pos.set(x, y, z);
				if (!level.getFluidState(pos).is(FluidTags.WATER) || !level.getFluidState(pos).isSource() || !level.getBlockState(pos).is(Blocks.WATER)) {
					continue;
				}
				BlockState below = level.getBlockState(pos.below());
				if (!below.is(Blocks.SAND) && !below.is(Blocks.GRAVEL) && !below.is(Blocks.CLAY)
						&& !below.is(Blocks.DIRT) && !below.is(Blocks.MOSS_BLOCK)
						&& !below.is(Blocks.STONE) && !below.is(Blocks.ANDESITE) && !below.is(Blocks.TUFF)) {
					continue;
				}
				level.setBlock(pos, state, 2);
				placed++;
			}
		}
		return placed > 0;
	}
}
