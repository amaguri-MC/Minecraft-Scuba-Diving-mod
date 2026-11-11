package net.scubadive.creature;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/** Leaf sheep (Costasiella): a 1 cm slug that grazes algae, so it lives on seagrass as well as reef rock and coral. */
public class LeafSheep extends Nudibranch {
	public LeafSheep(EntityType<? extends LeafSheep> type, Level level) {
		super(type, level);
	}

	/** Seagrass (short or tall) in the cell itself. */
	public static boolean isSeagrass(BlockState state) {
		return state.is(Blocks.SEAGRASS) || state.is(Blocks.TALL_SEAGRASS);
	}

	/** Reef floor below, or seagrass in the cell. */
	public static boolean isLeafSheepHome(LevelReader level, BlockPos pos) {
		return isReefFloor(level.getBlockState(pos.below())) || isSeagrass(level.getBlockState(pos));
	}

	@Override
	protected boolean isHome(BlockPos pos) {
		return isLeafSheepHome(this.level(), pos);
	}

	@Override
	protected double crawlSpeed() {
		return 0.002;
	}
}
