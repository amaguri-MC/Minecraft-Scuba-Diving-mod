package net.scubadive.client.creature.vanilla;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.fish.Cod;
import net.scubadive.ScubaDive;

/** Renewed look for the vanilla cod (Pacific cod); the cod itself is untouched. */
public class VanillaCodRenderer extends MobRenderer<Cod, VanillaCodRenderState, VanillaFishModel<VanillaCodRenderState>> {
	private static final Identifier TEXTURE = ScubaDive.id("textures/entity/vanilla/cod.png");

	public VanillaCodRenderer(EntityRendererProvider.Context context) {
		super(context, new VanillaFishModel<>(context.bakeLayer(VanillaFishModel.COD), s -> s.phase, 0.45f), 0.3f);
	}

	@Override
	public VanillaCodRenderState createRenderState() {
		return new VanillaCodRenderState();
	}

	@Override
	public void extractRenderState(Cod entity, VanillaCodRenderState state, float partialTick) {
		super.extractRenderState(entity, state, partialTick);
		state.phase = entity.getId() * 7.0f;
	}

	@Override
	public Identifier getTextureLocation(VanillaCodRenderState state) {
		return TEXTURE;
	}

	@Override
	protected void setupRotations(VanillaCodRenderState state, PoseStack poseStack, float bodyRot, float scale) {
		super.setupRotations(state, poseStack, bodyRot, scale);
		poseStack.mulPose(Axis.YP.rotationDegrees(3.0f * Mth.sin(0.45f * (state.ageInTicks + state.phase))));
		if (!state.isInWater) {
			// Stranded: lies on its side and flops, like vanilla.
			poseStack.translate(0.1f, 0.1f, -0.1f);
			poseStack.mulPose(Axis.ZP.rotationDegrees(90.0f));
		}
	}
}
