package net.scubadive.tour.site;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.KelpBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.TallSeagrassBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

import net.scubadive.creature.ScubaCreatures;
import net.scubadive.reef.FanCoralBlock;
import net.scubadive.reef.ScubaReefs;

/** Column-local underwater plants, corals and simple trees shared by the sites. */
final class Plants {
	private static final int WATER_TOP = SiteGen.WATER_TOP;
	private static final Direction[] HORIZONTAL = {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};
	/** Vanilla live corals: {block, plant, fan}. */
	private static final Block[][] VANILLA = {
			{Blocks.TUBE_CORAL_BLOCK, Blocks.TUBE_CORAL, Blocks.TUBE_CORAL_FAN},
			{Blocks.BRAIN_CORAL_BLOCK, Blocks.BRAIN_CORAL, Blocks.BRAIN_CORAL_FAN},
			{Blocks.BUBBLE_CORAL_BLOCK, Blocks.BUBBLE_CORAL, Blocks.BUBBLE_CORAL_FAN},
			{Blocks.FIRE_CORAL_BLOCK, Blocks.FIRE_CORAL, Blocks.FIRE_CORAL_FAN},
			{Blocks.HORN_CORAL_BLOCK, Blocks.HORN_CORAL, Blocks.HORN_CORAL_FAN},
	};

	private Plants() {
	}

	/** Waterlogs the state if it can be. */
	static BlockState wl(BlockState state) {
		if (state.hasProperty(BlockStateProperties.WATERLOGGED)) {
			return state.setValue(BlockStateProperties.WATERLOGGED, true);
		}
		return state;
	}

	static void seagrass(SiteCtx c, int lx, int y, int lz, boolean tall) {
		if (tall && y + 1 <= WATER_TOP) {
			BlockState t = Blocks.TALL_SEAGRASS.defaultBlockState();
			c.set(lx, y, lz, t.setValue(TallSeagrassBlock.HALF, DoubleBlockHalf.LOWER));
			c.set(lx, y + 1, lz, t.setValue(TallSeagrassBlock.HALF, DoubleBlockHalf.UPPER));
		} else {
			c.set(lx, y, lz, Blocks.SEAGRASS.defaultBlockState());
		}
	}

	/** A kelp stem of len blocks starting at y (the top block is the kelp head). */
	static void kelp(SiteCtx c, int lx, int y, int lz, int len) {
		for (int i = 0; i < len - 1; i++) {
			c.set(lx, y + i, lz, Blocks.KELP_PLANT.defaultBlockState());
		}
		c.set(lx, y + len - 1, lz, Blocks.KELP.defaultBlockState().setValue(KelpBlock.AGE, 25));
	}

	/**
	 * Reef decoration of one column: anemone patches, coral colonies (coral = chance inside a colony zone),
	 * and seagrass elsewhere (grass = chance).
	 */
	static void reef(SiteCtx c, long seed, int lx, int lz, int f, double coral, double grass) {
		int y = f + 1;
		if (y > WATER_TOP - 2) {
			return;
		}
		double r = Noise.rnd(seed + 5, lx, lz);
		double patch = Noise.fbm(seed + 22, lx * 0.09, lz * 0.09, 2);
		double zone = Noise.fbm(seed + 21, lx * 0.05, lz * 0.05, 2);
		if (patch > 0.6 && r < 0.65) {
			c.set(lx, y, lz, wl(ScubaCreatures.SEA_ANEMONE.defaultBlockState()));
		} else if (zone > 0.47 && r < coral) {
			coralAt(c, seed, lx, y, lz);
		} else if (r > 1.0 - grass) {
			seagrass(c, lx, y, lz, Noise.rnd(seed + 8, lx, lz) < 0.3);
		}
	}

	private static void coralAt(SiteCtx c, long seed, int lx, int y, int lz) {
		int roll = (int) (Noise.rnd(seed + 6, lx >> 2, lz >> 2) * 100.0);
		if (Noise.rnd(seed + 7, lx, lz) < 0.3) {
			roll = (int) (Noise.rnd(seed + 9, lx, lz) * 100.0);
		}
		double r = Noise.rnd(seed + 10, lx, lz);
		if (roll < 22) {
			c.set(lx, y, lz, wl(ScubaReefs.TABLE_CORAL.defaultBlockState()));
		} else if (roll < 42) {
			c.set(lx, y, lz, ScubaReefs.BRAIN_CORAL.defaultBlockState());
			if (r < 0.4 && y + 1 <= WATER_TOP - 1) {
				c.set(lx, y + 1, lz, ScubaReefs.BRAIN_CORAL.defaultBlockState());
				if (r < 0.15 && y + 2 <= WATER_TOP - 1) {
					c.set(lx, y + 2, lz, wl(ScubaReefs.BRANCH_CORAL.defaultBlockState()));
				}
			}
		} else if (roll < 66) {
			c.set(lx, y, lz, wl(ScubaReefs.BRANCH_CORAL.defaultBlockState()));
		} else if (roll < 82) {
			c.set(lx, y, lz, wl(ScubaReefs.FAN_CORAL.defaultBlockState()
					.setValue(FanCoralBlock.FACING, HORIZONTAL[(int) (r * 4.0) & 3])));
		} else {
			Block[] set = VANILLA[(int) (Noise.rnd(seed + 12, lx, lz) * VANILLA.length) % VANILLA.length];
			int kind = (int) (r * 3.0) % 3;
			if (kind == 0) {
				c.set(lx, y, lz, wl(set[0].defaultBlockState()));
				if (r < 0.2 && y + 1 <= WATER_TOP - 1) {
					c.set(lx, y + 1, lz, wl(set[1].defaultBlockState()));
				}
			} else {
				c.set(lx, y, lz, wl(set[kind].defaultBlockState()));
			}
		}
	}

	/** A coral block for a reef mound surface. */
	static BlockState moundCoral(long seed, int lx, int y, int lz) {
		double r = Noise.rnd(seed + 13, lx >> 1, y, lz >> 1);
		if (r < 0.3) {
			return ScubaReefs.BRAIN_CORAL.defaultBlockState();
		}
		Block[] set = VANILLA[(int) (r * 40.0) % VANILLA.length];
		return set[0].defaultBlockState();
	}

	/** A small tree: trunk of h logs on ground y, round leaf canopy (persistent leaves). */
	static void tree(SiteCtx c, int tx, int ground, int tz, int h, BlockState log, BlockState leaves) {
		BlockState leaf = leaves.hasProperty(LeavesBlock.PERSISTENT) ? leaves.setValue(LeavesBlock.PERSISTENT, true) : leaves;
		int top = ground + h;
		for (int dy = -2; dy <= 1; dy++) {
			int r = dy <= -1 ? 2 : 1;
			for (int dx = -r; dx <= r; dx++) {
				for (int dz = -r; dz <= r; dz++) {
					if (dx == 0 && dz == 0 && dy <= 0) {
						continue;
					}
					if (Math.abs(dx) == r && Math.abs(dz) == r && (r == 2 || dy == 1)
							&& Noise.rnd(c.seed + 14, tx + dx, top + dy, tz + dz) < 0.5) {
						continue;
					}
					int x = tx + dx;
					int z = tz + dz;
					if (c.in(x, z) && c.get(x, top + dy, z).isAir()) {
						c.set(x, top + dy, z, leaf);
					}
				}
			}
		}
		for (int y = ground + 1; y <= top; y++) {
			if (c.in(tx, tz)) {
				BlockState cur = c.get(tx, y, tz);
				if (cur.isAir() || cur.is(Blocks.SHORT_GRASS) || cur.getBlock() instanceof LeavesBlock) {
					c.set(tx, y, tz, log);
				}
			}
		}
	}
}
