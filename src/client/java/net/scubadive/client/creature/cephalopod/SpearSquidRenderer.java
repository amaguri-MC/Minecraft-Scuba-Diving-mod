package net.scubadive.client.creature.cephalopod;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;

import net.scubadive.ScubaDive;
import net.scubadive.creature.cephalopod.SpearSquid;

public class SpearSquidRenderer extends MobRenderer<SpearSquid, CephalopodRenderState, SpearSquidModel> {
	private static final Identifier TEXTURE = ScubaDive.id("textures/entity/spear_squid.png");
	/** The model is about 25 px from tentacle tip to tail; drawn at this scale it is about 0.65 blocks long. */
	private static final float MODEL_SCALE = 0.42f;

	public SpearSquidRenderer(EntityRendererProvider.Context context) {
		super(context, new SpearSquidModel(context.bakeLayer(SpearSquidModel.LAYER)), 0.12f);
	}

	@Override
	public CephalopodRenderState createRenderState() {
		return new CephalopodRenderState();
	}

	@Override
	public void extractRenderState(SpearSquid entity, CephalopodRenderState state, float partialTick) {
		super.extractRenderState(entity, state, partialTick);
		state.phase = entity.getId() * 7.0f;
		state.jetting = entity.isJetting();
	}

	@Override
	public Identifier getTextureLocation(CephalopodRenderState state) {
		return TEXTURE;
	}

	@Override
	protected void scale(CephalopodRenderState state, PoseStack poseStack) {
		super.scale(state, poseStack);
		poseStack.scale(MODEL_SCALE, MODEL_SCALE, MODEL_SCALE);
	}
}
