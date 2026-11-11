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
 * Indonesian sea nettle: bright orange-pink dome bell, four long ruffled oral arms (two segments, cross-shaped
 * frills) and a ring of sixteen long thin two-segment tentacles. Medium pulses.
 * Texture 64x64 with alpha (tools/K6_jelly_tex.py).
 */
public class IndonesianSeaNettleModel extends EntityModel<CritterRenderState> {
	public static final ModelLayerLocation LAYER = new ModelLayerLocation(ScubaDive.id("indonesian_sea_nettle"), "main");
	static final float PULSE = 44.0f;
	private static final int TENTACLES = 16;
	private static final float SEGMENT = 10.0f;
	private static final float ARM = 8.0f;

	private final ModelPart bell;
	private final ModelPart[] armBases = new ModelPart[4];
	private final ModelPart[] armTips = new ModelPart[4];
	private final ModelPart[] bases = new ModelPart[TENTACLES];
	private final ModelPart[] tips = new ModelPart[TENTACLES];

	public IndonesianSeaNettleModel(ModelPart root) {
		super(root, RenderTypes::entityTranslucent);
		this.bell = root.getChild("bell");
		for (int i = 0; i < 4; i++) {
			this.armBases[i] = root.getChild("arm" + i);
			this.armTips[i] = this.armBases[i].getChild("tip");
		}
		for (int i = 0; i < TENTACLES; i++) {
			this.bases[i] = root.getChild("tentacle" + i);
			this.tips[i] = this.bases[i].getChild("tip");
		}
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();
		float rimY = 24.0f - SEGMENT * 2;
		root.addOrReplaceChild("bell", CubeListBuilder.create()
				.texOffs(0, 0).addBox(-5.0f, -1.0f, -5.0f, 10.0f, 1.0f, 10.0f)
				.texOffs(0, 12).addBox(-4.5f, -4.0f, -4.5f, 9.0f, 3.0f, 9.0f)
				.texOffs(0, 26).addBox(-3.0f, -6.0f, -3.0f, 6.0f, 2.0f, 6.0f), PartPose.offset(0.0f, rimY, 0.0f));
		for (int i = 0; i < 4; i++) {
			float a = (float) (i * Math.PI / 2 + Math.PI / 4);
			PartDefinition arm = root.addOrReplaceChild("arm" + i, CubeListBuilder.create()
							.texOffs(40, 0).addBox(-1.5f, 0.0f, -0.25f, 3.0f, ARM, 0.5f)
							.texOffs(50, 0).addBox(-0.25f, 0.5f, -1.0f, 0.5f, ARM - 1.0f, 2.0f),
					PartPose.offsetAndRotation((float) Math.cos(a) * 1.2f, rimY, (float) Math.sin(a) * 1.2f,
							0.05f, (float) Math.PI / 2 - a, 0.0f));
			arm.addOrReplaceChild("tip", CubeListBuilder.create()
					.texOffs(40, 10).addBox(-1.25f, 0.0f, -0.25f, 2.5f, ARM, 0.5f)
					.texOffs(50, 10).addBox(-0.25f, 0.0f, -0.9f, 0.5f, ARM - 1.0f, 1.8f), PartPose.offset(0.0f, ARM, 0.0f));
		}
		for (int i = 0; i < TENTACLES; i++) {
			float a = (float) (i * 2.0 * Math.PI / TENTACLES);
			PartDefinition base = root.addOrReplaceChild("tentacle" + i, CubeListBuilder.create().texOffs(56, 0)
							.addBox(-0.25f, 0.0f, -0.25f, 0.5f, SEGMENT, 0.5f),
					PartPose.offset((float) Math.cos(a) * 4.6f, rimY, (float) Math.sin(a) * 4.6f));
			base.addOrReplaceChild("tip", CubeListBuilder.create().texOffs(60, 0)
					.addBox(-0.25f, 0.0f, -0.25f, 0.5f, SEGMENT - (i % 3), 0.5f), PartPose.offset(0.0f, SEGMENT, 0.0f));
		}
		return LayerDefinition.create(mesh, 64, 64);
	}

	@Override
	public void setupAnim(CritterRenderState state) {
		super.setupAnim(state);
		float t = state.ageInTicks;
		float squeeze = JellyPulse.squeeze(t, PULSE);
		JellyPulse.bell(this.bell, squeeze, 0.18f);
		for (int i = 0; i < 4; i++) {
			JellyPulse.sway(this.armBases[i], this.armTips[i], t, i * 4, 0.045f, 0.12f, squeeze);
		}
		for (int i = 0; i < TENTACLES; i++) {
			JellyPulse.sway(this.bases[i], this.tips[i], t, i, 0.04f, 0.16f, squeeze);
		}
	}
}
