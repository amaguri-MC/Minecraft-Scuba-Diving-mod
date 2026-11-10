package net.scubadive.client.creature.cold;

import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.resources.Identifier;
import net.scubadive.ScubaDive;
import net.scubadive.client.creature.bottom.BottomRenderer;
import net.scubadive.client.creature.bottom.MorayEelModel;
import net.scubadive.client.creature.reef.ReefFishModel;
import net.scubadive.client.creature.reef.ReefFishRenderer;
import net.scubadive.creature.cold.ColdCreatures;
import net.scubadive.creature.reef.ReefLoneFish;

/** Client registration for the cold-water creatures. */
public final class ColdCreaturesClient {
	private static final Identifier WOLF_EEL_TEX = ScubaDive.id("textures/entity/wolf_eel.png");

	private ColdCreaturesClient() {
	}

	public static void init() {
		ModelLayerRegistry.registerModelLayer(ClioneModel.LAYER, ClioneModel::createBodyLayer);
		EntityRenderers.register(ColdCreatures.CLIONE, ClioneRenderer::new);
		// Lumpsucker: the round boxfish body drawn tiny (~0.12 blocks) with its own texture.
		EntityRenderers.register(ColdCreatures.LUMPSUCKER, context ->
				new ReefFishRenderer<ReefLoneFish>(context, ReefFishModel.BOX, "lumpsucker", 0.13f, 0.30f, 0.45f, 0.03f));
		// Wolf eel: the moray body, larger, with a grey spotted texture.
		EntityRenderers.register(ColdCreatures.WOLF_EEL, ctx ->
				new BottomRenderer<>(ctx, new MorayEelModel(ctx.bakeLayer(MorayEelModel.LAYER)), 0.0f, 0.85f, s -> WOLF_EEL_TEX));
	}
}
