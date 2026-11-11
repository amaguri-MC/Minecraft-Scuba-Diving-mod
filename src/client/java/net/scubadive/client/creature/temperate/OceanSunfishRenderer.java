package net.scubadive.client.creature.temperate;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;
import net.scubadive.ScubaDive;
import net.scubadive.creature.temperate.OceanSunfish;

public class OceanSunfishRenderer extends MobRenderer<OceanSunfish, OceanSunfishRenderState, OceanSunfishModel> {
	private static final Identifier TEXTURE = ScubaDive.id("textures/entity/ocean_sunfish.png");
	/** Model is about 48 px from fin tip to fin tip: about 2.25 blocks at this scale. */
	private static final float MODEL_SCALE = 0.75f;
	/** Height of the body centre above the entity origin (24 px * scale / 16). */
	private static final float CENTRE = 24.0f * MODEL_SCALE / 16.0f;

	public OceanSunfishRenderer(EntityRendererProvider.Context context) {
		super(context, new OceanSunfishModel(context.bakeLayer(OceanSunfishModel.LAYER)), 0.6f);
	}

	@Override
	public OceanSunfishRenderState createRenderState() {
		return new OceanSunfishRenderState();
	}

	@Override
	public void extractRenderState(OceanSunfish entity, OceanSunfishRenderState state, float partialTick) {
		super.extractRenderState(entity, state, partialTick);
		state.roll = entity.isInWater() ? entity.getRoll(partialTick) : 1.0f;
		state.phase = entity.getId() * 7.0f;
	}

	@Override
	public Identifier getTextureLocation(OceanSunfishRenderState state) {
		return TEXTURE;
	}

	@Override
	protected void scale(OceanSunfishRenderState state, PoseStack poseStack) {
		super.scale(state, poseStack);
		poseStack.scale(MODEL_SCALE, MODEL_SCALE, MODEL_SCALE);
	}

	@Override
	protected void setupRotations(OceanSunfishRenderState state, PoseStack poseStack, float bodyRot, float scale) {
		super.setupRotations(state, poseStack, bodyRot, scale);
		// Pitch and the basking roll turn about the body centre, not the feet.
		poseStack.translate(0.0f, CENTRE, 0.0f);
		poseStack.mulPose(Axis.XP.rotationDegrees(-state.xRot));
		poseStack.mulPose(Axis.ZP.rotationDegrees(state.roll * 80.0f));
		poseStack.translate(0.0f, -CENTRE, 0.0f);
	}
}
