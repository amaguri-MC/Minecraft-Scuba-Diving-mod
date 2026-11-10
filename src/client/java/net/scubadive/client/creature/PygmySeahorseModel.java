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

/**
 * Pygmy seahorse (Hippocampus bargibanti): upright body covered in pink tubercles, short snout,
 * small dorsal fin and a prehensile tail curling forward. Built on a fine grid and scaled down
 * in {@link PygmySeahorseRenderer}. Generated together with its 64x64 texture.
 */
@SuppressWarnings("unused")
public class PygmySeahorseModel extends EntityModel<PygmySeahorseRenderState> {
	public static final ModelLayerLocation LAYER = new ModelLayerLocation(ScubaDive.id("pygmy_seahorse"), "main");

	private final ModelPart torso;
	private final ModelPart fin;
	private final ModelPart tail1;
	private final ModelPart tail2;
	private final ModelPart tail3;
	private final ModelPart tail4;
	private final ModelPart tail5;

	public PygmySeahorseModel(ModelPart root) {
		super(root);
		this.torso = root.getChild("torso");
		this.fin = this.torso.getChild("fin");
		this.tail1 = root.getChild("tail1");
		this.tail2 = this.tail1.getChild("tail2");
		this.tail3 = this.tail2.getChild("tail3");
		this.tail4 = this.tail3.getChild("tail4");
		this.tail5 = this.tail4.getChild("tail5");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();
		PartDefinition torso = root.addOrReplaceChild("torso", CubeListBuilder.create()
				.texOffs(0, 0).addBox(-1.5f, -6f, -1.5f, 3f, 6f, 3f)
				.texOffs(13, 0).addBox(-1.5f, -5f, -2.5f, 3f, 4f, 1f)
				.texOffs(22, 0).addBox(-1f, -6.5f, 1.5f, 2f, 6f, 1f)
				.texOffs(29, 0).addBox(-1f, -8f, -0.5f, 2f, 2f, 2f)
				.texOffs(38, 0).addBox(-1.5f, -11f, -1.5f, 3f, 3f, 3f)
				.texOffs(51, 0).addBox(-0.5f, -12f, -0.5f, 1f, 1f, 1f)
				.texOffs(56, 0).addBox(-0.5f, -10f, -3.5f, 1f, 1f, 2f)
				.texOffs(0, 10).addBox(1.5f, -10.5f, -1f, 0.5f, 1f, 1f)
				.texOffs(4, 10).addBox(-2f, -10.5f, -1f, 0.5f, 1f, 1f)
				.texOffs(8, 10).addBox(1.3f, -4.5f, -1.2f, 1f, 1f, 1f)
				.texOffs(13, 10).addBox(-2.3f, -3.2f, -0.2f, 1f, 1f, 1f)
				.texOffs(18, 10).addBox(1.3f, -2.2f, 0.4f, 1f, 1f, 1f)
				.texOffs(23, 10).addBox(-2.3f, -5.4f, -1.3f, 1f, 1f, 1f)
				.texOffs(28, 10).addBox(0.1f, -3.6f, -3.2f, 1f, 1f, 1f)
				.texOffs(33, 10).addBox(-1.2f, -1.6f, -3.2f, 1f, 1f, 1f)
				.texOffs(38, 10).addBox(-0.6f, -5.6f, -3f, 1f, 1f, 1f)
				.texOffs(43, 10).addBox(1.3f, -11.2f, 0.2f, 1f, 1f, 1f)
				.texOffs(48, 10).addBox(-2.3f, -10f, 0.6f, 1f, 1f, 1f)
				.texOffs(53, 10).addBox(-0.5f, -7.8f, 1.2f, 1f, 1f, 1f), PartPose.offsetAndRotation(0f, 18f, 0f, 0.12f, 0f, 0f));
		PartDefinition fin = torso.addOrReplaceChild("fin", CubeListBuilder.create()
				.texOffs(58, 10).addBox(0f, -1.5f, 0f, 0f, 3f, 2f), PartPose.offsetAndRotation(0f, -3.5f, 2.5f, 0f, 0f, 0f));
		PartDefinition tail1 = root.addOrReplaceChild("tail1", CubeListBuilder.create()
				.texOffs(0, 16).addBox(-1f, 0f, -1f, 2f, 2.5f, 2f)
				.texOffs(9, 16).addBox(0.7f, 0.6f, -0.4f, 0.8f, 0.8f, 0.8f), PartPose.offsetAndRotation(0f, 18f, 0.6f, 0.15f, 0f, 0f));
		PartDefinition tail2 = tail1.addOrReplaceChild("tail2", CubeListBuilder.create()
				.texOffs(14, 16).addBox(-0.75f, 0f, -0.75f, 1.5f, 2.2f, 1.5f), PartPose.offsetAndRotation(0f, 2.5f, 0f, -0.45f, 0f, 0f));
		PartDefinition tail3 = tail2.addOrReplaceChild("tail3", CubeListBuilder.create()
				.texOffs(21, 16).addBox(-0.6f, 0f, -0.6f, 1.2f, 1.8f, 1.2f)
				.texOffs(27, 16).addBox(-1.2f, 0.4f, -0.3f, 0.7f, 0.7f, 0.7f), PartPose.offsetAndRotation(0f, 2.2f, 0f, -0.8f, 0f, 0f));
		PartDefinition tail4 = tail3.addOrReplaceChild("tail4", CubeListBuilder.create()
				.texOffs(31, 16).addBox(-0.5f, 0f, -0.5f, 1f, 1.6f, 1f), PartPose.offsetAndRotation(0f, 1.8f, 0f, -1f, 0f, 0f));
		PartDefinition tail5 = tail4.addOrReplaceChild("tail5", CubeListBuilder.create()
				.texOffs(36, 16).addBox(-0.4f, 0f, -0.4f, 0.8f, 1.4f, 0.8f), PartPose.offsetAndRotation(0f, 1.6f, 0f, -1.1f, 0f, 0f));
		return LayerDefinition.create(mesh, 64, 64);
	}

	@Override
	public void setupAnim(PygmySeahorseRenderState state) {
		super.setupAnim(state);
		float t = state.ageInTicks;
		// Gentle bob, fluttering dorsal fin and a slowly curling tail.
		this.torso.xRot = 0.12f + (float) Math.sin(t * 0.05f) * 0.05f;
		this.fin.yRot = (float) Math.sin(t * 0.9f) * 0.35f;
		float curl = (float) Math.sin(t * 0.06f) * 0.12f;
		this.tail2.xRot = -0.45f + curl;
		this.tail3.xRot = -0.8f + curl;
		this.tail4.xRot = -1f + curl * 1.3f;
		this.tail5.xRot = -1.1f + curl * 1.6f;
	}
}
