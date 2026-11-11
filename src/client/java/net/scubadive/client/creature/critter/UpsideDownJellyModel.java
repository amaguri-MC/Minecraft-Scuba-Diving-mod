package net.scubadive.client.creature.critter;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.rendertype.RenderTypes;

import net.scubadive.ScubaDive;

/**
 * Upside-down jelly: lies bell-down on the sand. The flat bell rim breathes (pulses) while eight bushy oral arms
 * spread up and outward, each a short stalk carrying cauliflower-like clumps. Green-brown with white spots.
 * Texture 64x64 with alpha (tools/K6_jelly_tex.py).
 */
public class UpsideDownJellyModel extends EntityModel<CritterRenderState> {
	public static final ModelLayerLocation LAYER = new ModelLayerLocation(ScubaDive.id("upside_down_jelly"), "main");
	static final float PULSE = 30.0f;
	private static final int ARMS = 8;

	private final ModelPart bell;
	private final ModelPart[] arms = new ModelPart[ARMS];

	public UpsideDownJellyModel(ModelPart root) {
		super(root, RenderTypes::entityTranslucent);
		this.bell = root.getChild("bell");
		for (int i = 0; i < ARMS; i++) {
			this.arms[i] = root.getChild("arm" + i);
		}
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();
		// Bell rests on the floor (y = 24): narrow dome below, wide flat rim on top, the arms rise from its centre.
		root.addOrReplaceChild("bell", CubeListBuilder.create()
				.texOffs(0, 14).addBox(-5.0f, -1.0f, -5.0f, 10.0f, 1.0f, 10.0f)
				.texOffs(0, 0).addBox(-6.0f, -2.5f, -6.0f, 12.0f, 1.5f, 12.0f), PartPose.offset(0.0f, 24.0f, 0.0f));
		root.addOrReplaceChild("centre", CubeListBuilder.create().texOffs(0, 26)
				.addBox(-1.5f, -2.0f, -1.5f, 3.0f, 2.0f, 3.0f), PartPose.offset(0.0f, 21.5f, 0.0f));
		for (int i = 0; i < ARMS; i++) {
			float a = (float) (i * 2.0 * Math.PI / ARMS);
			float lean = (i % 2 == 0) ? 0.6f : 0.8f;
			// xRot leans the upward stalk outward, the yaw points it along angle a.
			PartDefinition arm = root.addOrReplaceChild("arm" + i, CubeListBuilder.create()
							.texOffs(16, 26).addBox(-0.75f, -3.0f, -0.75f, 1.5f, 3.0f, 1.5f),
					PartPose.offsetAndRotation((float) Math.cos(a) * 1.2f, 21.0f, (float) Math.sin(a) * 1.2f,
							lean, (float) (-a - Math.PI / 2), 0.0f));
			arm.addOrReplaceChild("clump", CubeListBuilder.create()
					.texOffs(26, 26).addBox(-1.25f, -2.0f, -1.25f, 2.5f, 2.0f, 2.5f), PartPose.offsetAndRotation(0.0f, -2.5f, 0.0f, -0.2f, 0.0f, 0.0f));
			arm.addOrReplaceChild("bud", CubeListBuilder.create()
					.texOffs(40, 26).addBox(-0.75f, -1.5f, -0.75f, 1.5f, 1.5f, 1.5f),
					PartPose.offsetAndRotation((i % 2 == 0) ? 0.9f : -0.9f, -1.2f, 0.0f, 0.0f, 0.0f, (i % 2 == 0) ? 0.6f : -0.6f));
		}
		return LayerDefinition.create(mesh, 64, 64);
	}

	@Override
	public void setupAnim(CritterRenderState state) {
		super.setupAnim(state);
		float t = state.ageInTicks;
		// The rim flattens and widens each beat (it pumps water up through the arms).
		float squeeze = JellyPulse.squeeze(t, PULSE);
		this.bell.xScale = 1.0f - 0.12f * squeeze;
		this.bell.zScale = 1.0f - 0.12f * squeeze;
		this.bell.yScale = 1.0f + 0.3f * squeeze;
		for (int i = 0; i < ARMS; i++) {
			this.arms[i].xRot += (float) Math.sin(t * 0.04f + i * 0.8f) * 0.06f - squeeze * 0.05f;
			this.arms[i].zRot += (float) Math.cos(t * 0.03f + i) * 0.05f;
		}
	}
}
