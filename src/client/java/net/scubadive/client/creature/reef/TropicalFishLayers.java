package net.scubadive.client.creature.reef;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.scubadive.ScubaDive;

/**
 * Body shapes for the K1 tropical fish, drawn by {@link ReefFishModel} (same part names: body, tail,
 * dorsal, anal, pecL, pecR). Generated with the textures (tools/K1_reef.py).
 */
@SuppressWarnings("unused")
public final class TropicalFishLayers {
	public static final ModelLayerLocation YELLOW_TANG = new ModelLayerLocation(ScubaDive.id("yellow_tang"), "main");
	public static final ModelLayerLocation CLEANER_WRASSE = new ModelLayerLocation(ScubaDive.id("cleaner_wrasse"), "main");
	public static final ModelLayerLocation MANDARINFISH = new ModelLayerLocation(ScubaDive.id("mandarinfish"), "main");
	public static final ModelLayerLocation NAPOLEON_WRASSE = new ModelLayerLocation(ScubaDive.id("napoleon_wrasse"), "main");

	private TropicalFishLayers() {
	}

	public static LayerDefinition createTangLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();
		PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create()
				.texOffs(0, 0).addBox(-1.5f, -5f, -2f, 3f, 10f, 4f)
				.texOffs(15, 0).addBox(-1.5f, -4f, -4f, 3f, 8f, 2f)
				.texOffs(26, 0).addBox(-1.5f, -4f, 2f, 3f, 8f, 2f)
				.texOffs(37, 0).addBox(-1f, -3f, -6f, 2f, 5f, 2f)
				.texOffs(52, 11).addBox(-0.5f, -1f, -8f, 1f, 2f, 2f)
				.texOffs(46, 0).addBox(-1f, -2f, 4f, 2f, 4f, 2f), PartPose.offsetAndRotation(0f, 18f, 0f, 0f, 0f, 0f));
		PartDefinition tail = body.addOrReplaceChild("tail", CubeListBuilder.create()
				.texOffs(59, 9).addBox(0f, -2f, 0f, 0f, 4f, 2f)
				.texOffs(55, 0).addBox(0f, -4f, 1f, 0f, 3f, 3f)
				.texOffs(54, 4).addBox(0f, 1f, 1f, 0f, 3f, 3f), PartPose.offsetAndRotation(0f, 0f, 6f, 0f, 0f, 0f));
		PartDefinition dorsal = body.addOrReplaceChild("dorsal", CubeListBuilder.create()
				.texOffs(37, 0).addBox(0f, -3f, -3f, 0f, 3f, 8f), PartPose.offsetAndRotation(0f, -5f, 0f, 0f, 0f, 0f));
		PartDefinition anal = body.addOrReplaceChild("anal", CubeListBuilder.create()
				.texOffs(15, 4).addBox(0f, 0f, -2f, 0f, 3f, 7f), PartPose.offsetAndRotation(0f, 5f, 0f, 0f, 0f, 0f));
		PartDefinition pecL = body.addOrReplaceChild("pecL", CubeListBuilder.create()
				.texOffs(30, 8).addBox(0f, -1f, 0f, 0f, 3f, 3f), PartPose.offsetAndRotation(1.5f, 0.5f, -3f, 0f, 0.3f, 0f));
		PartDefinition pecR = body.addOrReplaceChild("pecR", CubeListBuilder.create()
				.texOffs(30, 8).addBox(0f, -1f, 0f, 0f, 3f, 3f), PartPose.offsetAndRotation(-1.5f, 0.5f, -3f, 0f, -0.3f, 0f));
		return LayerDefinition.create(mesh, 64, 64);
	}

	public static LayerDefinition createWrasseLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();
		PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create()
				.texOffs(0, 0).addBox(-1f, -1.5f, -4f, 2f, 3f, 8f)
				.texOffs(13, 0).addBox(-1f, -1.5f, -6f, 2f, 3f, 2f)
				.texOffs(32, 0).addBox(-0.5f, -0.5f, -7f, 1f, 1f, 1f)
				.texOffs(0, 0).addBox(-0.5f, -1f, 4f, 1f, 2f, 2f), PartPose.offsetAndRotation(0f, 21f, 0f, 0f, 0f, 0f));
		PartDefinition tail = body.addOrReplaceChild("tail", CubeListBuilder.create()
				.texOffs(37, 0).addBox(0f, -1f, 0f, 0f, 2f, 1f)
				.texOffs(22, 0).addBox(0f, -2f, 1f, 0f, 4f, 2f), PartPose.offsetAndRotation(0f, 0f, 6f, 0f, 0f, 0f));
		PartDefinition dorsal = body.addOrReplaceChild("dorsal", CubeListBuilder.create()
				.texOffs(21, 0).addBox(0f, -1f, -3f, 0f, 1f, 8f), PartPose.offsetAndRotation(0f, -1.5f, 0f, 0f, 0f, 0f));
		PartDefinition anal = body.addOrReplaceChild("anal", CubeListBuilder.create()
				.texOffs(27, 0).addBox(0f, 0f, 0f, 0f, 1f, 5f), PartPose.offsetAndRotation(0f, 1.5f, 0f, 0f, 0f, 0f));
		PartDefinition pecL = body.addOrReplaceChild("pecL", CubeListBuilder.create()
				.texOffs(27, 0).addBox(0f, -0.5f, 0f, 0f, 2f, 2f), PartPose.offsetAndRotation(1f, 0.3f, -3.5f, 0f, 0.3f, 0f));
		PartDefinition pecR = body.addOrReplaceChild("pecR", CubeListBuilder.create()
				.texOffs(27, 0).addBox(0f, -0.5f, 0f, 0f, 2f, 2f), PartPose.offsetAndRotation(-1f, 0.3f, -3.5f, 0f, -0.3f, 0f));
		return LayerDefinition.create(mesh, 64, 64);
	}

	public static LayerDefinition createMandarinLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();
		PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create()
				.texOffs(0, 0).addBox(-3f, -4f, -4f, 6f, 8f, 10f)
				.texOffs(33, 0).addBox(-3f, -4f, -10f, 6f, 8f, 6f)
				.texOffs(13, 19).addBox(-2f, -1f, -12f, 4f, 4f, 2f)
				.texOffs(0, 0).addBox(2f, -5f, -8f, 2f, 2f, 2f)
				.texOffs(23, 0).addBox(-4f, -5f, -8f, 2f, 2f, 2f)
				.texOffs(46, 15).addBox(-2f, -3f, 6f, 4f, 6f, 4f), PartPose.offsetAndRotation(0f, 18f, 0f, 0f, 0f, 0f));
		PartDefinition tail = body.addOrReplaceChild("tail", CubeListBuilder.create()
				.texOffs(58, 0).addBox(0f, -3f, 0f, 0f, 6f, 2f)
				.texOffs(33, 9).addBox(0f, -6f, 2f, 0f, 12f, 6f), PartPose.offsetAndRotation(0f, 0f, 10f, 0f, 0f, 0f));
		PartDefinition dorsal = body.addOrReplaceChild("dorsal", CubeListBuilder.create()
				.texOffs(21, 22).addBox(0f, -8f, -3f, 0f, 8f, 4f)
				.texOffs(46, 18).addBox(0f, -4f, 2f, 0f, 4f, 8f), PartPose.offsetAndRotation(0f, -4f, 0f, 0f, 0f, 0f));
		PartDefinition anal = body.addOrReplaceChild("anal", CubeListBuilder.create()
				.texOffs(0, 18).addBox(0f, 0f, -1f, 0f, 4f, 10f), PartPose.offsetAndRotation(0f, 4f, 0f, 0f, 0f, 0f));
		PartDefinition pecL = body.addOrReplaceChild("pecL", CubeListBuilder.create()
				.texOffs(0, 13).addBox(0f, -4f, 0f, 0f, 8f, 6f), PartPose.offsetAndRotation(3f, 1f, -4f, 0f, 0.3f, 0f));
		PartDefinition pecR = body.addOrReplaceChild("pecR", CubeListBuilder.create()
				.texOffs(0, 13).addBox(0f, -4f, 0f, 0f, 8f, 6f), PartPose.offsetAndRotation(-3f, 1f, -4f, 0f, -0.3f, 0f));
		return LayerDefinition.create(mesh, 64, 64);
	}

	public static LayerDefinition createNapoleonLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();
		PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create()
				.texOffs(0, 0).addBox(-5f, -8f, -8f, 10f, 16f, 18f)
				.texOffs(57, 0).addBox(-4.5f, -7f, -15f, 9f, 13f, 7f)
				.texOffs(82, 19).addBox(-3.5f, -11f, -15f, 7f, 4f, 8f)
				.texOffs(57, 21).addBox(-3.5f, -3f, -19f, 7f, 7f, 4f)
				.texOffs(46, 0).addBox(-3f, -2f, -21f, 6f, 2f, 2f)
				.texOffs(0, 0).addBox(-3f, 1f, -21f, 6f, 3f, 2f)
				.texOffs(90, 0).addBox(-4f, -6f, 10f, 8f, 12f, 6f)
				.texOffs(109, 28).addBox(-2.5f, -4f, 16f, 5f, 8f, 4f), PartPose.offsetAndRotation(0f, 13f, 0f, 0f, 0f, 0f));
		PartDefinition tail = body.addOrReplaceChild("tail", CubeListBuilder.create()
				.texOffs(39, 0).addBox(0f, -5f, 0f, 0f, 10f, 3f)
				.texOffs(119, 0).addBox(0f, -9f, 3f, 0f, 18f, 4f), PartPose.offsetAndRotation(0f, 0f, 20f, 0f, 0f, 0f));
		PartDefinition dorsal = body.addOrReplaceChild("dorsal", CubeListBuilder.create()
				.texOffs(57, 23).addBox(0f, -4f, -6f, 0f, 4f, 10f)
				.texOffs(80, 19).addBox(0f, -6f, 4f, 0f, 6f, 13f), PartPose.offsetAndRotation(0f, -8f, 0f, 0f, 0f, 0f));
		PartDefinition anal = body.addOrReplaceChild("anal", CubeListBuilder.create()
				.texOffs(0, 22).addBox(0f, 0f, 2f, 0f, 5f, 13f), PartPose.offsetAndRotation(0f, 8f, 0f, 0f, 0f, 0f));
		PartDefinition pecL = body.addOrReplaceChild("pecL", CubeListBuilder.create()
				.texOffs(0, 0).addBox(0f, -3f, 0f, 0f, 6f, 8f), PartPose.offsetAndRotation(5f, 2f, -6f, 0f, 0.3f, 0f));
		PartDefinition pecR = body.addOrReplaceChild("pecR", CubeListBuilder.create()
				.texOffs(0, 0).addBox(0f, -3f, 0f, 0f, 6f, 8f), PartPose.offsetAndRotation(-5f, 2f, -6f, 0f, -0.3f, 0f));
		return LayerDefinition.create(mesh, 128, 128);
	}
}
