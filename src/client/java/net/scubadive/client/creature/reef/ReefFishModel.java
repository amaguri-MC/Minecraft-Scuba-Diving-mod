package net.scubadive.client.creature.reef;

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
 * Shared reef fish model. Four body shapes (slender, disc, idol, lion) are drawn from the same
 * part names; each species picks a shape and its own texture. Generated with the textures.
 */
@SuppressWarnings("unused")
public class ReefFishModel extends EntityModel<ReefFishRenderState> {
	public static final ModelLayerLocation SLENDER = layer("reef_fish_slender");
	public static final ModelLayerLocation DISC = layer("reef_fish_disc");
	public static final ModelLayerLocation IDOL = layer("reef_fish_idol");
	public static final ModelLayerLocation LION = layer("reef_fish_lion");

	private final ModelPart body;
	private final ModelPart tail;
	private final ModelPart dorsal;
	private final ModelPart pecL;
	private final ModelPart pecR;
	private final ModelPart whip;
	private final boolean lion;

	public ReefFishModel(ModelPart root, boolean lion) {
		super(root);
		this.lion = lion;
		this.body = root.getChild("body");
		this.tail = this.body.getChild("tail");
		this.dorsal = this.body.getChild("dorsal");
		this.pecL = this.body.getChild("pecL");
		this.pecR = this.body.getChild("pecR");
		this.whip = this.dorsal.hasChild("whip") ? this.dorsal.getChild("whip") : null;
	}

	private static ModelLayerLocation layer(String name) {
		return new ModelLayerLocation(ScubaDive.id(name), "main");
	}

	public static LayerDefinition createSlenderLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();
		PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create()
				.texOffs(0, 0).addBox(-1.5f, -2.5f, -3f, 3f, 5f, 6f)
				.texOffs(19, 0).addBox(-1.5f, -2f, -5f, 3f, 4f, 2f)
				.texOffs(30, 0).addBox(-1f, -1f, -6f, 2f, 2f, 1f)
				.texOffs(37, 0).addBox(-1f, -1.5f, 3f, 2f, 3f, 2f), PartPose.offsetAndRotation(0f, 20f, 0f, 0f, 0f, 0f));
		PartDefinition tail = body.addOrReplaceChild("tail", CubeListBuilder.create()
				.texOffs(46, 0).addBox(0f, -2f, 0f, 0f, 4f, 2f)
				.texOffs(51, 0).addBox(0f, -4f, 2f, 0f, 3f, 2f)
				.texOffs(56, 0).addBox(0f, 1f, 2f, 0f, 3f, 2f), PartPose.offsetAndRotation(0f, 0f, 5f, 0f, 0f, 0f));
		PartDefinition dorsal = body.addOrReplaceChild("dorsal", CubeListBuilder.create()
				.texOffs(0, 12).addBox(0f, -2f, -2f, 0f, 2f, 3f)
				.texOffs(7, 12).addBox(0f, -2f, 1f, 0f, 2f, 4f), PartPose.offsetAndRotation(0f, -2.5f, 0f, 0f, 0f, 0f));
		PartDefinition anal = body.addOrReplaceChild("anal", CubeListBuilder.create()
				.texOffs(16, 12).addBox(0f, 0f, 0f, 0f, 2f, 4f), PartPose.offsetAndRotation(0f, 2.5f, 0f, 0f, 0f, 0f));
		PartDefinition pecL = body.addOrReplaceChild("pecL", CubeListBuilder.create()
				.texOffs(25, 12).addBox(0f, -1f, 0f, 0f, 3f, 3f), PartPose.offsetAndRotation(1.5f, 0.5f, -2.5f, 0f, 0.3f, 0f));
		PartDefinition pecR = body.addOrReplaceChild("pecR", CubeListBuilder.create()
				.texOffs(32, 12).addBox(0f, -1f, 0f, 0f, 3f, 3f), PartPose.offsetAndRotation(-1.5f, 0.5f, -2.5f, 0f, -0.3f, 0f));
		return LayerDefinition.create(mesh, 64, 64);
	}

	public static LayerDefinition createDiscLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();
		PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create()
				.texOffs(0, 0).addBox(-1.5f, -4f, -1.5f, 3f, 8f, 3f)
				.texOffs(13, 0).addBox(-1.5f, -3f, -3.5f, 3f, 6f, 2f)
				.texOffs(24, 0).addBox(-1.5f, -3f, 1.5f, 3f, 6f, 2f)
				.texOffs(35, 0).addBox(-1f, -2f, -5.5f, 2f, 4f, 2f)
				.texOffs(44, 0).addBox(-1f, -1f, -7.5f, 2f, 2f, 2f)
				.texOffs(53, 0).addBox(-1f, -1.5f, 3.5f, 2f, 3f, 2f), PartPose.offsetAndRotation(0f, 18f, 0f, 0f, 0f, 0f));
		PartDefinition tail = body.addOrReplaceChild("tail", CubeListBuilder.create()
				.texOffs(0, 12).addBox(0f, -2f, 0f, 0f, 4f, 2f)
				.texOffs(5, 12).addBox(0f, -4f, 1f, 0f, 3f, 3f)
				.texOffs(12, 12).addBox(0f, 1f, 1f, 0f, 3f, 3f), PartPose.offsetAndRotation(0f, 0f, 5.5f, 0f, 0f, 0f));
		PartDefinition anal = body.addOrReplaceChild("anal", CubeListBuilder.create()
				.texOffs(19, 12).addBox(0f, 0f, -1f, 0f, 2f, 5f), PartPose.offsetAndRotation(0f, 4f, 0f, 0f, 0f, 0f));
		PartDefinition pecL = body.addOrReplaceChild("pecL", CubeListBuilder.create()
				.texOffs(30, 12).addBox(0f, -1f, 0f, 0f, 3f, 4f), PartPose.offsetAndRotation(1.5f, 0.5f, -3f, 0f, 0.3f, 0f));
		PartDefinition pecR = body.addOrReplaceChild("pecR", CubeListBuilder.create()
				.texOffs(39, 12).addBox(0f, -1f, 0f, 0f, 3f, 4f), PartPose.offsetAndRotation(-1.5f, 0.5f, -3f, 0f, -0.3f, 0f));
		PartDefinition dorsal = body.addOrReplaceChild("dorsal", CubeListBuilder.create()
				.texOffs(48, 12).addBox(0f, -2f, -2f, 0f, 2f, 4f)
				.texOffs(57, 12).addBox(0f, -2f, 2f, 0f, 2f, 3f), PartPose.offsetAndRotation(0f, -4f, 0f, 0f, 0f, 0f));
		return LayerDefinition.create(mesh, 64, 64);
	}

	public static LayerDefinition createIdolLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();
		PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create()
				.texOffs(0, 0).addBox(-1.5f, -4f, -1.5f, 3f, 8f, 3f)
				.texOffs(13, 0).addBox(-1.5f, -3f, -3.5f, 3f, 6f, 2f)
				.texOffs(24, 0).addBox(-1.5f, -3f, 1.5f, 3f, 6f, 2f)
				.texOffs(35, 0).addBox(-1f, -2f, -5.5f, 2f, 4f, 2f)
				.texOffs(44, 0).addBox(-1f, -1f, -7.5f, 2f, 2f, 2f)
				.texOffs(53, 0).addBox(-1f, -1.5f, 3.5f, 2f, 3f, 2f), PartPose.offsetAndRotation(0f, 18f, 0f, 0f, 0f, 0f));
		PartDefinition tail = body.addOrReplaceChild("tail", CubeListBuilder.create()
				.texOffs(0, 12).addBox(0f, -2f, 0f, 0f, 4f, 2f)
				.texOffs(5, 12).addBox(0f, -4f, 1f, 0f, 3f, 3f)
				.texOffs(12, 12).addBox(0f, 1f, 1f, 0f, 3f, 3f), PartPose.offsetAndRotation(0f, 0f, 5.5f, 0f, 0f, 0f));
		PartDefinition anal = body.addOrReplaceChild("anal", CubeListBuilder.create()
				.texOffs(19, 12).addBox(0f, 0f, -1f, 0f, 2f, 5f), PartPose.offsetAndRotation(0f, 4f, 0f, 0f, 0f, 0f));
		PartDefinition pecL = body.addOrReplaceChild("pecL", CubeListBuilder.create()
				.texOffs(30, 12).addBox(0f, -1f, 0f, 0f, 3f, 4f), PartPose.offsetAndRotation(1.5f, 0.5f, -3f, 0f, 0.3f, 0f));
		PartDefinition pecR = body.addOrReplaceChild("pecR", CubeListBuilder.create()
				.texOffs(39, 12).addBox(0f, -1f, 0f, 0f, 3f, 4f), PartPose.offsetAndRotation(-1.5f, 0.5f, -3f, 0f, -0.3f, 0f));
		PartDefinition dorsal = body.addOrReplaceChild("dorsal", CubeListBuilder.create()
				.texOffs(48, 12).addBox(0f, -3f, -1f, 0f, 3f, 3f)
				.texOffs(55, 12).addBox(0f, -2f, 2f, 0f, 2f, 3f), PartPose.offsetAndRotation(0f, -4f, 0f, 0f, 0f, 0f));
		PartDefinition whip = dorsal.addOrReplaceChild("whip", CubeListBuilder.create()
				.texOffs(62, 12).addBox(0f, -7f, 0f, 0f, 7f, 1f), PartPose.offsetAndRotation(0f, -3f, 1.5f, -0.9f, 0f, 0f));
		return LayerDefinition.create(mesh, 64, 64);
	}

	public static LayerDefinition createLionLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();
		PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create()
				.texOffs(0, 0).addBox(-2f, -3f, -3f, 4f, 6f, 6f)
				.texOffs(21, 0).addBox(-2f, -2f, -6f, 4f, 5f, 3f)
				.texOffs(36, 0).addBox(-1f, 1f, -7f, 2f, 2f, 1f)
				.texOffs(43, 0).addBox(-2f, -3f, -5f, 1f, 1f, 1f)
				.texOffs(48, 0).addBox(1f, -3f, -5f, 1f, 1f, 1f)
				.texOffs(53, 0).addBox(-1f, -2f, 3f, 2f, 4f, 2f), PartPose.offsetAndRotation(0f, 19f, 0f, 0f, 0f, 0f));
		PartDefinition tail = body.addOrReplaceChild("tail", CubeListBuilder.create()
				.texOffs(0, 13).addBox(0f, -3f, 0f, 0f, 6f, 4f), PartPose.offsetAndRotation(0f, 0f, 5f, 0f, 0f, 0f));
		PartDefinition dorsal = body.addOrReplaceChild("dorsal", CubeListBuilder.create()
				.texOffs(9, 13).addBox(0f, -5f, -2.5f, 0f, 5f, 1f)
				.texOffs(12, 13).addBox(0f, -6f, -1f, 0f, 6f, 1f)
				.texOffs(15, 13).addBox(0f, -6f, 0.5f, 0f, 6f, 1f)
				.texOffs(18, 13).addBox(0f, -5f, 2f, 0f, 5f, 1f)
				.texOffs(21, 13).addBox(0f, -3f, 3f, 0f, 3f, 3f), PartPose.offsetAndRotation(0f, -3f, 0f, 0f, 0f, 0f));
		PartDefinition anal = body.addOrReplaceChild("anal", CubeListBuilder.create()
				.texOffs(28, 13).addBox(0f, 0f, 0.5f, 0f, 2f, 3f), PartPose.offsetAndRotation(0f, 3f, 0f, 0f, 0f, 0f));
		PartDefinition pecL = body.addOrReplaceChild("pecL", CubeListBuilder.create()
				.texOffs(35, 13).addBox(0f, -4f, 0f, 0f, 8f, 6f), PartPose.offsetAndRotation(2.05f, 0f, -2.5f, 0f, 0.7f, 0f));
		PartDefinition pecR = body.addOrReplaceChild("pecR", CubeListBuilder.create()
				.texOffs(48, 13).addBox(0f, -4f, 0f, 0f, 8f, 6f), PartPose.offsetAndRotation(-2.05f, 0f, -2.5f, 0f, -0.7f, 0f));
		return LayerDefinition.create(mesh, 64, 64);
	}

	@Override
	public void setupAnim(ReefFishRenderState state) {
		super.setupAnim(state);
		float t = state.ageInTicks;
		float swim = Math.min(1.0f, state.walkAnimationSpeed * 4.0f);
		float tailSpeed = this.lion ? 0.12f : 0.25f;
		// Tail sweeps faster and wider while the fish is moving; pectorals paddle at rest.
		this.tail.yRot = (float) Math.sin(t * (tailSpeed + swim * 0.5f)) * (0.12f + swim * 0.35f);
		this.body.yRot = -this.tail.yRot * 0.25f;
		float flap = (float) Math.sin(t * (this.lion ? 0.09f : 0.22f));
		float rest = this.lion ? 0.7f : 0.3f;
		this.pecL.yRot = rest + flap * (this.lion ? 0.12f : 0.2f);
		this.pecR.yRot = -rest - flap * (this.lion ? 0.12f : 0.2f);
		this.dorsal.xRot = (float) Math.sin(t * 0.05f) * 0.03f;
		if (this.whip != null) {
			this.whip.xRot = -0.9f + (float) Math.sin(t * 0.08f) * 0.12f;
		}
	}
}
