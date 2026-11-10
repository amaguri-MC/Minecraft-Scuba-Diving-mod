package net.scubadive.client.tour;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;

import net.scubadive.tour.TourLoadingPayload;

/** Client side of tours (loading screen, HUD). Owner: tour session module (H2). */
public final class TourClient {
	private TourClient() {
	}

	public static void init() {
		ClientPlayNetworking.registerGlobalReceiver(TourLoadingPayload.TYPE, (payload, context) -> {
			Minecraft client = Minecraft.getInstance();
			if (payload.percent() < 0) {
				if (client.gui.screen() instanceof TourLoadingScreen) {
					client.gui.setScreen(null);
				}
				return;
			}
			if (client.gui.screen() instanceof TourLoadingScreen screen) {
				screen.update(payload.percent());
			} else if (client.gui.screen() == null) {
				TourLoadingScreen screen = new TourLoadingScreen(payload.pointKey());
				screen.update(payload.percent());
				client.gui.setScreen(screen);
			}
		});
	}
}
