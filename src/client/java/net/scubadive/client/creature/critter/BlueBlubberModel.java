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
 * Blue blubber jelly: thick, rubbery blue dome bell with a white rim band and eight stout two-segment oral arms
 * with frilled flaps. No marginal tentacles. Calm pulses.
 * Texture 64x64 with alpha (tools/K6_jelly_tex.py).
 */
public class BlueBlubberModel extends EntityModel<CritterRenderState> {
	public static final ModelLayerLocation LAYER = new ModelLayerLocation(ScubaDive.id("blue_blubber_jelly"), "main");
	static final float PULSE = 40.0f;
	private static final int ARMS = 8;
	private static final float ARM = 4.0f;

	private final ModelPart bell;
	private final ModelPart[] bases = new ModelPart[ARMS];
	private final ModelPart[] tips = new ModelPart[ARMS];

	public BlueBlubberModel(ModelPart root) {
		super(root, RenderTypes::entityTranslucent);
		this.bell = root.getChild("bell");
		for (int i = 0; i < ARMS; i++) {
			this.bases[i] = root.getChild("arm" + i);
			this.tips[i] = this.bases[i].getChild("tip");
		}
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();
		float rimY = 24.0f - ARM * 2;
		root.addOrReplaceChild("bell", CubeListBuilder.create()
				.texOffs(0, 0).addBox(-5.5f, -2.0f, -5.5f, 11.0f, 2.0f, 11.0f)
				.texOffs(0, 13).addBox(-5.0f, -5.0f, -5.0f, 10.0f, 3.0f, 10.0f)
				.texOffs(0, 26).addBox(-4.0f, -7.0f, -4.0f, 8.0f, 2.0f, 8.0f)
				.texOffs(0, 36).addBox(-2.5f, -8.0f, -2.5f, 5.0f, 1.0f, 5.0f), PartPose.offset(0.0f, rimY, 0.0f));
		root.addOrReplaceChild("column", CubeListBuilder.create().texOffs(40, 26)
				.addBox(-1.5f, 0.0f, -1.5f, 3.0f, 2.0f, 3.0f), PartPose.offset(0.0f, rimY, 0.0f));
		for (int i = 0; i < ARMS; i++) {
			float a = (float) (i * 2.0 * Math.PI / ARMS + Math.PI / ARMS);
			PartDefinition arm = root.addOrReplaceChild("arm" + i, CubeListBuilder.create()
							.texOffs(48, 0).addBox(-0.8f, 0.0f, -0.8f, 1.6f, ARM, 1.6f)
							.texOffs(56, 0).addBox(0.5f, 0.3f, -1.0f, 0.5f, ARM - 0.5f, 2.0f),
					PartPose.offsetAndRotation((float) Math.cos(a) * 1.8f, rimY + 1.0f, (float) Math.sin(a) * 1.8f,
							0.12f, (float) Math.PI / 2 - a, 0.0f));
			arm.addOrReplaceChild("tip", CubeListBuilder.create()
					.texOffs(48, 6).addBox(-0.7f, 0.0f, -0.7f, 1.4f, ARM, 1.4f)
					.texOffs(56, 6).addBox(0.4f, 0.0f, -0.9f, 0.5f, ARM - 0.5f, 1.8f), PartPose.offsetAndRotation(0.0f, ARM, 0.0f, -0.05f, 0.0f, 0.0f));
		}
		return LayerDefinition.create(mesh, 64, 64);
	}

	@Override
	public void setupAnim(CritterRenderState state) {
		super.setupAnim(state);
		float t = state.ageInTicks;
		float squeeze = JellyPulse.squeeze(t, PULSE);
		JellyPulse.bell(this.bell, squeeze, 0.14f);
		for (int i = 0; i < ARMS; i++) {
			JellyPulse.sway(this.bases[i], this.tips[i], t, i, 0.045f, 0.08f, squeeze);
		}
	}
}
