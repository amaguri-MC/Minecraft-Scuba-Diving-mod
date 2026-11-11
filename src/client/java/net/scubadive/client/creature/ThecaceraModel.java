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

/** Thecacera pennigera: orange slug spotted black and blue, rhinophores backed by tall horn-like sheaths, a gill crown and two long dark-tipped processes behind it. Generated with its texture by tools/K4_gen.py. */
@SuppressWarnings("unused")
public class ThecaceraModel extends EntityModel<NudibranchRenderState> {
	public static final ModelLayerLocation LAYER = new ModelLayerLocation(ScubaDive.id("thecacera"), "main");

	private final ModelPart rhinoL;
	private final ModelPart rhinoR;
	private final ModelPart gills;
	private final ModelPart procL;
	private final ModelPart procR;

	public ThecaceraModel(ModelPart root) {
		super(root);
		this.rhinoL = root.getChild("rhino_l");
		this.rhinoR = root.getChild("rhino_r");
		this.gills = root.getChild("gills");
		this.procL = root.getChild("proc_l");
		this.procR = root.getChild("proc_r");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();
		PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create()
				.texOffs(0, 0).addBox(-1f, -2f, -3f, 2f, 2f, 5f)
				.texOffs(15, 0).addBox(-0.5f, -1f, 2f, 1f, 1f, 2f), PartPose.offsetAndRotation(0f, 24f, 0f, 0f, 0f, 0f));
		PartDefinition rhinoL = root.addOrReplaceChild("rhino_l", CubeListBuilder.create()
				.texOffs(22, 0).addBox(-0.5f, -2f, -0.5f, 1f, 2f, 1f), PartPose.offsetAndRotation(0.6f, 22f, -2.4f, 0.1f, 0f, 0.12f));
		PartDefinition rhinoR = root.addOrReplaceChild("rhino_r", CubeListBuilder.create()
				.texOffs(22, 0).addBox(-0.5f, -2f, -0.5f, 1f, 2f, 1f), PartPose.offsetAndRotation(-0.6f, 22f, -2.4f, 0.1f, 0f, -0.12f));
		PartDefinition sheathL = root.addOrReplaceChild("sheath_l", CubeListBuilder.create()
				.texOffs(27, 0).addBox(-0.5f, -3f, -0.5f, 1f, 3f, 1f), PartPose.offsetAndRotation(0.6f, 22f, -1.5f, -0.35f, 0f, 0.12f));
		PartDefinition sheathR = root.addOrReplaceChild("sheath_r", CubeListBuilder.create()
				.texOffs(27, 0).addBox(-0.5f, -3f, -0.5f, 1f, 3f, 1f), PartPose.offsetAndRotation(-0.6f, 22f, -1.5f, -0.35f, 0f, -0.12f));
		PartDefinition gills = root.addOrReplaceChild("gills", CubeListBuilder.create()
				.texOffs(0, 8).addBox(-1f, -1f, -0.5f, 2f, 1f, 1f)
				.texOffs(7, 8).addBox(-0.5f, -2f, -0.5f, 1f, 2f, 1f), PartPose.offsetAndRotation(0f, 22f, 0.3f, 0f, 0f, 0f));
		PartDefinition procL = root.addOrReplaceChild("proc_l", CubeListBuilder.create()
				.texOffs(12, 8).addBox(-0.5f, -2f, -0.5f, 1f, 2f, 1f), PartPose.offsetAndRotation(0.8f, 22f, 1.2f, 0.5f, 0f, 0.3f));
		PartDefinition procR = root.addOrReplaceChild("proc_r", CubeListBuilder.create()
				.texOffs(12, 8).addBox(-0.5f, -2f, -0.5f, 1f, 2f, 1f), PartPose.offsetAndRotation(-0.8f, 22f, 1.2f, 0.5f, 0f, -0.3f));
		return LayerDefinition.create(mesh, 32, 32);
	}

	@Override
	public void setupAnim(NudibranchRenderState state) {
		super.setupAnim(state);
		float t = state.ageInTicks;
		this.rhinoL.xRot = 0.1f + (float) Math.sin(t * 0.14f) * 0.15f;
		this.rhinoR.xRot = 0.1f + (float) Math.sin(t * 0.14f + 1.3f) * 0.15f;
		this.gills.zRot = (float) Math.sin(t * 0.09f) * 0.12f;
		this.procL.xRot = 0.5f + (float) Math.sin(t * 0.07f) * 0.1f;
		this.procR.xRot = 0.5f + (float) Math.sin(t * 0.07f + 0.6f) * 0.1f;
	}
}
