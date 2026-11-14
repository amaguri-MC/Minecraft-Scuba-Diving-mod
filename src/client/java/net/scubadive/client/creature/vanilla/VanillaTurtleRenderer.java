package net.scubadive.client.creature.vanilla;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.animal.turtle.Turtle;
import net.scubadive.ScubaDive;

/** Renewed look for the vanilla turtle; the turtle itself (AI, eggs, babies) is untouched. */
public class VanillaTurtleRenderer extends MobRenderer<Turtle, VanillaTurtleRenderState, VanillaTurtleModel> {
	private static final Identifier TEXTURE = ScubaDive.id("textures/entity/vanilla/turtle.png");

	public VanillaTurtleRenderer(EntityRendererProvider.Context context) {
		super(context, new VanillaTurtleModel(context.bakeLayer(VanillaTurtleModel.LAYER)), 0.7f);
	}

	@Override
	public VanillaTurtleRenderState createRenderState() {
		return new VanillaTurtleRenderState();
	}

	@Override
	public void extractRenderState(Turtle entity, VanillaTurtleRenderState state, float partialTick) {
		super.extractRenderState(entity, state, partialTick);
		state.isOnLand = !entity.isInWater() && entity.onGround();
		state.isLayingEgg = entity.isLayingEgg();
		state.hasEgg = !entity.isBaby() && entity.hasEgg();
		state.phase = entity.getId() * 7.0f;
	}

	@Override
	public Identifier getTextureLocation(VanillaTurtleRenderState state) {
		return TEXTURE;
	}

	@Override
	protected void scale(VanillaTurtleRenderState state, PoseStack poseStack) {
		super.scale(state, poseStack);
		// One model for all ages: hatchlings are the adult shrunk to the vanilla age scale.
		poseStack.scale(state.ageScale, state.ageScale, state.ageScale);
	}

	@Override
	protected float getShadowRadius(VanillaTurtleRenderState state) {
		return super.getShadowRadius(state) * state.ageScale;
	}
}
