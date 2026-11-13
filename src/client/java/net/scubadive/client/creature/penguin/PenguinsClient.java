package net.scubadive.client.creature.penguin;

import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.scubadive.creature.penguin.Penguins;

/** Client registration for the penguins. Sizes match the spec: gentoo 1.0, rockhopper 0.78, cape 0.9. */
public final class PenguinsClient {
	private PenguinsClient() {
	}

	public static void init() {
		ModelLayerRegistry.registerModelLayer(PenguinModel.GENTOO, PenguinModel::createGentooLayer);
		ModelLayerRegistry.registerModelLayer(PenguinModel.ROCKHOPPER, PenguinModel::createRockhopperLayer);
		ModelLayerRegistry.registerModelLayer(PenguinModel.CAPE, PenguinModel::createCapeLayer);
		EntityRenderers.register(Penguins.GENTOO, ctx -> new PenguinRenderer(ctx, PenguinModel.GENTOO, "penguin_gentoo", 1.0f));
		EntityRenderers.register(Penguins.ROCKHOPPER, ctx -> new PenguinRenderer(ctx, PenguinModel.ROCKHOPPER, "penguin_rockhopper", 0.78f));
		EntityRenderers.register(Penguins.CAPE, ctx -> new PenguinRenderer(ctx, PenguinModel.CAPE, "penguin_cape", 0.9f));
	}
}
