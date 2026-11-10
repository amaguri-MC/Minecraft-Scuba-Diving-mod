package net.scubadive.client.creature;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;
import net.scubadive.creature.Nudibranch;

public class NudibranchRenderer extends MobRenderer<Nudibranch, NudibranchRenderState, NudibranchModel> {
	public NudibranchRenderer(EntityRendererProvider.Context context) {
		super(context, new NudibranchModel(context.bakeLayer(NudibranchModel.LAYER)), 0.04f);
	}

	@Override
	public NudibranchRenderState createRenderState() {
		return new NudibranchRenderState();
	}

	@Override
	public void extractRenderState(Nudibranch entity, NudibranchRenderState state, float partialTick) {
		super.extractRenderState(entity, state, partialTick);
		state.variant = entity.getVariant();
	}

	@Override
	public Identifier getTextureLocation(NudibranchRenderState state) {
		return NudibranchModel.texture(state.variant);
	}

	/** The model is drawn smaller than its pixel grid so the creature is tiny in the world. */
	private static final float MODEL_SCALE = 0.4f;

	@Override
	protected void scale(NudibranchRenderState state, PoseStack poseStack) {
		super.scale(state, poseStack);
		poseStack.scale(MODEL_SCALE, MODEL_SCALE, MODEL_SCALE);
	}
}
