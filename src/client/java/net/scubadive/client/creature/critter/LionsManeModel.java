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
 * Lion's mane jelly: big flat reddish-brown bell with eight scalloped lobes round the rim, a curtain of four
 * frilly oral arms and a "mane" of many long two-segment tentacles in two rings. Slow, heavy pulses.
 * Texture 64x64 with alpha (tools/K3_jelly_tex.py).
 */
public class LionsManeModel extends EntityModel<CritterRenderState> {
	public static final ModelLayerLocation LAYER = new ModelLayerLocation(ScubaDive.id("lions_mane_jelly"), "main");
	static final float PULSE = 64.0f;
	private static final int TENTACLES = 28;
	private static final float SEGMENT = 11.0f;
	private static final float ARM = 7.0f;

	private final ModelPart bell;
	private final ModelPart[] armBases = new ModelPart[4];
	private final ModelPart[] armTips = new ModelPart[4];
	private final ModelPart[] bases = new ModelPart[TENTACLES];
	private final ModelPart[] tips = new ModelPart[TENTACLES];

	public LionsManeModel(ModelPart root) {
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
		PartDefinition bell = root.addOrReplaceChild("bell", CubeListBuilder.create()
				.texOffs(0, 0).addBox(-8.0f, -1.0f, -8.0f, 16.0f, 1.0f, 16.0f)
				.texOffs(0, 17).addBox(-7.0f, -3.0f, -7.0f, 14.0f, 2.0f, 14.0f)
				.texOffs(0, 33).addBox(-5.0f, -5.0f, -5.0f, 10.0f, 2.0f, 10.0f), PartPose.offset(0.0f, rimY, 0.0f));
		for (int i = 0; i < 8; i++) {
			// Scalloped lobes hang just outside the rim.
			float a = (float) (i * Math.PI / 4 + Math.PI / 8);
			bell.addOrReplaceChild("lobe" + i, CubeListBuilder.create().texOffs(40, 33)
							.addBox(-2.5f, 0.0f, -1.0f, 5.0f, 1.5f, 2.0f),
					PartPose.offsetAndRotation((float) Math.cos(a) * 7.2f, -0.5f, (float) Math.sin(a) * 7.2f,
							0.25f, (float) Math.PI / 2 - a, 0.0f));
		}
		for (int i = 0; i < 4; i++) {
			float a = (float) (i * Math.PI / 2 + Math.PI / 4);
			PartDefinition arm = root.addOrReplaceChild("arm" + i, CubeListBuilder.create().texOffs(0, 45)
							.addBox(-2.5f, 0.0f, -0.75f, 5.0f, ARM, 1.5f),
					PartPose.offsetAndRotation((float) Math.cos(a) * 1.8f, rimY, (float) Math.sin(a) * 1.8f,
							0.0f, (float) Math.PI / 2 - a, 0.0f));
			arm.addOrReplaceChild("tip", CubeListBuilder.create().texOffs(16, 45)
					.addBox(-2.0f, 0.0f, -0.6f, 4.0f, ARM, 1.2f), PartPose.offset(0.0f, ARM, 0.0f));
		}
		for (int i = 0; i < TENTACLES; i++) {
			// Two rings: outer under the rim, inner a little further in, staggered.
			boolean outer = i % 2 == 0;
			float a = (float) (i * 2.0 * Math.PI / TENTACLES);
			float r = outer ? 6.6f : 5.2f;
			int u = outer ? 56 : 60;
			PartDefinition base = root.addOrReplaceChild("tentacle" + i, CubeListBuilder.create().texOffs(u, 0)
							.addBox(-0.25f, 0.0f, -0.25f, 0.5f, SEGMENT, 0.5f),
					PartPose.offsetAndRotation((float) Math.cos(a) * r, rimY, (float) Math.sin(a) * r, 0.0f, 0.0f, 0.0f));
			base.addOrReplaceChild("tip", CubeListBuilder.create().texOffs(u + 2, 0)
					.addBox(-0.25f, 0.0f, -0.25f, 0.5f, SEGMENT - (i % 3), 0.5f), PartPose.offset(0.0f, SEGMENT, 0.0f));
		}
		return LayerDefinition.create(mesh, 64, 64);
	}

	@Override
	public void setupAnim(CritterRenderState state) {
		super.setupAnim(state);
		float t = state.ageInTicks;
		float squeeze = JellyPulse.squeeze(t, PULSE);
		JellyPulse.bell(this.bell, squeeze, 0.12f);
		for (int i = 0; i < 4; i++) {
			JellyPulse.sway(this.armBases[i], this.armTips[i], t, i * 3, 0.04f, 0.1f, squeeze);
		}
		for (int i = 0; i < TENTACLES; i++) {
			JellyPulse.sway(this.bases[i], this.tips[i], t, i, 0.035f, 0.13f, squeeze);
		}
	}
}
