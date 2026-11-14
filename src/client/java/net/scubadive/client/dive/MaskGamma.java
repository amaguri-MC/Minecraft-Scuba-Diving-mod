package net.scubadive.client.dive;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.EquipmentSlot;

import net.fabricmc.loader.api.FabricLoader;

import net.scubadive.ScubaDive;
import net.scubadive.dive.ScubaDiveGear;

/**
 * Brightness boost while looking through the dive mask under water, like a gamma mod but only when diving.
 * The strength is in config/scubadive-client.properties (mask_gamma, 1.0 = vanilla "Bright", 10.0 = 1000%).
 */
public final class MaskGamma {
	private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("scubadive-client.properties");
	private static final float DEFAULT = 10.0f;
	private static float value = -1f;

	private MaskGamma() {
	}

	/** The boosted brightness, or -1 when it does not apply (no mask, or the eyes are not in water). */
	public static float boost() {
		LocalPlayer player = Minecraft.getInstance().player;
		if (player == null || !player.isEyeInFluid(FluidTags.WATER)
				|| !ScubaDiveGear.isWearing(player, EquipmentSlot.HEAD, ScubaDiveGear.DIVE_MASK)) {
			return -1f;
		}
		return configured();
	}

	private static float configured() {
		if (value < 0f) {
			Properties p = new Properties();
			try {
				if (Files.exists(FILE)) {
					try (Reader r = Files.newBufferedReader(FILE)) {
						p.load(r);
					}
				}
				value = Float.parseFloat(p.getProperty("mask_gamma", Float.toString(DEFAULT)));
				if (!Files.exists(FILE)) {
					p.setProperty("mask_gamma", Float.toString(DEFAULT));
					try (Writer w = Files.newBufferedWriter(FILE)) {
						p.store(w, "Brightness while wearing the dive mask under water (1.0 = vanilla Bright, 10.0 = 1000%)");
					}
				}
			} catch (IOException | NumberFormatException e) {
				ScubaDive.LOGGER.warn("Could not read {}: {}", FILE, e.toString());
				value = DEFAULT;
			}
		}
		return value;
	}
}
