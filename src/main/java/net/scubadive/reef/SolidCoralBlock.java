package net.scubadive.reef;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/** Full coral block (brain coral) that cracks when a diver lands on it hard. */
public class SolidCoralBlock extends Block {
	public SolidCoralBlock(Properties properties) {
		super(properties);
	}

	@Override
	public void fallOn(Level level, BlockState state, BlockPos pos, Entity entity, double fallDistance) {
		super.fallOn(level, state, pos, entity, fallDistance);
		CoralBreak.onLand(level, pos, entity, fallDistance);
	}
}
