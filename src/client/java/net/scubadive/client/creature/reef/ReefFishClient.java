package net.scubadive.client.creature.reef;

import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.scubadive.creature.reef.ReefFish;
import net.scubadive.creature.reef.ReefLoneFish;

/** Client registration: model layers and renderers for the reef fish. */
public final class ReefFishClient {
	private ReefFishClient() {
	}

	public static void init() {
		ModelLayerRegistry.registerModelLayer(ReefFishModel.SLENDER, ReefFishModel::createSlenderLayer);
		ModelLayerRegistry.registerModelLayer(ReefFishModel.DISC, ReefFishModel::createDiscLayer);
		ModelLayerRegistry.registerModelLayer(ReefFishModel.IDOL, ReefFishModel::createIdolLayer);
		ModelLayerRegistry.registerModelLayer(ReefFishModel.PARROT, ReefFishModel::createParrotLayer);
		ModelLayerRegistry.registerModelLayer(ReefFishModel.BOX, ReefFishModel::createBoxLayer);
		ModelLayerRegistry.registerModelLayer(ReefFishModel.TRIGGER, ReefFishModel::createTriggerLayer);
		ModelLayerRegistry.registerModelLayer(ReefFishModel.GROUPER, ReefFishModel::createGrouperLayer);
		ModelLayerRegistry.registerModelLayer(ReefFishModel.SNAPPER, ReefFishModel::createSnapperLayer);
		ModelLayerRegistry.registerModelLayer(LionfishModel.LAYER, LionfishModel::createLayer);

		register(ReefFish.BLUE_DAMSEL, ReefFishModel.SLENDER, "blue_damsel", 0.32f);
		register(ReefFish.CHROMIS, ReefFishModel.SLENDER, "chromis", 0.30f);
		register(ReefFish.ANTHIAS, ReefFishModel.SLENDER, "anthias", 0.36f);
		register(ReefFish.BLUE_TANG, ReefFishModel.DISC, "blue_tang", 0.32f);
		register(ReefFish.BUTTERFLYFISH, ReefFishModel.DISC, "butterflyfish", 0.30f);
		register(ReefFish.MOORISH_IDOL, ReefFishModel.IDOL, "moorish_idol", 0.30f);
		EntityRenderers.register(ReefFish.LIONFISH, context -> new LionfishRenderer<ReefLoneFish>(context));

		// Body lengths in blocks: parrotfish ~0.5, boxfish ~0.3, triggerfish ~0.4, grouper ~0.8, snapper ~0.3.
		register(ReefFish.PARROTFISH, ReefFishModel.PARROT, "parrotfish", 0.40f, 0.22f, 0.20f, 0.10f);
		register(ReefFish.BOXFISH, ReefFishModel.BOX, "boxfish", 0.30f, 0.30f, 0.45f, 0.08f);
		register(ReefFish.TRIGGERFISH, ReefFishModel.TRIGGER, "triggerfish", 0.32f, 0.25f, 0.30f, 0.10f);
		register(ReefFish.GROUPER, ReefFishModel.GROUPER, "grouper", 0.46f, 0.12f, 0.10f, 0.20f);
		register(ReefFish.BLUESTRIPE_SNAPPER, ReefFishModel.SNAPPER, "bluestripe_snapper", 0.16f, 0.28f, 0.24f, 0.06f);
	}

	private static <T extends Mob> void register(EntityType<T> type, ModelLayerLocation layer, String id, float scale) {
		register(type, layer, id, scale, 0.25f, 0.22f, 0.06f);
	}

	private static <T extends Mob> void register(EntityType<T> type, ModelLayerLocation layer, String id, float scale,
			float tailSpeed, float flapSpeed, float shadow) {
		EntityRenderers.register(type, context -> new ReefFishRenderer<T>(context, layer, id, scale, tailSpeed, flapSpeed, shadow));
	}
}
