package net.scubadive.client.creature;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;
import net.scubadive.creature.Nudibranch;

public class NudibranchRenderer extends MobRenderer<Nudibranch, NudibranchRenderState, NudibranchModel> {
	public NudibranchRenderer(EntityRendererProvider.Context context) {
		super(context, new NudibranchModel(context.bakeLayer(NudibranchModel.LAYER)), 0.1f);
	}

	@Override
	public NudibranchRenderState createRenderState() {
		return new NudibranchRenderState();
	}

	@Override
	public void extractRenderState(Nudibranch entity, NudibranchRenderState state, float partialTick) {
		super.extractRenderState(entity, state, partialTick);
		state.variant = entity.getVariant();
	}

	@Override
	public Identifier getTextureLocation(NudibranchRenderState state) {
		return NudibranchModel.texture(state.variant);
	}
}
