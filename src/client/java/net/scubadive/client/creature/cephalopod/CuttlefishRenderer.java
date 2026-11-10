package net.scubadive.client.creature.cephalopod;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;

import net.scubadive.ScubaDive;
import net.scubadive.creature.cephalopod.Cuttlefish;

/**
 * Cuttlefish: the skin pattern drifts through four frames (a dark "passing cloud" band sweeping over the
 * mottled body) and switches to the zebra stripes during the warning display.
 */
public class CuttlefishRenderer extends MobRenderer<Cuttlefish, CephalopodRenderState, CuttlefishModel> {
	private static final int FRAMES = 4;
	/** Ticks per pattern frame. */
	private static final float FRAME_TICKS = 7.0f;
	private static final Identifier[] PATTERN = new Identifier[FRAMES];
	private static final Identifier ZEBRA = ScubaDive.id("textures/entity/cuttlefish_zebra.png");
	/** The model is about 17 px long; drawn at this scale it is about 0.6 blocks long. */
	private static final float MODEL_SCALE = 0.55f;

	static {
		for (int i = 0; i < FRAMES; i++) {
			PATTERN[i] = ScubaDive.id("textures/entity/cuttlefish_" + i + ".png");
		}
	}

	public CuttlefishRenderer(EntityRendererProvider.Context context) {
		super(context, new CuttlefishModel(context.bakeLayer(CuttlefishModel.LAYER)), 0.18f);
	}

	@Override
	public CephalopodRenderState createRenderState() {
		return new CephalopodRenderState();
	}

	@Override
	public void extractRenderState(Cuttlefish entity, CephalopodRenderState state, float partialTick) {
		super.extractRenderState(entity, state, partialTick);
		state.phase = entity.getId() * 11.0f;
		state.jetting = entity.isJetting();
		state.display = entity.isDisplaying();
	}

	@Override
	public Identifier getTextureLocation(CephalopodRenderState state) {
		if (state.display) {
			return ZEBRA;
		}
		// Frame 0 is the plain mottle; frames 1-3 sweep a dark band head to tail, then it rests on frame 0.
		int cycle = (int) ((state.ageInTicks + state.phase) / FRAME_TICKS) % (FRAMES + 2);
		return PATTERN[cycle < FRAMES ? cycle : 0];
	}

	@Override
	protected void scale(CephalopodRenderState state, PoseStack poseStack) {
		super.scale(state, poseStack);
		poseStack.scale(MODEL_SCALE, MODEL_SCALE, MODEL_SCALE);
	}
}
