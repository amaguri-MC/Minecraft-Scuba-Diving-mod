package net.scubadive.client.creature.bottom;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;
import net.scubadive.creature.bottom.Octopus;

/** Octopus renderer: picks the camouflage texture from the synced camo index. */
public class OctopusRenderer extends MobRenderer<Octopus, OctopusRenderState, OctopusModel> {
	private static final float MODEL_SCALE = 0.55f;
	private final Identifier[] textures;

	public OctopusRenderer(EntityRendererProvider.Context context, OctopusModel model, Identifier[] textures) {
		super(context, model, 0.3f);
		this.textures = textures;
	}

	@Override
	public OctopusRenderState createRenderState() {
		return new OctopusRenderState();
	}

	@Override
	public void extractRenderState(Octopus entity, OctopusRenderState state, float partialTick) {
		super.extractRenderState(entity, state, partialTick);
		state.camo = entity.getCamo();
		state.jetting = entity.isJetting();
	}

	@Override
	public Identifier getTextureLocation(OctopusRenderState state) {
		return this.textures[Math.floorMod(state.camo, this.textures.length)];
	}

	@Override
	protected void scale(OctopusRenderState state, PoseStack poseStack) {
		super.scale(state, poseStack);
		poseStack.scale(MODEL_SCALE, MODEL_SCALE, MODEL_SCALE);
	}
}
