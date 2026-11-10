package net.scubadive.client.creature.reef;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Mob;
import net.scubadive.ScubaDive;

/** One renderer for every reef fish: species differ by model shape, texture and scale. */
public class ReefFishRenderer<T extends Mob> extends MobRenderer<T, ReefFishRenderState, ReefFishModel> {
	private final Identifier texture;
	private final float modelScale;

	public ReefFishRenderer(EntityRendererProvider.Context context, ModelLayerLocation layer, String id, float modelScale,
			float tailSpeed, float flapSpeed, float shadow) {
		super(context, new ReefFishModel(context.bakeLayer(layer), tailSpeed, flapSpeed), shadow);
		this.texture = ScubaDive.id("textures/entity/" + id + ".png");
		this.modelScale = modelScale;
	}

	@Override
	public ReefFishRenderState createRenderState() {
		return new ReefFishRenderState();
	}

	@Override
	public Identifier getTextureLocation(ReefFishRenderState state) {
		return this.texture;
	}

	/** The models are built on a coarse grid and drawn smaller so the fish are tiny in the world. */
	@Override
	protected void scale(ReefFishRenderState state, PoseStack poseStack) {
		super.scale(state, poseStack);
		poseStack.scale(this.modelScale, this.modelScale, this.modelScale);
	}

	@Override
	protected void setupRotations(ReefFishRenderState state, PoseStack poseStack, float bodyRot, float scale) {
		super.setupRotations(state, poseStack, bodyRot, scale);
		if (!state.isInWater) {
			// Stranded fish lie on their side.
			poseStack.translate(0.04f, 0.04f, -0.04f);
			poseStack.mulPose(Axis.ZP.rotationDegrees(90.0f));
		}
	}
}
