package net.scubadive.client.creature.critter;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Mob;

/** One renderer for sea stars, urchins and jellyfish: own model, texture and draw scale each. */
public class CritterRenderer<T extends Mob> extends MobRenderer<T, CritterRenderState, EntityModel<CritterRenderState>> {
	private final Identifier texture;
	private final float modelScale;

	public CritterRenderer(EntityRendererProvider.Context context, EntityModel<CritterRenderState> model, float shadow,
			float modelScale, Identifier texture) {
		super(context, model, shadow);
		this.modelScale = modelScale;
		this.texture = texture;
	}

	@Override
	public CritterRenderState createRenderState() {
		return new CritterRenderState();
	}

	@Override
	public Identifier getTextureLocation(CritterRenderState state) {
		return this.texture;
	}

	@Override
	protected void scale(CritterRenderState state, PoseStack poseStack) {
		super.scale(state, poseStack);
		poseStack.scale(this.modelScale, this.modelScale, this.modelScale);
	}
}
