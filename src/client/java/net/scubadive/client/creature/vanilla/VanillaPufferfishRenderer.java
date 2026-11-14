package net.scubadive.client.creature.vanilla;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.fish.Pufferfish;
import net.scubadive.ScubaDive;

/** Renewed look for the vanilla pufferfish: like vanilla, one model per puff state, swapped before drawing. */
public class VanillaPufferfishRenderer extends MobRenderer<Pufferfish, VanillaPufferfishRenderState, VanillaPufferfishModel> {
	private static final Identifier[] TEXTURES = {
			ScubaDive.id("textures/entity/vanilla/pufferfish_small.png"),
			ScubaDive.id("textures/entity/vanilla/pufferfish_mid.png"),
			ScubaDive.id("textures/entity/vanilla/pufferfish_big.png")};
	private final VanillaPufferfishModel small;
	private final VanillaPufferfishModel mid;
	private final VanillaPufferfishModel big;

	public VanillaPufferfishRenderer(EntityRendererProvider.Context context) {
		super(context, new VanillaPufferfishModel(context.bakeLayer(VanillaPufferfishModel.SMALL)), 0.2f);
		this.small = this.model;
		this.mid = new VanillaPufferfishModel(context.bakeLayer(VanillaPufferfishModel.MID));
		this.big = new VanillaPufferfishModel(context.bakeLayer(VanillaPufferfishModel.BIG));
	}

	@Override
	public VanillaPufferfishRenderState createRenderState() {
		return new VanillaPufferfishRenderState();
	}

	@Override
	public void extractRenderState(Pufferfish entity, VanillaPufferfishRenderState state, float partialTick) {
		super.extractRenderState(entity, state, partialTick);
		state.puffState = entity.getPuffState();
		state.phase = entity.getId() * 7.0f;
	}

	@Override
	public void submit(VanillaPufferfishRenderState state, PoseStack poseStack, SubmitNodeCollector nodes, CameraRenderState camera) {
		this.model = switch (state.puffState) {
			case 0 -> this.small;
			case 1 -> this.mid;
			default -> this.big;
		};
		super.submit(state, poseStack, nodes, camera);
	}

	@Override
	public Identifier getTextureLocation(VanillaPufferfishRenderState state) {
		return TEXTURES[Mth.clamp(state.puffState, 0, 2)];
	}

	@Override
	protected float getShadowRadius(VanillaPufferfishRenderState state) {
		return 0.1f + 0.1f * state.puffState;
	}

	@Override
	protected void setupRotations(VanillaPufferfishRenderState state, PoseStack poseStack, float bodyRot, float scale) {
		// Same gentle hover bob as vanilla.
		poseStack.translate(0.0f, Mth.cos(state.ageInTicks * 0.05f) * 0.08f, 0.0f);
		super.setupRotations(state, poseStack, bodyRot, scale);
	}
}
