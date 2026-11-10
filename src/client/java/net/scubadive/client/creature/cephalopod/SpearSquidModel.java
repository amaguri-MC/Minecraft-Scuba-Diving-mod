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
 * Spear squid: long slender mantle ending in a point, an arrowhead-shaped pair of fins at the tail, big eyes,
 * eight short arms and two long tentacles in front (arms point to -Z, the swimming direction).
 * Texture 64x32 with alpha (translucent body, fin shape cut out by alpha).
 */
@SuppressWarnings("unused")
public class SpearSquidModel extends EntityModel<CephalopodRenderState> {
	public static final ModelLayerLocation LAYER = new ModelLayerLocation(ScubaDive.id("spear_squid"), "main");

	private final ModelPart body;
	private final ModelPart leftFin;
	private final ModelPart rightFin;
	private final ModelPart[] arms = new ModelPart[8];
	private final ModelPart[] tentacles = new ModelPart[2];
	private final float[] armX = new float[8];
	private final float[] armY = new float[8];

	public SpearSquidModel(ModelPart root) {
		super(root, RenderTypes::entityTranslucent);
		this.body = root.getChild("body");
		this.leftFin = this.body.getChild("left_fin");
		this.rightFin = this.body.getChild("right_fin");
		for (int i = 0; i < 8; i++) {
			this.arms[i] = this.body.getChild("arm" + i);
			this.armX[i] = this.arms[i].xRot;
			this.armY[i] = this.arms[i].yRot;
		}
		this.tentacles[0] = this.body.getChild("tentacle0");
		this.tentacles[1] = this.body.getChild("tentacle1");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();
		PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create()
				.texOffs(0, 0).addBox(-1.5f, -1.5f, -1f, 3f, 3f, 12f)
				.texOffs(30, 0).addBox(-1f, -1f, 11f, 2f, 2f, 3f)
				.texOffs(40, 0).addBox(-1.5f, -1.5f, -3f, 3f, 3f, 2f), PartPose.offsetAndRotation(0f, 21f, 0f, 0f, 0f, 0f));
		PartDefinition leftEye = body.addOrReplaceChild("left_eye", CubeListBuilder.create()
				.texOffs(50, 0).addBox(1.1f, -1f, -2.8f, 1f, 2f, 2f), PartPose.offsetAndRotation(0f, 0f, 0f, 0f, 0f, 0f));
		PartDefinition rightEye = body.addOrReplaceChild("right_eye", CubeListBuilder.create()
				.texOffs(56, 0).addBox(-2.1f, -1f, -2.8f, 1f, 2f, 2f), PartPose.offsetAndRotation(0f, 0f, 0f, 0f, 0f, 0f));
		PartDefinition leftFin = body.addOrReplaceChild("left_fin", CubeListBuilder.create()
				.texOffs(0, 15).addBox(0f, 0f, 0f, 5f, 0f, 8f), PartPose.offsetAndRotation(1.3f, -0.5f, 6f, 0f, 0f, 0f));
		PartDefinition rightFin = body.addOrReplaceChild("right_fin", CubeListBuilder.create()
				.texOffs(26, 15).addBox(-5f, 0f, 0f, 5f, 0f, 8f), PartPose.offsetAndRotation(-1.3f, -0.5f, 6f, 0f, 0f, 0f));
		// Eight short arms in a ring around the mouth, splayed a little outwards.
		PartDefinition arm0 = body.addOrReplaceChild("arm0", CubeListBuilder.create()
				.texOffs(0, 23).addBox(-0.5f, -0.5f, -4f, 1f, 1f, 4f), PartPose.offsetAndRotation(0.9f, 0f, -2.8f, 0f, -0.18f, 0f));
		PartDefinition arm1 = body.addOrReplaceChild("arm1", CubeListBuilder.create()
				.texOffs(0, 23).addBox(-0.5f, -0.5f, -4f, 1f, 1f, 4f), PartPose.offsetAndRotation(0.64f, 0.64f, -2.8f, 0.13f, -0.13f, 0f));
		PartDefinition arm2 = body.addOrReplaceChild("arm2", CubeListBuilder.create()
				.texOffs(0, 23).addBox(-0.5f, -0.5f, -4f, 1f, 1f, 4f), PartPose.offsetAndRotation(0f, 0.9f, -2.8f, 0.18f, 0f, 0f));
		PartDefinition arm3 = body.addOrReplaceChild("arm3", CubeListBuilder.create()
				.texOffs(0, 23).addBox(-0.5f, -0.5f, -4f, 1f, 1f, 4f), PartPose.offsetAndRotation(-0.64f, 0.64f, -2.8f, 0.13f, 0.13f, 0f));
		PartDefinition arm4 = body.addOrReplaceChild("arm4", CubeListBuilder.create()
				.texOffs(0, 23).addBox(-0.5f, -0.5f, -4f, 1f, 1f, 4f), PartPose.offsetAndRotation(-0.9f, 0f, -2.8f, 0f, 0.18f, 0f));
		PartDefinition arm5 = body.addOrReplaceChild("arm5", CubeListBuilder.create()
				.texOffs(0, 23).addBox(-0.5f, -0.5f, -4f, 1f, 1f, 4f), PartPose.offsetAndRotation(-0.64f, -0.64f, -2.8f, -0.13f, 0.13f, 0f));
		PartDefinition arm6 = body.addOrReplaceChild("arm6", CubeListBuilder.create()
				.texOffs(0, 23).addBox(-0.5f, -0.5f, -4f, 1f, 1f, 4f), PartPose.offsetAndRotation(0f, -0.9f, -2.8f, -0.18f, 0f, 0f));
		PartDefinition arm7 = body.addOrReplaceChild("arm7", CubeListBuilder.create()
				.texOffs(0, 23).addBox(-0.5f, -0.5f, -4f, 1f, 1f, 4f), PartPose.offsetAndRotation(0.64f, -0.64f, -2.8f, -0.13f, -0.13f, 0f));
		// Two long tentacles from the lower side.
		PartDefinition tentacle0 = body.addOrReplaceChild("tentacle0", CubeListBuilder.create()
				.texOffs(10, 23).addBox(-0.5f, -0.5f, -8f, 1f, 1f, 8f), PartPose.offsetAndRotation(0.45f, 0.5f, -2.9f, 0.05f, -0.05f, 0f));
		PartDefinition tentacle1 = body.addOrReplaceChild("tentacle1", CubeListBuilder.create()
				.texOffs(10, 23).addBox(-0.5f, -0.5f, -8f, 1f, 1f, 8f), PartPose.offsetAndRotation(-0.45f, 0.5f, -2.9f, 0.05f, 0.05f, 0f));
		return LayerDefinition.create(mesh, 64, 32);
	}

	@Override
	public void setupAnim(CephalopodRenderState state) {
		super.setupAnim(state);
		float t = state.ageInTicks + state.phase;
		if (state.jetting) {
			// Jetting: arms pressed together into a streamlined point, fins folded along the mantle.
			for (int i = 0; i < 8; i++) {
				this.arms[i].xRot = this.armX[i] * 0.2f;
				this.arms[i].yRot = this.armY[i] * 0.2f;
			}
			this.tentacles[0].yRot = 0.0f;
			this.tentacles[1].yRot = 0.0f;
			this.leftFin.zRot = 0.55f;
			this.rightFin.zRot = -0.55f;
			this.body.xRot = 0.0f;
			return;
		}
		// Cruising: the fins ripple, the arms and tentacles trail and sway gently.
		float flap = Mth.sin(t * 0.3f);
		this.leftFin.zRot = flap * 0.35f;
		this.rightFin.zRot = -flap * 0.35f;
		for (int i = 0; i < 8; i++) {
			float s = Mth.sin(t * 0.12f + i * 0.8f) * 0.06f;
			this.arms[i].xRot = this.armX[i] + s;
			this.arms[i].yRot = this.armY[i] + Mth.cos(t * 0.1f + i) * 0.05f;
		}
		this.tentacles[0].yRot = -0.05f + Mth.sin(t * 0.09f) * 0.08f;
		this.tentacles[1].yRot = 0.05f + Mth.sin(t * 0.09f + 1.3f) * 0.08f;
		this.tentacles[0].xRot = 0.05f + Mth.sin(t * 0.07f) * 0.05f;
		this.tentacles[1].xRot = 0.05f + Mth.sin(t * 0.07f + 0.7f) * 0.05f;
		this.body.xRot = Mth.sin(t * 0.05f) * 0.03f;
	}
}
