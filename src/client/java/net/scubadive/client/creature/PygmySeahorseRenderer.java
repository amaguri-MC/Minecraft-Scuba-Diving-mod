package net.scubadive.client.creature;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;
import net.scubadive.ScubaDive;
import net.scubadive.creature.PygmySeahorse;

public class PygmySeahorseRenderer extends MobRenderer<PygmySeahorse, PygmySeahorseRenderState, PygmySeahorseModel> {
	private static final Identifier TEXTURE = ScubaDive.id("textures/entity/pygmy_seahorse.png");

	public PygmySeahorseRenderer(EntityRendererProvider.Context context) {
		super(context, new PygmySeahorseModel(context.bakeLayer(PygmySeahorseModel.LAYER)), 0.04f);
	}

	@Override
	public PygmySeahorseRenderState createRenderState() {
		return new PygmySeahorseRenderState();
	}

	@Override
	public Identifier getTextureLocation(PygmySeahorseRenderState state) {
		return TEXTURE;
	}

	/** The model is drawn smaller than its pixel grid so the creature is tiny in the world. */
	private static final float MODEL_SCALE = 0.3f;

	@Override
	protected void scale(PygmySeahorseRenderState state, PoseStack poseStack) {
		super.scale(state, poseStack);
		poseStack.scale(MODEL_SCALE, MODEL_SCALE, MODEL_SCALE);
	}
}
