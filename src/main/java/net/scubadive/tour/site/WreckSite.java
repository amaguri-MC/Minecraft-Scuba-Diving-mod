package net.scubadive.tour.site;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootTable;

/**
 * Sand bottom (12-30 m) with a large broken warped-timber ship lying on a flat pad, rubble around it
 * and two chests with the vanilla shipwreck loot. Ship frame: l = along (bow +x), w = across (z), y up from the keel.
 */
final class WreckSite extends SiteGen {
	/** Ship centre (local x, z) and keel depth (y of the keel layer). */
	private static final int SX = 6;
	private static final int SZ = 15;
	private static final int PAD_DEPTH = 26;
	private static final int BASE_Y = WATER_TOP - PAD_DEPTH + 1;

	private static final BlockState PLANKS = Blocks.WARPED_PLANKS.defaultBlockState();
	private static final BlockState FRAME = Blocks.STRIPPED_WARPED_STEM.defaultBlockState();
	private static final BlockState BALLAST = Blocks.MOSSY_COBBLESTONE.defaultBlockState();
	private static final BlockState MAST = Blocks.WARPED_STEM.defaultBlockState();
	private static final BlockState FENCE = Plants.wl(Blocks.WARPED_FENCE.defaultBlockState());
	private static final BlockState SLAB = Plants.wl(Blocks.WARPED_SLAB.defaultBlockState());

	WreckSite(long seed) {
		super(seed);
	}

	/** 0 on the pad centre ... 1 at the pad edge (elliptical). */
	private static double padDist(int lx, int lz) {
		double a = (lx - SX) / 22.0;
		double b = (lz - SZ) / 11.0;
		return Math.sqrt(a * a + b * b);
	}

	@Override
	int baseFloor(int lx, int lz) {
		double n = Noise.fbm(seed + 171, lx * 0.03, lz * 0.03, 3) - 0.5;
		double dune = (Noise.fbm(seed + 172, lx * 0.1, lz * 0.1, 2) - 0.5) * 3.0;
		double depth = 21.0 + lx * 0.07 + n * 10.0 + dune;
		depth = Math.max(12.0, Math.min(30.0, depth));
		double w = Noise.smooth((1.15 - padDist(lx, lz)) / 0.5);
		depth += (PAD_DEPTH - depth) * w;
		return WATER_TOP - (int) Math.round(depth);
	}

	@Override
	BlockState solid(int lx, int lz, int y, int depth, int f) {
		if (depth <= 1) {
			return Noise.rnd(seed + 173, lx >> 2, lz >> 2) < 0.12 ? Blocks.GRAVEL.defaultBlockState() : Blocks.SAND.defaultBlockState();
		}
		return depth <= 8 ? Blocks.SANDSTONE.defaultBlockState() : Blocks.STONE.defaultBlockState();
	}

	@Override
	void decorateColumn(SiteCtx c, int lx, int lz, int f, boolean rimmed) {
		if (rimmed || f >= WATER_TOP - 2) {
			return;
		}
		double r = Noise.rnd(seed + 174, lx, lz);
		if (r < 0.03) {
			Plants.seagrass(c, lx, f + 1, lz, false);
			return;
		}
		if (padDist(lx, lz) < 1.5 && r > 0.978) {
			// rubble: loose timber, ballast stones
			double k = Noise.rnd(seed + 175, lx, lz);
			BlockState s;
			if (k < 0.3) {
				s = PLANKS;
			} else if (k < 0.45) {
				s = SLAB;
			} else if (k < 0.6) {
				s = FRAME;
			} else if (k < 0.8) {
				s = BALLAST;
			} else if (k < 0.9) {
				s = FENCE;
			} else {
				s = Blocks.COBBLESTONE.defaultBlockState();
			}
			c.set(lx, f + 1, lz, s);
			if (Noise.rnd(seed + 176, lx, lz) < 0.3) {
				c.set(lx, f + 2, lz, k < 0.5 ? PLANKS : BALLAST);
			}
		}
	}

	@Override
	void structures(SiteCtx c) {
		ship(c);
		fallenMast(c);
	}

	/** Half beam of the hull at l. */
	private static int halfBeam(int l) {
		if (l > 8) {
			return Math.max(0, 4 - (int) ((l - 8) * 0.7 + 0.5));
		}
		return l < -10 ? 3 : 4;
	}

	private static int heel(int w) {
		return (int) Math.floor(w * 0.3);
	}

	private void ship(SiteCtx c) {
		// bounding box of the ship in local x/z (w -5..5, l -15..15)
		int lo = Math.max(c.x0, SX - 15);
		int hi = Math.min(c.x1, SX + 15);
		for (int lx = lo; lx <= hi; lx++) {
			int l = lx - SX;
			for (int lz = Math.max(c.z0, SZ - 5); lz <= Math.min(c.z1, SZ + 5); lz++) {
				int w = lz - SZ;
				for (int y = 0; y <= 15; y++) {
					BlockState s = shipBlock(l, w, y);
					if (s != null) {
						c.set(lx, BASE_Y + y + heel(w), lz, s);
					}
				}
			}
		}
		chest(c, -2, 0, 1, BuiltInLootTables.SHIPWRECK_TREASURE, 1);
		chest(c, -7, 0, 1, BuiltInLootTables.SHIPWRECK_SUPPLY, 2);
	}

	private void chest(SiteCtx c, int l, int w, int y, ResourceKey<LootTable> table, int salt) {
		int lx = SX + l;
		int lz = SZ + w;
		int wy = BASE_Y + y + heel(w);
		RandomSource random = c.random(500 + salt);
		Direction facing = Direction.Plane.HORIZONTAL.getRandomDirection(random);
		BlockState chest = Blocks.CHEST.defaultBlockState()
				.setValue(ChestBlock.FACING, facing)
				.setValue(ChestBlock.WATERLOGGED, true);
		if (c.set(lx, wy, lz, chest)) {
			BlockPos pos = c.pos(lx, wy, lz);
			RandomizableContainer.setBlockEntityLootTable(c.level, random, pos, table);
			c.set(lx, wy + 1, lz, WATER); // keep the lid free
		}
	}

	private BlockState shipBlock(int l, int w, int y) {
		if (l < -14 || l > 14) {
			return null;
		}
		int aw = Math.abs(w);
		int hb = halfBeam(l);
		BlockState yd = yard(l, w, y);
		if (yd != null) {
			return yd;
		}
		if (aw > hb) {
			return null;
		}
		int hwY = Math.min(hb, 1 + y);
		double dmg = Noise.fbm(seed + 181, l * 0.25, w * 0.25, 2);
		int sideTop = 5 - (int) (dmg * dmg * 5.0) - (w > 0 ? 1 : 0) - (l < -9 ? 1 : 0);
		sideTop = Math.max(1, sideTop);
		double r = Noise.rnd(seed + 182, l, w, y);

		// keel and bottom planking
		if (y == 0) {
			if (aw > hwY) {
				return null;
			}
			return aw == 0 ? FRAME : (r < 0.15 ? BALLAST : PLANKS);
		}
		// bow and stern posts
		if (l == 14 && w == 0 && y <= 6) {
			return FRAME;
		}
		// deck
		if (y == 3 && aw < hb && l >= -9 && l <= 10) {
			boolean hole = Noise.fbm(seed + 183, l * 0.3, w * 0.3, 2) > 0.57 || (l >= -4 && l <= 0 && aw <= 1);
			return hole || r < 0.07 ? null : PLANKS;
		}
		// hull sides and ribs
		if (aw >= Math.min(hb, y) && aw <= hwY && y <= sideTop && r > 0.1) {
			return PLANKS;
		}
		if (aw == hb - 1 && aw > 0 && y >= 3 && y <= sideTop - 1 && Math.floorMod(l, 4) == 0) {
			return FRAME;
		}
		// aft cabin
		if (l >= -12 && l <= -8 && y >= 4 && y <= 8 && aw <= 3) {
			boolean wall = l == -12 || l == -8 || aw == 3;
			if (y == 8) {
				return r < 0.55 ? SLAB : null;
			}
			if (wall && r > 0.1 + (y - 4) * 0.12) {
				return PLANKS;
			}
			return null;
		}
		// masts
		if (aw == 0) {
			int top = -1;
			if (l == 2) {
				top = 13;
			} else if (l == 8) {
				top = 9;
			} else if (l == -5) {
				top = 6;
			}
			if (top > 0 && y >= 3 && y <= top) {
				return MAST;
			}
		}
		// rail
		if (y == sideTop + 1 && aw == hb && l >= -9 && l <= 9 && sideTop >= 4 && r < 0.5) {
			return FENCE;
		}
		return null;
	}

	/** Yards: planks across the masts. */
	private BlockState yard(int l, int w, int y) {
		int aw = Math.abs(w);
		if (aw > 3 || aw == 0) {
			return null;
		}
		if ((l == 2 && y == 11) || (l == 8 && y == 8)) {
			return Noise.rnd(seed + 184, l, w, y) < 0.7 ? PLANKS : null;
		}
		return null;
	}

	/** The foremast lying on the sand beside the ship. */
	private void fallenMast(SiteCtx c) {
		BlockState log = MAST.setValue(RotatedPillarBlock.AXIS, Direction.Axis.X);
		int z = SZ - 8;
		for (int i = 0; i < 11; i++) {
			int x = SX - 6 + i;
			if (c.in(x, z)) {
				int f = floor(x, z);
				c.set(x, f + 1, z, log);
			}
		}
	}
}
