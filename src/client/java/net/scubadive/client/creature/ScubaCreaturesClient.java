package net.scubadive.client.creature;

import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.scubadive.creature.ScubaCreatures;

/** Client registration: model layers and entity renderers. */
public final class ScubaCreaturesClient {
	private ScubaCreaturesClient() {
	}

	public static void init() {
		ModelLayerRegistry.registerModelLayer(NudibranchModel.LAYER, NudibranchModel::createBodyLayer);
		ModelLayerRegistry.registerModelLayer(PygmySeahorseModel.LAYER, PygmySeahorseModel::createBodyLayer);
		EntityRenderers.register(ScubaCreatures.NUDIBRANCH, NudibranchRenderer::new);
		EntityRenderers.register(ScubaCreatures.PYGMY_SEAHORSE, PygmySeahorseRenderer::new);
		NudibranchClient.init();
		AnemoneTint.init();
	}
}
