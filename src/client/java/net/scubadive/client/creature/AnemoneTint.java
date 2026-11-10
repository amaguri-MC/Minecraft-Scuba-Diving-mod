package net.scubadive.client.creature;

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

/** Position-based smooth colour variation for sea anemones (like grass colour maps). */
public final class AnemoneTint {
	private static final int[] PALETTE = {
			0xB45AD2, // purple
			0xF06EA0, // pink
			0xF08A5A, // coral orange
			0x8FD39A, // light green
			0xEAD9B8, // cream
	};
	/** Noise cell size in blocks; colours drift slowly over this scale. */
	private static final float CELL = 7.0F;

	private AnemoneTint() {
	}

	public static void init() {
		Block block = BuiltInRegistries.BLOCK.getValue(Identifier.fromNamespaceAndPath("scubadive", "sea_anemone"));
		if (block == Blocks.AIR) {
			return;
		}
		BlockColorRegistry.register(List.of(new BlockTintSource() {
			@Override
			public int color(BlockState state) {
				return PALETTE[0];
			}

			@Override
			public int colorInWorld(BlockState state, BlockAndTintGetter level, BlockPos pos) {
				return colorAt(pos.getX(), pos.getZ());
			}
		}), block);
	}

	/** Deterministic, continuous colour for a column. */
	public static int colorAt(int x, int z) {
		float fx = x / CELL;
		float fz = z / CELL;
		float n = noise(fx, fz, 0) * 0.75F + noise(fx * 2.3F + 11.7F, fz * 2.3F - 5.3F, 1) * 0.25F;
		// value noise tends to cluster around the middle; stretch it so the whole palette is used
		float t = Math.max(0.0F, Math.min(1.0F, (n - 0.5F) * 1.6F + 0.5F));
		float scaled = t * (PALETTE.length - 1);
		int i = Math.min((int) scaled, PALETTE.length - 2);
		return lerp(PALETTE[i], PALETTE[i + 1], scaled - i);
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
