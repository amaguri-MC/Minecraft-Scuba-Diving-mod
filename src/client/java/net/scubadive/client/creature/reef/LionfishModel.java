package net.scubadive.client.creature.reef;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.scubadive.ScubaDive;

/**
 * Dedicated lionfish model: fanned pectoral fins made of separate rays, a spiny dorsal fin and
 * eyebrow tentacles. Generated with the texture (tools/F2_reef.py).
 */
@SuppressWarnings("unused")
public class LionfishModel extends EntityModel<ReefFishRenderState> {
	public static final ModelLayerLocation LAYER = new ModelLayerLocation(ScubaDive.id("lionfish"), "main");
	private static final float[] RAY_BASE = {1.15192f, 0.767945f, 0.383972f, 0f, -0.383972f, -0.767945f};

	private final ModelPart body;
	private final ModelPart tail;
	private final ModelPart dorsal;
	private final ModelPart pecL;
	private final ModelPart pecR;
	private final ModelPart tentL;
	private final ModelPart tentR;
	private final ModelPart tentL2;
	private final ModelPart tentR2;
	private final ModelPart[] rayL = new ModelPart[RAY_BASE.length];
	private final ModelPart[] rayR = new ModelPart[RAY_BASE.length];

	public LionfishModel(ModelPart root) {
		super(root);
		this.body = root.getChild("body");
		this.tail = this.body.getChild("tail");
		this.dorsal = this.body.getChild("dorsal");
		this.pecL = this.body.getChild("pecL");
		this.pecR = this.body.getChild("pecR");
		this.tentL = this.body.getChild("tentL");
		this.tentR = this.body.getChild("tentR");
		this.tentL2 = this.tentL.getChild("tentL2");
		this.tentR2 = this.tentR.getChild("tentR2");
		for (int i = 0; i < RAY_BASE.length; i++) {
			this.rayL[i] = this.pecL.getChild("pecL_r" + i);
			this.rayR[i] = this.pecR.getChild("pecR_r" + i);
		}
	}

	public static LayerDefinition createLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();
		PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create()
				.texOffs(0, 0).addBox(-4f, -6f, -6f, 8f, 12f, 16f)
				.texOffs(49, 0).addBox(-4f, -5f, -12f, 8f, 10f, 6f)
				.texOffs(27, 29).addBox(-3f, -2f, -15f, 6f, 5f, 3f)
				.texOffs(0, 0).addBox(-2f, 3f, -14f, 4f, 2f, 2f)
				.texOffs(95, 0).addBox(4f, -4f, -10f, 1f, 3f, 3f)
				.texOffs(101, 34).addBox(-5f, -4f, -10f, 1f, 3f, 3f)
				.texOffs(78, 0).addBox(-2f, -4f, 10f, 4f, 8f, 4f), PartPose.offsetAndRotation(0f, 14f, 0f, 0f, 0f, 0f));
		PartDefinition tail = body.addOrReplaceChild("tail", CubeListBuilder.create()
				.texOffs(46, 30).addBox(0f, -5f, 0f, 0f, 10f, 4f)
				.texOffs(115, 21).addBox(0f, -7f, 4f, 0f, 14f, 5f), PartPose.offsetAndRotation(0f, 0f, 14f, 0f, 0f, 0f));
		PartDefinition anal = body.addOrReplaceChild("anal", CubeListBuilder.create()
				.texOffs(95, 0).addBox(0f, -2f, 6f, 0f, 8f, 8f)
				.texOffs(33, 0).addBox(3f, -1f, -6f, 0f, 7f, 6f)
				.texOffs(112, 0).addBox(-3f, -1f, -6f, 0f, 7f, 6f), PartPose.offsetAndRotation(0f, 6f, 0f, 0f, 0f, 0f));
		PartDefinition tentL = body.addOrReplaceChild("tentL", CubeListBuilder.create()
				.texOffs(55, 34).addBox(-1f, -6f, -1f, 2f, 6f, 2f), PartPose.offsetAndRotation(3f, -5f, -9f, 0.4f, 0f, 0.45f));
		PartDefinition tentL2 = tentL.addOrReplaceChild("tentL2", CubeListBuilder.create()
				.texOffs(33, 0).addBox(-0.5f, -4f, -0.5f, 1f, 4f, 1f), PartPose.offsetAndRotation(0f, -6f, 0f, 0.3f, 0f, 0.3f));
		PartDefinition tentR = body.addOrReplaceChild("tentR", CubeListBuilder.create()
				.texOffs(55, 34).addBox(-1f, -6f, -1f, 2f, 6f, 2f), PartPose.offsetAndRotation(-3f, -5f, -9f, 0.4f, 0f, -0.45f));
		PartDefinition tentR2 = tentR.addOrReplaceChild("tentR2", CubeListBuilder.create()
				.texOffs(33, 0).addBox(-0.5f, -4f, -0.5f, 1f, 4f, 1f), PartPose.offsetAndRotation(0f, -6f, 0f, 0.3f, 0f, -0.3f));
		PartDefinition pecL = body.addOrReplaceChild("pecL", CubeListBuilder.create(), PartPose.offsetAndRotation(4f, 1f, -5f, 0f, 1f, 0f));
		PartDefinition pecL_r0 = pecL.addOrReplaceChild("pecL_r0", CubeListBuilder.create()
				.texOffs(0, 27).addBox(0f, 0f, 0f, 0f, 1f, 13f)
				.texOffs(0, 16).addBox(0f, 1f, 0f, 0f, 7f, 13f), PartPose.offsetAndRotation(0f, 0f, 0f, 1.15192f, 0f, 0f));
		PartDefinition pecL_r1 = pecL.addOrReplaceChild("pecL_r1", CubeListBuilder.create()
				.texOffs(0, 22).addBox(0f, 0f, 0f, 0f, 1f, 16f)
				.texOffs(49, 10).addBox(0f, 1f, 0f, 0f, 7f, 16f), PartPose.offsetAndRotation(0f, 0f, 0f, 0.767945f, 0f, 0f));
		PartDefinition pecL_r2 = pecL.addOrReplaceChild("pecL_r2", CubeListBuilder.create()
				.texOffs(62, 16).addBox(0f, 0f, 0f, 0f, 1f, 18f)
				.texOffs(49, 0).addBox(0f, 1f, 0f, 0f, 7f, 18f), PartPose.offsetAndRotation(0f, 0f, 0f, 0.383972f, 0f, 0f));
		PartDefinition pecL_r3 = pecL.addOrReplaceChild("pecL_r3", CubeListBuilder.create()
				.texOffs(64, 18).addBox(0f, 0f, 0f, 0f, 1f, 18f)
				.texOffs(86, 0).addBox(0f, 1f, 0f, 0f, 7f, 18f), PartPose.offsetAndRotation(0f, 0f, 0f, 0f, 0f, 0f));
		PartDefinition pecL_r4 = pecL.addOrReplaceChild("pecL_r4", CubeListBuilder.create()
				.texOffs(64, 22).addBox(0f, 0f, 0f, 0f, 1f, 16f)
				.texOffs(82, 10).addBox(0f, 1f, 0f, 0f, 7f, 16f), PartPose.offsetAndRotation(0f, 0f, 0f, -0.383972f, 0f, 0f));
		PartDefinition pecL_r5 = pecL.addOrReplaceChild("pecL_r5", CubeListBuilder.create()
				.texOffs(64, 27).addBox(0f, 0f, 0f, 0f, 1f, 13f), PartPose.offsetAndRotation(0f, 0f, 0f, -0.767945f, 0f, 0f));
		PartDefinition pecR = body.addOrReplaceChild("pecR", CubeListBuilder.create(), PartPose.offsetAndRotation(-4f, 1f, -5f, 0f, -1f, 0f));
		PartDefinition pecR_r0 = pecR.addOrReplaceChild("pecR_r0", CubeListBuilder.create()
				.texOffs(0, 27).addBox(0f, 0f, 0f, 0f, 1f, 13f)
				.texOffs(0, 16).addBox(0f, 1f, 0f, 0f, 7f, 13f), PartPose.offsetAndRotation(0f, 0f, 0f, 1.15192f, 0f, 0f));
		PartDefinition pecR_r1 = pecR.addOrReplaceChild("pecR_r1", CubeListBuilder.create()
				.texOffs(0, 22).addBox(0f, 0f, 0f, 0f, 1f, 16f)
				.texOffs(49, 10).addBox(0f, 1f, 0f, 0f, 7f, 16f), PartPose.offsetAndRotation(0f, 0f, 0f, 0.767945f, 0f, 0f));
		PartDefinition pecR_r2 = pecR.addOrReplaceChild("pecR_r2", CubeListBuilder.create()
				.texOffs(62, 16).addBox(0f, 0f, 0f, 0f, 1f, 18f)
				.texOffs(49, 0).addBox(0f, 1f, 0f, 0f, 7f, 18f), PartPose.offsetAndRotation(0f, 0f, 0f, 0.383972f, 0f, 0f));
		PartDefinition pecR_r3 = pecR.addOrReplaceChild("pecR_r3", CubeListBuilder.create()
				.texOffs(64, 18).addBox(0f, 0f, 0f, 0f, 1f, 18f)
				.texOffs(86, 0).addBox(0f, 1f, 0f, 0f, 7f, 18f), PartPose.offsetAndRotation(0f, 0f, 0f, 0f, 0f, 0f));
		PartDefinition pecR_r4 = pecR.addOrReplaceChild("pecR_r4", CubeListBuilder.create()
				.texOffs(64, 22).addBox(0f, 0f, 0f, 0f, 1f, 16f)
				.texOffs(82, 10).addBox(0f, 1f, 0f, 0f, 7f, 16f), PartPose.offsetAndRotation(0f, 0f, 0f, -0.383972f, 0f, 0f));
		PartDefinition pecR_r5 = pecR.addOrReplaceChild("pecR_r5", CubeListBuilder.create()
				.texOffs(64, 27).addBox(0f, 0f, 0f, 0f, 1f, 13f), PartPose.offsetAndRotation(0f, 0f, 0f, -0.767945f, 0f, 0f));
		PartDefinition dorsal = body.addOrReplaceChild("dorsal", CubeListBuilder.create()
				.texOffs(27, 39).addBox(0f, -9f, -8f, 0f, 9f, 1f)
				.texOffs(110, 33).addBox(0f, -12f, -6f, 0f, 12f, 1f)
				.texOffs(13, 0).addBox(0f, -14f, -4f, 0f, 14f, 1f)
				.texOffs(125, 0).addBox(0f, -15f, -2f, 0f, 15f, 1f)
				.texOffs(126, 16).addBox(0f, -15f, 0f, 0f, 15f, 1f)
				.texOffs(46, 0).addBox(0f, -14f, 2f, 0f, 14f, 1f)
				.texOffs(126, 32).addBox(0f, -13f, 4f, 0f, 13f, 1f)
				.texOffs(36, 37).addBox(0f, -11f, 6f, 0f, 11f, 1f)
				.texOffs(123, 16).addBox(0f, -8f, 8f, 0f, 8f, 1f)
				.texOffs(104, 0).addBox(0f, -6f, -7f, 0f, 6f, 1f)
				.texOffs(30, 39).addBox(0f, -9f, -5f, 0f, 9f, 1f)
				.texOffs(39, 37).addBox(0f, -11f, -3f, 0f, 11f, 1f)
				.texOffs(33, 37).addBox(0f, -12f, -1f, 0f, 12f, 1f)
				.texOffs(42, 37).addBox(0f, -11f, 1f, 0f, 11f, 1f)
				.texOffs(97, 37).addBox(0f, -10f, 3f, 0f, 10f, 1f)
				.texOffs(91, 39).addBox(0f, -8f, 5f, 0f, 8f, 1f)
				.texOffs(107, 0).addBox(0f, -5f, 7f, 0f, 5f, 1f)
				.texOffs(0, 0).addBox(0f, -7f, 10f, 0f, 8f, 6f), PartPose.offsetAndRotation(0f, -5f, 0f, 0f, 0f, 0f));
		return LayerDefinition.create(mesh, 128, 128);
	}

	@Override
	public void setupAnim(ReefFishRenderState state) {
		super.setupAnim(state);
		float t = state.ageInTicks;
		float swim = Math.min(1.0f, state.walkAnimationSpeed * 4.0f);
		// Lionfish drift: slow tail, fans held wide with a gentle ripple running across the rays.
		this.tail.yRot = (float) Math.sin(t * (0.14f + swim * 0.45f)) * (0.14f + swim * 0.3f);
		this.body.yRot = -this.tail.yRot * 0.25f;
		float flap = (float) Math.sin(t * 0.1f);
		this.pecL.yRot = 1.0f + flap * 0.1f;
		this.pecR.yRot = -1.0f - flap * 0.1f;
		for (int i = 0; i < RAY_BASE.length; i++) {
			float ripple = (float) Math.sin(t * 0.13f - i * 0.7f) * 0.06f;
			this.rayL[i].xRot = RAY_BASE[i] + ripple;
			this.rayR[i].xRot = RAY_BASE[i] + ripple;
		}
		this.dorsal.xRot = (float) Math.sin(t * 0.05f) * 0.03f;
		float sway = (float) Math.sin(t * 0.09f) * 0.08f;
		this.tentL.zRot = 0.45f + sway;
		this.tentR.zRot = -0.45f - sway;
		this.tentL2.xRot = 0.3f + (float) Math.sin(t * 0.12f) * 0.1f;
		this.tentR2.xRot = 0.3f + (float) Math.sin(t * 0.12f + 1.0f) * 0.1f;
	}
}
