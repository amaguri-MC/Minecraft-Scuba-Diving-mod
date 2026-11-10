package net.scubadive.client.reef;

import java.util.List;

import net.fabricmc.fabric.api.client.rendering.v1.BlockColorRegistry;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/** Position-based colour variation for the reef corals, with the odd bleached (white) patch. */
public final class ReefTint {
	private static final int[] PALETTE = {
			0xE0508C, // magenta pink
			0xF0786E, // salmon
			0xF2A04A, // orange
			0xE8C84E, // yellow
			0x6CCB9A, // teal green
			0x4FA6D8, // blue
			0x9A68D6, // violet
	};
	private static final int BLEACHED = 0xF1EDE4;
	private static final float CELL = 5.0F;
	private static final float BLEACH_CELL = 9.0F;
	private static final String[] BLOCKS = {"table_coral", "brain_coral", "branch_coral", "fan_coral"};

	private ReefTint() {
	}

	public static void init() {
		for (String path : BLOCKS) {
			Block block = BuiltInRegistries.BLOCK.getValue(Identifier.fromNamespaceAndPath("scubadive", path));
			if (block == Blocks.AIR) {
				continue;
			}
			BlockTintSource main = new BlockTintSource() {
				@Override
				public int color(BlockState state) {
					return 0xFF000000 | PALETTE[0];
				}

				@Override
				public int colorInWorld(BlockState state, BlockAndTintGetter level, BlockPos pos) {
					return 0xFF000000 | colorAt(pos.getX(), pos.getZ());
				}
			};
			// Second colour (brain coral overlay): another part of the palette, so each block shows a mix.
			BlockTintSource accent = new BlockTintSource() {
				@Override
				public int color(BlockState state) {
					return 0xFF000000 | PALETTE[3];
				}

				@Override
				public int colorInWorld(BlockState state, BlockAndTintGetter level, BlockPos pos) {
					// Opaque alpha matters: the overlay is a cutout quad and a zero tint alpha discards every pixel.
					return 0xFF000000 | accentAt(pos.getX(), pos.getY(), pos.getZ());
				}
			};
			BlockColorRegistry.register(List.of(main, accent), block);
		}
	}

	/** Accent colour: palette entry a few steps away from the main colour, varying per block. */
	public static int accentAt(int x, int y, int z) {
		int h = (x * 73_856_093) ^ (y * 19_349_663) ^ (z * 83_492_791);
		int step = 2 + Math.floorMod(h >> 4, 3);
		float n = noise(x / CELL, z / CELL, 0);
		int i = Math.floorMod((int) (n * PALETTE.length) + step, PALETTE.length);
		return Math.floorMod(h, 5) == 0 ? BLEACHED : PALETTE[i];
	}

	/** Deterministic colour for a column: smooth palette drift, with bleached patches. */
	public static int colorAt(int x, int z) {
		float n = noise(x / CELL, z / CELL, 0) * 0.7F + noise(x / CELL * 2.1F + 4.3F, z / CELL * 2.1F - 9.1F, 1) * 0.3F;
		float t = Math.max(0.0F, Math.min(1.0F, (n - 0.5F) * 1.7F + 0.5F));
		float scaled = t * (PALETTE.length - 1);
		int i = Math.min((int) scaled, PALETTE.length - 2);
		int color = lerp(PALETTE[i], PALETTE[i + 1], scaled - i);
		// bleached coral: a separate, coarser noise decides where colour fades to white
		float b = noise(x / BLEACH_CELL + 17.0F, z / BLEACH_CELL - 3.0F, 2);
		if (b > 0.74F) {
			color = lerp(color, BLEACHED, Math.min(1.0F, (b - 0.74F) / 0.07F));
		}
		return color;
	}

	private static float noise(float x, float z, int salt) {
		int x0 = (int) Math.floor(x);
		int z0 = (int) Math.floor(z);
		float tx = smooth(x - x0);
		float tz = smooth(z - z0);
		float a = hash(x0, z0, salt);
		float b = hash(x0 + 1, z0, salt);
		float c = hash(x0, z0 + 1, salt);
		float d = hash(x0 + 1, z0 + 1, salt);
		float top = a + (b - a) * tx;
		float bottom = c + (d - c) * tx;
		return top + (bottom - top) * tz;
	}

	private static float smooth(float t) {
		return t * t * (3.0F - 2.0F * t);
	}

	private static float hash(int x, int z, int salt) {
		int h = x * 374761393 + z * 668265263 + salt * 1274126177;
		h = (h ^ (h >>> 13)) * 1274126177;
		h ^= h >>> 16;
		return (h & 0xFFFFFF) / (float) 0x1000000;
	}

	private static int lerp(int c0, int c1, float t) {
		int r = Math.round(((c0 >> 16) & 0xFF) * (1 - t) + ((c1 >> 16) & 0xFF) * t);
		int g = Math.round(((c0 >> 8) & 0xFF) * (1 - t) + ((c1 >> 8) & 0xFF) * t);
		int b = Math.round((c0 & 0xFF) * (1 - t) + (c1 & 0xFF) * t);
		return 0xFF000000 | (r << 16) | (g << 8) | b;
	}
}
