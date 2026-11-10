package net.scubadive.client.creature.cephalopod;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.util.Mth;

import net.scubadive.ScubaDive;

/**
 * Cuttlefish: broad flat mantle with a thin fin running along both sides (five segments per side that ripple
 * in a travelling wave), a wide head with W-pupil eyes on the sides, eight short arms in a cone and two
 * tentacles (arms point to -Z). Texture 64x32; the renderer swaps between pattern frames.
 */
@SuppressWarnings("unused")
public class CuttlefishModel extends EntityModel<CephalopodRenderState> {
	public static final ModelLayerLocation LAYER = new ModelLayerLocation(ScubaDive.id("cuttlefish"), "main");
	private static final int SEGMENTS = 5;

	private final ModelPart body;
	private final ModelPart[] leftFin = new ModelPart[SEGMENTS];
	private final ModelPart[] rightFin = new ModelPart[SEGMENTS];
	private final ModelPart[] arms = new ModelPart[8];
	private final float[] armX = new float[8];
	private final float[] armY = new float[8];

	public CuttlefishModel(ModelPart root) {
		super(root, RenderTypes::entityTranslucent);
		this.body = root.getChild("body");
		for (int i = 0; i < SEGMENTS; i++) {
			this.leftFin[i] = this.body.getChild("left_fin" + i);
			this.rightFin[i] = this.body.getChild("right_fin" + i);
		}
		for (int i = 0; i < 8; i++) {
			this.arms[i] = this.body.getChild("arm" + i);
			this.armX[i] = this.arms[i].xRot;
			this.armY[i] = this.arms[i].yRot;
		}
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();
		PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create()
				.texOffs(0, 0).addBox(-3f, -1.5f, -2f, 6f, 3f, 10f)
				.texOffs(32, 0).addBox(-2.5f, -1.5f, -5f, 5f, 3f, 3f), PartPose.offsetAndRotation(0f, 22f, 0f, 0f, 0f, 0f));
		PartDefinition leftEye = body.addOrReplaceChild("left_eye", CubeListBuilder.create()
				.texOffs(48, 0).addBox(2f, -1.7f, -4.8f, 1f, 2f, 3f), PartPose.offsetAndRotation(0f, 0f, 0f, 0f, 0f, 0f));
		PartDefinition rightEye = body.addOrReplaceChild("right_eye", CubeListBuilder.create()
				.texOffs(56, 0).addBox(-3f, -1.7f, -4.8f, 1f, 2f, 3f), PartPose.offsetAndRotation(0f, 0f, 0f, 0f, 0f, 0f));
		// Fin skirt: five segments along each side of the mantle (z -2 to 8).
		PartDefinition leftFin0 = body.addOrReplaceChild("left_fin0", CubeListBuilder.create()
				.texOffs(0, 14).addBox(0f, 0f, 0f, 2f, 0f, 2f), PartPose.offsetAndRotation(2.8f, 0.4f, -2f, 0f, 0f, 0f));
		PartDefinition leftFin1 = body.addOrReplaceChild("left_fin1", CubeListBuilder.create()
				.texOffs(0, 14).addBox(0f, 0f, 0f, 2f, 0f, 2f), PartPose.offsetAndRotation(2.8f, 0.4f, 0f, 0f, 0f, 0f));
		PartDefinition leftFin2 = body.addOrReplaceChild("left_fin2", CubeListBuilder.create()
				.texOffs(0, 14).addBox(0f, 0f, 0f, 2f, 0f, 2f), PartPose.offsetAndRotation(2.8f, 0.4f, 2f, 0f, 0f, 0f));
		PartDefinition leftFin3 = body.addOrReplaceChild("left_fin3", CubeListBuilder.create()
				.texOffs(0, 14).addBox(0f, 0f, 0f, 2f, 0f, 2f), PartPose.offsetAndRotation(2.8f, 0.4f, 4f, 0f, 0f, 0f));
		PartDefinition leftFin4 = body.addOrReplaceChild("left_fin4", CubeListBuilder.create()
				.texOffs(0, 14).addBox(0f, 0f, 0f, 2f, 0f, 2f), PartPose.offsetAndRotation(2.8f, 0.4f, 6f, 0f, 0f, 0f));
		PartDefinition rightFin0 = body.addOrReplaceChild("right_fin0", CubeListBuilder.create()
				.texOffs(8, 14).addBox(-2f, 0f, 0f, 2f, 0f, 2f), PartPose.offsetAndRotation(-2.8f, 0.4f, -2f, 0f, 0f, 0f));
		PartDefinition rightFin1 = body.addOrReplaceChild("right_fin1", CubeListBuilder.create()
				.texOffs(8, 14).addBox(-2f, 0f, 0f, 2f, 0f, 2f), PartPose.offsetAndRotation(-2.8f, 0.4f, 0f, 0f, 0f, 0f));
		PartDefinition rightFin2 = body.addOrReplaceChild("right_fin2", CubeListBuilder.create()
				.texOffs(8, 14).addBox(-2f, 0f, 0f, 2f, 0f, 2f), PartPose.offsetAndRotation(-2.8f, 0.4f, 2f, 0f, 0f, 0f));
		PartDefinition rightFin3 = body.addOrReplaceChild("right_fin3", CubeListBuilder.create()
				.texOffs(8, 14).addBox(-2f, 0f, 0f, 2f, 0f, 2f), PartPose.offsetAndRotation(-2.8f, 0.4f, 4f, 0f, 0f, 0f));
		PartDefinition rightFin4 = body.addOrReplaceChild("right_fin4", CubeListBuilder.create()
				.texOffs(8, 14).addBox(-2f, 0f, 0f, 2f, 0f, 2f), PartPose.offsetAndRotation(-2.8f, 0.4f, 6f, 0f, 0f, 0f));
		// Eight short arms in a cone in front of the head (a flattened ring), tips slightly together.
		PartDefinition arm0 = body.addOrReplaceChild("arm0", CubeListBuilder.create()
				.texOffs(0, 18).addBox(-0.5f, -0.5f, -3f, 1f, 1f, 3f), PartPose.offsetAndRotation(1.6f, 0f, -4.8f, 0f, 0.15f, 0f));
		PartDefinition arm1 = body.addOrReplaceChild("arm1", CubeListBuilder.create()
				.texOffs(0, 18).addBox(-0.5f, -0.5f, -3f, 1f, 1f, 3f), PartPose.offsetAndRotation(1.1f, 0.6f, -4.8f, -0.08f, 0.1f, 0f));
		PartDefinition arm2 = body.addOrReplaceChild("arm2", CubeListBuilder.create()
				.texOffs(0, 18).addBox(-0.5f, -0.5f, -3f, 1f, 1f, 3f), PartPose.offsetAndRotation(0.4f, 0.8f, -4.8f, -0.1f, 0.03f, 0f));
		PartDefinition arm3 = body.addOrReplaceChild("arm3", CubeListBuilder.create()
				.texOffs(0, 18).addBox(-0.5f, -0.5f, -3f, 1f, 1f, 3f), PartPose.offsetAndRotation(-0.4f, 0.8f, -4.8f, -0.1f, -0.03f, 0f));
		PartDefinition arm4 = body.addOrReplaceChild("arm4", CubeListBuilder.create()
				.texOffs(0, 18).addBox(-0.5f, -0.5f, -3f, 1f, 1f, 3f), PartPose.offsetAndRotation(-1.1f, 0.6f, -4.8f, -0.08f, -0.1f, 0f));
		PartDefinition arm5 = body.addOrReplaceChild("arm5", CubeListBuilder.create()
				.texOffs(0, 18).addBox(-0.5f, -0.5f, -3f, 1f, 1f, 3f), PartPose.offsetAndRotation(-1.6f, 0f, -4.8f, 0f, -0.15f, 0f));
		PartDefinition arm6 = body.addOrReplaceChild("arm6", CubeListBuilder.create()
				.texOffs(0, 18).addBox(-0.5f, -0.5f, -3f, 1f, 1f, 3f), PartPose.offsetAndRotation(-0.7f, -0.7f, -4.8f, 0.1f, -0.05f, 0f));
		PartDefinition arm7 = body.addOrReplaceChild("arm7", CubeListBuilder.create()
				.texOffs(0, 18).addBox(-0.5f, -0.5f, -3f, 1f, 1f, 3f), PartPose.offsetAndRotation(0.7f, -0.7f, -4.8f, 0.1f, 0.05f, 0f));
		// Two tentacles, mostly tucked in between the lower arms.
		PartDefinition tentacle0 = body.addOrReplaceChild("tentacle0", CubeListBuilder.create()
				.texOffs(8, 18).addBox(-0.5f, -0.5f, -4f, 1f, 1f, 4f), PartPose.offsetAndRotation(0.5f, 0.3f, -4.9f, -0.04f, 0.02f, 0f));
		PartDefinition tentacle1 = body.addOrReplaceChild("tentacle1", CubeListBuilder.create()
				.texOffs(8, 18).addBox(-0.5f, -0.5f, -4f, 1f, 1f, 4f), PartPose.offsetAndRotation(-0.5f, 0.3f, -4.9f, -0.04f, -0.02f, 0f));
		return LayerDefinition.create(mesh, 64, 32);
	}

	@Override
	public void setupAnim(CephalopodRenderState state) {
		super.setupAnim(state);
		float t = state.ageInTicks + state.phase;
		boolean fast = state.jetting || state.display;
		// A wave travels from head to tail along the fin skirt (faster while startled).
		float speed = fast ? 0.9f : 0.35f;
		float amp = state.jetting ? 0.15f : 0.4f;
		for (int i = 0; i < SEGMENTS; i++) {
			float wave = Mth.sin(t * speed - i * 1.1f) * amp;
			this.leftFin[i].zRot = wave;
			this.rightFin[i].zRot = -wave;
		}
		for (int i = 0; i < 8; i++) {
			if (state.jetting) {
				this.arms[i].xRot = 0.0f;
				this.arms[i].yRot = 0.0f;
			} else if (state.display) {
				// Warning display: arms spread wide.
				this.arms[i].xRot = this.armX[i] * -2.5f;
				this.arms[i].yRot = this.armY[i] * -2.5f;
			} else {
				this.arms[i].xRot = this.armX[i] + Mth.sin(t * 0.1f + i * 0.9f) * 0.05f;
				this.arms[i].yRot = this.armY[i] + Mth.cos(t * 0.08f + i) * 0.04f;
			}
		}
		// Gentle hovering bob.
		this.body.y = 22.0f + Mth.sin(t * 0.06f) * 0.3f;
		this.body.xRot = state.display ? -0.12f : Mth.sin(t * 0.04f) * 0.03f;
	}
}
