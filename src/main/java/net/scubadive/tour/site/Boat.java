package net.scubadive.tour.site;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

/**
 * The tour boat at the site centre. Local x = across (-3..3), z = along (-7 stern .. +7 bow).
 * Deck blocks are at y = SURFACE_Y, divers stand on y = SURFACE_Y + 1; the hull sits in the top water layers.
 * A ladder hangs on the +x side (x = 4, z = 0).
 */
final class Boat {
	private static final int H = DiveSites.SURFACE_Y;
	private static final int[] SPAWN_X = {-2, -1, 1, 2};

	private static final BlockState SPRUCE = Blocks.SPRUCE_PLANKS.defaultBlockState();
	private static final BlockState DARK = Blocks.DARK_OAK_PLANKS.defaultBlockState();
	private static final BlockState DECK = Blocks.OAK_PLANKS.defaultBlockState();
	private static final BlockState SLAB = Blocks.SPRUCE_SLAB.defaultBlockState();
	private static final BlockState FENCE = Blocks.SPRUCE_FENCE.defaultBlockState();
	private static final BlockState GLASS = Blocks.GLASS.defaultBlockState();
	private static final BlockState LANTERN = Blocks.LANTERN.defaultBlockState();
	private static final BlockState LADDER = Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, Direction.EAST);
	private static final BlockState RED = Blocks.WOOL.red().defaultBlockState();
	private static final BlockState WHITE = Blocks.WOOL.white().defaultBlockState();

	private Boat() {
	}

	/** Where diver number index (0..7+) stands: two rows of four on the open midship deck. */
	static BlockPos spawn(BlockPos origin, int index) {
		int i = Math.max(0, index);
		return origin.offset(SPAWN_X[i % 4], 1, -1 + (i / 4) % 3);
	}

	/** True on or just above the boat (deck, cabin roof, ladder side). */
	static boolean on(BlockPos origin, BlockPos pos) {
		return Math.abs(pos.getX() - origin.getX()) <= 4 && Math.abs(pos.getZ() - origin.getZ()) <= 7 && pos.getY() >= origin.getY();
	}

	/** Half beam at stern position z. */
	private static int hw(int z) {
		return z <= 3 ? 3 : (z <= 5 ? 2 : 1);
	}

	private static boolean hull(int x, int z) {
		return z >= -7 && z <= 7 && Math.abs(x) <= hw(z);
	}

	private static boolean boundary(int x, int z) {
		return hull(x, z) && !(hull(x - 1, z) && hull(x + 1, z) && hull(x, z - 1) && hull(x, z + 1));
	}

	private static boolean rail(int x, int z) {
		if (!hull(x, z)) {
			return false;
		}
		if (x == 3 && z == 0) {
			return false; // boarding gap
		}
		if (z == -7 || z == 7) {
			return true;
		}
		return Math.abs(x) >= hw(z + 1);
	}

	private static boolean cabinWall(int x, int z) {
		return z >= -6 && z <= -4 && Math.abs(x) <= 2 && (z == -6 || z == -4 || Math.abs(x) == 2);
	}

	/** Block of the boat at (x, y, z) relative to the surface; null = none. */
	private static BlockState at(int x, int y, int z) {
		int ax = Math.abs(x);
		if (x == 4 && z == 0 && y >= -2 && y <= 0) {
			return LADDER;
		}
		if (x == 3 && z == 0 && y == -2) {
			return DARK; // ladder mount
		}
		switch (y) {
			case -2:
				return z >= -6 && z <= 5 && hull(x, z) && ax <= hw(z) - 2 ? SPRUCE : null;
			case -1:
				return hull(x, z) ? (boundary(x, z) ? DARK : SPRUCE) : null;
			case 0:
				return hull(x, z) ? (boundary(x, z) ? DARK : DECK) : null;
			case 1:
				if (x == 0 && z == 3) {
					return Blocks.SPRUCE_FENCE.defaultBlockState(); // mast
				}
				if (x == 0 && z == -5) {
					return LANTERN;
				}
				if (cabinWall(x, z) && !(x == 0 && z == -4)) {
					return SPRUCE;
				}
				if (rail(x, z)) {
					return FENCE
							.setValue(BlockStateProperties.NORTH, rail(x, z - 1))
							.setValue(BlockStateProperties.SOUTH, rail(x, z + 1))
							.setValue(BlockStateProperties.EAST, rail(x + 1, z))
							.setValue(BlockStateProperties.WEST, rail(x - 1, z));
				}
				return null;
			case 2:
				if (x == 0 && z == 3) {
					return FENCE;
				}
				if (cabinWall(x, z) && !(x == 0 && z == -4)) {
					return ax == 2 && z == -5 ? GLASS : SPRUCE;
				}
				return null;
			case 3:
				if (x == 0 && z == 3) {
					return FENCE;
				}
				if (ax <= 2 && z >= -6 && z <= -4) {
					return SLAB; // cabin roof
				}
				return null;
			case 4:
			case 5:
				return x == 0 && z == 3 ? FENCE : null;
			case 6:
			case 7:
				if (x == 0 && z == 3) {
					return FENCE;
				}
				if (z == 3 && x >= 1 && x <= 3) {
					boolean red = (x + y) % 2 == 1;
					return red ? RED : WHITE;
				}
				return null;
			case 8:
				return x == 0 && z == 3 ? FENCE : null;
			default:
				return null;
		}
	}

	static void build(SiteCtx c) {
		for (int x = Math.max(c.x0, -4); x <= Math.min(c.x1, 4); x++) {
			for (int z = Math.max(c.z0, -7); z <= Math.min(c.z1, 7); z++) {
				for (int y = -2; y <= 8; y++) {
					BlockState s = at(x, y, z);
					if (s != null) {
						c.set(x, H + y, z, s);
					}
				}
			}
		}
	}
}
