package net.scubadive.client.creature.cephalopod;

import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
import net.minecraft.client.renderer.entity.EntityRenderers;

import net.scubadive.creature.cephalopod.Cephalopods;

/** Client registration for the spear squid and the cuttlefish. */
public final class CephalopodsClient {
	private CephalopodsClient() {
	}

	public static void init() {
		ModelLayerRegistry.registerModelLayer(SpearSquidModel.LAYER, SpearSquidModel::createBodyLayer);
		ModelLayerRegistry.registerModelLayer(CuttlefishModel.LAYER, CuttlefishModel::createBodyLayer);
		EntityRenderers.register(Cephalopods.SPEAR_SQUID, SpearSquidRenderer::new);
		EntityRenderers.register(Cephalopods.CUTTLEFISH, CuttlefishRenderer::new);
	}
}
