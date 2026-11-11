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

/** Leaf sheep (Costasiella): a tiny pale body thick with green leaf-like cerata, a sheep face with black eyes and two little horn rhinophores. Generated with its texture by tools/K4_gen.py. */
@SuppressWarnings("unused")
public class LeafSheepModel extends EntityModel<NudibranchRenderState> {
	public static final ModelLayerLocation LAYER = new ModelLayerLocation(ScubaDive.id("leaf_sheep"), "main");

	private final ModelPart head;
	private final ModelPart[] leaves = new ModelPart[10];
	private final float[] leafRoll = new float[10];

	public LeafSheepModel(ModelPart root) {
		super(root);
		this.head = root.getChild("head");
		for (int i = 0; i < this.leaves.length; i++) {
			this.leaves[i] = root.getChild("leaf" + i);
			this.leafRoll[i] = this.leaves[i].zRot;
		}
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();
		PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create()
				.texOffs(0, 0).addBox(-1f, -1f, -2f, 2f, 1f, 4f), PartPose.offsetAndRotation(0f, 24f, 0f, 0f, 0f, 0f));
		PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create()
				.texOffs(13, 0).addBox(-1f, -2f, -2f, 2f, 2f, 2f), PartPose.offsetAndRotation(0f, 24f, -2f, 0f, 0f, 0f));
		PartDefinition hornL = head.addOrReplaceChild("horn_l", CubeListBuilder.create()
				.texOffs(22, 0).addBox(-0.5f, -2f, -0.5f, 1f, 2f, 1f), PartPose.offsetAndRotation(0.6f, -2f, -0.8f, -0.2f, 0f, 0.45f));
		PartDefinition hornR = head.addOrReplaceChild("horn_r", CubeListBuilder.create()
				.texOffs(22, 0).addBox(-0.5f, -2f, -0.5f, 1f, 2f, 1f), PartPose.offsetAndRotation(-0.6f, -2f, -0.8f, -0.2f, 0f, -0.45f));
		PartDefinition leaf0 = root.addOrReplaceChild("leaf0", CubeListBuilder.create()
				.texOffs(27, 0).addBox(-0.5f, -2f, -0.5f, 1f, 2f, 1f), PartPose.offsetAndRotation(0.55f, 23f, -1.5f, 0.15f, 0f, 0.65f));
		PartDefinition leaf1 = root.addOrReplaceChild("leaf1", CubeListBuilder.create()
				.texOffs(27, 0).addBox(-0.5f, -2f, -0.5f, 1f, 2f, 1f), PartPose.offsetAndRotation(-0.55f, 23f, -1.5f, 0.15f, 0f, -0.65f));
		PartDefinition leaf2 = root.addOrReplaceChild("leaf2", CubeListBuilder.create()
				.texOffs(27, 0).addBox(-0.5f, -2f, -0.5f, 1f, 2f, 1f), PartPose.offsetAndRotation(0.55f, 23f, -0.5f, 0.15f, 0f, 0.65f));
		PartDefinition leaf3 = root.addOrReplaceChild("leaf3", CubeListBuilder.create()
				.texOffs(27, 0).addBox(-0.5f, -2f, -0.5f, 1f, 2f, 1f), PartPose.offsetAndRotation(-0.55f, 23f, -0.5f, 0.15f, 0f, -0.65f));
		PartDefinition leaf4 = root.addOrReplaceChild("leaf4", CubeListBuilder.create()
				.texOffs(27, 0).addBox(-0.5f, -2f, -0.5f, 1f, 2f, 1f), PartPose.offsetAndRotation(0.55f, 23f, 0.5f, -0.25f, 0f, 0.65f));
		PartDefinition leaf5 = root.addOrReplaceChild("leaf5", CubeListBuilder.create()
				.texOffs(27, 0).addBox(-0.5f, -2f, -0.5f, 1f, 2f, 1f), PartPose.offsetAndRotation(-0.55f, 23f, 0.5f, -0.25f, 0f, -0.65f));
		PartDefinition leaf6 = root.addOrReplaceChild("leaf6", CubeListBuilder.create()
				.texOffs(27, 0).addBox(-0.5f, -2f, -0.5f, 1f, 2f, 1f), PartPose.offsetAndRotation(0.55f, 23f, 1.5f, -0.25f, 0f, 0.65f));
		PartDefinition leaf7 = root.addOrReplaceChild("leaf7", CubeListBuilder.create()
				.texOffs(27, 0).addBox(-0.5f, -2f, -0.5f, 1f, 2f, 1f), PartPose.offsetAndRotation(-0.55f, 23f, 1.5f, -0.25f, 0f, -0.65f));
		PartDefinition leaf8 = root.addOrReplaceChild("leaf8", CubeListBuilder.create()
				.texOffs(27, 0).addBox(-0.5f, -2f, -0.5f, 1f, 2f, 1f), PartPose.offsetAndRotation(0f, 23f, -1f, 0.15f, 0f, 0f));
		PartDefinition leaf9 = root.addOrReplaceChild("leaf9", CubeListBuilder.create()
				.texOffs(27, 0).addBox(-0.5f, -2f, -0.5f, 1f, 2f, 1f), PartPose.offsetAndRotation(0f, 23f, 1f, -0.25f, 0f, 0f));
		return LayerDefinition.create(mesh, 32, 32);
	}

	@Override
	public void setupAnim(NudibranchRenderState state) {
		super.setupAnim(state);
		float t = state.ageInTicks;
		for (int i = 0; i < this.leaves.length; i++) {
			this.leaves[i].zRot = this.leafRoll[i] + (float) Math.sin(t * 0.08f + i * 0.7f) * 0.08f;
		}
		// Nibbling: the head dips now and then.
		this.head.xRot = (float) Math.max(0.0, Math.sin(t * 0.05f)) * 0.25f;
	}
}
