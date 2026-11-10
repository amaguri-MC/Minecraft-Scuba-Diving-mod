package net.scubadive.client.creature.bottom;

import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.resources.Identifier;
import net.scubadive.ScubaDive;
import net.scubadive.creature.bottom.BottomDwellers;

/** Client registration: model layers and entity renderers for the bottom dwellers. */
public final class BottomDwellersClient {
	private static final Identifier CLOWNFISH_TEX = ScubaDive.id("textures/entity/clownfish.png");
	private static final Identifier SHRIMP_TEX = ScubaDive.id("textures/entity/anemone_shrimp.png");
	private static final Identifier GOBY_TEX = ScubaDive.id("textures/entity/goby.png");
	private static final Identifier EEL_TEX = ScubaDive.id("textures/entity/moray_eel.png");
	private static final Identifier[] FROGFISH_TEX = {
			ScubaDive.id("textures/entity/frogfish_grey.png"),
			ScubaDive.id("textures/entity/frogfish_yellow.png"),
			ScubaDive.id("textures/entity/frogfish_red.png")
	};

	private BottomDwellersClient() {
	}

	public static void init() {
		ModelLayerRegistry.registerModelLayer(ClownfishModel.LAYER, ClownfishModel::createBodyLayer);
		ModelLayerRegistry.registerModelLayer(AnemoneShrimpModel.LAYER, AnemoneShrimpModel::createBodyLayer);
		ModelLayerRegistry.registerModelLayer(GobyModel.LAYER, GobyModel::createBodyLayer);
		ModelLayerRegistry.registerModelLayer(MorayEelModel.LAYER, MorayEelModel::createBodyLayer);
		ModelLayerRegistry.registerModelLayer(FrogfishModel.LAYER, FrogfishModel::createBodyLayer);

		EntityRenderers.register(BottomDwellers.CLOWNFISH, ctx ->
				new BottomRenderer<>(ctx, new ClownfishModel(ctx.bakeLayer(ClownfishModel.LAYER)), 0.06f, 0.3f, s -> CLOWNFISH_TEX));
		EntityRenderers.register(BottomDwellers.ANEMONE_SHRIMP, ctx ->
				new BottomRenderer<>(ctx, new AnemoneShrimpModel(ctx.bakeLayer(AnemoneShrimpModel.LAYER)), 0.0f, 0.15f, s -> SHRIMP_TEX));
		EntityRenderers.register(BottomDwellers.GOBY, ctx ->
				new BottomRenderer<>(ctx, new GobyModel(ctx.bakeLayer(GobyModel.LAYER)), 0.06f, 0.25f, s -> GOBY_TEX));
		EntityRenderers.register(BottomDwellers.MORAY_EEL, ctx ->
				new BottomRenderer<>(ctx, new MorayEelModel(ctx.bakeLayer(MorayEelModel.LAYER)), 0.0f, 0.7f, s -> EEL_TEX));
		EntityRenderers.register(BottomDwellers.FROGFISH, ctx ->
				new BottomRenderer<>(ctx, new FrogfishModel(ctx.bakeLayer(FrogfishModel.LAYER)), 0.1f, 0.5f,
						s -> FROGFISH_TEX[Math.floorMod(s.variant, FROGFISH_TEX.length)]));
	}
}
