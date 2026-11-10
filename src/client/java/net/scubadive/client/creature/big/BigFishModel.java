package net.scubadive.client.creature.big;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.scubadive.ScubaDive;

/**
 * Shared voxel model for the big fish (sharks, barracuda, bigeye trevally, manta ray, stingray).
 * All species use the same part names so one swim animation drives them. Generated together
 * with the 64x64 textures by tools/F1_gen.py.
 */
public class BigFishModel extends EntityModel<BigFishRenderState> {
	public static final ModelLayerLocation REEF_SHARK = new ModelLayerLocation(ScubaDive.id("reef_shark"), "main");
	public static final ModelLayerLocation HAMMERHEAD_SHARK = new ModelLayerLocation(ScubaDive.id("hammerhead_shark"), "main");
	public static final ModelLayerLocation WHALE_SHARK = new ModelLayerLocation(ScubaDive.id("whale_shark"), "main");
	public static final ModelLayerLocation BARRACUDA = new ModelLayerLocation(ScubaDive.id("barracuda"), "main");
	public static final ModelLayerLocation BIGEYE_TREVALLY = new ModelLayerLocation(ScubaDive.id("bigeye_trevally"), "main");
	public static final ModelLayerLocation MANTA_RAY = new ModelLayerLocation(ScubaDive.id("manta_ray"), "main");
	public static final ModelLayerLocation STINGRAY = new ModelLayerLocation(ScubaDive.id("stingray"), "main");

	private final ModelPart body;
	private final ModelPart head;
	private final ModelPart tail1;
	private final ModelPart tail2;
	private final ModelPart tailFin;
	private final ModelPart pecR;
	private final ModelPart pecL;
	private final float speed;
	private final float amp;
	private final float flapAmp;

	public BigFishModel(ModelPart root, float speed, float amp) {
		this(root, speed, amp, 0.12f * amp);
	}

	/** {@code flapAmp}: pectoral fin / wing flap amplitude in radians. */
	public BigFishModel(ModelPart root, float speed, float amp, float flapAmp) {
		super(root);
		this.speed = speed;
		this.amp = amp;
		this.flapAmp = flapAmp;
		this.body = root.getChild("body");
		this.head = this.body.getChild("head");
		this.tail1 = this.body.getChild("tail1");
		this.tail2 = this.tail1.getChild("tail2");
		this.tailFin = this.tail2.getChild("tailfin");
		this.pecR = this.body.getChild("pec_r");
		this.pecL = this.body.getChild("pec_l");
	}

	public static LayerDefinition createReefSharkLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();
		PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create()
				.texOffs(0, 0).addBox(-3f, -3f, -4f, 6f, 6f, 9f)
				.texOffs(50, 8).addBox(-0.5f, -7f, -1f, 1f, 4f, 3f)
				.texOffs(56, 15).addBox(-0.5f, -9f, 0f, 1f, 2f, 3f)
				.texOffs(58, 5).addBox(-0.5f, 3f, 2f, 1f, 1f, 2f), PartPose.offsetAndRotation(0f, 19f, 0f, 0f, 0f, 0f));
		PartDefinition head = body.addOrReplaceChild("head", CubeListBuilder.create()
				.texOffs(30, 10).addBox(-2.5f, -2.5f, -5f, 5f, 4f, 5f)
				.texOffs(0, 24).addBox(-2f, -1.5f, -7f, 4f, 3f, 2f), PartPose.offsetAndRotation(0f, 0.5f, -4f, 0f, 0f, 0f));
		PartDefinition tail1 = body.addOrReplaceChild("tail1", CubeListBuilder.create()
				.texOffs(30, 0).addBox(-2.5f, -2.5f, 0f, 5f, 5f, 5f)
				.texOffs(56, 20).addBox(-0.5f, -4.5f, 1f, 1f, 2f, 3f)
				.texOffs(58, 8).addBox(-0.5f, 2.5f, 2f, 1f, 1f, 2f), PartPose.offsetAndRotation(0f, 0f, 5f, 0f, 0f, 0f));
		PartDefinition tail2 = tail1.addOrReplaceChild("tail2", CubeListBuilder.create()
				.texOffs(0, 15).addBox(-1.5f, -2f, 0f, 3f, 4f, 5f), PartPose.offsetAndRotation(0f, 0f, 5f, 0f, 0f, 0f));
		PartDefinition tailfin = tail2.addOrReplaceChild("tailfin", CubeListBuilder.create()
				.texOffs(50, 0).addBox(-0.5f, -5f, 0f, 1f, 5f, 3f)
				.texOffs(16, 15).addBox(-0.5f, -8f, 2f, 1f, 3f, 3f)
				.texOffs(58, 0).addBox(-0.5f, 0f, 0f, 1f, 3f, 2f), PartPose.offsetAndRotation(0f, 0f, 4f, 0f, 0f, 0f));
		PartDefinition pec_r = body.addOrReplaceChild("pec_r", CubeListBuilder.create()
				.texOffs(24, 19).addBox(0f, -0.5f, 0f, 4f, 1f, 4f), PartPose.offsetAndRotation(3f, 2f, -2f, 0f, -0.5f, 0.5f));
		PartDefinition pec_l = body.addOrReplaceChild("pec_l", CubeListBuilder.create()
				.texOffs(40, 19).addBox(-4f, -0.5f, 0f, 4f, 1f, 4f), PartPose.offsetAndRotation(-3f, 2f, -2f, 0f, 0.5f, -0.5f));
		return LayerDefinition.create(mesh, 64, 64);
	}

	public static LayerDefinition createHammerheadSharkLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();
		PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create()
				.texOffs(0, 0).addBox(-2.5f, -3f, -4f, 5f, 6f, 9f)
				.texOffs(46, 0).addBox(-0.5f, -8f, -1f, 1f, 5f, 4f)
				.texOffs(52, 16).addBox(-0.5f, -10f, 1f, 1f, 2f, 3f)
				.texOffs(58, 21).addBox(-0.5f, 3f, 2f, 1f, 1f, 2f), PartPose.offsetAndRotation(0f, 19.5f, 0f, 0f, 0f, 0f));
		PartDefinition head = body.addOrReplaceChild("head", CubeListBuilder.create()
				.texOffs(44, 9).addBox(-2f, -2f, -3f, 4f, 4f, 3f)
				.texOffs(0, 17).addBox(-8f, -1.5f, -6f, 16f, 3f, 4f), PartPose.offsetAndRotation(0f, 0f, -4f, 0f, 0f, 0f));
		PartDefinition tail1 = body.addOrReplaceChild("tail1", CubeListBuilder.create()
				.texOffs(28, 0).addBox(-2f, -2f, 0f, 4f, 4f, 5f)
				.texOffs(18, 24).addBox(-0.5f, -4f, 1f, 1f, 2f, 3f), PartPose.offsetAndRotation(0f, 0f, 5f, 0f, 0f, 0f));
		PartDefinition tail2 = tail1.addOrReplaceChild("tail2", CubeListBuilder.create()
				.texOffs(28, 9).addBox(-1.5f, -1.5f, 0f, 3f, 3f, 5f), PartPose.offsetAndRotation(0f, 0f, 5f, 0f, 0f, 0f));
		PartDefinition tailfin = tail2.addOrReplaceChild("tailfin", CubeListBuilder.create()
				.texOffs(56, 0).addBox(-0.5f, -6f, 0f, 1f, 6f, 3f)
				.texOffs(44, 16).addBox(-0.5f, -10f, 2f, 1f, 4f, 3f)
				.texOffs(58, 9).addBox(-0.5f, 0f, 0f, 1f, 4f, 2f), PartPose.offsetAndRotation(0f, 0f, 4f, 0f, 0f, 0f));
		PartDefinition pec_r = body.addOrReplaceChild("pec_r", CubeListBuilder.create()
				.texOffs(40, 23).addBox(0f, -0.5f, 0f, 5f, 1f, 4f), PartPose.offsetAndRotation(2.5f, 2f, -2f, 0f, -0.5f, 0.5f));
		PartDefinition pec_l = body.addOrReplaceChild("pec_l", CubeListBuilder.create()
				.texOffs(0, 24).addBox(-5f, -0.5f, 0f, 5f, 1f, 4f), PartPose.offsetAndRotation(-2.5f, 2f, -2f, 0f, 0.5f, -0.5f));
		return LayerDefinition.create(mesh, 64, 64);
	}

	public static LayerDefinition createWhaleSharkLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();
		PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create()
				.texOffs(0, 0).addBox(-5f, -4.5f, -6f, 10f, 9f, 12f)
				.texOffs(54, 11).addBox(-0.5f, -8.5f, 0f, 1f, 4f, 4f)
				.texOffs(42, 22).addBox(-0.5f, 4.5f, 3f, 1f, 1f, 3f), PartPose.offsetAndRotation(0f, 17.6f, 0f, 0f, 0f, 0f));
		PartDefinition head = body.addOrReplaceChild("head", CubeListBuilder.create()
				.texOffs(0, 34).addBox(-6f, -3f, -8f, 12f, 4f, 8f), PartPose.offsetAndRotation(0f, 1.5f, -6f, 0f, 0f, 0f));
		PartDefinition tail1 = body.addOrReplaceChild("tail1", CubeListBuilder.create()
				.texOffs(0, 21).addBox(-3f, -3f, 0f, 6f, 6f, 7f)
				.texOffs(34, 21).addBox(-0.5f, -4.5f, 2f, 1f, 2f, 3f), PartPose.offsetAndRotation(0f, 0f, 6f, 0f, 0f, 0f));
		PartDefinition tail2 = tail1.addOrReplaceChild("tail2", CubeListBuilder.create()
				.texOffs(44, 0).addBox(-2f, -2.5f, 0f, 4f, 5f, 6f), PartPose.offsetAndRotation(0f, 0f, 7f, 0f, 0f, 0f));
		PartDefinition tailfin = tail2.addOrReplaceChild("tailfin", CubeListBuilder.create()
				.texOffs(44, 11).addBox(-0.5f, -7f, 0f, 1f, 7f, 4f)
				.texOffs(26, 21).addBox(-0.5f, -11f, 2f, 1f, 4f, 3f)
				.texOffs(54, 19).addBox(-0.5f, 0f, 0f, 1f, 5f, 3f)
				.texOffs(26, 28).addBox(-0.5f, 4f, 1f, 1f, 3f, 2f), PartPose.offsetAndRotation(0f, 0f, 5f, 0f, 0f, 0f));
		PartDefinition pec_r = body.addOrReplaceChild("pec_r", CubeListBuilder.create()
				.texOffs(34, 27).addBox(0f, -0.5f, 0f, 6f, 1f, 4f), PartPose.offsetAndRotation(5f, 3f, -3f, 0f, -0.5f, 0.45f));
		PartDefinition pec_l = body.addOrReplaceChild("pec_l", CubeListBuilder.create()
				.texOffs(40, 32).addBox(-6f, -0.5f, 0f, 6f, 1f, 4f), PartPose.offsetAndRotation(-5f, 3f, -3f, 0f, 0.5f, -0.45f));
		return LayerDefinition.create(mesh, 64, 64);
	}

	public static LayerDefinition createBarracudaLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();
		PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create()
				.texOffs(0, 0).addBox(-1.5f, -2f, -4f, 3f, 4f, 9f)
				.texOffs(36, 12).addBox(-0.5f, -4f, 0f, 1f, 2f, 3f)
				.texOffs(58, 12).addBox(-0.5f, 2f, 1f, 1f, 1f, 2f), PartPose.offsetAndRotation(0f, 20f, 0f, 0f, 0f, 0f));
		PartDefinition head = body.addOrReplaceChild("head", CubeListBuilder.create()
				.texOffs(38, 0).addBox(-1.5f, -1.5f, -4f, 3f, 3f, 4f)
				.texOffs(38, 7).addBox(-1f, -1f, -8f, 2f, 1f, 4f)
				.texOffs(24, 8).addBox(-1f, 0f, -8.5f, 2f, 1f, 4f), PartPose.offsetAndRotation(0f, -0.5f, -4f, 0f, 0f, 0f));
		PartDefinition tail1 = body.addOrReplaceChild("tail1", CubeListBuilder.create()
				.texOffs(24, 0).addBox(-1f, -1.5f, 0f, 2f, 3f, 5f)
				.texOffs(8, 13).addBox(-0.5f, -2.5f, 1f, 1f, 1f, 2f)
				.texOffs(14, 13).addBox(-0.5f, 1.5f, 1f, 1f, 1f, 2f), PartPose.offsetAndRotation(0f, 0f, 5f, 0f, 0f, 0f));
		PartDefinition tail2 = tail1.addOrReplaceChild("tail2", CubeListBuilder.create()
				.texOffs(44, 12).addBox(-0.5f, -1f, 0f, 1f, 2f, 2f), PartPose.offsetAndRotation(0f, 0f, 5f, 0f, 0f, 0f));
		PartDefinition tailfin = tail2.addOrReplaceChild("tailfin", CubeListBuilder.create()
				.texOffs(52, 0).addBox(-0.5f, -3.5f, 0f, 1f, 3f, 3f)
				.texOffs(52, 6).addBox(-0.5f, 0.5f, 0f, 1f, 3f, 3f)
				.texOffs(60, 0).addBox(-0.5f, -1f, 0f, 1f, 2f, 1f), PartPose.offsetAndRotation(0f, 0f, 1f, 0f, 0f, 0f));
		PartDefinition pec_r = body.addOrReplaceChild("pec_r", CubeListBuilder.create()
				.texOffs(50, 12).addBox(0f, -0.5f, 0f, 2f, 1f, 2f), PartPose.offsetAndRotation(1.5f, 1f, -2f, 0f, -0.4f, 0.5f));
		PartDefinition pec_l = body.addOrReplaceChild("pec_l", CubeListBuilder.create()
				.texOffs(0, 13).addBox(-2f, -0.5f, 0f, 2f, 1f, 2f), PartPose.offsetAndRotation(-1.5f, 1f, -2f, 0f, 0.4f, -0.5f));
		return LayerDefinition.create(mesh, 64, 64);
	}

	public static LayerDefinition createBigeyeTrevallyLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();
		PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create()
				.texOffs(0, 0).addBox(-1.5f, -3.5f, -4f, 3f, 7f, 8f)
				.texOffs(36, 0).addBox(-0.5f, -5.5f, -1f, 1f, 2f, 5f)
				.texOffs(36, 7).addBox(-0.5f, 3.5f, 1f, 1f, 2f, 4f), PartPose.offsetAndRotation(0f, 19f, 0f, 0f, 0f, 0f));
		PartDefinition head = body.addOrReplaceChild("head", CubeListBuilder.create()
				.texOffs(22, 0).addBox(-1.5f, -2.5f, -4f, 3f, 5f, 4f)
				.texOffs(48, 13).addBox(-1f, -1f, -5f, 2f, 3f, 1f), PartPose.offsetAndRotation(0f, 0f, -4f, 0f, 0f, 0f));
		PartDefinition tail1 = body.addOrReplaceChild("tail1", CubeListBuilder.create()
				.texOffs(48, 0).addBox(-1f, -2f, 0f, 2f, 4f, 3f)
				.texOffs(32, 13).addBox(-0.5f, -3f, 0f, 1f, 1f, 3f)
				.texOffs(40, 13).addBox(-0.5f, 2f, 0f, 1f, 1f, 3f), PartPose.offsetAndRotation(0f, 0f, 4f, 0f, 0f, 0f));
		PartDefinition tail2 = tail1.addOrReplaceChild("tail2", CubeListBuilder.create()
				.texOffs(58, 0).addBox(-0.5f, -1.5f, 0f, 1f, 3f, 2f), PartPose.offsetAndRotation(0f, 0f, 3f, 0f, 0f, 0f));
		PartDefinition tailfin = tail2.addOrReplaceChild("tailfin", CubeListBuilder.create()
				.texOffs(46, 7).addBox(-0.5f, -4f, 0f, 1f, 3f, 3f)
				.texOffs(54, 7).addBox(-0.5f, 1f, 0f, 1f, 3f, 3f)
				.texOffs(54, 13).addBox(-0.5f, -1f, 0f, 1f, 2f, 1f), PartPose.offsetAndRotation(0f, 0f, 2f, 0f, 0f, 0f));
		PartDefinition pec_r = body.addOrReplaceChild("pec_r", CubeListBuilder.create()
				.texOffs(22, 9).addBox(0f, -0.5f, 0f, 2f, 1f, 3f), PartPose.offsetAndRotation(1.5f, 1.5f, -2f, 0f, -0.4f, 0.5f));
		PartDefinition pec_l = body.addOrReplaceChild("pec_l", CubeListBuilder.create()
				.texOffs(22, 13).addBox(-2f, -0.5f, 0f, 2f, 1f, 3f), PartPose.offsetAndRotation(-1.5f, 1.5f, -2f, 0f, 0.4f, -0.5f));
		return LayerDefinition.create(mesh, 64, 64);
	}

	public static LayerDefinition createMantaRayLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();
		PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create()
				.texOffs(0, 0).addBox(-5f, -1f, -5f, 10f, 2f, 10f)
				.texOffs(0, 12).addBox(-2f, -2f, -3f, 4f, 1f, 6f), PartPose.offsetAndRotation(0f, 20f, 0f, 0f, 0f, 0f));
		PartDefinition head = body.addOrReplaceChild("head", CubeListBuilder.create()
				.texOffs(32, 22).addBox(-3.5f, -1f, -3f, 7f, 2f, 3f)
				.texOffs(52, 21).addBox(-3.5f, -0.5f, -6f, 2f, 1f, 3f)
				.texOffs(52, 25).addBox(1.5f, -0.5f, -6f, 2f, 1f, 3f), PartPose.offsetAndRotation(0f, 0f, -5f, 0f, 0f, 0f));
		PartDefinition tail1 = body.addOrReplaceChild("tail1", CubeListBuilder.create()
				.texOffs(40, 0).addBox(-0.5f, -0.5f, 0f, 1f, 1f, 7f), PartPose.offsetAndRotation(0f, 0.5f, 5f, 0f, 0f, 0f));
		PartDefinition tail2 = tail1.addOrReplaceChild("tail2", CubeListBuilder.create()
				.texOffs(40, 8).addBox(-0.5f, -0.5f, 0f, 1f, 1f, 7f), PartPose.offsetAndRotation(0f, 0f, 7f, 0f, 0f, 0f));
		PartDefinition tailfin = tail2.addOrReplaceChild("tailfin", CubeListBuilder.create()
				.texOffs(52, 16).addBox(-0.5f, -0.5f, 0f, 1f, 1f, 4f), PartPose.offsetAndRotation(0f, 0f, 7f, 0f, 0f, 0f));
		PartDefinition pec_r = body.addOrReplaceChild("pec_r", CubeListBuilder.create()
				.texOffs(20, 16).addBox(0f, -0.5f, 0f, 11f, 1f, 5f)
				.texOffs(32, 27).addBox(0f, -0.5f, 5f, 6f, 1f, 3f)
				.texOffs(20, 12).addBox(0f, -0.5f, -2f, 4f, 1f, 2f), PartPose.offsetAndRotation(4.5f, 0f, -2f, 0f, -0.25f, 0f));
		PartDefinition pec_l = body.addOrReplaceChild("pec_l", CubeListBuilder.create()
				.texOffs(0, 22).addBox(-11f, -0.5f, 0f, 11f, 1f, 5f)
				.texOffs(0, 28).addBox(-6f, -0.5f, 5f, 6f, 1f, 3f)
				.texOffs(18, 28).addBox(-4f, -0.5f, -2f, 4f, 1f, 2f), PartPose.offsetAndRotation(-4.5f, 0f, -2f, 0f, 0.25f, 0f));
		return LayerDefinition.create(mesh, 64, 64);
	}

	public static LayerDefinition createStingrayLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();
		PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create()
				.texOffs(0, 0).addBox(-4f, -1f, -4f, 8f, 2f, 8f)
				.texOffs(14, 16).addBox(-2f, -2f, -2f, 4f, 1f, 5f), PartPose.offsetAndRotation(0f, 22.5f, 0f, 0f, 0f, 0f));
		PartDefinition head = body.addOrReplaceChild("head", CubeListBuilder.create()
				.texOffs(14, 10).addBox(-2f, -1f, -2f, 4f, 2f, 2f)
				.texOffs(58, 3).addBox(-2.5f, -2f, -1f, 1f, 1f, 1f)
				.texOffs(58, 5).addBox(1.5f, -2f, -1f, 1f, 1f, 1f), PartPose.offsetAndRotation(0f, 0f, -4f, 0f, 0f, 0f));
		PartDefinition tail1 = body.addOrReplaceChild("tail1", CubeListBuilder.create()
				.texOffs(0, 10).addBox(-0.5f, -0.5f, 0f, 1f, 1f, 6f), PartPose.offsetAndRotation(0f, 0.3f, 4f, 0f, 0f, 0f));
		PartDefinition tail2 = tail1.addOrReplaceChild("tail2", CubeListBuilder.create()
				.texOffs(32, 16).addBox(-0.5f, -0.5f, 0f, 1f, 1f, 5f), PartPose.offsetAndRotation(0f, 0f, 6f, 0f, 0f, 0f));
		PartDefinition tailfin = tail2.addOrReplaceChild("tailfin", CubeListBuilder.create()
				.texOffs(44, 16).addBox(-0.5f, -0.5f, 0f, 1f, 1f, 3f)
				.texOffs(58, 0).addBox(-0.5f, -1.5f, 0f, 1f, 1f, 2f), PartPose.offsetAndRotation(0f, 0f, 5f, 0f, 0f, 0f));
		PartDefinition pec_r = body.addOrReplaceChild("pec_r", CubeListBuilder.create()
				.texOffs(32, 0).addBox(0f, -0.5f, 0f, 6f, 1f, 7f)
				.texOffs(52, 16).addBox(0f, -0.5f, 7f, 4f, 1f, 2f), PartPose.offsetAndRotation(4f, 0f, -3f, 0f, 0f, 0f));
		PartDefinition pec_l = body.addOrReplaceChild("pec_l", CubeListBuilder.create()
				.texOffs(32, 8).addBox(-6f, -0.5f, 0f, 6f, 1f, 7f)
				.texOffs(0, 17).addBox(-4f, -0.5f, 7f, 4f, 1f, 2f), PartPose.offsetAndRotation(-4f, 0f, -3f, 0f, 0f, 0f));
		return LayerDefinition.create(mesh, 64, 64);
	}

	@Override
	public void setupAnim(BigFishRenderState state) {
		super.setupAnim(state);
		// Phase advances with distance swum plus a slow idle beat, so the tail never freezes.
		float phase = state.ageInTicks * this.speed * 0.35f + state.walkAnimationPos * this.speed * 1.6f;
		float a = this.amp * (0.7f + Math.min(state.walkAnimationSpeed * 2.0f, 0.6f));
		this.body.yRot = -Mth.sin(phase) * 0.06f * a;
		this.head.yRot = Mth.sin(phase) * 0.08f * a;
		this.tail1.yRot = Mth.sin(phase - 0.7f) * 0.20f * a;
		this.tail2.yRot = Mth.sin(phase - 1.5f) * 0.28f * a;
		this.tailFin.yRot = Mth.sin(phase - 2.3f) * 0.36f * a;
		float flap = Mth.sin(phase * 0.5f) * this.flapAmp;
		// Follow the climb / dive angle (gliding fish steer their pitch).
		this.body.xRot = Mth.clamp(state.xRot * 0.017453292f, -0.6f, 0.6f);
		this.pecR.zRot += flap;
		this.pecL.zRot -= flap;
	}
}
