package net.scubadive.client.creature.critter;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Mob;

/**
 * A critter with a faint, steady glow layer (additive, like spider eyes) on top of its normally lit body.
 * Unlike {@link GlowCritterRenderer} the body is not full-bright and the glow keeps its texture colours.
 */
public class SoftGlowRenderer<T extends Mob> extends CritterRenderer<T> {
	public SoftGlowRenderer(EntityRendererProvider.Context context, EntityModel<CritterRenderState> model, float shadow,
			float modelScale, Identifier texture, Identifier glow) {
		super(context, model, shadow, modelScale, texture);
		this.addLayer(new Glow(this, glow));
	}

	/** Draws the glow texture with a slow, gentle brightness breathing. */
	static class Glow extends RenderLayer<CritterRenderState, EntityModel<CritterRenderState>> {
		private final Identifier glow;

		Glow(RenderLayerParent<CritterRenderState, EntityModel<CritterRenderState>> parent, Identifier glow) {
			super(parent);
			this.glow = glow;
		}

		@Override
		public void submit(PoseStack poseStack, SubmitNodeCollector nodes, int light, CritterRenderState state, float yRot, float xRot) {
			if (state.isInvisible) {
				return;
			}
			int v = 200 + (int) (Math.sin(state.ageInTicks * 0.05f) * 55.0f);
			int tint = 0xFF000000 | (v << 16) | (v << 8) | v;
			nodes.submitModel(this.getParentModel(), state, poseStack, RenderTypes.eyes(this.glow), light, OverlayTexture.NO_OVERLAY,
					tint, null, state.outlineColor, null);
		}
	}
}
