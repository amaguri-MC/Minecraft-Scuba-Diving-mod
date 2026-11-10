package net.scubadive.client.creature.big;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Mob;
import net.scubadive.ScubaDive;

/** One renderer for all big fish; species differ by layer, texture, scale and swim animation. */
public class BigFishRenderer<T extends Mob> extends MobRenderer<T, BigFishRenderState, BigFishModel> {
	private final Identifier texture;
	private final float modelScale;

	public BigFishRenderer(EntityRendererProvider.Context context, ModelLayerLocation layer, String textureName,
			float modelScale, float shadow, float animSpeed, float animAmp) {
		super(context, new BigFishModel(context.bakeLayer(layer), animSpeed, animAmp), shadow);
		this.texture = ScubaDive.id("textures/entity/" + textureName + ".png");
		this.modelScale = modelScale;
	}

	@Override
	public BigFishRenderState createRenderState() {
		return new BigFishRenderState();
	}

	@Override
	public Identifier getTextureLocation(BigFishRenderState state) {
		return this.texture;
	}

	@Override
	protected void scale(BigFishRenderState state, PoseStack poseStack) {
		super.scale(state, poseStack);
		poseStack.scale(this.modelScale, this.modelScale, this.modelScale);
	}
}
