package net.scubadive.client.creature.critter;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

/**
 * Glowing overlay drawn additively (like spider eyes) from a glow texture, tinted with a slowly cycling hue so the
 * rainbow comb rows of the comb jelly shimmer through the colours.
 */
public class ShimmerLayer extends RenderLayer<CritterRenderState, EntityModel<CritterRenderState>> {
	private final Identifier glow;

	public ShimmerLayer(RenderLayerParent<CritterRenderState, EntityModel<CritterRenderState>> parent, Identifier glow) {
		super(parent);
		this.glow = glow;
	}

	@Override
	public void submit(PoseStack poseStack, SubmitNodeCollector nodes, int light, CritterRenderState state, float yRot, float xRot) {
		if (state.isInvisible) {
			return;
		}
		float hue = (state.ageInTicks * 0.012f) % 1.0f;
		int tint = Mth.hsvToArgb(hue, 0.45f, 1.0f, 255);
		nodes.submitModel(this.getParentModel(), state, poseStack, RenderTypes.eyes(this.glow), light, OverlayTexture.NO_OVERLAY,
				tint, null, state.outlineColor, null);
	}
}
