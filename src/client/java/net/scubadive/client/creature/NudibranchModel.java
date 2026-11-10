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

/** Box model of a nudibranch: flat body, two rhinophores in front, gill tuft on the back. Texture 32x32. */
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
		PartPose origin = PartPose.offset(0.0f, 24.0f, 0.0f);
		root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0f, -2.0f, -2.5f, 4.0f, 2.0f, 5.0f), origin);
		root.addOrReplaceChild("rhino_left", CubeListBuilder.create().texOffs(0, 8).addBox(0.5f, -4.0f, -2.5f, 1.0f, 2.0f, 1.0f), origin);
		root.addOrReplaceChild("rhino_right", CubeListBuilder.create().texOffs(4, 8).addBox(-1.5f, -4.0f, -2.5f, 1.0f, 2.0f, 1.0f), origin);
		root.addOrReplaceChild("gills", CubeListBuilder.create().texOffs(0, 12).addBox(-1.5f, -3.0f, 0.0f, 3.0f, 1.0f, 2.0f), origin);
		return LayerDefinition.create(mesh, 32, 32);
	}

	@Override
	public void setupAnim(NudibranchRenderState state) {
		super.setupAnim(state);
		float t = state.ageInTicks;
		this.rhinoLeft.xRot = (float) Math.sin(t * 0.15f) * 0.2f;
		this.rhinoRight.xRot = (float) Math.sin(t * 0.15f + 1.2f) * 0.2f;
		this.gills.zRot = (float) Math.sin(t * 0.1f) * 0.12f;
	}

	public static Identifier texture(int variant) {
		return switch (variant) {
			case 1 -> ScubaDive.id("textures/entity/nudibranch_orange.png");
			case 2 -> ScubaDive.id("textures/entity/nudibranch_black_white.png");
			default -> ScubaDive.id("textures/entity/nudibranch_blue.png");
		};
	}
}
