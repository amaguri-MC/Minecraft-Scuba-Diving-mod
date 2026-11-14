package net.scubadive.client.creature.vanilla;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.util.Mth;
import net.scubadive.ScubaDive;

/**
 * Bottlenose dolphin on the vanilla dolphin. The body pitches with the dolphin (as in vanilla) and the tail beats up
 * and down in a wave through three joints to the flukes; the flippers trim a little.
 */
public class VanillaDolphinModel extends EntityModel<VanillaDolphinRenderState> {
	public static final ModelLayerLocation LAYER = new ModelLayerLocation(ScubaDive.id("vanilla_dolphin"), "main");
	/** Mouth position in the body part (model px), for the carried item. */
	static final float MOUTH_Y = 1.6f;
	static final float MOUTH_Z = -14.0f;

	private final ModelPart body;
	private final ModelPart tail;
	private final ModelPart tail2;
	private final ModelPart flukes;
	private final ModelPart pecRight;
	private final ModelPart pecLeft;

	public VanillaDolphinModel(ModelPart root) {
		super(root);
		this.body = root.getChild("body");
		this.tail = this.body.getChild("tail");
		this.tail2 = this.tail.getChild("tail2");
		this.flukes = this.tail2.getChild("flukes");
		this.pecRight = this.body.getChild("pec_r");
		this.pecLeft = this.body.getChild("pec_l");
	}

	public static LayerDefinition createLayer() {
		return DolphinGeometry.createLayer();
	}

	ModelPart body() {
		return this.body;
	}

	@Override
	public void setupAnim(VanillaDolphinRenderState state) {
		super.setupAnim(state);
		float t = state.ageInTicks + state.phase;
		this.body.xRot = state.xRot * Mth.DEG_TO_RAD;
		float speed = state.isMoving ? 0.3f : 0.12f;
		float amp = state.isMoving ? 1.0f : 0.4f;
		if (state.isMoving) {
			this.body.xRot += -0.05f - 0.05f * Mth.cos(t * speed);
		}
		this.tail.xRot = -0.12f * amp * Mth.cos(t * speed);
		this.tail2.xRot = -0.2f * amp * Mth.cos(t * speed - 0.7f);
		this.flukes.xRot = -0.32f * amp * Mth.cos(t * speed - 1.4f);
		float trim = Mth.sin(t * 0.08f) * 0.12f;
		this.pecRight.zRot = trim;
		this.pecLeft.zRot = -trim;
	}
}
