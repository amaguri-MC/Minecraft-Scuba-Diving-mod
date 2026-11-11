package net.scubadive.client.creature.critter;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Mob;

/** A critter that lights itself (full block light) and carries a shimmering glow layer. */
public class GlowCritterRenderer<T extends Mob> extends CritterRenderer<T> {
	public GlowCritterRenderer(EntityRendererProvider.Context context, EntityModel<CritterRenderState> model, float shadow,
			float modelScale, Identifier texture, Identifier glow) {
		super(context, model, shadow, modelScale, texture);
		this.addLayer(new ShimmerLayer(this, glow));
	}

	@Override
	protected int getBlockLightLevel(T entity, BlockPos pos) {
		return 15;
	}
}
