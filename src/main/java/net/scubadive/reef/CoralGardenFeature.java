package net.scubadive.reef;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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

/** A dense colony of coral blocks on the sea floor and on rock mounds, grouped into reef zones. */
public class CoralGardenFeature extends Feature<NoneFeatureConfiguration> {
	private static final int RADIUS = 9;
	private static final Direction[] HORIZONTAL = {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};
	/** Vanilla live corals: {block, plant, fan}. */
	private static final Block[][] VANILLA = {
			{Blocks.TUBE_CORAL_BLOCK, Blocks.TUBE_CORAL, Blocks.TUBE_CORAL_FAN},
			{Blocks.BRAIN_CORAL_BLOCK, Blocks.BRAIN_CORAL, Blocks.BRAIN_CORAL_FAN},
			{Blocks.BUBBLE_CORAL_BLOCK, Blocks.BUBBLE_CORAL, Blocks.BUBBLE_CORAL_FAN},
			{Blocks.FIRE_CORAL_BLOCK, Blocks.FIRE_CORAL, Blocks.FIRE_CORAL_FAN},
			{Blocks.HORN_CORAL_BLOCK, Blocks.HORN_CORAL, Blocks.HORN_CORAL_FAN},
	};

	public CoralGardenFeature(Codec<NoneFeatureConfiguration> codec) {
		super(codec);
	}

	@Override
	public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
		WorldGenLevel level = context.level();
		BlockPos origin = context.origin();
		RandomSource random = context.random();

		float zone = ReefNoise.zone(level.getSeed(), origin.getX(), origin.getZ());
		int attempts;
		if (zone > 0.55F) {
			attempts = 170 + random.nextInt(90);
		} else if (zone > 0.4F) {
			attempts = 70 + random.nextInt(60);
		} else if (random.nextInt(4) == 0) {
			attempts = 40;
		} else {
			return false;
		}

		int placed = 0;
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
		for (int i = 0; i < attempts; i++) {
			// triangular distribution: densest in the middle
			int dx = random.nextInt(RADIUS + 1) + random.nextInt(RADIUS + 1) - RADIUS;
			int dz = random.nextInt(RADIUS + 1) + random.nextInt(RADIUS + 1) - RADIUS;
			if (dx * dx + dz * dz > RADIUS * RADIUS) {
				continue;
			}
			int x = origin.getX() + dx;
			int z = origin.getZ() + dz;
			int y = level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, x, z);
			pos.set(x, y, z);
			if (!canStand(level, pos)) {
				continue;
			}

			// colonies: one type dominates each 4x4 cell, with some strays
			int roll = (int) (ReefNoise.hash(x >> 2, z >> 2, 91) * 100.0F);
			if (random.nextInt(10) < 3) {
				roll = random.nextInt(100);
			}
			if (roll < 22) {
				placed += set(level, pos, ScubaReefs.TABLE_CORAL.defaultBlockState());
			} else if (roll < 42) {
				placed += brain(level, random, pos);
			} else if (roll < 66) {
				placed += set(level, pos, ScubaReefs.BRANCH_CORAL.defaultBlockState());
			} else if (roll < 82) {
				BlockState fan = ScubaReefs.FAN_CORAL.defaultBlockState()
						.setValue(net.scubadive.reef.FanCoralBlock.FACING, HORIZONTAL[random.nextInt(4)]);
				placed += set(level, pos, fan);
			} else {
				placed += vanilla(level, random, pos);
			}
		}
		return placed > 0;
	}

	private static boolean canStand(WorldGenLevel level, BlockPos pos) {
		if (!level.getBlockState(pos).is(Blocks.WATER)) {
			return false;
		}
		BlockPos below = pos.below();
		return level.getBlockState(below).isFaceSturdy(level, below, Direction.UP);
	}

	private static int set(WorldGenLevel level, BlockPos pos, BlockState state) {
		if (state.hasProperty(BlockStateProperties.WATERLOGGED)) {
			state = state.setValue(BlockStateProperties.WATERLOGGED, true);
		}
		level.setBlock(pos, state, 2);
		return 1;
	}

	/** One or two stacked brain corals, sometimes topped with a table or branch coral. */
	private static int brain(WorldGenLevel level, RandomSource random, BlockPos pos) {
		int n = set(level, pos, ScubaReefs.BRAIN_CORAL.defaultBlockState());
		BlockPos top = pos.above();
		if (random.nextInt(2) == 0 && level.getBlockState(top).is(Blocks.WATER)) {
			n += set(level, top, ScubaReefs.BRAIN_CORAL.defaultBlockState());
			top = top.above();
		}
		if (random.nextInt(3) == 0 && level.getBlockState(top).is(Blocks.WATER)) {
			Block cap = random.nextBoolean() ? ScubaReefs.TABLE_CORAL : ScubaReefs.BRANCH_CORAL;
			n += set(level, top, cap.defaultBlockState());
		}
		return n;
	}

	private static int vanilla(WorldGenLevel level, RandomSource random, BlockPos pos) {
		Block[] set = VANILLA[random.nextInt(VANILLA.length)];
		int kind = random.nextInt(3);
		if (kind == 0) {
			int n = set(level, pos, set[0].defaultBlockState());
			BlockPos top = pos.above();
			if (random.nextInt(2) == 0 && level.getBlockState(top).is(Blocks.WATER)) {
				n += set(level, top, set[1].defaultBlockState());
			}
			return n;
		}
		return set(level, pos, set[kind].defaultBlockState());
	}
}
