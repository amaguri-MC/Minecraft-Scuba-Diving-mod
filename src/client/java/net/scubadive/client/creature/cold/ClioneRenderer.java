package net.scubadive.client.creature.cold;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;
import net.scubadive.ScubaDive;
import net.scubadive.creature.cold.Clione;

public class ClioneRenderer extends MobRenderer<Clione, ClioneRenderState, ClioneModel> {
	private static final Identifier TEXTURE = ScubaDive.id("textures/entity/clione.png");
	/** The model grid is about 11 px tall; drawn at this scale the clione is 0.15 blocks. */
	private static final float MODEL_SCALE = 0.22f;

	public ClioneRenderer(EntityRendererProvider.Context context) {
		super(context, new ClioneModel(context.bakeLayer(ClioneModel.LAYER)), 0.03f);
	}

	@Override
	public ClioneRenderState createRenderState() {
		return new ClioneRenderState();
	}

	@Override
	public void extractRenderState(Clione entity, ClioneRenderState state, float partialTick) {
		super.extractRenderState(entity, state, partialTick);
		state.phase = entity.getId() * 5.0f;
	}

	@Override
	public Identifier getTextureLocation(ClioneRenderState state) {
		return TEXTURE;
	}

	@Override
	protected void scale(ClioneRenderState state, PoseStack poseStack) {
		super.scale(state, poseStack);
		poseStack.scale(MODEL_SCALE, MODEL_SCALE, MODEL_SCALE);
	}
}
