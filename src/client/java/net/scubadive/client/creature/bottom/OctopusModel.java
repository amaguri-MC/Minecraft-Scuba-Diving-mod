package net.scubadive.client.creature.bottom;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.scubadive.ScubaDive;

/** Octopus: bulbous mantle, eyed head and eight three-jointed arms that ripple. Texture by tools/G5_gen.py. */
public class OctopusModel extends EntityModel<OctopusRenderState> {
	public static final ModelLayerLocation LAYER = new ModelLayerLocation(ScubaDive.id("octopus"), "main");
	private static final int ARMS = 8;

	private final ModelPart body;
	private final ModelPart mantle;
	/** arms[i][j] is joint j (0 = root) of arm i. */
	private final ModelPart[][] arms = new ModelPart[ARMS][3];
	private final float[] armYaw = new float[ARMS];

	public OctopusModel(ModelPart root) {
		super(root);
		this.body = root.getChild("body");
		this.mantle = this.body.getChild("mantle");
		for (int i = 0; i < ARMS; i++) {
			ModelPart s1 = this.body.getChild("a" + i);
			this.arms[i][0] = s1;
			this.arms[i][1] = s1.getChild("s2");
			this.arms[i][2] = this.arms[i][1].getChild("s3");
			this.armYaw[i] = s1.yRot;
		}
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();
		PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create()
				.texOffs(0, 0).addBox(-4f, -5f, -4f, 8f, 5f, 8f)
				.texOffs(32, 0).addBox(3f, -7f, -3f, 2f, 2f, 2f)
				.texOffs(44, 0).addBox(-5f, -7f, -3f, 2f, 2f, 2f), PartPose.offsetAndRotation(0f, 22f, 0f, 0f, 0f, 0f));
		PartDefinition mantle = body.addOrReplaceChild("mantle", CubeListBuilder.create()
				.texOffs(0, 14).addBox(-3f, -8f, -3f, 6f, 8f, 6f), PartPose.offsetAndRotation(0f, -3f, 3f, -0.5f, 0f, 0f));
		PartDefinition a0s1 = body.addOrReplaceChild("a0", CubeListBuilder.create()
				.texOffs(0, 30).addBox(-1.5f, -1f, 0f, 3f, 2f, 3f), PartPose.offsetAndRotation(1.339f, 1f, 3.234f, 0f, 0.3927f, 0f));
		PartDefinition a0s2 = a0s1.addOrReplaceChild("s2", CubeListBuilder.create()
				.texOffs(12, 30).addBox(-1f, -1f, 0f, 2f, 2f, 3f), PartPose.offsetAndRotation(0f, 0f, 3f, 0f, 0f, 0f));
		PartDefinition a0s3 = a0s2.addOrReplaceChild("s3", CubeListBuilder.create()
				.texOffs(22, 30).addBox(-1f, -0.5f, 0f, 2f, 1f, 2f), PartPose.offsetAndRotation(0f, 0f, 3f, 0f, 0f, 0f));
		PartDefinition a1s1 = body.addOrReplaceChild("a1", CubeListBuilder.create()
				.texOffs(0, 30).addBox(-1.5f, -1f, 0f, 3f, 2f, 3f), PartPose.offsetAndRotation(3.234f, 1f, 1.339f, 0f, 1.1781f, 0f));
		PartDefinition a1s2 = a1s1.addOrReplaceChild("s2", CubeListBuilder.create()
				.texOffs(12, 30).addBox(-1f, -1f, 0f, 2f, 2f, 3f), PartPose.offsetAndRotation(0f, 0f, 3f, 0f, 0f, 0f));
		PartDefinition a1s3 = a1s2.addOrReplaceChild("s3", CubeListBuilder.create()
				.texOffs(22, 30).addBox(-1f, -0.5f, 0f, 2f, 1f, 2f), PartPose.offsetAndRotation(0f, 0f, 3f, 0f, 0f, 0f));
		PartDefinition a2s1 = body.addOrReplaceChild("a2", CubeListBuilder.create()
				.texOffs(0, 30).addBox(-1.5f, -1f, 0f, 3f, 2f, 3f), PartPose.offsetAndRotation(3.234f, 1f, -1.339f, 0f, 1.9635f, 0f));
		PartDefinition a2s2 = a2s1.addOrReplaceChild("s2", CubeListBuilder.create()
				.texOffs(12, 30).addBox(-1f, -1f, 0f, 2f, 2f, 3f), PartPose.offsetAndRotation(0f, 0f, 3f, 0f, 0f, 0f));
		PartDefinition a2s3 = a2s2.addOrReplaceChild("s3", CubeListBuilder.create()
				.texOffs(22, 30).addBox(-1f, -0.5f, 0f, 2f, 1f, 2f), PartPose.offsetAndRotation(0f, 0f, 3f, 0f, 0f, 0f));
		PartDefinition a3s1 = body.addOrReplaceChild("a3", CubeListBuilder.create()
				.texOffs(0, 30).addBox(-1.5f, -1f, 0f, 3f, 2f, 3f), PartPose.offsetAndRotation(1.339f, 1f, -3.234f, 0f, 2.7489f, 0f));
		PartDefinition a3s2 = a3s1.addOrReplaceChild("s2", CubeListBuilder.create()
				.texOffs(12, 30).addBox(-1f, -1f, 0f, 2f, 2f, 3f), PartPose.offsetAndRotation(0f, 0f, 3f, 0f, 0f, 0f));
		PartDefinition a3s3 = a3s2.addOrReplaceChild("s3", CubeListBuilder.create()
				.texOffs(22, 30).addBox(-1f, -0.5f, 0f, 2f, 1f, 2f), PartPose.offsetAndRotation(0f, 0f, 3f, 0f, 0f, 0f));
		PartDefinition a4s1 = body.addOrReplaceChild("a4", CubeListBuilder.create()
				.texOffs(0, 30).addBox(-1.5f, -1f, 0f, 3f, 2f, 3f), PartPose.offsetAndRotation(-1.339f, 1f, -3.234f, 0f, 3.5343f, 0f));
		PartDefinition a4s2 = a4s1.addOrReplaceChild("s2", CubeListBuilder.create()
				.texOffs(12, 30).addBox(-1f, -1f, 0f, 2f, 2f, 3f), PartPose.offsetAndRotation(0f, 0f, 3f, 0f, 0f, 0f));
		PartDefinition a4s3 = a4s2.addOrReplaceChild("s3", CubeListBuilder.create()
				.texOffs(22, 30).addBox(-1f, -0.5f, 0f, 2f, 1f, 2f), PartPose.offsetAndRotation(0f, 0f, 3f, 0f, 0f, 0f));
		PartDefinition a5s1 = body.addOrReplaceChild("a5", CubeListBuilder.create()
				.texOffs(0, 30).addBox(-1.5f, -1f, 0f, 3f, 2f, 3f), PartPose.offsetAndRotation(-3.234f, 1f, -1.339f, 0f, 4.3197f, 0f));
		PartDefinition a5s2 = a5s1.addOrReplaceChild("s2", CubeListBuilder.create()
				.texOffs(12, 30).addBox(-1f, -1f, 0f, 2f, 2f, 3f), PartPose.offsetAndRotation(0f, 0f, 3f, 0f, 0f, 0f));
		PartDefinition a5s3 = a5s2.addOrReplaceChild("s3", CubeListBuilder.create()
				.texOffs(22, 30).addBox(-1f, -0.5f, 0f, 2f, 1f, 2f), PartPose.offsetAndRotation(0f, 0f, 3f, 0f, 0f, 0f));
		PartDefinition a6s1 = body.addOrReplaceChild("a6", CubeListBuilder.create()
				.texOffs(0, 30).addBox(-1.5f, -1f, 0f, 3f, 2f, 3f), PartPose.offsetAndRotation(-3.234f, 1f, 1.339f, 0f, 5.1051f, 0f));
		PartDefinition a6s2 = a6s1.addOrReplaceChild("s2", CubeListBuilder.create()
				.texOffs(12, 30).addBox(-1f, -1f, 0f, 2f, 2f, 3f), PartPose.offsetAndRotation(0f, 0f, 3f, 0f, 0f, 0f));
		PartDefinition a6s3 = a6s2.addOrReplaceChild("s3", CubeListBuilder.create()
				.texOffs(22, 30).addBox(-1f, -0.5f, 0f, 2f, 1f, 2f), PartPose.offsetAndRotation(0f, 0f, 3f, 0f, 0f, 0f));
		PartDefinition a7s1 = body.addOrReplaceChild("a7", CubeListBuilder.create()
				.texOffs(0, 30).addBox(-1.5f, -1f, 0f, 3f, 2f, 3f), PartPose.offsetAndRotation(-1.339f, 1f, 3.234f, 0f, 5.8905f, 0f));
		PartDefinition a7s2 = a7s1.addOrReplaceChild("s2", CubeListBuilder.create()
				.texOffs(12, 30).addBox(-1f, -1f, 0f, 2f, 2f, 3f), PartPose.offsetAndRotation(0f, 0f, 3f, 0f, 0f, 0f));
		PartDefinition a7s3 = a7s2.addOrReplaceChild("s3", CubeListBuilder.create()
				.texOffs(22, 30).addBox(-1f, -0.5f, 0f, 2f, 1f, 2f), PartPose.offsetAndRotation(0f, 0f, 3f, 0f, 0f, 0f));
		return LayerDefinition.create(mesh, 64, 40);
	}

	@Override
	public void setupAnim(OctopusRenderState state) {
		super.setupAnim(state);
		float t = state.ageInTicks;
		float move = Math.min(1.0f, state.walkAnimationSpeed * 6.0f);
		// Wave amplitude grows while crawling; at rest the arms only drift.
		float amp = 0.12f + 0.38f * move;
		float rate = 0.10f + 0.10f * move;
		float[] baseX = {0.0f, 0.15f, 0.55f};

		for (int i = 0; i < ARMS; i++) {
			ModelPart[] arm = this.arms[i];
			// Normalised heading in (-PI, PI]; jetting pulls every arm back behind the mantle.
			float yaw = this.armYaw[i];
			if (yaw > Math.PI) {
				yaw -= (float) (2 * Math.PI);
			}
			arm[0].yRot = state.jetting ? yaw * 0.3f : yaw;
			for (int j = 0; j < 3; j++) {
				float phase = t * rate - i * 0.85f - j * 0.8f;
				if (state.jetting) {
					arm[j].xRot = 0.25f + j * 0.1f;
					arm[j].yRot = j == 0 ? arm[0].yRot : 0.0f;
				} else {
					arm[j].xRot = baseX[j] + (float) Math.sin(phase) * amp * (0.6f + 0.4f * j);
					if (j > 0) {
						arm[j].yRot = (float) Math.sin(phase + 1.3f) * (0.18f + 0.3f * move) * j;
					}
				}
			}
		}
		this.mantle.xRot = -0.5f + (float) Math.sin(t * 0.07f) * 0.04f - (state.jetting ? 0.25f : 0.0f);
		this.body.y = 22.0f + (float) Math.sin(t * 0.07f) * 0.15f;
	}
}
