package net.scubadive.client;

import net.fabricmc.api.ClientModInitializer;

public class ScubaDiveClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		net.scubadive.client.creature.ScubaCreaturesClient.init();
		net.scubadive.client.dive.ScubaDiveGearClient.init();
		net.scubadive.client.camera.ScubaCameraClient.init();
		net.scubadive.client.silt.SiltClient.init();
		net.scubadive.client.reef.ScubaReefsClient.init();
	}
}
