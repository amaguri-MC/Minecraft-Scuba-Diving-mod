package net.scubadive.client.creature.vanilla;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.squid.Squid;
import net.scubadive.ScubaDive;

/**
 * Renewed look for the vanilla squid (the glow squid keeps its own renderer). The vanilla squid swims mantle first
 * and tilts with xBodyRot / spins with zBodyRot about its middle; the renewed model lies along z (arms at -z), so it
 * is turned to stand on that same axis: mantle along the vanilla "up" of the squid.
 */
public class VanillaSquidRenderer extends MobRenderer<Squid, VanillaSquidRenderState, VanillaSquidModel> {
	private static final Identifier TEXTURE = ScubaDive.id("textures/entity/vanilla/squid.png");
	/** Model body centre above the entity origin and its z, in blocks, at scale 1 (see LivingEntityRenderer). */
	private static final float CENTRE_UP = 1.501f - SquidGeometry.CENTRE_Y / 16.0f;
	private static final float CENTRE_Z = SquidGeometry.CENTRE_Z / 16.0f;

	public VanillaSquidRenderer(EntityRendererProvider.Context context) {
		super(context, new VanillaSquidModel(context.bakeLayer(VanillaSquidModel.LAYER)), 0.7f);
	}

	@Override
	public VanillaSquidRenderState createRenderState() {
		return new VanillaSquidRenderState();
	}

	@Override
	public void extractRenderState(Squid entity, VanillaSquidRenderState state, float partialTick) {
		super.extractRenderState(entity, state, partialTick);
		state.tentacleAngle = Mth.lerp(partialTick, entity.oldTentacleAngle, entity.tentacleAngle);
		state.xBodyRot = Mth.lerp(partialTick, entity.xBodyRotO, entity.xBodyRot);
		state.zBodyRot = Mth.lerp(partialTick, entity.zBodyRotO, entity.zBodyRot);
		state.phase = entity.getId() * 7.0f;
	}

	@Override
	public Identifier getTextureLocation(VanillaSquidRenderState state) {
		return TEXTURE;
	}

	@Override
	protected void scale(VanillaSquidRenderState state, PoseStack poseStack) {
		super.scale(state, poseStack);
		poseStack.scale(state.ageScale, state.ageScale, state.ageScale);
	}

	@Override
	protected float getShadowRadius(VanillaSquidRenderState state) {
		return super.getShadowRadius(state) * state.ageScale;
	}

	@Override
	protected void setupRotations(VanillaSquidRenderState state, PoseStack poseStack, float bodyRot, float scale) {
		float s = state.ageScale;
		poseStack.translate(0.0f, state.boundingBoxHeight * 0.5f, 0.0f);
		poseStack.mulPose(Axis.YP.rotationDegrees(180.0f - bodyRot));
		poseStack.mulPose(Axis.XP.rotationDegrees(state.xBodyRot));
		poseStack.mulPose(Axis.YP.rotationDegrees(state.zBodyRot));
		// Model +z (mantle) onto the squid's own up axis, then centre the body on the pivot.
		poseStack.mulPose(Axis.XP.rotationDegrees(-90.0f));
		poseStack.translate(0.0f, -CENTRE_UP * s, -CENTRE_Z * s);
	}
}
