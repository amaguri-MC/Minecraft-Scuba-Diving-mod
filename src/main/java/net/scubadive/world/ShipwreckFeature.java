package net.scubadive.world;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;

/**
 * A small, broken, heeled-over wooden ship. Local coordinates: l = along the ship (-4 stern .. +4 bow),
 * w = across (-2..2), y = up from the keel. Rotated randomly about the vertical axis.
 */
public class ShipwreckFeature extends Feature<NoneFeatureConfiguration> {
	public ShipwreckFeature(Codec<NoneFeatureConfiguration> codec) {
		super(codec);
	}

	@Override
	public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
		WorldGenLevel level = context.level();
		BlockPos origin = context.origin();
		RandomSource random = context.random();
		int rot = random.nextInt(4);

		// Sea floor height at the footprint corners and centre: sit on the lowest point.
		int base = Integer.MAX_VALUE;
		int[][] samples = {{0, 0}, {-4, -2}, {-4, 2}, {4, -2}, {4, 2}, {4, 0}, {-4, 0}};
		for (int[] s : samples) {
			int x = wx(origin, rot, s[0], s[1]);
			int z = wz(origin, rot, s[0], s[1]);
			int floor = level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, x, z);
			if (!level.getFluidState(new BlockPos(x, floor, z)).is(FluidTags.WATER)) {
				return false;
			}
			base = Math.min(base, floor);
		}
		if (level.getSeaLevel() - base < 7) {
			return false; // too shallow
		}

		// Warped (green) timber, to set these wrecks apart from vanilla ones. Waterlogged so no air pockets appear.
		BlockState planks = Blocks.WARPED_PLANKS.defaultBlockState();
		BlockState dark = Blocks.STRIPPED_WARPED_STEM.defaultBlockState();
		BlockState ballast = Blocks.MOSSY_COBBLESTONE.defaultBlockState();
		BlockState fence = Blocks.WARPED_FENCE.defaultBlockState().setValue(BlockStateProperties.WATERLOGGED, true);
		BlockState slab = Blocks.WARPED_SLAB.defaultBlockState().setValue(BlockStateProperties.WATERLOGGED, true);
		BlockState stairs = Blocks.WARPED_STAIRS.defaultBlockState()
				.setValue(StairBlock.FACING, bowDir(rot))
				.setValue(BlockStateProperties.WATERLOGGED, true);
		BlockState log = Blocks.WARPED_STEM.defaultBlockState();

		for (int l = -4; l <= 4; l++) {
			int bw = l == 4 ? 0 : (l == 3 ? 1 : (l == -4 ? 1 : 2)); // half-beam: pointed bow, narrow stern
			for (int w = -bw; w <= bw; w++) {
				int aw = Math.abs(w);
				boolean side = aw == bw;
				for (int y = 0; y <= 3; y++) {
					BlockState state = null;
					if (y == 0) {
						state = aw < bw || bw == 0 ? (random.nextInt(6) == 0 ? ballast : dark) : null;
						if (bw == 1 && aw == 1) {
							state = null;
						}
					} else if (y <= 2) {
						if (side) {
							state = planks;
						} else if (y == 2 && l >= -3 && l <= 3) {
							// deck: mostly planks, some half slabs, and a stair at the aft cabin step
							state = l == -3 && w == 0 ? stairs : (random.nextInt(4) == 0 ? slab : planks);
						}
					} else if (side && l >= -3 && l <= 3) {
						state = fence; // rail
					}
					if (state == null) {
						continue;
					}
					// Broken: the starboard side and stern are partly gone, deck has holes.
					if (y >= 2 && w > 0 && l >= 0 && random.nextInt(10) < 6) {
						continue;
					}
					if (y >= 1 && l == -4 && random.nextInt(10) < 4) {
						continue;
					}
					if (random.nextInt(8) == 0 && y > 0) {
						continue;
					}
					int dy = w < 0 ? -1 : 0; // heeled over to one side
					set(level, origin, rot, l, w, base + y + dy, state, false);
				}
			}
		}

		// Mast with a snapped, leaning top and a broken yard.
		int mastTop = 4 + random.nextInt(2);
		for (int y = 1; y <= mastTop; y++) {
			int lean = y >= 4 ? 1 : 0;
			set(level, origin, rot, 1, lean, base + y, log, true);
		}
		for (int w = -2; w <= 2; w++) {
			if (random.nextInt(3) != 0) {
				set(level, origin, rot, 1, w + 1, base + 4, planks, false);
			}
		}

		// A little seagrass growing inside the hull.
		for (int l = -2; l <= 3; l++) {
			for (int w = -1; w <= 1; w++) {
				if (random.nextInt(9) != 0) {
					continue;
				}
				int y = base + 1 + (w < 0 ? -1 : 0);
				BlockPos p = new BlockPos(wx(origin, rot, l, w), y, wz(origin, rot, l, w));
				BlockState below = level.getBlockState(p.below());
				if (level.getBlockState(p).is(Blocks.WATER) && !below.isAir() && !below.is(Blocks.WATER)) {
					level.setBlock(p, Blocks.SEAGRASS.defaultBlockState(), 2);
				}
			}
		}

		// Treasure chest inside the hull (under a hole in the deck).
		int cx = wx(origin, rot, -1, 0);
		int cz = wz(origin, rot, -1, 0);
		BlockPos chestPos = new BlockPos(cx, base + 1, cz);
		Direction facing = Direction.Plane.HORIZONTAL.getRandomDirection(random);
		BlockState chest = Blocks.CHEST.defaultBlockState()
				.setValue(ChestBlock.FACING, facing)
				.setValue(ChestBlock.WATERLOGGED, true);
		level.setBlock(chestPos, chest, 2);
		level.setBlock(chestPos.above(), Blocks.WATER.defaultBlockState(), 2); // keep the hole open
		RandomizableContainer.setBlockEntityLootTable(level, random, chestPos, BuiltInLootTables.SHIPWRECK_TREASURE);
		return true;
	}

	/** World direction of the bow (+l) for a rotation. */
	private static Direction bowDir(int rot) {
		return switch (rot) {
			case 0 -> Direction.EAST;
			case 1 -> Direction.WEST;
			case 2 -> Direction.SOUTH;
			default -> Direction.NORTH;
		};
	}

	private static int wx(BlockPos o, int rot, int l, int w) {
		return o.getX() + switch (rot) {
			case 0 -> l;
			case 1 -> -l;
			case 2 -> w;
			default -> -w;
		};
	}

	private static int wz(BlockPos o, int rot, int l, int w) {
		return o.getZ() + switch (rot) {
			case 0 -> w;
			case 1 -> -w;
			case 2 -> l;
			default -> -l;
		};
	}

	private static void set(WorldGenLevel level, BlockPos origin, int rot, int l, int w, int y, BlockState state, boolean force) {
		BlockPos p = new BlockPos(wx(origin, rot, l, w), y, wz(origin, rot, l, w));
		// Only replace water (never carve into the sea floor), unless forced (mast).
		if (force || level.getFluidState(p).is(FluidTags.WATER)) {
			level.setBlock(p, state, 2);
		}
	}
}
