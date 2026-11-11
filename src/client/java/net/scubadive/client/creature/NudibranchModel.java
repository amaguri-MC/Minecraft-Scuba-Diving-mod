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

/** Nudibranch (Chromodoris type): flat dorsum on a frilled mantle, two rhinophores in front, gill crown on the back. Generated with its texture by tools/K4_gen.py. */
@SuppressWarnings("unused")
public class NudibranchModel extends EntityModel<NudibranchRenderState> {
	public static final ModelLayerLocation LAYER = new ModelLayerLocation(ScubaDive.id("nudibranch"), "main");

	private final ModelPart rhinoLeft;
	private final ModelPart rhinoRight;
	private final ModelPart gills;

	public NudibranchModel(ModelPart root) {
		super(root);
		this.rhinoLeft = root.getChild("rhino_left");
		this.rhinoRight = root.getChild("rhino_right");
		this.gills = root.getChild("gills");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();
		PartDefinition mantle = root.addOrReplaceChild("mantle", CubeListBuilder.create()
				.texOffs(0, 0).addBox(-2.5f, -1f, -3.5f, 5f, 1f, 7f), PartPose.offsetAndRotation(0f, 24f, 0f, 0f, 0f, 0f));
		PartDefinition dorsum = root.addOrReplaceChild("dorsum", CubeListBuilder.create()
				.texOffs(0, 9).addBox(-2f, -2f, -3f, 4f, 1f, 6f), PartPose.offsetAndRotation(0f, 24f, 0f, 0f, 0f, 0f));
		PartDefinition rhinoLeft = root.addOrReplaceChild("rhino_left", CubeListBuilder.create()
				.texOffs(21, 9).addBox(-0.5f, -2f, -0.5f, 1f, 2f, 1f), PartPose.offsetAndRotation(1f, 22f, -2.2f, 0.2f, 0f, 0.15f));
		PartDefinition rhinoRight = root.addOrReplaceChild("rhino_right", CubeListBuilder.create()
				.texOffs(21, 9).addBox(-0.5f, -2f, -0.5f, 1f, 2f, 1f), PartPose.offsetAndRotation(-1f, 22f, -2.2f, 0.2f, 0f, -0.15f));
		PartDefinition gills = root.addOrReplaceChild("gills", CubeListBuilder.create()
				.texOffs(26, 9).addBox(-0.5f, -2f, -0.5f, 1f, 2f, 1f)
				.texOffs(0, 17).addBox(-1.5f, -1f, -0.5f, 1f, 1f, 1f)
				.texOffs(0, 17).addBox(0.5f, -1f, -0.5f, 1f, 1f, 1f), PartPose.offsetAndRotation(0f, 22f, 1.8f, -0.25f, 0f, 0f));
		return LayerDefinition.create(mesh, 32, 32);
	}

	@Override
	public void setupAnim(NudibranchRenderState state) {
		super.setupAnim(state);
		float t = state.ageInTicks;
		this.rhinoLeft.xRot = 0.2f + (float) Math.sin(t * 0.15f) * 0.2f;
		this.rhinoRight.xRot = 0.2f + (float) Math.sin(t * 0.15f + 1.2f) * 0.2f;
		this.gills.zRot = (float) Math.sin(t * 0.1f) * 0.12f;
		this.gills.xRot = -0.25f + (float) Math.sin(t * 0.07f) * 0.1f;
	}

	public static Identifier texture(int variant) {
		return switch (variant) {
			case 1 -> ScubaDive.id("textures/entity/nudibranch_orange.png");
			case 2 -> ScubaDive.id("textures/entity/nudibranch_black_white.png");
			default -> ScubaDive.id("textures/entity/nudibranch_blue.png");
		};
	}
}
