package net.scubadive.client.dive;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.EquipmentSlot;

import net.fabricmc.loader.api.FabricLoader;

import net.scubadive.ScubaDive;
import net.scubadive.dive.ScubaDiveGear;

/**
 * Brightness boost while wearing the dive mask, like a gamma mod (on land and under water).
 * The strength is in config/scubadive-client.properties (mask_gamma, 1.0 = vanilla "Bright", 15.0 = 1500%).
 */
public final class MaskGamma {
	private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("scubadive-client.properties");
	private static final float DEFAULT = 15.0f;
	/** The first release wrote 10.0 into the config; that file is moved to the new default. */
	private static final float OLD_DEFAULT = 10.0f;
	private static float value = -1f;

	private MaskGamma() {
	}

	/** The boosted brightness, or -1 when it does not apply (no mask). */
	public static float boost() {
		LocalPlayer player = Minecraft.getInstance().player;
		if (player == null || !ScubaDiveGear.isWearing(player, EquipmentSlot.HEAD, ScubaDiveGear.DIVE_MASK)) {
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
				if (value == OLD_DEFAULT) {
					value = DEFAULT;
				}
				if (!Files.exists(FILE) || !Float.toString(value).equals(p.getProperty("mask_gamma"))) {
					p.setProperty("mask_gamma", Float.toString(value));
					try (Writer w = Files.newBufferedWriter(FILE)) {
						p.store(w, "Brightness while wearing the dive mask (1.0 = vanilla Bright, 15.0 = 1500%)");
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
