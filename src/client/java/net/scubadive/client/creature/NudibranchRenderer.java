package net.scubadive.client.creature;

import java.util.function.IntFunction;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;
import net.scubadive.creature.Nudibranch;
import net.scubadive.creature.SpanishDancer;

/** Renderer shared by every sea slug species: its own model, texture(s) and draw scale. */
public class NudibranchRenderer extends MobRenderer<Nudibranch, NudibranchRenderState, EntityModel<NudibranchRenderState>> {
	/** The original model is drawn smaller than its pixel grid so the creature is tiny in the world. */
	private static final float MODEL_SCALE = 0.4f;

	private final IntFunction<Identifier> textures;
	private final float modelScale;

	/** The original nudibranch (three colour variants). */
	public NudibranchRenderer(EntityRendererProvider.Context context) {
		this(context, new NudibranchModel(context.bakeLayer(NudibranchModel.LAYER)), NudibranchModel::texture, MODEL_SCALE, 0.04f);
	}

	public NudibranchRenderer(EntityRendererProvider.Context context, EntityModel<NudibranchRenderState> model,
			IntFunction<Identifier> textures, float modelScale, float shadow) {
		super(context, model, shadow);
		this.textures = textures;
		this.modelScale = modelScale;
	}

	@Override
	public NudibranchRenderState createRenderState() {
		return new NudibranchRenderState();
	}

	@Override
	public void extractRenderState(Nudibranch entity, NudibranchRenderState state, float partialTick) {
		super.extractRenderState(entity, state, partialTick);
		state.variant = entity.getVariant();
		state.dancing = entity instanceof SpanishDancer dancer && dancer.isDancing();
	}

	@Override
	public Identifier getTextureLocation(NudibranchRenderState state) {
		return this.textures.apply(state.variant);
	}

	@Override
	protected void scale(NudibranchRenderState state, PoseStack poseStack) {
		super.scale(state, poseStack);
		poseStack.scale(this.modelScale, this.modelScale, this.modelScale);
	}
}
