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

/** Ceratosoma trilobatum: a big pink-to-purple slug edged in yellow and orange, with flared mantle lobes, a gill crown and a single horn-like process rising from the back behind the gills. Generated with its texture by tools/K5_gen.py. */
@SuppressWarnings("unused")
public class CeratosomaModel extends EntityModel<NudibranchRenderState> {
	public static final ModelLayerLocation LAYER = new ModelLayerLocation(ScubaDive.id("ceratosoma"), "main");

	private final ModelPart rhinoL;
	private final ModelPart rhinoR;
	private final ModelPart gills;
	private final ModelPart horn;
	private final ModelPart[] lobes = new ModelPart[4];
	private final float[] lobeRoll = new float[4];

	public CeratosomaModel(ModelPart root) {
		super(root);
		this.rhinoL = root.getChild("rhino_l");
		this.rhinoR = root.getChild("rhino_r");
		this.gills = root.getChild("gills");
		this.horn = root.getChild("horn");
		String[] names = {"lobe_fl", "lobe_fr", "lobe_ml", "lobe_mr"};
		for (int i = 0; i < 4; i++) {
			this.lobes[i] = root.getChild(names[i]);
			this.lobeRoll[i] = this.lobes[i].zRot;
		}
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();
		PartDefinition foot = root.addOrReplaceChild("foot", CubeListBuilder.create()
				.texOffs(0, 0).addBox(-1.5f, -1f, -5f, 3f, 1f, 10f)
				.texOffs(27, 0).addBox(-0.5f, -1f, 5f, 1f, 1f, 2f), PartPose.offsetAndRotation(0f, 24f, 0f, 0f, 0f, 0f));
		PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create()
				.texOffs(34, 0).addBox(-1f, -3f, -4.5f, 2f, 2f, 8f), PartPose.offsetAndRotation(0f, 24f, 0f, 0f, 0f, 0f));
		PartDefinition lobeFL = root.addOrReplaceChild("lobe_fl", CubeListBuilder.create()
				.texOffs(0, 12).addBox(0f, -0.5f, 0f, 2f, 1f, 3f), PartPose.offsetAndRotation(1f, 22f, -3.5f, 0f, 0f, -0.35f));
		PartDefinition lobeFR = root.addOrReplaceChild("lobe_fr", CubeListBuilder.create()
				.texOffs(11, 12).addBox(-2f, -0.5f, 0f, 2f, 1f, 3f), PartPose.offsetAndRotation(-1f, 22f, -3.5f, 0f, 0f, 0.35f));
		PartDefinition lobeML = root.addOrReplaceChild("lobe_ml", CubeListBuilder.create()
				.texOffs(0, 12).addBox(0f, -0.5f, 0f, 2f, 1f, 3f), PartPose.offsetAndRotation(1f, 22f, 0f, 0f, 0f, -0.35f));
		PartDefinition lobeMR = root.addOrReplaceChild("lobe_mr", CubeListBuilder.create()
				.texOffs(11, 12).addBox(-2f, -0.5f, 0f, 2f, 1f, 3f), PartPose.offsetAndRotation(-1f, 22f, 0f, 0f, 0f, 0.35f));
		PartDefinition rhinoL = root.addOrReplaceChild("rhino_l", CubeListBuilder.create()
				.texOffs(22, 12).addBox(-0.5f, -2f, -0.5f, 1f, 2f, 1f), PartPose.offsetAndRotation(0.6f, 21f, -3.6f, 0.2f, 0f, 0.15f));
		PartDefinition rhinoR = root.addOrReplaceChild("rhino_r", CubeListBuilder.create()
				.texOffs(22, 12).addBox(-0.5f, -2f, -0.5f, 1f, 2f, 1f), PartPose.offsetAndRotation(-0.6f, 21f, -3.6f, 0.2f, 0f, -0.15f));
		PartDefinition gills = root.addOrReplaceChild("gills", CubeListBuilder.create()
				.texOffs(27, 12).addBox(-1f, -2f, -0.5f, 2f, 2f, 1f)
				.texOffs(34, 12).addBox(-0.5f, -3f, -0.5f, 1f, 1f, 1f), PartPose.offsetAndRotation(0f, 21f, 1f, -0.2f, 0f, 0f));
		PartDefinition horn = root.addOrReplaceChild("horn", CubeListBuilder.create()
				.texOffs(39, 12).addBox(-0.5f, -4f, -0.5f, 1f, 4f, 1f), PartPose.offsetAndRotation(0f, 21f, 2.6f, -0.45f, 0f, 0f));
		return LayerDefinition.create(mesh, 64, 32);
	}

	@Override
	public void setupAnim(NudibranchRenderState state) {
		super.setupAnim(state);
		float t = state.ageInTicks;
		this.rhinoL.xRot = 0.2f + (float) Math.sin(t * 0.12f) * 0.18f;
		this.rhinoR.xRot = 0.2f + (float) Math.sin(t * 0.12f + 1.2f) * 0.18f;
		this.gills.xRot = -0.2f + (float) Math.sin(t * 0.07f) * 0.1f;
		this.gills.zRot = (float) Math.sin(t * 0.09f) * 0.1f;
		this.horn.xRot = -0.45f + (float) Math.sin(t * 0.05f) * 0.06f;
		// The mantle lobes ripple slowly as it crawls.
		for (int i = 0; i < 4; i++) {
			float side = i % 2 == 0 ? 1.0f : -1.0f;
			this.lobes[i].zRot = this.lobeRoll[i] - (float) Math.sin(t * 0.09f + i * 0.8f) * 0.1f * side;
		}
	}
}
