package net.scubadive.client.creature.temperate;

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
 * Ocean sunfish: a flat disc body with a tall dorsal and anal fin and the scalloped clavus instead of a tail.
 * The two tall fins scull together from side to side. Generated with the texture (tools/K2_temperate.py).
 */
@SuppressWarnings("unused")
public class OceanSunfishModel extends EntityModel<OceanSunfishRenderState> {
	public static final ModelLayerLocation LAYER = new ModelLayerLocation(ScubaDive.id("ocean_sunfish"), "main");

	private final ModelPart body;
	private final ModelPart dorsal;
	private final ModelPart anal;
	private final ModelPart clavus;
	private final ModelPart pecL;
	private final ModelPart pecR;

	public OceanSunfishModel(ModelPart root) {
		super(root);
		this.body = root.getChild("body");
		this.dorsal = this.body.getChild("dorsal");
		this.anal = this.body.getChild("anal");
		this.clavus = this.body.getChild("clavus");
		this.pecL = this.body.getChild("pecL");
		this.pecR = this.body.getChild("pecR");
	}

	public static LayerDefinition createLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();
		PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create()
				.texOffs(0, 0).addBox(-3f, -9f, -7f, 6f, 18f, 14f)
				.texOffs(41, 0).addBox(-3f, -7f, -10f, 6f, 14f, 3f)
				.texOffs(27, 0).addBox(-1.5f, -1f, -11f, 3f, 2f, 1f)
				.texOffs(60, 0).addBox(-2.5f, -8f, 7f, 5f, 16f, 2f)
				.texOffs(91, 12).addBox(-2.5f, -10f, -5f, 5f, 1f, 10f)
				.texOffs(41, 19).addBox(-2.5f, 9f, -5f, 5f, 1f, 10f), PartPose.offsetAndRotation(0f, 0f, 0f, 0f, 0f, 0f));
		PartDefinition dorsal = body.addOrReplaceChild("dorsal", CubeListBuilder.create()
				.texOffs(75, 0).addBox(0f, -15f, -2f, 0f, 15f, 6f), PartPose.offsetAndRotation(0f, -9f, 2f, 0.12f, 0f, 0f));
		PartDefinition anal = body.addOrReplaceChild("anal", CubeListBuilder.create()
				.texOffs(88, 0).addBox(0f, 0f, -2f, 0f, 15f, 6f), PartPose.offsetAndRotation(0f, 9f, 2f, -0.12f, 0f, 0f));
		PartDefinition clavus = body.addOrReplaceChild("clavus", CubeListBuilder.create()
				.texOffs(112, 0).addBox(0f, -9f, 0f, 0f, 18f, 3f), PartPose.offsetAndRotation(0f, 0f, 9f, 0f, 0f, 0f));
		PartDefinition pecL = body.addOrReplaceChild("pecL", CubeListBuilder.create()
				.texOffs(0, 0).addBox(0f, -2f, 0f, 0f, 4f, 3f), PartPose.offsetAndRotation(3f, -0.5f, -5f, 0f, 0.3f, 0f));
		PartDefinition pecR = body.addOrReplaceChild("pecR", CubeListBuilder.create()
				.texOffs(0, 0).addBox(0f, -2f, 0f, 0f, 4f, 3f), PartPose.offsetAndRotation(-3f, -0.5f, -5f, 0f, -0.3f, 0f));
		return LayerDefinition.create(mesh, 128, 128);
	}

	@Override
	public void setupAnim(OceanSunfishRenderState state) {
		super.setupAnim(state);
		float t = state.ageInTicks + state.phase;
		// Slow sculling while cruising, a lazy wave when basking.
		float speed = Mth.lerp(state.roll, 0.13f, 0.045f);
		float amp = Mth.lerp(state.roll, 0.5f, 0.25f);
		float stroke = Mth.sin(t * speed) * amp;
		// Both fin tips swing to the same side (like a bird's wings beating up together).
		this.dorsal.zRot = stroke;
		this.anal.zRot = -stroke;
		this.clavus.yRot = Mth.sin(t * speed - 1.2f) * 0.12f;
		this.body.yRot = -stroke * 0.06f;
		float flutter = Mth.sin(t * 0.35f) * 0.25f;
		this.pecL.yRot = 0.3f + flutter;
		this.pecR.yRot = -0.3f - flutter;
	}
}
