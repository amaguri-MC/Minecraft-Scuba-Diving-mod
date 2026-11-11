package net.scubadive.client.creature.reef;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;
import net.scubadive.ScubaDive;
import net.scubadive.creature.bottom.GardenEel;

/** Garden eel renderer: the model slides into the sand block below as the eel retracts. */
public class GardenEelRenderer extends MobRenderer<GardenEel, GardenEelRenderState, GardenEelModel> {
	private static final Identifier TEXTURE = ScubaDive.id("textures/entity/garden_eel.png");
	private static final float MODEL_SCALE = 0.45f;

	public GardenEelRenderer(EntityRendererProvider.Context context) {
		super(context, new GardenEelModel(context.bakeLayer(GardenEelModel.LAYER)), 0.0f);
	}

	@Override
	public GardenEelRenderState createRenderState() {
		return new GardenEelRenderState();
	}

	@Override
	public void extractRenderState(GardenEel entity, GardenEelRenderState state, float partialTick) {
		super.extractRenderState(entity, state, partialTick);
		state.retract = entity.getRetract(partialTick);
		// Neighbours sway out of step with each other.
		state.phase = Math.floorMod(entity.getId() * 37, 200);
	}

	@Override
	public Identifier getTextureLocation(GardenEelRenderState state) {
		return TEXTURE;
	}

	@Override
	protected void scale(GardenEelRenderState state, PoseStack poseStack) {
		super.scale(state, poseStack);
		poseStack.scale(MODEL_SCALE, MODEL_SCALE, MODEL_SCALE);
	}
}
