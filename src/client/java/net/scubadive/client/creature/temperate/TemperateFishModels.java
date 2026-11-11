package net.scubadive.client.creature.temperate;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.scubadive.ScubaDive;

/**
 * Body shapes of the temperate fish. They use the part names of the shared ReefFishModel
 * (body, tail, dorsal, anal, pecL, pecR), which animates them. Generated with the textures (tools/K2_temperate.py).
 */
@SuppressWarnings("unused")
public final class TemperateFishModels {
	public static final ModelLayerLocation SARDINE = layer("temperate_sardine");
	public static final ModelLayerLocation RED_SEA_BREAM = layer("temperate_red_sea_bream");
	public static final ModelLayerLocation STRIPED_BEAKFISH = layer("temperate_striped_beakfish");
	public static final ModelLayerLocation ARCTIC_CHAR = layer("temperate_arctic_char");

	private TemperateFishModels() {
	}

	private static ModelLayerLocation layer(String name) {
		return new ModelLayerLocation(ScubaDive.id(name), "main");
	}

	public static LayerDefinition createSardineLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();
		PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create()
				.texOffs(0, 0).addBox(-1.5f, -2f, -3f, 3f, 4f, 7f)
				.texOffs(14, 0).addBox(-1.5f, -1.5f, -6f, 3f, 3f, 3f)
				.texOffs(0, 0).addBox(-1f, -1f, -7f, 2f, 2f, 1f)
				.texOffs(27, 0).addBox(-1f, -1.5f, 4f, 2f, 3f, 2f), PartPose.offsetAndRotation(0f, 20f, 0f, 0f, 0f, 0f));
		PartDefinition tail = body.addOrReplaceChild("tail", CubeListBuilder.create()
				.texOffs(58, 0).addBox(0f, -1f, 0f, 0f, 2f, 1f)
				.texOffs(36, 0).addBox(0f, -3f, 1f, 0f, 3f, 2f)
				.texOffs(41, 0).addBox(0f, 0f, 1f, 0f, 3f, 2f), PartPose.offsetAndRotation(0f, 0f, 6f, 0f, 0f, 0f));
		PartDefinition dorsal = body.addOrReplaceChild("dorsal", CubeListBuilder.create()
				.texOffs(46, 0).addBox(0f, -2f, -1f, 0f, 2f, 3f), PartPose.offsetAndRotation(0f, -2f, 0f, 0f, 0f, 0f));
		PartDefinition anal = body.addOrReplaceChild("anal", CubeListBuilder.create()
				.texOffs(0, 2).addBox(0f, 0f, 2f, 0f, 1f, 2f), PartPose.offsetAndRotation(0f, 2f, 0f, 0f, 0f, 0f));
		PartDefinition pecL = body.addOrReplaceChild("pecL", CubeListBuilder.create()
				.texOffs(53, 0).addBox(0f, -1f, 0f, 0f, 2f, 2f), PartPose.offsetAndRotation(1.5f, 0.5f, -3f, 0f, 0.3f, 0f));
		PartDefinition pecR = body.addOrReplaceChild("pecR", CubeListBuilder.create()
				.texOffs(53, 0).addBox(0f, -1f, 0f, 0f, 2f, 2f), PartPose.offsetAndRotation(-1.5f, 0.5f, -3f, 0f, -0.3f, 0f));
		return LayerDefinition.create(mesh, 64, 64);
	}

	public static LayerDefinition createRedSeaBreamLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();
		PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create()
				.texOffs(0, 0).addBox(-2f, -5f, -4f, 4f, 10f, 9f)
				.texOffs(27, 0).addBox(-2f, -4f, -8f, 4f, 8f, 4f)
				.texOffs(53, 7).addBox(-1.5f, -1f, -10f, 3f, 3f, 2f)
				.texOffs(44, 0).addBox(-1.5f, -3f, 5f, 3f, 6f, 2f), PartPose.offsetAndRotation(0f, 17f, 0f, 0f, 0f, 0f));
		PartDefinition tail = body.addOrReplaceChild("tail", CubeListBuilder.create()
				.texOffs(0, 0).addBox(0f, -3f, 0f, 0f, 6f, 2f)
				.texOffs(44, 5).addBox(0f, -6f, 2f, 0f, 5f, 4f)
				.texOffs(27, 9).addBox(0f, 1f, 2f, 0f, 5f, 4f), PartPose.offsetAndRotation(0f, 0f, 7f, 0f, 0f, 0f));
		PartDefinition dorsal = body.addOrReplaceChild("dorsal", CubeListBuilder.create()
				.texOffs(36, 9).addBox(0f, -3f, -4f, 0f, 3f, 6f)
				.texOffs(49, 12).addBox(0f, -2f, 2f, 0f, 2f, 5f), PartPose.offsetAndRotation(0f, -5f, 0f, 0f, 0f, 0f));
		PartDefinition anal = body.addOrReplaceChild("anal", CubeListBuilder.create()
				.texOffs(27, 14).addBox(0f, 0f, 1f, 0f, 2f, 5f), PartPose.offsetAndRotation(0f, 5f, 0f, 0f, 0f, 0f));
		PartDefinition pecL = body.addOrReplaceChild("pecL", CubeListBuilder.create()
				.texOffs(53, 8).addBox(0f, -1f, 0f, 0f, 3f, 5f), PartPose.offsetAndRotation(2f, 1f, -3f, 0f, 0.3f, 0f));
		PartDefinition pecR = body.addOrReplaceChild("pecR", CubeListBuilder.create()
				.texOffs(53, 8).addBox(0f, -1f, 0f, 0f, 3f, 5f), PartPose.offsetAndRotation(-2f, 1f, -3f, 0f, -0.3f, 0f));
		return LayerDefinition.create(mesh, 64, 64);
	}

	public static LayerDefinition createStripedBeakfishLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();
		PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create()
				.texOffs(0, 0).addBox(-3f, -7f, -6f, 6f, 14f, 15f)
				.texOffs(43, 0).addBox(-3f, -6f, -12f, 6f, 12f, 6f)
				.texOffs(28, 0).addBox(-2f, -1.5f, -14f, 4f, 3f, 2f)
				.texOffs(0, 0).addBox(-2f, -4f, 9f, 4f, 8f, 3f), PartPose.offsetAndRotation(0f, 13f, 0f, 0f, 0f, 0f));
		PartDefinition tail = body.addOrReplaceChild("tail", CubeListBuilder.create()
				.texOffs(115, 0).addBox(0f, -4f, 0f, 0f, 8f, 3f)
				.texOffs(68, 0).addBox(0f, -7f, 3f, 0f, 14f, 4f), PartPose.offsetAndRotation(0f, 0f, 12f, 0f, 0f, 0f));
		PartDefinition dorsal = body.addOrReplaceChild("dorsal", CubeListBuilder.create()
				.texOffs(98, 0).addBox(0f, -4f, -6f, 0f, 4f, 8f)
				.texOffs(77, 0).addBox(0f, -4f, 2f, 0f, 4f, 10f), PartPose.offsetAndRotation(0f, -7f, 0f, 0f, 0f, 0f));
		PartDefinition anal = body.addOrReplaceChild("anal", CubeListBuilder.create()
				.texOffs(28, 0).addBox(0f, 0f, 3f, 0f, 4f, 7f), PartPose.offsetAndRotation(0f, 7f, 0f, 0f, 0f, 0f));
		PartDefinition pecL = body.addOrReplaceChild("pecL", CubeListBuilder.create()
				.texOffs(77, 0).addBox(0f, -2f, 0f, 0f, 4f, 5f), PartPose.offsetAndRotation(3f, 1.5f, -5f, 0f, 0.3f, 0f));
		PartDefinition pecR = body.addOrReplaceChild("pecR", CubeListBuilder.create()
				.texOffs(77, 0).addBox(0f, -2f, 0f, 0f, 4f, 5f), PartPose.offsetAndRotation(-3f, 1.5f, -5f, 0f, -0.3f, 0f));
		return LayerDefinition.create(mesh, 128, 128);
	}

	public static LayerDefinition createArcticCharLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();
		PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create()
				.texOffs(0, 0).addBox(-2f, -3f, -5f, 4f, 6f, 10f)
				.texOffs(19, 0).addBox(-2f, -2.5f, -9f, 4f, 5f, 4f)
				.texOffs(49, 0).addBox(-1.5f, -1.5f, -11f, 3f, 3f, 2f)
				.texOffs(36, 0).addBox(-1.5f, -2f, 5f, 3f, 4f, 3f), PartPose.offsetAndRotation(0f, 19f, 0f, 0f, 0f, 0f));
		PartDefinition tail = body.addOrReplaceChild("tail", CubeListBuilder.create()
				.texOffs(36, 6).addBox(0f, -2f, 0f, 0f, 4f, 2f)
				.texOffs(49, 3).addBox(0f, -4f, 2f, 0f, 3f, 3f)
				.texOffs(56, 3).addBox(0f, 1f, 2f, 0f, 3f, 3f), PartPose.offsetAndRotation(0f, 0f, 8f, 0f, 0f, 0f));
		PartDefinition dorsal = body.addOrReplaceChild("dorsal", CubeListBuilder.create()
				.texOffs(0, 0).addBox(0f, -3f, -3f, 0f, 3f, 4f), PartPose.offsetAndRotation(0f, -3f, 0f, 0f, 0f, 0f));
		PartDefinition adipose = body.addOrReplaceChild("adipose", CubeListBuilder.create()
				.texOffs(0, 0).addBox(0f, -1f, 0f, 0f, 1f, 1f), PartPose.offsetAndRotation(0f, -2f, 5f, 0f, 0f, 0f));
		PartDefinition anal = body.addOrReplaceChild("anal", CubeListBuilder.create()
				.texOffs(41, 5).addBox(0f, 0f, 3f, 0f, 2f, 3f)
				.texOffs(60, 0).addBox(0f, 0f, -1f, 0f, 2f, 2f), PartPose.offsetAndRotation(0f, 3f, 0f, 0f, 0f, 0f));
		PartDefinition pecL = body.addOrReplaceChild("pecL", CubeListBuilder.create()
				.texOffs(29, 7).addBox(0f, -1f, 0f, 0f, 2f, 3f), PartPose.offsetAndRotation(2f, 2f, -6f, 0f, 0.3f, 0f));
		PartDefinition pecR = body.addOrReplaceChild("pecR", CubeListBuilder.create()
				.texOffs(29, 7).addBox(0f, -1f, 0f, 0f, 2f, 3f), PartPose.offsetAndRotation(-2f, 2f, -6f, 0f, -0.3f, 0f));
		return LayerDefinition.create(mesh, 64, 64);
	}
}
