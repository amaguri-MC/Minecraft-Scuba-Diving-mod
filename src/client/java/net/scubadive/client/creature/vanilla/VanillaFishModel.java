package net.scubadive.client.creature.vanilla;

import java.util.function.ToDoubleFunction;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.util.Mth;
import net.scubadive.ScubaDive;

/**
 * Renewed salmon / cod: body, a two-joint tail (the wave runs from the tail joint to the caudal fin) and two
 * pectorals that paddle. Out of water the fish flops: faster, wider beats.
 */
public class VanillaFishModel<S extends LivingEntityRenderState> extends EntityModel<S> {
	public static final ModelLayerLocation SALMON = new ModelLayerLocation(ScubaDive.id("vanilla_salmon"), "main");
	public static final ModelLayerLocation COD = new ModelLayerLocation(ScubaDive.id("vanilla_cod"), "main");

	private final ModelPart tail;
	private final ModelPart fin;
	private final ModelPart pecRight;
	private final ModelPart pecLeft;
	private final ToDoubleFunction<S> phase;
	private final float beatSpeed;

	public VanillaFishModel(ModelPart root, ToDoubleFunction<S> phase, float beatSpeed) {
		super(root);
		ModelPart body = root.getChild("body");
		this.tail = body.getChild("tail");
		this.fin = this.tail.getChild("fin");
		this.pecRight = body.getChild("pec_r");
		this.pecLeft = body.getChild("pec_l");
		this.phase = phase;
		this.beatSpeed = beatSpeed;
	}

	@Override
	public void setupAnim(S state) {
		super.setupAnim(state);
		float t = state.ageInTicks + (float) this.phase.applyAsDouble(state);
		boolean water = state.isInWater;
		float speed = water ? this.beatSpeed : this.beatSpeed * 2.2f;
		float amp = water ? 1.0f : 1.6f;
		float w = t * speed;
		this.tail.yRot = Mth.sin(w) * 0.32f * amp;
		this.fin.yRot = Mth.sin(w - 0.9f) * 0.42f * amp;
		float pad = Mth.sin(t * speed * 0.7f) * 0.35f;
		this.pecRight.yRot = pad;
		this.pecLeft.yRot = -pad;
	}
}
