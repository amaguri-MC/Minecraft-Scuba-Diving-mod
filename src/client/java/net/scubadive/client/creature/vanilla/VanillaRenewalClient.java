package net.scubadive.client.creature.vanilla;

import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
import net.minecraft.world.entity.EntityTypes;

/**
 * Renewed looks for seven vanilla sea animals (turtle, dolphin, nautilus, pufferfish, squid, salmon, cod): only the renderers are
 * replaced, the entities stay vanilla. Glow squid, tropical fish and zombie nautilus keep their vanilla look.
 */
public final class VanillaRenewalClient {
	private VanillaRenewalClient() {
	}

	public static void init() {
		ModelLayerRegistry.registerModelLayer(VanillaTurtleModel.LAYER, VanillaTurtleModel::createLayer);
		ModelLayerRegistry.registerModelLayer(VanillaDolphinModel.LAYER, VanillaDolphinModel::createLayer);
		ModelLayerRegistry.registerModelLayer(VanillaNautilusModel.LAYER, VanillaNautilusModel::createLayer);
		ModelLayerRegistry.registerModelLayer(VanillaNautilusGearModel.SADDLE, NautilusSaddleGeometry::createLayer);
		ModelLayerRegistry.registerModelLayer(VanillaNautilusGearModel.ARMOR, NautilusArmorGeometry::createLayer);
		ModelLayerRegistry.registerModelLayer(VanillaPufferfishModel.SMALL, PufferfishSmallGeometry::createLayer);
		ModelLayerRegistry.registerModelLayer(VanillaPufferfishModel.MID, PufferfishMidGeometry::createLayer);
		ModelLayerRegistry.registerModelLayer(VanillaPufferfishModel.BIG, PufferfishBigGeometry::createLayer);
		ModelLayerRegistry.registerModelLayer(VanillaSquidModel.LAYER, SquidGeometry::createLayer);
		ModelLayerRegistry.registerModelLayer(VanillaFishModel.SALMON, SalmonGeometry::createLayer);
		ModelLayerRegistry.registerModelLayer(VanillaFishModel.COD, CodGeometry::createLayer);

		// Fabric applies these after the vanilla registrations, so they replace the vanilla renderers.
		EntityRendererRegistry.register(EntityTypes.TURTLE, VanillaTurtleRenderer::new);
		EntityRendererRegistry.register(EntityTypes.DOLPHIN, VanillaDolphinRenderer::new);
		EntityRendererRegistry.register(EntityTypes.NAUTILUS, VanillaNautilusRenderer::new);
		EntityRendererRegistry.register(EntityTypes.PUFFERFISH, VanillaPufferfishRenderer::new);
		EntityRendererRegistry.register(EntityTypes.SQUID, VanillaSquidRenderer::new);
		EntityRendererRegistry.register(EntityTypes.SALMON, VanillaSalmonRenderer::new);
		EntityRendererRegistry.register(EntityTypes.COD, VanillaCodRenderer::new);
	}
}
