package net.scubadive.tour.center;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
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
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;

/**
 * The dive center hut + pier, built in code. Local frame: u across (right of the pier direction), v along the pier
 * (+v towards the sea), y absolute. Hut: |u| <= 4, |v| <= 3, floor at fy. Pier deck: |u| <= 1, v from 4 to the end.
 * Every write is clipped to the box given to {@link #build}, so a structure piece only touches the chunk being placed.
 */
public class DiveCenterPiece extends StructurePiece {
	static final int MAX_PIER_END = 12;
	private static final int PILE_DEPTH = 14;

	private static final BlockState PLANKS = Blocks.SPRUCE_PLANKS.defaultBlockState();
	private static final BlockState LOG = Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState();
	private static final BlockState WALL_LOG = Blocks.SPRUCE_LOG.defaultBlockState();
	private static final BlockState GLASS = Blocks.GLASS.defaultBlockState();
	private static final BlockState AIR = Blocks.AIR.defaultBlockState();

	private final int originX;
	private final int originZ;
	private final Direction sea;
	private final int floorY;
	private final int pierEnd;

	public DiveCenterPiece(int originX, int originZ, Direction sea, int floorY, int pierEnd) {
		super(DiveCenterStructure.PIECE, 0, makeBox(originX, originZ, sea, floorY, pierEnd));
		this.originX = originX;
		this.originZ = originZ;
		this.sea = sea;
		this.floorY = floorY;
		this.pierEnd = pierEnd;
	}

	public DiveCenterPiece(CompoundTag tag) {
		super(DiveCenterStructure.PIECE, tag);
		this.originX = tag.getIntOr("OX", 0);
		this.originZ = tag.getIntOr("OZ", 0);
		this.sea = Direction.from2DDataValue(tag.getIntOr("Sea", 0));
		this.floorY = tag.getIntOr("FY", 64);
		this.pierEnd = tag.getIntOr("Pier", 8);
	}

	@Override
	protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
		tag.putInt("OX", originX);
		tag.putInt("OZ", originZ);
		tag.putInt("Sea", sea.get2DDataValue());
		tag.putInt("FY", floorY);
		tag.putInt("Pier", pierEnd);
	}

	/** Box around everything the build may write: hut + roof overhang, pier, piles and stilts. */
	private static BoundingBox makeBox(int ox, int oz, Direction sea, int fy, int pierEnd) {
		Frame f = new Frame(ox, oz, sea);
		BlockPos a = f.at(-5, -4, fy - PILE_DEPTH);
		BlockPos b = f.at(5, Math.max(pierEnd, 4), fy + 8);
		return BoundingBox.fromCorners(a, b);
	}

	@Override
	public void postProcess(WorldGenLevel level, StructureManager structureManager, ChunkGenerator generator,
			RandomSource random, BoundingBox chunkBox, ChunkPos chunkPos, BlockPos referencePos) {
		// Each block position belongs to exactly one chunk, so the NPCs are spawned exactly once.
		build(level, new Frame(originX, originZ, sea), floorY, pierEnd, chunkBox);
	}

	/** Position and orientation of one hut. */
	record Frame(int ox, int oz, Direction sea) {
		Direction right() {
			return sea.getClockWise();
		}

		BlockPos at(int u, int v, int y) {
			Direction r = right();
			return new BlockPos(
					ox + r.getStepX() * u + sea.getStepX() * v,
					y,
					oz + r.getStepZ() * u + sea.getStepZ() * v);
		}
	}

	// ---- building (shared with DiveCenterFeature) ---------------------------------------------------

	static void build(WorldGenLevel level, Frame f, int fy, int pierEnd, BoundingBox clip) {
		clearVolume(level, f, fy, clip);
		buildHut(level, f, fy, clip);
		buildPier(level, f, fy, pierEnd, clip);
		decorate(level, f, fy, pierEnd, clip);
		spawn(level, DiveCenter.CLERK, f.at(-3, 0, fy + 1), f.right().toYRot(), clip);
		spawn(level, DiveCenter.GUIDE, f.at(2, 0, fy + 1), f.right().getOpposite().toYRot(), clip);
	}

	private static void set(WorldGenLevel level, BlockPos pos, BlockState state, BoundingBox clip) {
		if (clip.isInside(pos)) {
			level.setBlock(pos, state, 2);
		}
	}

	/** Removes trees and bumps in and above the hut. */
	private static void clearVolume(WorldGenLevel level, Frame f, int fy, BoundingBox clip) {
		for (int u = -5; u <= 5; u++) {
			for (int v = -4; v <= 4; v++) {
				for (int y = fy + 1; y <= fy + 8; y++) {
					BlockPos p = f.at(u, v, y);
					if (clip.isInside(p) && !level.getBlockState(p).isAir()) {
						level.setBlock(p, AIR, 2);
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

	/** A post from just below y down through air and water until it meets solid ground. */
	private static void post(WorldGenLevel level, Frame f, int u, int v, int top, int depth, BoundingBox clip) {
		for (int y = top; y >= top - depth; y--) {
			BlockPos pos = f.at(u, v, y);
			if (!clip.isInside(pos)) {
				return;
			}
			BlockState existing = level.getBlockState(pos);
			if (!existing.isAir() && level.getFluidState(pos).isEmpty()) {
				return;
			}
			level.setBlock(pos, LOG, 2);
		}
	}

	private static void buildHut(WorldGenLevel level, Frame f, int fy, BoundingBox clip) {
		for (int u = -4; u <= 4; u++) {
			for (int v = -3; v <= 3; v++) {
				set(level, f.at(u, v, fy), PLANKS, clip);
			}
		}
		// Stilts under the corners, door sides and middle of the long walls.
		int[][] posts = {{-4, -3}, {4, -3}, {-4, 3}, {4, 3}, {0, -3}, {0, 3}, {-4, 0}, {4, 0}};
		for (int[] p : posts) {
			post(level, f, p[0], p[1], fy - 1, 5, clip);
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
					set(level, f.at(u, v, y), state, clip);
				}
			}
		}
		// Gable ends (u = +-4) above the walls.
		for (int u = -4; u <= 4; u += 8) {
			for (int v = -2; v <= 2; v++) {
				set(level, f.at(u, v, fy + 4), PLANKS, clip);
			}
			for (int v = -1; v <= 1; v++) {
				set(level, f.at(u, v, fy + 5), PLANKS, clip);
			}
		}
		// Roof: stairs sloping down towards both long sides, ridge planks in the middle, one block of overhang.
		Direction land = f.sea().getOpposite();
		for (int u = -5; u <= 5; u++) {
			// A stair's FACING is its tall back: it must face the ridge, so the step side runs down to the eaves.
			set(level, f.at(u, 4, fy + 3), stair(land, false), clip);
			set(level, f.at(u, -4, fy + 3), stair(f.sea(), false), clip);
			for (int v = 1; v <= 3; v++) {
				int y = fy + 3 + (4 - v);
				set(level, f.at(u, v, y), stair(land, false), clip);
				set(level, f.at(u, -v, y), stair(f.sea(), false), clip);
			}
			set(level, f.at(u, 0, fy + 6), PLANKS, clip);
		}
	}

	private static void buildPier(WorldGenLevel level, Frame f, int fy, int end, BoundingBox clip) {
		for (int v = 4; v <= end; v++) {
			for (int u = -1; u <= 1; u++) {
				set(level, f.at(u, v, fy), PLANKS, clip);
			}
			// Piles every third plank, running down to the sea floor.
			if ((v - 4) % 3 == 0 || v == end) {
				for (int u = -1; u <= 1; u += 2) {
					post(level, f, u, v, fy - 1, PILE_DEPTH - 1, clip);
					set(level, f.at(u, v, fy + 1), LOG, clip);
				}
			}
		}
		BlockState lantern = Blocks.LANTERN.defaultBlockState();
		set(level, f.at(-1, end, fy + 2), lantern, clip);
		set(level, f.at(1, end, fy + 2), lantern, clip);
		set(level, f.at(-1, 4, fy + 2), lantern, clip);
		set(level, f.at(1, 4, fy + 2), lantern, clip);
		// A step up from the beach at the land door.
		BlockPos stepPos = f.at(0, -4, fy);
		if (clip.isInside(stepPos)) {
			int gy = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, stepPos.getX(), stepPos.getZ());
			if (gy == fy) {
				level.setBlock(stepPos, stair(f.sea().getOpposite(), false), 2);
			}
		}
	}

	private static void decorate(WorldGenLevel level, Frame f, int fy, int end, BoundingBox clip) {
		BlockState carpet = Blocks.CARPET.blue().defaultBlockState();
		BlockState barrel = Blocks.BARREL.defaultBlockState().setValue(BarrelBlock.FACING, Direction.UP);
		BlockState tankBody = Blocks.CONCRETE.yellow().defaultBlockState();
		BlockState tankCap = Blocks.POLISHED_ANDESITE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.BOTTOM);
		BlockState lantern = Blocks.LANTERN.defaultBlockState();

		// Counter in front of the clerk (left side), rug on the guide's side.
		for (int v = -1; v <= 1; v++) {
			set(level, f.at(-2, v, fy + 1), PLANKS, clip);
		}
		set(level, f.at(-2, 0, fy + 2), lantern, clip);
		for (int u = 1; u <= 3; u++) {
			for (int v = -1; v <= 1; v++) {
				set(level, f.at(u, v, fy + 1), carpet, clip);
			}
		}
		// Barrels and air tanks along the walls.
		set(level, f.at(-3, 2, fy + 1), barrel, clip);
		set(level, f.at(-3, -2, fy + 1), barrel, clip);
		set(level, f.at(3, 2, fy + 1), barrel, clip);
		for (int[] t : new int[][]{{3, -2}, {2, -2}}) {
			set(level, f.at(t[0], t[1], fy + 1), tankBody, clip);
			set(level, f.at(t[0], t[1], fy + 2), tankBody, clip);
			set(level, f.at(t[0], t[1], fy + 3), tankCap, clip);
		}
		// Gear on the pier: tanks and barrels near the end.
		set(level, f.at(-1, end - 1, fy + 1), tankBody, clip);
		set(level, f.at(-1, end - 1, fy + 2), tankBody, clip);
		set(level, f.at(-1, end - 1, fy + 3), tankCap, clip);
		set(level, f.at(1, end - 2, fy + 1), barrel, clip);
		set(level, f.at(0, end, fy + 1), barrel, clip);

		// Signs on both long walls.
		placeSign(level, f.at(3, 4, fy + 2), f.sea(), clip);
		placeSign(level, f.at(3, -4, fy + 2), f.sea().getOpposite(), clip);
	}

	private static void placeSign(WorldGenLevel level, BlockPos pos, Direction facing, BoundingBox clip) {
		if (!clip.isInside(pos)) {
			return;
		}
		BlockState sign = Blocks.SPRUCE_WALL_SIGN.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, facing);
		level.setBlock(pos, sign, 2);
		BlockEntity be = level.getBlockEntity(pos);
		if (be instanceof SignBlockEntity signEntity) {
			SignText text = new SignText()
					.setMessage(1, Component.translatable("sign.scubadive.dive_center.line1"))
					.setMessage(2, Component.translatable("sign.scubadive.dive_center.line2"));
			// No setText here: it calls markUpdated, which needs a level that world generation does not give.
			((net.scubadive.mixin.SignBlockEntityAccessor) signEntity).scubadive$setFrontText(text);
		}
	}

	private static void spawn(WorldGenLevel level, EntityType<? extends Mob> type, BlockPos pos, float yaw, BoundingBox clip) {
		if (!clip.isInside(pos)) {
			return;
		}
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
