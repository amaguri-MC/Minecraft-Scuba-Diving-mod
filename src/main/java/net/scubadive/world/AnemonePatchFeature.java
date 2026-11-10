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

/** Scatters waterlogged sea anemones on sand/gravel around the origin (replaces vanilla random_patch, removed in 26.2). */
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
		for (int i = 0; i < 24; i++) {
			int x = origin.getX() + random.nextInt(11) - 5;
			int z = origin.getZ() + random.nextInt(11) - 5;
			int y = level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, x, z);
			pos.set(x, y, z);
			if (!level.getFluidState(pos).is(FluidTags.WATER) || !level.getFluidState(pos).isSource() || !level.getBlockState(pos).is(Blocks.WATER)) {
				continue;
			}
			BlockState below = level.getBlockState(pos.below());
			if (!below.is(Blocks.SAND) && !below.is(Blocks.GRAVEL)) {
				continue;
			}
			level.setBlock(pos, state, 2);
			placed++;
		}
		return placed > 0;
	}
}
