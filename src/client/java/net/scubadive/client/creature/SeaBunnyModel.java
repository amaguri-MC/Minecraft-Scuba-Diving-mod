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

/** Sea bunny (Jorunna parva): a fluffy white oval with two rabbit-ear rhinophores and a small gill tuft. Generated with its texture by tools/K4_gen.py. */
@SuppressWarnings("unused")
public class SeaBunnyModel extends EntityModel<NudibranchRenderState> {
	public static final ModelLayerLocation LAYER = new ModelLayerLocation(ScubaDive.id("sea_bunny"), "main");

	private final ModelPart body;
	private final ModelPart earLeft;
	private final ModelPart earRight;
	private final ModelPart tuft;

	public SeaBunnyModel(ModelPart root) {
		super(root);
		this.body = root.getChild("body");
		this.earLeft = root.getChild("ear_left");
		this.earRight = root.getChild("ear_right");
		this.tuft = root.getChild("tuft");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();
		PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create()
				.texOffs(0, 0).addBox(-1.5f, -2f, -2.5f, 3f, 2f, 5f)
				.texOffs(17, 0).addBox(-1f, -3f, -1.5f, 2f, 1f, 3f), PartPose.offsetAndRotation(0f, 24f, 0f, 0f, 0f, 0f));
		PartDefinition earLeft = root.addOrReplaceChild("ear_left", CubeListBuilder.create()
				.texOffs(28, 0).addBox(-0.5f, -3f, -0.5f, 1f, 3f, 1f), PartPose.offsetAndRotation(0.8f, 22f, -1.6f, -0.15f, 0f, 0.25f));
		PartDefinition earRight = root.addOrReplaceChild("ear_right", CubeListBuilder.create()
				.texOffs(28, 0).addBox(-0.5f, -3f, -0.5f, 1f, 3f, 1f), PartPose.offsetAndRotation(-0.8f, 22f, -1.6f, -0.15f, 0f, -0.25f));
		PartDefinition tuft = root.addOrReplaceChild("tuft", CubeListBuilder.create()
				.texOffs(0, 8).addBox(-1f, -1f, -0.5f, 2f, 1f, 1f), PartPose.offsetAndRotation(0f, 22f, 1.8f, 0.3f, 0f, 0f));
		return LayerDefinition.create(mesh, 32, 32);
	}

	@Override
	public void setupAnim(NudibranchRenderState state) {
		super.setupAnim(state);
		float t = state.ageInTicks;
		// Ears twitch one after the other, like a rabbit listening.
		float twitch = (float) Math.max(0.0, Math.sin(t * 0.09f)) * 0.25f;
		this.earLeft.xRot = -0.15f + (float) Math.sin(t * 0.13f) * 0.12f - twitch;
		this.earRight.xRot = -0.15f + (float) Math.sin(t * 0.13f + 1.7f) * 0.12f;
		this.earLeft.zRot = 0.25f + (float) Math.sin(t * 0.05f) * 0.06f;
		this.earRight.zRot = -0.25f - (float) Math.sin(t * 0.05f + 0.8f) * 0.06f;
		this.tuft.xRot = 0.3f + (float) Math.sin(t * 0.08f) * 0.12f;
		this.body.y = 24.0f + (float) Math.sin(t * 0.06f) * 0.08f;
	}
}
