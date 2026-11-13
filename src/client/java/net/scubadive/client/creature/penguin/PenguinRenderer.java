package net.scubadive.client.creature.penguin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.scubadive.ScubaDive;
import net.scubadive.creature.penguin.Penguin;

/** One renderer for the three penguins; the model is built on a 1/4 px grid, so it is drawn at 0.25 x size. */
public class PenguinRenderer extends MobRenderer<Penguin, PenguinRenderState, PenguinModel> {
	private final Identifier texture;
	private final float modelScale;

	public PenguinRenderer(EntityRendererProvider.Context context, ModelLayerLocation layer, String texture, float size) {
		super(context, new PenguinModel(context.bakeLayer(layer)), 0.25f * size);
		this.texture = ScubaDive.id("textures/entity/" + texture + ".png");
		this.modelScale = 0.25f * size;
	}

	@Override
	public PenguinRenderState createRenderState() {
		return new PenguinRenderState();
	}

	@Override
	public void extractRenderState(Penguin entity, PenguinRenderState state, float partialTick) {
		super.extractRenderState(entity, state, partialTick);
		state.swimAmount = Mth.lerp(partialTick, entity.swimAmountO, entity.swimAmount);
		state.phase = (entity.getId() * 37) % 260;
	}

	@Override
	public Identifier getTextureLocation(PenguinRenderState state) {
		return this.texture;
	}

	@Override
	protected void scale(PenguinRenderState state, PoseStack poseStack) {
		super.scale(state, poseStack);
		// The feet sit at the model's y = 24 (part "base"), which stays on the ground at any scale.
		poseStack.scale(this.modelScale, this.modelScale, this.modelScale);
	}
}
