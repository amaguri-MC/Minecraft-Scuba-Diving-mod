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
 * Shared reef fish model. Several body shapes are drawn from the same part names; each species
 * picks a shape and its own texture. Generated with the textures (tools/F2_reef.py).
 */
@SuppressWarnings("unused")
public class ReefFishModel extends EntityModel<ReefFishRenderState> {
	public static final ModelLayerLocation SLENDER = layer("reef_fish_slender");
	public static final ModelLayerLocation DISC = layer("reef_fish_disc");
	public static final ModelLayerLocation IDOL = layer("reef_fish_idol");
	public static final ModelLayerLocation PARROT = layer("reef_fish_parrot");
	public static final ModelLayerLocation BOX = layer("reef_fish_box");
	public static final ModelLayerLocation TRIGGER = layer("reef_fish_trigger");
	public static final ModelLayerLocation GROUPER = layer("reef_fish_grouper");
	public static final ModelLayerLocation SNAPPER = layer("reef_fish_snapper");

	private final ModelPart body;
	private final ModelPart tail;
	private final ModelPart dorsal;
	private final ModelPart pecL;
	private final ModelPart pecR;
	private final ModelPart whip;
	private final float tailSpeed;
	private final float flapSpeed;

	public ReefFishModel(ModelPart root, float tailSpeed, float flapSpeed) {
		super(root);
		this.tailSpeed = tailSpeed;
		this.flapSpeed = flapSpeed;
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
				.texOffs(53, 0).addBox(-1f, -1f, -6f, 2f, 2f, 1f)
				.texOffs(30, 0).addBox(-1f, -1.5f, 3f, 2f, 3f, 2f), PartPose.offsetAndRotation(0f, 20f, 0f, 0f, 0f, 0f));
		PartDefinition tail = body.addOrReplaceChild("tail", CubeListBuilder.create()
				.texOffs(60, 0).addBox(0f, -2f, 0f, 0f, 4f, 2f)
				.texOffs(0, 0).addBox(0f, -4f, 2f, 0f, 3f, 2f)
				.texOffs(13, 0).addBox(0f, 1f, 2f, 0f, 3f, 2f), PartPose.offsetAndRotation(0f, 0f, 5f, 0f, 0f, 0f));
		PartDefinition dorsal = body.addOrReplaceChild("dorsal", CubeListBuilder.create()
				.texOffs(53, 1).addBox(0f, -2f, -2f, 0f, 2f, 3f)
				.texOffs(30, 2).addBox(0f, -2f, 1f, 0f, 2f, 4f), PartPose.offsetAndRotation(0f, -2.5f, 0f, 0f, 0f, 0f));
		PartDefinition anal = body.addOrReplaceChild("anal", CubeListBuilder.create()
				.texOffs(19, 3).addBox(0f, 0f, 0f, 0f, 2f, 4f), PartPose.offsetAndRotation(0f, 2.5f, 0f, 0f, 0f, 0f));
		PartDefinition pecL = body.addOrReplaceChild("pecL", CubeListBuilder.create()
				.texOffs(39, 0).addBox(0f, -1f, 0f, 0f, 3f, 3f), PartPose.offsetAndRotation(1.5f, 0.5f, -2.5f, 0f, 0.3f, 0f));
		PartDefinition pecR = body.addOrReplaceChild("pecR", CubeListBuilder.create()
				.texOffs(46, 0).addBox(0f, -1f, 0f, 0f, 3f, 3f), PartPose.offsetAndRotation(-1.5f, 0.5f, -2.5f, 0f, -0.3f, 0f));
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
				.texOffs(53, 0).addBox(-1f, -1f, -7.5f, 2f, 2f, 2f)
				.texOffs(44, 0).addBox(-1f, -1.5f, 3.5f, 2f, 3f, 2f), PartPose.offsetAndRotation(0f, 18f, 0f, 0f, 0f, 0f));
		PartDefinition tail = body.addOrReplaceChild("tail", CubeListBuilder.create()
				.texOffs(53, 7).addBox(0f, -2f, 0f, 0f, 4f, 2f)
				.texOffs(35, 4).addBox(0f, -4f, 1f, 0f, 3f, 3f)
				.texOffs(24, 6).addBox(0f, 1f, 1f, 0f, 3f, 3f), PartPose.offsetAndRotation(0f, 0f, 5.5f, 0f, 0f, 0f));
		PartDefinition anal = body.addOrReplaceChild("anal", CubeListBuilder.create()
				.texOffs(13, 4).addBox(0f, 0f, -1f, 0f, 2f, 5f), PartPose.offsetAndRotation(0f, 4f, 0f, 0f, 0f, 0f));
		PartDefinition pecL = body.addOrReplaceChild("pecL", CubeListBuilder.create()
				.texOffs(53, 1).addBox(0f, -1f, 0f, 0f, 3f, 4f), PartPose.offsetAndRotation(1.5f, 0.5f, -3f, 0f, 0.3f, 0f));
		PartDefinition pecR = body.addOrReplaceChild("pecR", CubeListBuilder.create()
				.texOffs(44, 2).addBox(0f, -1f, 0f, 0f, 3f, 4f), PartPose.offsetAndRotation(-1.5f, 0.5f, -3f, 0f, -0.3f, 0f));
		PartDefinition dorsal = body.addOrReplaceChild("dorsal", CubeListBuilder.create()
				.texOffs(42, 6).addBox(0f, -2f, -2f, 0f, 2f, 4f)
				.texOffs(58, 6).addBox(0f, -2f, 2f, 0f, 2f, 3f), PartPose.offsetAndRotation(0f, -4f, 0f, 0f, 0f, 0f));
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
				.texOffs(53, 0).addBox(-1f, -1f, -7.5f, 2f, 2f, 2f)
				.texOffs(44, 0).addBox(-1f, -1.5f, 3.5f, 2f, 3f, 2f), PartPose.offsetAndRotation(0f, 18f, 0f, 0f, 0f, 0f));
		PartDefinition tail = body.addOrReplaceChild("tail", CubeListBuilder.create()
				.texOffs(60, 7).addBox(0f, -2f, 0f, 0f, 4f, 2f)
				.texOffs(35, 4).addBox(0f, -4f, 1f, 0f, 3f, 3f)
				.texOffs(24, 6).addBox(0f, 1f, 1f, 0f, 3f, 3f), PartPose.offsetAndRotation(0f, 0f, 5.5f, 0f, 0f, 0f));
		PartDefinition anal = body.addOrReplaceChild("anal", CubeListBuilder.create()
				.texOffs(13, 4).addBox(0f, 0f, -1f, 0f, 2f, 5f), PartPose.offsetAndRotation(0f, 4f, 0f, 0f, 0f, 0f));
		PartDefinition pecL = body.addOrReplaceChild("pecL", CubeListBuilder.create()
				.texOffs(53, 1).addBox(0f, -1f, 0f, 0f, 3f, 4f), PartPose.offsetAndRotation(1.5f, 0.5f, -3f, 0f, 0.3f, 0f));
		PartDefinition pecR = body.addOrReplaceChild("pecR", CubeListBuilder.create()
				.texOffs(44, 2).addBox(0f, -1f, 0f, 0f, 3f, 4f), PartPose.offsetAndRotation(-1.5f, 0.5f, -3f, 0f, -0.3f, 0f));
		PartDefinition dorsal = body.addOrReplaceChild("dorsal", CubeListBuilder.create()
				.texOffs(53, 6).addBox(0f, -3f, -1f, 0f, 3f, 3f)
				.texOffs(42, 7).addBox(0f, -2f, 2f, 0f, 2f, 3f), PartPose.offsetAndRotation(0f, -4f, 0f, 0f, 0f, 0f));
		PartDefinition whip = dorsal.addOrReplaceChild("whip", CubeListBuilder.create()
				.texOffs(62, 0).addBox(0f, -7f, 0f, 0f, 7f, 1f), PartPose.offsetAndRotation(0f, -3f, 1.5f, -0.9f, 0f, 0f));
		return LayerDefinition.create(mesh, 64, 64);
	}

	public static LayerDefinition createParrotLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();
		PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create()
				.texOffs(0, 0).addBox(-2.5f, -4f, -4f, 5f, 8f, 9f)
				.texOffs(29, 0).addBox(-2.5f, -3f, -7f, 5f, 6f, 3f)
				.texOffs(20, 0).addBox(-1.5f, -1f, -8f, 3f, 3f, 1f)
				.texOffs(46, 0).addBox(-1.5f, -2f, 5f, 3f, 4f, 2f), PartPose.offsetAndRotation(0f, 17f, 0f, 0f, 0f, 0f));
		PartDefinition tail = body.addOrReplaceChild("tail", CubeListBuilder.create()
				.texOffs(57, 0).addBox(0f, -3f, 0f, 0f, 6f, 2f)
				.texOffs(29, 7).addBox(0f, -5f, 2f, 0f, 4f, 3f)
				.texOffs(36, 7).addBox(0f, 1f, 2f, 0f, 4f, 3f), PartPose.offsetAndRotation(0f, 0f, 7f, 0f, 0f, 0f));
		PartDefinition dorsal = body.addOrReplaceChild("dorsal", CubeListBuilder.create()
				.texOffs(46, 0).addBox(0f, -2f, -3f, 0f, 2f, 9f), PartPose.offsetAndRotation(0f, -4f, 0f, 0f, 0f, 0f));
		PartDefinition anal = body.addOrReplaceChild("anal", CubeListBuilder.create()
				.texOffs(43, 7).addBox(0f, 0f, 0f, 0f, 2f, 5f), PartPose.offsetAndRotation(0f, 4f, 0f, 0f, 0f, 0f));
		PartDefinition pecL = body.addOrReplaceChild("pecL", CubeListBuilder.create()
				.texOffs(0, 0).addBox(0f, -2f, 0f, 0f, 4f, 4f), PartPose.offsetAndRotation(2.5f, 1f, -2.5f, 0f, 0.3f, 0f));
		PartDefinition pecR = body.addOrReplaceChild("pecR", CubeListBuilder.create()
				.texOffs(0, 0).addBox(0f, -2f, 0f, 0f, 4f, 4f), PartPose.offsetAndRotation(-2.5f, 1f, -2.5f, 0f, -0.3f, 0f));
		return LayerDefinition.create(mesh, 64, 64);
	}

	public static LayerDefinition createBoxLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();
		PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create()
				.texOffs(0, 0).addBox(-3f, -3f, -4f, 6f, 6f, 8f)
				.texOffs(21, 0).addBox(-2f, -2f, -6f, 4f, 4f, 2f)
				.texOffs(0, 0).addBox(-1f, 0f, -7f, 2f, 2f, 1f)
				.texOffs(34, 0).addBox(-1f, -1f, 4f, 2f, 2f, 2f), PartPose.offsetAndRotation(0f, 18f, 0f, 0f, 0f, 0f));
		PartDefinition tail = body.addOrReplaceChild("tail", CubeListBuilder.create()
				.texOffs(48, 0).addBox(0f, -2f, 0f, 0f, 4f, 2f)
				.texOffs(43, 0).addBox(0f, -3f, 2f, 0f, 6f, 2f), PartPose.offsetAndRotation(0f, 0f, 6f, 0f, 0f, 0f));
		PartDefinition dorsal = body.addOrReplaceChild("dorsal", CubeListBuilder.create()
				.texOffs(53, 0).addBox(0f, -2f, 1f, 0f, 2f, 3f), PartPose.offsetAndRotation(0f, -3f, 0f, 0f, 0f, 0f));
		PartDefinition anal = body.addOrReplaceChild("anal", CubeListBuilder.create()
				.texOffs(0, 1).addBox(0f, 0f, 1f, 0f, 2f, 3f), PartPose.offsetAndRotation(0f, 3f, 0f, 0f, 0f, 0f));
		PartDefinition pecL = body.addOrReplaceChild("pecL", CubeListBuilder.create()
				.texOffs(60, 0).addBox(0f, -1f, 0f, 0f, 2f, 2f), PartPose.offsetAndRotation(3f, 0f, -3f, 0f, 0.3f, 0f));
		PartDefinition pecR = body.addOrReplaceChild("pecR", CubeListBuilder.create()
				.texOffs(60, 0).addBox(0f, -1f, 0f, 0f, 2f, 2f), PartPose.offsetAndRotation(-3f, 0f, -3f, 0f, -0.3f, 0f));
		return LayerDefinition.create(mesh, 64, 64);
	}

	public static LayerDefinition createTriggerLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();
		PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create()
				.texOffs(0, 0).addBox(-2f, -4f, -2f, 4f, 8f, 6f)
				.texOffs(21, 0).addBox(-2f, -3f, -5f, 4f, 6f, 3f)
				.texOffs(49, 0).addBox(-1.5f, -2f, -8f, 3f, 4f, 3f)
				.texOffs(41, 13).addBox(-1f, -1f, -9f, 2f, 2f, 1f)
				.texOffs(36, 0).addBox(-2f, -3f, 4f, 4f, 6f, 2f)
				.texOffs(56, 8).addBox(-1f, -2f, 6f, 2f, 4f, 1f), PartPose.offsetAndRotation(0f, 17f, 0f, 0f, 0f, 0f));
		PartDefinition tail = body.addOrReplaceChild("tail", CubeListBuilder.create()
				.texOffs(36, 7).addBox(0f, -3f, 0f, 0f, 6f, 2f)
				.texOffs(49, 5).addBox(0f, -4f, 2f, 0f, 8f, 3f), PartPose.offsetAndRotation(0f, 0f, 7f, 0f, 0f, 0f));
		PartDefinition dorsal = body.addOrReplaceChild("dorsal", CubeListBuilder.create()
				.texOffs(56, 12).addBox(0f, -4f, -3f, 0f, 4f, 2f)
				.texOffs(21, 4).addBox(0f, -2f, 0f, 0f, 2f, 6f), PartPose.offsetAndRotation(0f, -4f, 0f, 0f, 0f, 0f));
		PartDefinition anal = body.addOrReplaceChild("anal", CubeListBuilder.create()
				.texOffs(21, 7).addBox(0f, 0f, 0f, 0f, 2f, 6f), PartPose.offsetAndRotation(0f, 4f, 0f, 0f, 0f, 0f));
		PartDefinition pecL = body.addOrReplaceChild("pecL", CubeListBuilder.create()
				.texOffs(41, 6).addBox(0f, -1f, 0f, 0f, 3f, 3f), PartPose.offsetAndRotation(2f, 1f, -3f, 0f, 0.3f, 0f));
		PartDefinition pecR = body.addOrReplaceChild("pecR", CubeListBuilder.create()
				.texOffs(41, 6).addBox(0f, -1f, 0f, 0f, 3f, 3f), PartPose.offsetAndRotation(-2f, 1f, -3f, 0f, -0.3f, 0f));
		return LayerDefinition.create(mesh, 64, 64);
	}

	public static LayerDefinition createGrouperLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();
		PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create()
				.texOffs(0, 0).addBox(-3f, -5f, -5f, 6f, 10f, 11f)
				.texOffs(35, 0).addBox(-3f, -4f, -10f, 6f, 8f, 5f)
				.texOffs(13, 22).addBox(-2f, -3f, -12f, 4f, 3f, 2f)
				.texOffs(47, 21).addBox(-2.5f, 0f, -13f, 5f, 3f, 3f)
				.texOffs(35, 14).addBox(-2f, -3f, 6f, 4f, 6f, 3f), PartPose.offsetAndRotation(0f, 18f, 0f, 0f, 0f, 0f));
		PartDefinition tail = body.addOrReplaceChild("tail", CubeListBuilder.create()
				.texOffs(26, 19).addBox(0f, -4f, 0f, 0f, 8f, 3f)
				.texOffs(58, 0).addBox(0f, -6f, 3f, 0f, 12f, 3f), PartPose.offsetAndRotation(0f, 0f, 9f, 0f, 0f, 0f));
		PartDefinition dorsal = body.addOrReplaceChild("dorsal", CubeListBuilder.create()
				.texOffs(50, 9).addBox(0f, -3f, -4f, 0f, 3f, 7f)
				.texOffs(33, 18).addBox(0f, -3f, 3f, 0f, 3f, 6f), PartPose.offsetAndRotation(0f, -5f, 0f, 0f, 0f, 0f));
		PartDefinition anal = body.addOrReplaceChild("anal", CubeListBuilder.create()
				.texOffs(13, 22).addBox(0f, 0f, 1f, 0f, 3f, 6f), PartPose.offsetAndRotation(0f, 5f, 0f, 0f, 0f, 0f));
		PartDefinition pecL = body.addOrReplaceChild("pecL", CubeListBuilder.create()
				.texOffs(0, 16).addBox(0f, -3f, 0f, 0f, 6f, 6f), PartPose.offsetAndRotation(3f, 1.5f, -4f, 0f, 0.3f, 0f));
		PartDefinition pecR = body.addOrReplaceChild("pecR", CubeListBuilder.create()
				.texOffs(0, 16).addBox(0f, -3f, 0f, 0f, 6f, 6f), PartPose.offsetAndRotation(-3f, 1.5f, -4f, 0f, -0.3f, 0f));
		return LayerDefinition.create(mesh, 64, 64);
	}

	public static LayerDefinition createSnapperLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();
		PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create()
				.texOffs(0, 0).addBox(-3f, -5f, -6f, 6f, 10f, 14f)
				.texOffs(27, 0).addBox(-3f, -4f, -10f, 6f, 8f, 4f)
				.texOffs(63, 0).addBox(-2f, -2f, -12f, 4f, 4f, 2f)
				.texOffs(48, 0).addBox(-2f, -3f, 8f, 4f, 6f, 3f), PartPose.offsetAndRotation(0f, 17f, 0f, 0f, 0f, 0f));
		PartDefinition tail = body.addOrReplaceChild("tail", CubeListBuilder.create()
				.texOffs(76, 0).addBox(0f, -4f, 0f, 0f, 8f, 3f)
				.texOffs(117, 0).addBox(0f, -8f, 3f, 0f, 5f, 4f)
				.texOffs(63, 3).addBox(0f, 3f, 3f, 0f, 5f, 4f), PartPose.offsetAndRotation(0f, 0f, 11f, 0f, 0f, 0f));
		PartDefinition dorsal = body.addOrReplaceChild("dorsal", CubeListBuilder.create()
				.texOffs(48, 4).addBox(0f, -3f, -3f, 0f, 3f, 6f)
				.texOffs(83, 0).addBox(0f, -3f, 3f, 0f, 3f, 8f), PartPose.offsetAndRotation(0f, -5f, 0f, 0f, 0f, 0f));
		PartDefinition anal = body.addOrReplaceChild("anal", CubeListBuilder.create()
				.texOffs(100, 0).addBox(0f, 0f, 2f, 0f, 3f, 8f), PartPose.offsetAndRotation(0f, 5f, 0f, 0f, 0f, 0f));
		PartDefinition pecL = body.addOrReplaceChild("pecL", CubeListBuilder.create()
				.texOffs(0, 0).addBox(0f, -2f, 0f, 0f, 6f, 6f), PartPose.offsetAndRotation(3f, 1f, -5f, 0f, 0.3f, 0f));
		PartDefinition pecR = body.addOrReplaceChild("pecR", CubeListBuilder.create()
				.texOffs(0, 0).addBox(0f, -2f, 0f, 0f, 6f, 6f), PartPose.offsetAndRotation(-3f, 1f, -5f, 0f, -0.3f, 0f));
		return LayerDefinition.create(mesh, 128, 128);
	}

	@Override
	public void setupAnim(ReefFishRenderState state) {
		super.setupAnim(state);
		float t = state.ageInTicks;
		float swim = Math.min(1.0f, state.walkAnimationSpeed * 4.0f);
		// Tail sweeps faster and wider while the fish is moving; pectorals paddle at rest.
		this.tail.yRot = (float) Math.sin(t * (this.tailSpeed + swim * 0.5f)) * (0.12f + swim * 0.35f);
		this.body.yRot = -this.tail.yRot * 0.25f;
		float flap = (float) Math.sin(t * this.flapSpeed);
		this.pecL.yRot = 0.3f + flap * 0.2f;
		this.pecR.yRot = -0.3f - flap * 0.2f;
		this.dorsal.xRot = (float) Math.sin(t * 0.05f) * 0.03f;
		if (this.whip != null) {
			this.whip.xRot = -0.9f + (float) Math.sin(t * 0.08f) * 0.12f;
		}
	}
}
