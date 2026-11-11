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

/** Bornella: slender translucent orange body with a red network, finger-like cerata in clusters along both sides and sheathed rhinophores; it swims by bending its body from side to side. Generated with its texture by tools/K5_gen.py. */
@SuppressWarnings("unused")
public class BornellaModel extends EntityModel<NudibranchRenderState> {
	public static final ModelLayerLocation LAYER = new ModelLayerLocation(ScubaDive.id("bornella"), "main");

	private final ModelPart front;
	private final ModelPart rear;
	private final ModelPart tail;
	private final ModelPart rhinoL;
	private final ModelPart rhinoR;
	private final ModelPart[] clusters = new ModelPart[8];
	private final float[] roll = new float[8];

	public BornellaModel(ModelPart root) {
		super(root);
		this.front = root.getChild("front");
		this.rear = this.front.getChild("rear");
		this.tail = this.rear.getChild("tail");
		this.rhinoL = this.front.getChild("rhino_l");
		this.rhinoR = this.front.getChild("rhino_r");
		for (int k = 0; k < 4; k++) {
			ModelPart parent = k < 2 ? this.front : this.rear;
			this.clusters[k * 2] = parent.getChild("cluster_l" + k);
			this.clusters[k * 2 + 1] = parent.getChild("cluster_r" + k);
		}
		for (int i = 0; i < 8; i++) {
			this.roll[i] = this.clusters[i].zRot;
		}
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();
		PartDefinition front = root.addOrReplaceChild("front", CubeListBuilder.create()
				.texOffs(0, 0).addBox(-1f, -2f, -5f, 2f, 2f, 5f)
				.texOffs(15, 0).addBox(-1.5f, -1f, -6f, 3f, 1f, 1f), PartPose.offsetAndRotation(0f, 24f, 0f, 0f, 0f, 0f));
		PartDefinition rear = front.addOrReplaceChild("rear", CubeListBuilder.create()
				.texOffs(0, 8).addBox(-1f, -2f, 0f, 2f, 2f, 3f), PartPose.offsetAndRotation(0f, 0f, 0f, 0f, 0f, 0f));
		PartDefinition tail = rear.addOrReplaceChild("tail", CubeListBuilder.create()
				.texOffs(11, 8).addBox(-0.5f, -1f, 0f, 1f, 1f, 3f), PartPose.offsetAndRotation(0f, 0f, 3f, 0f, 0f, 0f));
		PartDefinition rhinoL = front.addOrReplaceChild("rhino_l", CubeListBuilder.create()
				.texOffs(20, 8).addBox(-0.5f, -2f, -0.5f, 1f, 2f, 1f)
				.texOffs(25, 8).addBox(0f, -1.5f, -0.5f, 1f, 1f, 1f), PartPose.offsetAndRotation(0.5f, -2f, -4.2f, 0.15f, 0f, 0.15f));
		PartDefinition rhinoR = front.addOrReplaceChild("rhino_r", CubeListBuilder.create()
				.texOffs(20, 8).addBox(-0.5f, -2f, -0.5f, 1f, 2f, 1f)
				.texOffs(25, 8).addBox(-1f, -1.5f, -0.5f, 1f, 1f, 1f), PartPose.offsetAndRotation(-0.5f, -2f, -4.2f, 0.15f, 0f, -0.15f));
		PartDefinition cl0 = front.addOrReplaceChild("cluster_l0", CubeListBuilder.create()
				.texOffs(0, 14).addBox(-0.5f, -3f, -1f, 1f, 3f, 1f)
				.texOffs(5, 14).addBox(-0.5f, -2f, 0f, 1f, 2f, 1f), PartPose.offsetAndRotation(0.8f, -1.6f, -2.8f, 0f, 0f, 0.55f));
		PartDefinition cr0 = front.addOrReplaceChild("cluster_r0", CubeListBuilder.create()
				.texOffs(0, 14).addBox(-0.5f, -3f, -1f, 1f, 3f, 1f)
				.texOffs(5, 14).addBox(-0.5f, -2f, 0f, 1f, 2f, 1f), PartPose.offsetAndRotation(-0.8f, -1.6f, -2.8f, 0f, 0f, -0.55f));
		PartDefinition cl1 = front.addOrReplaceChild("cluster_l1", CubeListBuilder.create()
				.texOffs(0, 14).addBox(-0.5f, -3f, -1f, 1f, 3f, 1f)
				.texOffs(5, 14).addBox(-0.5f, -2f, 0f, 1f, 2f, 1f), PartPose.offsetAndRotation(0.8f, -1.6f, -0.9f, 0f, 0f, 0.55f));
		PartDefinition cr1 = front.addOrReplaceChild("cluster_r1", CubeListBuilder.create()
				.texOffs(0, 14).addBox(-0.5f, -3f, -1f, 1f, 3f, 1f)
				.texOffs(5, 14).addBox(-0.5f, -2f, 0f, 1f, 2f, 1f), PartPose.offsetAndRotation(-0.8f, -1.6f, -0.9f, 0f, 0f, -0.55f));
		PartDefinition cl2 = rear.addOrReplaceChild("cluster_l2", CubeListBuilder.create()
				.texOffs(5, 14).addBox(-0.5f, -2f, -1f, 1f, 2f, 1f)
				.texOffs(5, 14).addBox(-0.5f, -2f, 0f, 1f, 2f, 1f), PartPose.offsetAndRotation(0.8f, -1.6f, 1f, 0f, 0f, 0.7f));
		PartDefinition cr2 = rear.addOrReplaceChild("cluster_r2", CubeListBuilder.create()
				.texOffs(5, 14).addBox(-0.5f, -2f, -1f, 1f, 2f, 1f)
				.texOffs(5, 14).addBox(-0.5f, -2f, 0f, 1f, 2f, 1f), PartPose.offsetAndRotation(-0.8f, -1.6f, 1f, 0f, 0f, -0.7f));
		PartDefinition cl3 = rear.addOrReplaceChild("cluster_l3", CubeListBuilder.create()
				.texOffs(5, 14).addBox(-0.5f, -2f, -1f, 1f, 2f, 1f)
				.texOffs(5, 14).addBox(-0.5f, -2f, 0f, 1f, 2f, 1f), PartPose.offsetAndRotation(0.8f, -1.6f, 2.5f, 0f, 0f, 0.7f));
		PartDefinition cr3 = rear.addOrReplaceChild("cluster_r3", CubeListBuilder.create()
				.texOffs(5, 14).addBox(-0.5f, -2f, -1f, 1f, 2f, 1f)
				.texOffs(5, 14).addBox(-0.5f, -2f, 0f, 1f, 2f, 1f), PartPose.offsetAndRotation(-0.8f, -1.6f, 2.5f, 0f, 0f, -0.7f));
		return LayerDefinition.create(mesh, 32, 32);
	}

	@Override
	public void setupAnim(NudibranchRenderState state) {
		super.setupAnim(state);
		float t = state.ageInTicks;
		boolean swim = state.dancing;
		// A side-to-side wave runs down the body; big and quick while swimming, a faint sway while crawling.
		float amp = swim ? 0.5f : 0.06f;
		float rate = swim ? 0.45f : 0.07f;
		this.front.yRot = (float) Math.sin(t * rate) * amp * 0.4f;
		this.rear.yRot = (float) Math.sin(t * rate - 1.2f) * amp;
		this.tail.yRot = (float) Math.sin(t * rate - 2.4f) * amp * 1.4f;
		this.front.y = 24.0f + (swim ? -0.6f + (float) Math.sin(t * rate * 2.0f) * 0.25f : 0.0f);
		// The finger clusters flare out while swimming and sway gently otherwise.
		for (int i = 0; i < 8; i++) {
			float side = i % 2 == 0 ? 1.0f : -1.0f;
			float flare = swim ? 0.3f + (float) Math.sin(t * rate + i * 0.6f) * 0.2f : (float) Math.sin(t * 0.08f + i * 0.9f) * 0.08f;
			this.clusters[i].zRot = this.roll[i] + flare * side;
		}
		this.rhinoL.xRot = 0.15f + (float) Math.sin(t * 0.13f) * 0.15f;
		this.rhinoR.xRot = 0.15f + (float) Math.sin(t * 0.13f + 1.1f) * 0.15f;
	}
}
