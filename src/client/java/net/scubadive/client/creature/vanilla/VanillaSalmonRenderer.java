package net.scubadive.client.creature.vanilla;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.fish.Salmon;
import net.scubadive.ScubaDive;

/** Renewed look for the vanilla salmon (chum salmon, silver sea phase); the small / medium / large variants are scaled. */
public class VanillaSalmonRenderer extends MobRenderer<Salmon, VanillaSalmonRenderState, VanillaFishModel<VanillaSalmonRenderState>> {
	private static final Identifier TEXTURE = ScubaDive.id("textures/entity/vanilla/salmon.png");

	public VanillaSalmonRenderer(EntityRendererProvider.Context context) {
		super(context, new VanillaFishModel<>(context.bakeLayer(VanillaFishModel.SALMON), s -> s.phase, 0.4f), 0.4f);
	}

	@Override
	public VanillaSalmonRenderState createRenderState() {
		return new VanillaSalmonRenderState();
	}

	@Override
	public void extractRenderState(Salmon entity, VanillaSalmonRenderState state, float partialTick) {
		super.extractRenderState(entity, state, partialTick);
		state.variant = entity.getVariant();
		state.phase = entity.getId() * 7.0f;
	}

	@Override
	public Identifier getTextureLocation(VanillaSalmonRenderState state) {
		return TEXTURE;
	}

	private static float sizeScale(VanillaSalmonRenderState state) {
		if (state.variant == null) return 1.0f;
		return switch (state.variant) {
			case SMALL -> 0.6f;
			case LARGE -> 1.4f;
			default -> 1.0f;
		};
	}

	@Override
	protected void scale(VanillaSalmonRenderState state, PoseStack poseStack) {
		super.scale(state, poseStack);
		float s = sizeScale(state);
		poseStack.scale(s, s, s);
	}

	@Override
	protected float getShadowRadius(VanillaSalmonRenderState state) {
		return super.getShadowRadius(state) * sizeScale(state);
	}

	@Override
	protected void setupRotations(VanillaSalmonRenderState state, PoseStack poseStack, float bodyRot, float scale) {
		super.setupRotations(state, poseStack, bodyRot, scale);
		poseStack.mulPose(Axis.YP.rotationDegrees(3.0f * Mth.sin(0.4f * (state.ageInTicks + state.phase))));
		if (!state.isInWater) {
			poseStack.translate(0.2f, 0.1f, 0.0f);
			poseStack.mulPose(Axis.ZP.rotationDegrees(90.0f));
		}
	}
}
