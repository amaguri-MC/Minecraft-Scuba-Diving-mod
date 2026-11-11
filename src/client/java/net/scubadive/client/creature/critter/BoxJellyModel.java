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
 * Box jellyfish: clear cube-shaped bell with a pinkish stomach inside, a flap (pedalium) at each of the four
 * corners and three long two-segment tentacles hanging from every pedalium. Quick, strong pulses.
 * Texture 64x32 with alpha (tools/K3_jelly_tex.py).
 */
public class BoxJellyModel extends EntityModel<CritterRenderState> {
	public static final ModelLayerLocation LAYER = new ModelLayerLocation(ScubaDive.id("box_jellyfish"), "main");
	static final float PULSE = 18.0f;
	private static final int PER_CORNER = 3;
	private static final float SEGMENT = 9.0f;

	private final ModelPart bell;
	private final ModelPart[] pedalia = new ModelPart[4];
	private final ModelPart[] bases = new ModelPart[4 * PER_CORNER];
	private final ModelPart[] tips = new ModelPart[4 * PER_CORNER];

	public BoxJellyModel(ModelPart root) {
		super(root, RenderTypes::entityTranslucent);
		this.bell = root.getChild("bell");
		for (int c = 0; c < 4; c++) {
			this.pedalia[c] = root.getChild("pedalium" + c);
			for (int k = 0; k < PER_CORNER; k++) {
				int i = c * PER_CORNER + k;
				this.bases[i] = this.pedalia[c].getChild("tentacle" + k);
				this.tips[i] = this.bases[i].getChild("tip");
			}
		}
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();
		float pedal = 2.5f;
		float rimY = 24.0f - pedal - SEGMENT * 2;
		root.addOrReplaceChild("bell", CubeListBuilder.create()
				.texOffs(0, 0).addBox(-4.0f, -9.0f, -4.0f, 8.0f, 9.0f, 8.0f)
				.texOffs(0, 17).addBox(-1.5f, -7.0f, -1.5f, 3.0f, 6.0f, 3.0f), PartPose.offset(0.0f, rimY, 0.0f));
		for (int c = 0; c < 4; c++) {
			float a = (float) (c * Math.PI / 2 + Math.PI / 4);
			float r = 4.6f;
			PartDefinition pedalium = root.addOrReplaceChild("pedalium" + c, CubeListBuilder.create().texOffs(14, 17)
							.addBox(-1.0f, 0.0f, -0.5f, 2.0f, pedal, 1.0f),
					PartPose.offsetAndRotation((float) Math.cos(a) * r, rimY - 0.5f, (float) Math.sin(a) * r, 0.0f, -a + (float) Math.PI / 2, 0.0f));
			for (int k = 0; k < PER_CORNER; k++) {
				PartDefinition base = pedalium.addOrReplaceChild("tentacle" + k, CubeListBuilder.create().texOffs(32, 0)
								.addBox(-0.25f, 0.0f, -0.25f, 0.5f, SEGMENT, 0.5f),
						PartPose.offsetAndRotation(-0.7f + k * 0.7f, pedal, 0.0f, 0.0f, 0.0f, (k - 1) * 0.08f));
				base.addOrReplaceChild("tip", CubeListBuilder.create().texOffs(36, 0)
						.addBox(-0.25f, 0.0f, -0.25f, 0.5f, SEGMENT, 0.5f), PartPose.offset(0.0f, SEGMENT, 0.0f));
			}
		}
		return LayerDefinition.create(mesh, 64, 32);
	}

	@Override
	public void setupAnim(CritterRenderState state) {
		super.setupAnim(state);
		float t = state.ageInTicks;
		float squeeze = JellyPulse.squeeze(t, PULSE);
		JellyPulse.bell(this.bell, squeeze, 0.14f);
		for (int c = 0; c < 4; c++) {
			// Pedalia flick inward on the power stroke.
			this.pedalia[c].xRot = -0.35f * squeeze;
		}
		for (int i = 0; i < this.bases.length; i++) {
			JellyPulse.sway(this.bases[i], this.tips[i], t, i, 0.09f, 0.12f, squeeze);
		}
	}
}
