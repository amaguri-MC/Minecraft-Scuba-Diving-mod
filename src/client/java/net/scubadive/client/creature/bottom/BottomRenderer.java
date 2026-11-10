package net.scubadive.client.creature.bottom;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.function.Function;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Mob;
import net.scubadive.creature.bottom.Frogfish;
import net.scubadive.creature.bottom.MorayEel;

/** One renderer for all bottom dwellers; each gets its own model, texture and draw scale. */
public class BottomRenderer<T extends Mob> extends MobRenderer<T, BottomRenderState, EntityModel<BottomRenderState>> {
	private final Function<BottomRenderState, Identifier> texture;
	private final float modelScale;

	public BottomRenderer(EntityRendererProvider.Context context, EntityModel<BottomRenderState> model, float shadow,
			float modelScale, Function<BottomRenderState, Identifier> texture) {
		super(context, model, shadow);
		this.modelScale = modelScale;
		this.texture = texture;
	}

	@Override
	public BottomRenderState createRenderState() {
		return new BottomRenderState();
	}

	@Override
	public void extractRenderState(T entity, BottomRenderState state, float partialTick) {
		super.extractRenderState(entity, state, partialTick);
		if (entity instanceof Frogfish frogfish) {
			state.variant = frogfish.getVariant();
		}
		if (entity instanceof MorayEel eel) {
			state.agitated = eel.isAngry();
		}
	}

	@Override
	public Identifier getTextureLocation(BottomRenderState state) {
		return this.texture.apply(state);
	}

	/** The model is drawn smaller than its pixel grid so the creature is tiny in the world. */
	@Override
	protected void scale(BottomRenderState state, PoseStack poseStack) {
		super.scale(state, poseStack);
		poseStack.scale(this.modelScale, this.modelScale, this.modelScale);
	}
}
