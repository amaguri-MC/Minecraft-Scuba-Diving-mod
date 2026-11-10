package net.scubadive.client.creature.big;

import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.scubadive.creature.big.BigFish;

/** Client registration for the big fish: model layers and renderers. */
public final class BigFishClient {
	private BigFishClient() {
	}

	public static void init() {
		ModelLayerRegistry.registerModelLayer(BigFishModel.REEF_SHARK, BigFishModel::createReefSharkLayer);
		ModelLayerRegistry.registerModelLayer(BigFishModel.HAMMERHEAD_SHARK, BigFishModel::createHammerheadSharkLayer);
		ModelLayerRegistry.registerModelLayer(BigFishModel.WHALE_SHARK, BigFishModel::createWhaleSharkLayer);
		ModelLayerRegistry.registerModelLayer(BigFishModel.BARRACUDA, BigFishModel::createBarracudaLayer);

		ModelLayerRegistry.registerModelLayer(BigFishModel.BIGEYE_TREVALLY, BigFishModel::createBigeyeTrevallyLayer);
		ModelLayerRegistry.registerModelLayer(BigFishModel.MANTA_RAY, BigFishModel::createMantaRayLayer);
		ModelLayerRegistry.registerModelLayer(BigFishModel.STINGRAY, BigFishModel::createStingrayLayer);

		EntityRenderers.register(BigFish.REEF_SHARK, ctx ->
				new BigFishRenderer<>(ctx, BigFishModel.REEF_SHARK, "reef_shark", 1.0f, 0.4f, 0.55f, 1.0f));
		EntityRenderers.register(BigFish.HAMMERHEAD_SHARK, ctx ->
				new BigFishRenderer<>(ctx, BigFishModel.HAMMERHEAD_SHARK, "hammerhead_shark", 1.8f, 0.7f, 0.6f, 1.0f));
		EntityRenderers.register(BigFish.WHALE_SHARK, ctx ->
				new BigFishRenderer<>(ctx, BigFishModel.WHALE_SHARK, "whale_shark", 4.6f, 2.4f, 0.28f, 0.8f));
		EntityRenderers.register(BigFish.BARRACUDA, ctx ->
				new BigFishRenderer<>(ctx, BigFishModel.BARRACUDA, "barracuda", 1.0f, 0.25f, 0.9f, 1.0f));
		EntityRenderers.register(BigFish.BIGEYE_TREVALLY, ctx ->
				new BigFishRenderer<>(ctx, BigFishModel.BIGEYE_TREVALLY, "bigeye_trevally", 1.3f, 0.3f, 0.85f, 1.0f));
		EntityRenderers.register(BigFish.MANTA_RAY, ctx ->
				new BigFishRenderer<>(ctx, BigFishModel.MANTA_RAY, "manta_ray", 2.4f, 1.2f, 1.0f, 0.4f, 0.5f));
		EntityRenderers.register(BigFish.STINGRAY, ctx ->
				new BigFishRenderer<>(ctx, BigFishModel.STINGRAY, "stingray", 1.3f, 0.5f, 0.8f, 0.3f, 0.14f));
	}
}
