package net.scubadive.client.creature.vanilla;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.HoldingEntityRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.animal.dolphin.Dolphin;
import net.scubadive.ScubaDive;

/** Renewed look for the vanilla dolphin; it still carries items in its mouth like the vanilla one. */
public class VanillaDolphinRenderer extends MobRenderer<Dolphin, VanillaDolphinRenderState, VanillaDolphinModel> {
	private static final Identifier TEXTURE = ScubaDive.id("textures/entity/vanilla/dolphin.png");

	public VanillaDolphinRenderer(EntityRendererProvider.Context context) {
		super(context, new VanillaDolphinModel(context.bakeLayer(VanillaDolphinModel.LAYER)), 0.7f);
		this.addLayer(new VanillaDolphinItemLayer(this));
	}

	@Override
	public VanillaDolphinRenderState createRenderState() {
		return new VanillaDolphinRenderState();
	}

	@Override
	public void extractRenderState(Dolphin entity, VanillaDolphinRenderState state, float partialTick) {
		super.extractRenderState(entity, state, partialTick);
		HoldingEntityRenderState.extractHoldingEntityRenderState(entity, state, this.itemModelResolver);
		state.isMoving = entity.getDeltaMovement().horizontalDistanceSqr() > 1.0E-7;
		state.phase = entity.getId() * 7.0f;
	}

	@Override
	public Identifier getTextureLocation(VanillaDolphinRenderState state) {
		return TEXTURE;
	}

	@Override
	protected void scale(VanillaDolphinRenderState state, PoseStack poseStack) {
		super.scale(state, poseStack);
		poseStack.scale(state.ageScale, state.ageScale, state.ageScale);
	}

	@Override
	protected float getShadowRadius(VanillaDolphinRenderState state) {
		return super.getShadowRadius(state) * state.ageScale;
	}
}
