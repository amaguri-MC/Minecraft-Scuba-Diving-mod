package net.scubadive.world;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.GrowingPlantHeadBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * Runs once per chunk after vegetation: cuts every kelp column in the chunk down to MAX_HEIGHT
 * and sets the new top to max age so it never grows taller.
 */
public class KelpTrimFeature extends Feature<NoneFeatureConfiguration> {
	private static final int MAX_HEIGHT = 3;
	private static final int MAX_SCAN = 64;

	public KelpTrimFeature(Codec<NoneFeatureConfiguration> codec) {
		super(codec);
	}

	@Override
	public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
		WorldGenLevel level = context.level();
		int baseX = context.origin().getX() & ~15;
		int baseZ = context.origin().getZ() & ~15;
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
		boolean changed = false;
		for (int dx = 0; dx < 16; dx++) {
			for (int dz = 0; dz < 16; dz++) {
				int x = baseX + dx;
				int z = baseZ + dz;
				int floor = level.getHeight(Heightmap.Types.OCEAN_FLOOR, x, z);
				int count = 0;
				for (int y = floor; y < floor + MAX_SCAN; y++) {
					pos.set(x, y, z);
					BlockState state = level.getBlockState(pos);
					if (!state.is(Blocks.KELP) && !state.is(Blocks.KELP_PLANT)) {
						break;
					}
					count++;
					if (count == MAX_HEIGHT) {
						// New capped top: a fully grown kelp head.
						level.setBlock(pos, Blocks.KELP.defaultBlockState().setValue(GrowingPlantHeadBlock.AGE, GrowingPlantHeadBlock.MAX_AGE), 2);
					} else if (count > MAX_HEIGHT) {
						level.setBlock(pos, Blocks.WATER.defaultBlockState(), 2);
						changed = true;
					}
				}
			}
		}
		return changed;
	}
}
