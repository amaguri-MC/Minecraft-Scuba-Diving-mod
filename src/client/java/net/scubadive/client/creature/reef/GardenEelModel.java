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
 * Garden eel: a chain of thin segments standing out of the sand, the neck bent forward into the current.
 * The part below y=24 sits inside the sand block; retracting slides the whole chain down into it.
 * Layer generated with the texture (tools/K1_reef.py).
 */
@SuppressWarnings("unused")
public class GardenEelModel extends EntityModel<GardenEelRenderState> {
	public static final ModelLayerLocation LAYER = new ModelLayerLocation(ScubaDive.id("garden_eel"), "main");
	private static final int SEGMENTS = 6;
	/** Forward bend per segment at rest (radians): upright at the sand, curling over at the neck. */
	private static final float[] BEND = {0.0f, 0.0f, 0.08f, 0.18f, 0.3f, 0.38f};
	/** Visible length in model pixels (segments plus head). */
	private static final float LENGTH = 22.0f;

	private final ModelPart base;
	private final ModelPart[] segs = new ModelPart[SEGMENTS];
	private final ModelPart head;

	public GardenEelModel(ModelPart root) {
		super(root);
		this.base = root.getChild("base");
		ModelPart parent = this.base;
		for (int i = 0; i < SEGMENTS; i++) {
			this.segs[i] = parent.getChild("seg" + i);
			parent = this.segs[i];
		}
		this.head = parent.getChild("head");
	}

	// @@LAYER-BEGIN@@
	public static LayerDefinition createBodyLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();
		PartDefinition base = root.addOrReplaceChild("base", CubeListBuilder.create()
				.texOffs(0, 0).addBox(-1f, 0f, -1f, 2f, 10f, 2f), PartPose.offsetAndRotation(0f, 24f, 0f, 0f, 0f, 0f));
		PartDefinition seg0 = base.addOrReplaceChild("seg0", CubeListBuilder.create()
				.texOffs(20, 0).addBox(-1f, -3f, -1f, 2f, 3f, 2f), PartPose.offsetAndRotation(0f, 0f, 0f, 0f, 0f, 0f));
		PartDefinition seg1 = seg0.addOrReplaceChild("seg1", CubeListBuilder.create()
				.texOffs(29, 0).addBox(-1f, -3f, -1f, 2f, 3f, 2f), PartPose.offsetAndRotation(0f, -3f, 0f, 0f, 0f, 0f));
		PartDefinition seg2 = seg1.addOrReplaceChild("seg2", CubeListBuilder.create()
				.texOffs(38, 0).addBox(-1f, -3f, -1f, 2f, 3f, 2f), PartPose.offsetAndRotation(0f, -3f, 0f, 0f, 0f, 0f));
		PartDefinition seg3 = seg2.addOrReplaceChild("seg3", CubeListBuilder.create()
				.texOffs(47, 0).addBox(-1f, -3f, -1f, 2f, 3f, 2f), PartPose.offsetAndRotation(0f, -3f, 0f, 0f, 0f, 0f));
		PartDefinition seg4 = seg3.addOrReplaceChild("seg4", CubeListBuilder.create()
				.texOffs(56, 0).addBox(-1f, -3f, -1f, 2f, 3f, 2f), PartPose.offsetAndRotation(0f, -3f, 0f, 0f, 0f, 0f));
		PartDefinition seg5 = seg4.addOrReplaceChild("seg5", CubeListBuilder.create()
				.texOffs(18, 6).addBox(-1f, -3f, -1f, 2f, 3f, 2f), PartPose.offsetAndRotation(0f, -3f, 0f, 0f, 0f, 0f));
		PartDefinition head = seg5.addOrReplaceChild("head", CubeListBuilder.create()
				.texOffs(9, 0).addBox(-1f, -3f, -1.5f, 2f, 3f, 3f)
				.texOffs(27, 6).addBox(-0.5f, -2f, -2.5f, 1f, 1f, 1f), PartPose.offsetAndRotation(0f, -3f, 0f, 0f, 0f, 0f));
		return LayerDefinition.create(mesh, 64, 64);
	}
	// @@LAYER-END@@

	@Override
	public void setupAnim(GardenEelRenderState state) {
		super.setupAnim(state);
		float t = state.ageInTicks + state.phase;
		float out = 1.0f - state.retract;
		// Sway travels up the body; the neck bobs now and then as if picking plankton.
		for (int i = 0; i < SEGMENTS; i++) {
			float k = (i + 1) / (float) SEGMENTS;
			this.segs[i].zRot = (float) Math.sin(t * 0.07f - i * 0.55f) * 0.07f * k;
			this.segs[i].xRot = BEND[i] * out + (float) Math.sin(t * 0.05f - i * 0.5f) * 0.05f * k;
		}
		float peck = (float) Math.max(0.0, Math.sin(t * 0.045f)) * 0.25f;
		this.head.xRot = (0.5f + peck) * out;
		this.head.zRot = (float) Math.sin(t * 0.09f) * 0.08f;
		// Pull the whole chain down into the sand.
		this.base.y = 24.0f + state.retract * LENGTH;
		this.base.visible = state.retract < 0.99f;
	}
}
