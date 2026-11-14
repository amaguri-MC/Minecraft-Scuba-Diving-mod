package net.scubadive.client.creature.vanilla;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.scubadive.ScubaDive;

/** Saddle and armour of the renewed nautilus, on models fitted to the coiled shell. */
public class VanillaNautilusGearLayer extends RenderLayer<VanillaNautilusRenderState, VanillaNautilusModel> {
	private static final Identifier SADDLE_TEXTURE = ScubaDive.id("textures/entity/vanilla/nautilus_saddle.png");
	private final VanillaNautilusGearModel saddle;
	private final VanillaNautilusGearModel armor;

	public VanillaNautilusGearLayer(RenderLayerParent<VanillaNautilusRenderState, VanillaNautilusModel> parent,
			VanillaNautilusGearModel saddle, VanillaNautilusGearModel armor) {
		super(parent);
		this.saddle = saddle;
		this.armor = armor;
	}

	@Override
	public void submit(PoseStack poseStack, SubmitNodeCollector nodes, int light, VanillaNautilusRenderState state, float yRot, float xRot) {
		if (state.isInvisible) {
			return;
		}
		int overlay = LivingEntityRenderer.getOverlayCoords(state, 0.0f);
		if (state.armorTexture != null) {
			nodes.submitModel(this.armor, state, poseStack, RenderTypes.entityCutout(state.armorTexture), light, overlay, -1, null,
					state.outlineColor, null);
		}
		if (state.hasSaddle) {
			nodes.submitModel(this.saddle, state, poseStack, RenderTypes.entityCutout(SADDLE_TEXTURE), light, overlay, -1, null,
					state.outlineColor, null);
		}
	}
}
