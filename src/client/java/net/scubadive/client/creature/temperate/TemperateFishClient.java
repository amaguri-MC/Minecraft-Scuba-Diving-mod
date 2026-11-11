package net.scubadive.client.creature.temperate;

import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.scubadive.client.creature.reef.ReefFishRenderer;
import net.scubadive.creature.temperate.TemperateFish;

/** Client registration for the temperate fish: their own body shapes on the shared reef fish model and renderer. */
public final class TemperateFishClient {
	private TemperateFishClient() {
	}

	public static void init() {
		ModelLayerRegistry.registerModelLayer(TemperateFishModels.SARDINE, TemperateFishModels::createSardineLayer);
		ModelLayerRegistry.registerModelLayer(TemperateFishModels.RED_SEA_BREAM, TemperateFishModels::createRedSeaBreamLayer);
		ModelLayerRegistry.registerModelLayer(TemperateFishModels.STRIPED_BEAKFISH, TemperateFishModels::createStripedBeakfishLayer);
		ModelLayerRegistry.registerModelLayer(TemperateFishModels.ARCTIC_CHAR, TemperateFishModels::createArcticCharLayer);
		ModelLayerRegistry.registerModelLayer(OceanSunfishModel.LAYER, OceanSunfishModel::createLayer);

		// Body lengths in blocks: sardine ~0.3, sea bream ~0.57, beakfish ~0.54, char ~0.45, sunfish ~1.1 (2.25 tall).
		fish(TemperateFish.SARDINE, TemperateFishModels.SARDINE, "sardine", 0.30f, 0.38f, 0.30f, 0.05f);
		fish(TemperateFish.RED_SEA_BREAM, TemperateFishModels.RED_SEA_BREAM, "red_sea_bream", 0.40f, 0.25f, 0.22f, 0.12f);
		fish(TemperateFish.STRIPED_BEAKFISH, TemperateFishModels.STRIPED_BEAKFISH, "striped_beakfish", 0.26f, 0.25f, 0.25f, 0.12f);
		fish(TemperateFish.ARCTIC_CHAR, TemperateFishModels.ARCTIC_CHAR, "arctic_char", 0.30f, 0.28f, 0.24f, 0.08f);
		EntityRenderers.register(TemperateFish.OCEAN_SUNFISH, OceanSunfishRenderer::new);
	}

	private static <T extends Mob> void fish(EntityType<T> type, ModelLayerLocation layer, String id, float scale,
			float tailSpeed, float flapSpeed, float shadow) {
		EntityRenderers.register(type, context -> new ReefFishRenderer<T>(context, layer, id, scale, tailSpeed, flapSpeed, shadow));
	}
}
