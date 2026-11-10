package net.scubadive.client.tour;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Mob;

/** Renders the dive center NPCs with the plain humanoid model and one fixed skin each. */
public class DiveCenterNpcRenderer<T extends Mob> extends HumanoidMobRenderer<T, HumanoidRenderState, HumanoidModel<HumanoidRenderState>> {
	private final Identifier texture;

	public DiveCenterNpcRenderer(EntityRendererProvider.Context context, Identifier texture) {
		super(context, new HumanoidModel<HumanoidRenderState>(context.bakeLayer(DiveCenterClient.NPC_LAYER)), 0.5f);
		this.texture = texture;
	}

	@Override
	public HumanoidRenderState createRenderState() {
		return new HumanoidRenderState();
	}

	@Override
	public Identifier getTextureLocation(HumanoidRenderState state) {
		return this.texture;
	}
}
