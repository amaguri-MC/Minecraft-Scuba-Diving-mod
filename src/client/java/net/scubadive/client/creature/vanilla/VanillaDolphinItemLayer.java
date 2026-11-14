package net.scubadive.client.creature.vanilla;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;

/** The item a dolphin carries, held at the tip of the renewed beak (follows the body pitch). */
public class VanillaDolphinItemLayer extends RenderLayer<VanillaDolphinRenderState, VanillaDolphinModel> {
	public VanillaDolphinItemLayer(RenderLayerParent<VanillaDolphinRenderState, VanillaDolphinModel> parent) {
		super(parent);
	}

	@Override
	public void submit(PoseStack poseStack, SubmitNodeCollector nodes, int light, VanillaDolphinRenderState state, float yRot, float xRot) {
		if (state.heldItem.isEmpty()) {
			return;
		}
		poseStack.pushPose();
		VanillaDolphinModel model = this.getParentModel();
		model.root().translateAndRotate(poseStack);
		model.body().translateAndRotate(poseStack);
		poseStack.translate(0.0f, VanillaDolphinModel.MOUTH_Y / 16.0f, VanillaDolphinModel.MOUTH_Z / 16.0f);
		state.heldItem.submit(poseStack, nodes, light, OverlayTexture.NO_OVERLAY, state.outlineColor);
		poseStack.popPose();
	}
}
