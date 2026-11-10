package net.scubadive.client.creature.reef;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Mob;
import net.scubadive.ScubaDive;

/** Renderer for the dedicated lionfish model (roughly half a block long, fans and spines included). */
public class LionfishRenderer<T extends Mob> extends MobRenderer<T, ReefFishRenderState, LionfishModel> {
	private static final Identifier TEXTURE = ScubaDive.id("textures/entity/lionfish.png");
	private static final float MODEL_SCALE = 0.24f;

	public LionfishRenderer(EntityRendererProvider.Context context) {
		super(context, new LionfishModel(context.bakeLayer(LionfishModel.LAYER)), 0.2f);
	}

	@Override
	public ReefFishRenderState createRenderState() {
		return new ReefFishRenderState();
	}

	@Override
	public Identifier getTextureLocation(ReefFishRenderState state) {
		return TEXTURE;
	}

	/** The model is built on a double-resolution grid and drawn smaller. */
	@Override
	protected void scale(ReefFishRenderState state, PoseStack poseStack) {
		super.scale(state, poseStack);
		poseStack.scale(MODEL_SCALE, MODEL_SCALE, MODEL_SCALE);
	}

	@Override
	protected void setupRotations(ReefFishRenderState state, PoseStack poseStack, float bodyRot, float scale) {
		super.setupRotations(state, poseStack, bodyRot, scale);
		if (!state.isInWater) {
			// Stranded fish lie on their side.
			poseStack.translate(0.1f, 0.1f, -0.1f);
			poseStack.mulPose(Axis.ZP.rotationDegrees(90.0f));
		}
	}
}
