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
		net.scubadive.client.creature.big.BigFishClient.init();
		net.scubadive.client.creature.reef.ReefFishClient.init();
		net.scubadive.client.creature.bottom.BottomDwellersClient.init();
		net.scubadive.client.creature.critter.CrittersClient.init();
		net.scubadive.client.equipment.WristSlotClient.init();
		net.scubadive.client.tour.TourClient.init();
		net.scubadive.client.tour.DiveCenterClient.init();
	}
}
