package net.scubadive.client.creature.reef;

import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.scubadive.creature.reef.ReefFish;

/** Client registration: model layers and renderers for the reef fish. */
public final class ReefFishClient {
	private ReefFishClient() {
	}

	public static void init() {
		ModelLayerRegistry.registerModelLayer(ReefFishModel.SLENDER, ReefFishModel::createSlenderLayer);
		ModelLayerRegistry.registerModelLayer(ReefFishModel.DISC, ReefFishModel::createDiscLayer);
		ModelLayerRegistry.registerModelLayer(ReefFishModel.IDOL, ReefFishModel::createIdolLayer);
		ModelLayerRegistry.registerModelLayer(ReefFishModel.LION, ReefFishModel::createLionLayer);

		register(ReefFish.BLUE_DAMSEL, ReefFishModel.SLENDER, "blue_damsel", 0.32f);
		register(ReefFish.CHROMIS, ReefFishModel.SLENDER, "chromis", 0.30f);
		register(ReefFish.ANTHIAS, ReefFishModel.SLENDER, "anthias", 0.36f);
		register(ReefFish.BLUE_TANG, ReefFishModel.DISC, "blue_tang", 0.32f);
		register(ReefFish.BUTTERFLYFISH, ReefFishModel.DISC, "butterflyfish", 0.30f);
		register(ReefFish.MOORISH_IDOL, ReefFishModel.IDOL, "moorish_idol", 0.30f);
		register(ReefFish.LIONFISH, ReefFishModel.LION, "lionfish", 0.34f);
	}

	private static <T extends Mob> void register(EntityType<T> type, ModelLayerLocation layer, String id, float scale) {
		EntityRenderers.register(type, context -> new ReefFishRenderer<T>(context, layer, id, scale));
	}
}
