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

/** Phyllidia varicosa: flat black mantle with pale blue-grey ridges crowned by rows of yellow tubercles, and two small yellow rhinophores. Generated with its texture by tools/K4_gen.py. */
@SuppressWarnings("unused")
public class PhyllidiaModel extends EntityModel<NudibranchRenderState> {
	public static final ModelLayerLocation LAYER = new ModelLayerLocation(ScubaDive.id("phyllidia"), "main");

	private final ModelPart rhinoL;
	private final ModelPart rhinoR;

	public PhyllidiaModel(ModelPart root) {
		super(root);
		this.rhinoL = root.getChild("rhino_l");
		this.rhinoR = root.getChild("rhino_r");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();
		PartDefinition mantle = root.addOrReplaceChild("mantle", CubeListBuilder.create()
				.texOffs(0, 0).addBox(-2.5f, -1f, -3.5f, 5f, 1f, 7f), PartPose.offsetAndRotation(0f, 24f, 0f, 0f, 0f, 0f));
		PartDefinition ridges = root.addOrReplaceChild("ridges", CubeListBuilder.create()
				.texOffs(0, 9).addBox(-0.5f, -2f, -2.5f, 1f, 1f, 5f)
				.texOffs(13, 9).addBox(-2f, -2f, -2f, 1f, 1f, 4f)
				.texOffs(13, 9).addBox(1f, -2f, -2f, 1f, 1f, 4f)
				.texOffs(24, 9).addBox(-0.5f, -3f, -2f, 1f, 1f, 1f)
				.texOffs(24, 9).addBox(-0.5f, -3f, 0.5f, 1f, 1f, 1f), PartPose.offsetAndRotation(0f, 24f, 0f, 0f, 0f, 0f));
		PartDefinition rhinoL = root.addOrReplaceChild("rhino_l", CubeListBuilder.create()
				.texOffs(0, 16).addBox(-0.5f, -1f, -0.5f, 1f, 1f, 1f), PartPose.offsetAndRotation(1f, 22.5f, -2.8f, 0.2f, 0f, 0.2f));
		PartDefinition rhinoR = root.addOrReplaceChild("rhino_r", CubeListBuilder.create()
				.texOffs(0, 16).addBox(-0.5f, -1f, -0.5f, 1f, 1f, 1f), PartPose.offsetAndRotation(-1f, 22.5f, -2.8f, 0.2f, 0f, -0.2f));
		return LayerDefinition.create(mesh, 32, 32);
	}

	@Override
	public void setupAnim(NudibranchRenderState state) {
		super.setupAnim(state);
		float t = state.ageInTicks;
		this.rhinoL.xRot = 0.2f + (float) Math.sin(t * 0.12f) * 0.15f;
		this.rhinoR.xRot = 0.2f + (float) Math.sin(t * 0.12f + 1.0f) * 0.15f;
	}
}
