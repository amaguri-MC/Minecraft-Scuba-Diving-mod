package net.scubadive.tour.center;

import java.util.ArrayList;
import java.util.List;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * A small spruce beach hut with a pier running out into the sea, a shop clerk and a tour guide inside.
 * Local frame: u across (right of the pier direction), v along the pier (+v towards the sea), y absolute.
 * Hut: |u| <= 4, |v| <= 3, floor at fy. Pier deck: |u| <= 1, v from 4 to the end. Stays within 12 blocks of the
 * origin so it never leaves the 3x3 chunk area that features may write to.
 */
public class DiveCenterFeature extends Feature<NoneFeatureConfiguration> {
	private static final int MAX_PIER_END = 12;

	private static final BlockState PLANKS = Blocks.SPRUCE_PLANKS.defaultBlockState();
	private static final BlockState LOG = Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState();
	private static final BlockState WALL_LOG = Blocks.SPRUCE_LOG.defaultBlockState();
	private static final BlockState GLASS = Blocks.GLASS.defaultBlockState();
	private static final BlockState AIR = Blocks.AIR.defaultBlockState();

	public DiveCenterFeature(Codec<NoneFeatureConfiguration> codec) {
		super(codec);
	}

	/** Position and orientation of one hut. */
	private record Frame(BlockPos origin, Direction sea, Direction right) {
		BlockPos at(int u, int v, int y) {
			return new BlockPos(
					origin.getX() + right.getStepX() * u + sea.getStepX() * v,
					y,
					origin.getZ() + right.getStepZ() * u + sea.getStepZ() * v);
		}
	}

	@Override
	public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
		WorldGenLevel level = context.level();
		BlockPos origin = context.origin();
		RandomSource random = context.random();
		int sea = level.getSeaLevel();

		// Dry ground at about sea level under the origin.
		int y0 = origin.getY() - 1;
		if (y0 < sea || y0 > sea + 3 || !isDry(level, origin.getX(), origin.getZ(), sea)) {
			return false;
		}

		// Pick a direction that reaches open water soon.
		List<Direction> dirs = new ArrayList<>();
		for (Direction d : Direction.Plane.HORIZONTAL) {
			dirs.add(d);
		}
		java.util.Collections.shuffle(dirs, new java.util.Random(random.nextLong()));
		Frame frame = null;
		int shore = -1;
		for (Direction dir : dirs) {
			Frame candidate = new Frame(origin, dir, dir.getClockWise());
			int k = findShore(level, candidate, sea);
			if (k > 0 && footprintDry(level, candidate, sea, y0)) {
				frame = candidate;
				shore = k;
				break;
			}
		}
		if (frame == null) {
			return false;
		}
		int fy = y0 + 1;
		int pierEnd = Math.min(MAX_PIER_END, shore + 4);

		clearVolume(level, frame, fy);
		buildHut(level, frame, fy);
		buildPier(level, frame, fy, pierEnd, sea);
		decorate(level, frame, fy, pierEnd);
		spawnNpcs(level, frame, fy);
		return true;
	}

	// ---- site checks --------------------------------------------------------------------------------

	private static boolean isWater(WorldGenLevel level, int x, int z, int sea) {
		return level.getFluidState(new BlockPos(x, sea - 1, z)).is(FluidTags.WATER);
	}

	/** Dry land: no water at sea level in this column and the surface is not water. */
	private static boolean isDry(WorldGenLevel level, int x, int z, int sea) {
		return !isWater(level, x, z, sea)
				&& level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, x, z) == level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z);
	}

	/** Distance (in blocks from the origin) of the first water column along the pier axis, or -1 if unsuitable. */
	private int findShore(WorldGenLevel level, Frame frame, int sea) {
		for (int k = 0; k <= 3; k++) {
			BlockPos p = frame.at(0, k, 0);
			if (isWater(level, p.getX(), p.getZ(), sea)) {
				return -1;
			}
		}
		for (int k = 4; k <= 8; k++) {
			BlockPos p = frame.at(0, k, 0);
			if (isWater(level, p.getX(), p.getZ(), sea)) {
				// Open water for a few blocks beyond the shore line.
				for (int j = 1; j <= 3; j++) {
					BlockPos q = frame.at(0, k + j, 0);
					if (!isWater(level, q.getX(), q.getZ(), sea)) {
						return -1;
					}
				}
				return k;
			}
		}
		return -1;
	}

	/** The hut corners must be dry and not much higher or lower than the origin. */
	private static boolean footprintDry(WorldGenLevel level, Frame frame, int sea, int y0) {
		int[][] corners = {{-4, -3}, {4, -3}, {-4, 3}, {4, 3}, {0, -3}, {0, 3}};
		for (int[] c : corners) {
			BlockPos p = frame.at(c[0], c[1], 0);
			if (!isDry(level, p.getX(), p.getZ(), sea)) {
				return false;
			}
			int ground = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, p.getX(), p.getZ()) - 1;
			if (ground < y0 - 2 || ground > y0 + 2) {
				return false;
			}
		}
		return true;
	}

	// ---- building -----------------------------------------------------------------------------------

	private static void set(WorldGenLevel level, BlockPos pos, BlockState state) {
		level.setBlock(pos, state, 2);
	}

	/** Removes trees and bumps in and above the hut. */
	private static void clearVolume(WorldGenLevel level, Frame frame, int fy) {
		for (int u = -5; u <= 5; u++) {
			for (int v = -4; v <= 4; v++) {
				for (int y = fy + 1; y <= fy + 8; y++) {
					BlockPos p = frame.at(u, v, y);
					if (!level.getBlockState(p).isAir()) {
						set(level, p, AIR);
					}
				}
			}
		}
	}

	private static BlockState stair(Direction facing, boolean top) {
		return Blocks.SPRUCE_STAIRS.defaultBlockState()
				.setValue(StairBlock.FACING, facing)
				.setValue(StairBlock.HALF, top ? Half.TOP : Half.BOTTOM);
	}

	private void buildHut(WorldGenLevel level, Frame f, int fy) {
		// Floor.
		for (int u = -4; u <= 4; u++) {
			for (int v = -3; v <= 3; v++) {
				set(level, f.at(u, v, fy), PLANKS);
			}
		}
		// Stilts under the corners, door sides and middle of the long walls.
		int[][] posts = {{-4, -3}, {4, -3}, {-4, 3}, {4, 3}, {0, -3}, {0, 3}, {-4, 0}, {4, 0}};
		for (int[] p : posts) {
			for (int y = fy - 1; y >= fy - 6; y--) {
				BlockPos pos = f.at(p[0], p[1], y);
				BlockState existing = level.getBlockState(pos);
				if (!existing.isAir() && level.getFluidState(pos).isEmpty()) {
					break;
				}
				set(level, pos, LOG);
			}
		}
		// Walls, three high. Doors at u = 0 on both long sides, glass windows on the middle row.
		for (int u = -4; u <= 4; u++) {
			for (int v = -3; v <= 3; v++) {
				boolean wall = Math.abs(u) == 4 || Math.abs(v) == 3;
				if (!wall) {
					continue;
				}
				boolean corner = Math.abs(u) == 4 && Math.abs(v) == 3;
				for (int y = fy + 1; y <= fy + 3; y++) {
					BlockState state = corner ? WALL_LOG : PLANKS;
					int row = y - fy;
					if (!corner && u == 0 && Math.abs(v) == 3 && row <= 2) {
						state = AIR; // doorway
					} else if (!corner && row == 2 && ((Math.abs(u) == 2 && Math.abs(v) == 3) || (Math.abs(u) == 4 && v == 0))) {
						state = GLASS;
					}
					set(level, f.at(u, v, y), state);
				}
			}
		}
		// Gable ends (u = +-4) above the walls.
		for (int u = -4; u <= 4; u += 8) {
			for (int v = -2; v <= 2; v++) {
				set(level, f.at(u, v, fy + 4), PLANKS);
			}
			for (int v = -1; v <= 1; v++) {
				set(level, f.at(u, v, fy + 5), PLANKS);
			}
		}
		// Roof: stairs sloping down towards both long sides, ridge planks in the middle, one block of overhang.
		Direction land = f.sea.getOpposite();
		for (int u = -5; u <= 5; u++) {
			set(level, f.at(u, 4, fy + 3), stair(f.sea, false));
			set(level, f.at(u, -4, fy + 3), stair(land, false));
			for (int v = 1; v <= 3; v++) {
				int y = fy + 3 + (4 - v);
				set(level, f.at(u, v, y), stair(f.sea, false));
				set(level, f.at(u, -v, y), stair(land, false));
			}
			set(level, f.at(u, 0, fy + 6), PLANKS);
		}
		// Underside of the roof overhang at the gables is left open; carpet and counter come in decorate().
	}

	private void buildPier(WorldGenLevel level, Frame f, int fy, int end, int sea) {
		for (int v = 4; v <= end; v++) {
			for (int u = -1; u <= 1; u++) {
				set(level, f.at(u, v, fy), PLANKS);
			}
			// Piles every third plank, running down to the sea floor.
			if ((v - 4) % 3 == 0 || v == end) {
				for (int u = -1; u <= 1; u += 2) {
					for (int y = fy - 1; y >= fy - 14; y--) {
						BlockPos pos = f.at(u, v, y);
						if (!level.getBlockState(pos).isAir() && level.getFluidState(pos).isEmpty()) {
							break;
						}
						set(level, pos, LOG);
					}
					set(level, f.at(u, v, fy + 1), LOG);
				}
			}
		}
		// Lanterns on the end posts.
		BlockState lantern = Blocks.LANTERN.defaultBlockState();
		set(level, f.at(-1, end, fy + 2), lantern);
		set(level, f.at(1, end, fy + 2), lantern);
		set(level, f.at(-1, 4, fy + 2), lantern);
		set(level, f.at(1, 4, fy + 2), lantern);
		// A step up from the beach at the land door.
		BlockPos stepPos = f.at(0, -4, 0);
		int gy = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, stepPos.getX(), stepPos.getZ());
		if (gy == fy) {
			set(level, f.at(0, -4, fy), stair(f.sea.getOpposite(), false));
		}
	}

	private void decorate(WorldGenLevel level, Frame f, int fy, int end) {
		BlockState carpet = Blocks.CARPET.blue().defaultBlockState();
		BlockState barrel = Blocks.BARREL.defaultBlockState().setValue(BarrelBlock.FACING, Direction.UP);
		BlockState tankBody = Blocks.CONCRETE.yellow().defaultBlockState();
		BlockState tankCap = Blocks.POLISHED_ANDESITE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.BOTTOM);
		BlockState lantern = Blocks.LANTERN.defaultBlockState();
		BlockState counter = Blocks.SPRUCE_PLANKS.defaultBlockState();

		// Counter in front of the clerk (left side), rug on the guide's side.
		for (int v = -1; v <= 1; v++) {
			set(level, f.at(-2, v, fy + 1), counter);
		}
		set(level, f.at(-2, 0, fy + 2), lantern);
		for (int u = 1; u <= 3; u++) {
			for (int v = -1; v <= 1; v++) {
				set(level, f.at(u, v, fy + 1), carpet);
			}
		}
		// Barrels and air tanks along the walls.
		set(level, f.at(-3, 2, fy + 1), barrel);
		set(level, f.at(-3, -2, fy + 1), barrel);
		set(level, f.at(3, 2, fy + 1), barrel);
		for (int[] t : new int[][]{{3, -2}, {2, -2}}) {
			set(level, f.at(t[0], t[1], fy + 1), tankBody);
			set(level, f.at(t[0], t[1], fy + 2), tankBody);
			set(level, f.at(t[0], t[1], fy + 3), tankCap);
		}
		// Gear on the pier: tanks and barrels near the end.
		set(level, f.at(-1, end - 1, fy + 1), tankBody);
		set(level, f.at(-1, end - 1, fy + 2), tankBody);
		set(level, f.at(-1, end - 1, fy + 3), tankCap);
		set(level, f.at(1, end - 2, fy + 1), barrel);
		set(level, f.at(0, end, fy + 1), barrel);

		// Signs on both long walls.
		placeSign(level, f, f.at(3, 4, fy + 2), f.sea);
		placeSign(level, f, f.at(3, -4, fy + 2), f.sea.getOpposite());
	}

	private void placeSign(WorldGenLevel level, Frame f, BlockPos pos, Direction facing) {
		BlockState sign = Blocks.SPRUCE_WALL_SIGN.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, facing);
		set(level, pos, sign);
		BlockEntity be = level.getBlockEntity(pos);
		if (be instanceof SignBlockEntity signEntity) {
			SignText text = new SignText()
					.setMessage(1, Component.translatable("sign.scubadive.dive_center.line1"))
					.setMessage(2, Component.translatable("sign.scubadive.dive_center.line2"));
			signEntity.setText(text, true);
		}
	}

	private void spawnNpcs(WorldGenLevel level, Frame f, int fy) {
		spawn(level, DiveCenter.CLERK, f.at(-3, 0, fy + 1), f.right.toYRot());
		spawn(level, DiveCenter.GUIDE, f.at(2, 0, fy + 1), f.right.getOpposite().toYRot());
	}

	private static void spawn(WorldGenLevel level, EntityType<? extends Mob> type, BlockPos pos, float yaw) {
		Mob mob = type.create(level.getLevel(), EntitySpawnReason.STRUCTURE);
		if (mob == null) {
			return;
		}
		mob.snapTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, yaw, 0.0f);
		mob.setYHeadRot(yaw);
		mob.setPersistenceRequired();
		level.addFreshEntity(mob);
	}
}
