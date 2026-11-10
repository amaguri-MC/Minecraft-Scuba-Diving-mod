package net.scubadive.client.creature;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.scubadive.ScubaDive;

/** Box model of a pygmy seahorse: head with snout, body, curled tail. Texture 32x32. */
public class PygmySeahorseModel extends EntityModel<PygmySeahorseRenderState> {
	public static final ModelLayerLocation LAYER = new ModelLayerLocation(ScubaDive.id("pygmy_seahorse"), "main");

	private final ModelPart tail;

	public PygmySeahorseModel(ModelPart root) {
		super(root);
		this.tail = root.getChild("tail");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();
		PartPose origin = PartPose.offset(0.0f, 24.0f, 0.0f);
		// Body at the bottom, head on top, snout toward -z, tail curling forward below the body.
		root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-1.0f, -4.0f, -1.0f, 2.0f, 1.0f, 2.0f), origin);
		root.addOrReplaceChild("snout", CubeListBuilder.create().texOffs(10, 0).addBox(-0.5f, -3.5f, -2.0f, 1.0f, 1.0f, 1.0f), origin);
		root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 5).addBox(-1.0f, -3.0f, -1.0f, 2.0f, 2.0f, 2.0f), origin);
		root.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(10, 5).addBox(-0.5f, -1.0f, -0.5f, 1.0f, 1.0f, 1.0f), origin);
		root.addOrReplaceChild("knob", CubeListBuilder.create().texOffs(16, 5).addBox(-0.5f, -3.5f, 1.0f, 1.0f, 1.0f, 1.0f), origin);
		return LayerDefinition.create(mesh, 32, 32);
	}

	@Override
	public void setupAnim(PygmySeahorseRenderState state) {
		super.setupAnim(state);
		this.tail.xRot = (float) Math.sin(state.ageInTicks * 0.08f) * 0.3f;
	}
}
