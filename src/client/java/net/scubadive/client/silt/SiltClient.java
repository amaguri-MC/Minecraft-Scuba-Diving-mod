package net.scubadive.client.silt;

import java.util.ArrayList;
import java.util.List;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.phys.Vec3;

import net.scubadive.ScubaDive;
import net.scubadive.silt.SiltClouds;
import net.scubadive.silt.SiltSyncPayload;

/** Client visuals for stirred-up sediment. Owner: silt module. */
public final class SiltClient {
	private static final float MAX_ALPHA = 0.75f;
	private static final int STALE_TICKS = 40;
	private static final float FOLLOW = 0.12f;

	private static List<SiltSyncPayload.Entry> clouds = List.of();
	private static int sinceSync;
	private static float haze;
	private static float prevHaze;

	private SiltClient() {
	}

	public static void init() {
		ClientPlayNetworking.registerGlobalReceiver(SiltSyncPayload.TYPE, (payload, context) -> {
			clouds = new ArrayList<>(payload.clouds());
			sinceSync = 0;
		});
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> reset());
		ClientTickEvents.END_CLIENT_TICK.register(SiltClient::tick);
		// After the depth tint (registered in ScubaDiveGearClient, which is initialised first), before MISC_OVERLAYS.
		HudElementRegistry.attachElementAfter(ScubaDive.id("depth_tint"), ScubaDive.id("silt_haze"), SiltClient::render);
	}

	private static void reset() {
		clouds = List.of();
		haze = 0f;
		prevHaze = 0f;
	}

	private static void tick(Minecraft client) {
		prevHaze = haze;
		if (client.player == null || client.level == null) {
			reset();
			return;
		}
		if (++sinceSync > STALE_TICKS) {
			clouds = List.of();
		}
		Vec3 cam = client.gameRenderer.mainCamera().position();
		float target = 0f;
		for (SiltSyncPayload.Entry e : clouds) {
			double dx = e.x() - cam.x;
			double dy = e.y() - cam.y;
			double dz = e.z() - cam.z;
			target = Math.max(target, SiltClouds.falloff(e.density(), e.radius(), Math.sqrt(dx * dx + dy * dy + dz * dz)));
		}
		haze += (Math.min(1f, target) - haze) * FOLLOW;
		if (haze < 0.002f) {
			haze = 0f;
		}
	}

	private static void render(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
		float t = deltaTracker.getGameTimeDeltaPartialTick(false);
		float h = prevHaze + (haze - prevHaze) * t;
		if (h < 0.01f) {
			return;
		}
		int a = Math.max(0, Math.min(255, Math.round(h * MAX_ALPHA * 255f)));
		// Murky beige.
		graphics.fill(0, 0, graphics.guiWidth(), graphics.guiHeight(), (a << 24) | (170 << 16) | (150 << 8) | 112);
	}
}
