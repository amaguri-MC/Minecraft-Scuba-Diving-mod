package net.scubadive.client.creature;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;
import net.scubadive.ScubaDive;
import net.scubadive.creature.PygmySeahorse;

public class PygmySeahorseRenderer extends MobRenderer<PygmySeahorse, PygmySeahorseRenderState, PygmySeahorseModel> {
	private static final Identifier TEXTURE = ScubaDive.id("textures/entity/pygmy_seahorse.png");

	public PygmySeahorseRenderer(EntityRendererProvider.Context context) {
		super(context, new PygmySeahorseModel(context.bakeLayer(PygmySeahorseModel.LAYER)), 0.05f);
	}

	@Override
	public PygmySeahorseRenderState createRenderState() {
		return new PygmySeahorseRenderState();
	}

	@Override
	public Identifier getTextureLocation(PygmySeahorseRenderState state) {
		return TEXTURE;
	}
}
