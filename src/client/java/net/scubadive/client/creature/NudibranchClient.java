package net.scubadive.client.creature;

import java.util.function.Function;

import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.scubadive.ScubaDive;
import net.scubadive.creature.Nudibranch;
import net.scubadive.creature.ScubaCreatures;

/**
 * Client registration for the extra sea slug species (the original nudibranch is registered in
 * {@link ScubaCreaturesClient}). Draw scales keep the small species tiny, as with the pygmy seahorse.
 */
public final class NudibranchClient {
	private NudibranchClient() {
	}

	public static void init() {
		ModelLayerRegistry.registerModelLayer(SeaBunnyModel.LAYER, SeaBunnyModel::createBodyLayer);
		ModelLayerRegistry.registerModelLayer(SpanishDancerModel.LAYER, SpanishDancerModel::createBodyLayer);
		ModelLayerRegistry.registerModelLayer(BlueDragonModel.LAYER, BlueDragonModel::createBodyLayer);
		ModelLayerRegistry.registerModelLayer(LeafSheepModel.LAYER, LeafSheepModel::createBodyLayer);
		ModelLayerRegistry.registerModelLayer(PhyllidiaModel.LAYER, PhyllidiaModel::createBodyLayer);
		ModelLayerRegistry.registerModelLayer(ThecaceraModel.LAYER, ThecaceraModel::createBodyLayer);

		register(ScubaCreatures.SEA_BUNNY, SeaBunnyModel.LAYER, SeaBunnyModel::new, "sea_bunny", 0.25f, 0.02f);
		register(ScubaCreatures.SPANISH_DANCER, SpanishDancerModel.LAYER, SpanishDancerModel::new, "spanish_dancer", 0.55f, 0.15f);
		register(ScubaCreatures.BLUE_DRAGON, BlueDragonModel.LAYER, BlueDragonModel::new, "blue_dragon", 0.28f, 0.0f);
		register(ScubaCreatures.LEAF_SHEEP, LeafSheepModel.LAYER, LeafSheepModel::new, "leaf_sheep", 0.16f, 0.015f);
		register(ScubaCreatures.PHYLLIDIA, PhyllidiaModel.LAYER, PhyllidiaModel::new, "phyllidia", 0.4f, 0.04f);
		register(ScubaCreatures.THECACERA, ThecaceraModel.LAYER, ThecaceraModel::new, "thecacera", 0.22f, 0.02f);
	}

	private static void register(EntityType<? extends Nudibranch> type, ModelLayerLocation layer,
			Function<ModelPart, ? extends EntityModel<NudibranchRenderState>> model, String name, float scale, float shadow) {
		Identifier texture = ScubaDive.id("textures/entity/nudibranch_" + name + ".png");
		EntityRendererProvider<Nudibranch> provider = context -> new NudibranchRenderer(context,
				model.apply(context.bakeLayer(layer)), variant -> texture, scale, shadow);
		EntityRenderers.register(type, provider);
	}
}
