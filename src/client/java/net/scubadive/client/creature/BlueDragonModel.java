package net.scubadive.client.creature;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.Identifier;
import net.scubadive.ScubaDive;

/** Blue dragon (Glaucus atlanticus): slender blue-and-silver body with three finger clusters (cerata) on each side, drawn as it floats on its back just under the surface. Generated with its texture by tools/K4_gen.py. */
@SuppressWarnings("unused")
public class BlueDragonModel extends EntityModel<NudibranchRenderState> {
	public static final ModelLayerLocation LAYER = new ModelLayerLocation(ScubaDive.id("blue_dragon"), "main");

	private final ModelPart body;
	private final ModelPart[] clusters = new ModelPart[6];

	public BlueDragonModel(ModelPart root) {
		super(root);
		this.body = root.getChild("body");
		for (int k = 0; k < 3; k++) {
			this.clusters[k] = this.body.getChild("cluster_l" + k);
			this.clusters[k + 3] = this.body.getChild("cluster_r" + k);
		}
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();
		PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create()
				.texOffs(0, 0).addBox(-1f, -1f, -3f, 2f, 1f, 4f)
				.texOffs(13, 0).addBox(-1f, -1f, -4f, 2f, 1f, 1f)
				.texOffs(20, 0).addBox(-0.5f, -1f, 1f, 1f, 1f, 4f), PartPose.offsetAndRotation(0f, 23.5f, 0f, 0f, 0f, 0f));
		PartDefinition cl0 = body.addOrReplaceChild("cluster_l0", CubeListBuilder.create(), PartPose.offsetAndRotation(1f, -0.5f, -3f, 0f, 0f, 0f));
		PartDefinition cl0f0 = cl0.addOrReplaceChild("f0", CubeListBuilder.create()
				.texOffs(0, 6).addBox(0f, -0.5f, -0.5f, 3f, 1f, 1f), PartPose.offsetAndRotation(0f, 0f, 0f, 0f, -0.4f, -0.15f));
		PartDefinition cl0f1 = cl0.addOrReplaceChild("f1", CubeListBuilder.create()
				.texOffs(0, 6).addBox(0f, -0.5f, -0.5f, 3f, 1f, 1f), PartPose.offsetAndRotation(0f, 0f, 0f, 0f, 0f, -0.15f));
		PartDefinition cl0f2 = cl0.addOrReplaceChild("f2", CubeListBuilder.create()
				.texOffs(0, 6).addBox(0f, -0.5f, -0.5f, 3f, 1f, 1f), PartPose.offsetAndRotation(0f, 0f, 0f, 0f, 0.4f, -0.15f));
		PartDefinition cr0 = body.addOrReplaceChild("cluster_r0", CubeListBuilder.create(), PartPose.offsetAndRotation(-1f, -0.5f, -3f, 0f, 0f, 0f));
		PartDefinition cr0f0 = cr0.addOrReplaceChild("f0", CubeListBuilder.create()
				.texOffs(9, 6).addBox(-3f, -0.5f, -0.5f, 3f, 1f, 1f), PartPose.offsetAndRotation(0f, 0f, 0f, 0f, 0.4f, 0.15f));
		PartDefinition cr0f1 = cr0.addOrReplaceChild("f1", CubeListBuilder.create()
				.texOffs(9, 6).addBox(-3f, -0.5f, -0.5f, 3f, 1f, 1f), PartPose.offsetAndRotation(0f, 0f, 0f, 0f, 0f, 0.15f));
		PartDefinition cr0f2 = cr0.addOrReplaceChild("f2", CubeListBuilder.create()
				.texOffs(9, 6).addBox(-3f, -0.5f, -0.5f, 3f, 1f, 1f), PartPose.offsetAndRotation(0f, 0f, 0f, 0f, -0.4f, 0.15f));
		PartDefinition cl1 = body.addOrReplaceChild("cluster_l1", CubeListBuilder.create(), PartPose.offsetAndRotation(1f, -0.5f, -1f, 0f, 0f, 0f));
		PartDefinition cl1f0 = cl1.addOrReplaceChild("f0", CubeListBuilder.create()
				.texOffs(0, 6).addBox(0f, -0.5f, -0.5f, 3f, 1f, 1f), PartPose.offsetAndRotation(0f, 0f, 0f, 0f, -0.4f, -0.15f));
		PartDefinition cl1f1 = cl1.addOrReplaceChild("f1", CubeListBuilder.create()
				.texOffs(0, 6).addBox(0f, -0.5f, -0.5f, 3f, 1f, 1f), PartPose.offsetAndRotation(0f, 0f, 0f, 0f, 0f, -0.15f));
		PartDefinition cl1f2 = cl1.addOrReplaceChild("f2", CubeListBuilder.create()
				.texOffs(0, 6).addBox(0f, -0.5f, -0.5f, 3f, 1f, 1f), PartPose.offsetAndRotation(0f, 0f, 0f, 0f, 0.4f, -0.15f));
		PartDefinition cr1 = body.addOrReplaceChild("cluster_r1", CubeListBuilder.create(), PartPose.offsetAndRotation(-1f, -0.5f, -1f, 0f, 0f, 0f));
		PartDefinition cr1f0 = cr1.addOrReplaceChild("f0", CubeListBuilder.create()
				.texOffs(9, 6).addBox(-3f, -0.5f, -0.5f, 3f, 1f, 1f), PartPose.offsetAndRotation(0f, 0f, 0f, 0f, 0.4f, 0.15f));
		PartDefinition cr1f1 = cr1.addOrReplaceChild("f1", CubeListBuilder.create()
				.texOffs(9, 6).addBox(-3f, -0.5f, -0.5f, 3f, 1f, 1f), PartPose.offsetAndRotation(0f, 0f, 0f, 0f, 0f, 0.15f));
		PartDefinition cr1f2 = cr1.addOrReplaceChild("f2", CubeListBuilder.create()
				.texOffs(9, 6).addBox(-3f, -0.5f, -0.5f, 3f, 1f, 1f), PartPose.offsetAndRotation(0f, 0f, 0f, 0f, -0.4f, 0.15f));
		PartDefinition cl2 = body.addOrReplaceChild("cluster_l2", CubeListBuilder.create(), PartPose.offsetAndRotation(1f, -0.5f, 1f, 0f, 0f, 0f));
		PartDefinition cl2f0 = cl2.addOrReplaceChild("f0", CubeListBuilder.create()
				.texOffs(18, 6).addBox(0f, -0.5f, -0.5f, 2f, 1f, 1f), PartPose.offsetAndRotation(0f, 0f, 0f, 0f, -0.4f, -0.15f));
		PartDefinition cl2f1 = cl2.addOrReplaceChild("f1", CubeListBuilder.create()
				.texOffs(18, 6).addBox(0f, -0.5f, -0.5f, 2f, 1f, 1f), PartPose.offsetAndRotation(0f, 0f, 0f, 0f, 0f, -0.15f));
		PartDefinition cl2f2 = cl2.addOrReplaceChild("f2", CubeListBuilder.create()
				.texOffs(18, 6).addBox(0f, -0.5f, -0.5f, 2f, 1f, 1f), PartPose.offsetAndRotation(0f, 0f, 0f, 0f, 0.4f, -0.15f));
		PartDefinition cr2 = body.addOrReplaceChild("cluster_r2", CubeListBuilder.create(), PartPose.offsetAndRotation(-1f, -0.5f, 1f, 0f, 0f, 0f));
		PartDefinition cr2f0 = cr2.addOrReplaceChild("f0", CubeListBuilder.create()
				.texOffs(25, 6).addBox(-2f, -0.5f, -0.5f, 2f, 1f, 1f), PartPose.offsetAndRotation(0f, 0f, 0f, 0f, 0.4f, 0.15f));
		PartDefinition cr2f1 = cr2.addOrReplaceChild("f1", CubeListBuilder.create()
				.texOffs(25, 6).addBox(-2f, -0.5f, -0.5f, 2f, 1f, 1f), PartPose.offsetAndRotation(0f, 0f, 0f, 0f, 0f, 0.15f));
		PartDefinition cr2f2 = cr2.addOrReplaceChild("f2", CubeListBuilder.create()
				.texOffs(25, 6).addBox(-2f, -0.5f, -0.5f, 2f, 1f, 1f), PartPose.offsetAndRotation(0f, 0f, 0f, 0f, -0.4f, 0.15f));
		return LayerDefinition.create(mesh, 32, 32);
	}

	@Override
	public void setupAnim(NudibranchRenderState state) {
		super.setupAnim(state);
		float t = state.ageInTicks;
		// The finger clusters sway slowly with the swell.
		for (int k = 0; k < 6; k++) {
			float side = k < 3 ? 1.0f : -1.0f;
			this.clusters[k].yRot = (float) Math.sin(t * 0.06f + k * 0.9f) * 0.18f * side;
			this.clusters[k].zRot = (float) Math.sin(t * 0.05f + k) * 0.1f * side;
		}
		this.body.y = 23.5f + (float) Math.sin(t * 0.05f) * 0.15f;
		this.body.zRot = (float) Math.sin(t * 0.04f) * 0.05f;
	}
}
