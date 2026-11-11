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

/** Spanish dancer (Hexabranchus): long red body with a frilled mantle skirt in three sections per side, red rhinophores and a pale gill rosette; the skirt ripples and, while swimming, flaps like a dancer's dress. Generated with its texture by tools/K4_gen.py. */
@SuppressWarnings("unused")
public class SpanishDancerModel extends EntityModel<NudibranchRenderState> {
	public static final ModelLayerLocation LAYER = new ModelLayerLocation(ScubaDive.id("spanish_dancer"), "main");

	private final ModelPart body;
	private final ModelPart gills;
	private final ModelPart[] skirtLeft = new ModelPart[3];
	private final ModelPart[] skirtRight = new ModelPart[3];

	public SpanishDancerModel(ModelPart root) {
		super(root);
		this.body = root.getChild("body");
		this.gills = this.body.getChild("gills");
		for (int i = 0; i < 3; i++) {
			this.skirtLeft[i] = this.body.getChild("skirt_l" + i);
			this.skirtRight[i] = this.body.getChild("skirt_r" + i);
		}
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();
		PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create()
				.texOffs(0, 0).addBox(-1.5f, -2f, -6f, 3f, 2f, 12f), PartPose.offsetAndRotation(0f, 24f, 0f, 0f, 0f, 0f));
		PartDefinition skirtL0 = body.addOrReplaceChild("skirt_l0", CubeListBuilder.create()
				.texOffs(31, 0).addBox(0f, -0.5f, 0f, 3f, 1f, 4f), PartPose.offsetAndRotation(1.5f, -1f, -6f, 0f, 0f, -0.2f));
		PartDefinition skirtR0 = body.addOrReplaceChild("skirt_r0", CubeListBuilder.create()
				.texOffs(46, 0).addBox(-3f, -0.5f, 0f, 3f, 1f, 4f), PartPose.offsetAndRotation(-1.5f, -1f, -6f, 0f, 0f, 0.2f));
		PartDefinition skirtL1 = body.addOrReplaceChild("skirt_l1", CubeListBuilder.create()
				.texOffs(31, 0).addBox(0f, -0.5f, 0f, 3f, 1f, 4f), PartPose.offsetAndRotation(1.5f, -1f, -2f, 0f, 0f, -0.2f));
		PartDefinition skirtR1 = body.addOrReplaceChild("skirt_r1", CubeListBuilder.create()
				.texOffs(46, 0).addBox(-3f, -0.5f, 0f, 3f, 1f, 4f), PartPose.offsetAndRotation(-1.5f, -1f, -2f, 0f, 0f, 0.2f));
		PartDefinition skirtL2 = body.addOrReplaceChild("skirt_l2", CubeListBuilder.create()
				.texOffs(31, 0).addBox(0f, -0.5f, 0f, 3f, 1f, 4f), PartPose.offsetAndRotation(1.5f, -1f, 2f, 0f, 0f, -0.2f));
		PartDefinition skirtR2 = body.addOrReplaceChild("skirt_r2", CubeListBuilder.create()
				.texOffs(46, 0).addBox(-3f, -0.5f, 0f, 3f, 1f, 4f), PartPose.offsetAndRotation(-1.5f, -1f, 2f, 0f, 0f, 0.2f));
		PartDefinition rhinoL = body.addOrReplaceChild("rhino_l", CubeListBuilder.create()
				.texOffs(0, 15).addBox(-0.5f, -2f, -0.5f, 1f, 2f, 1f), PartPose.offsetAndRotation(0.8f, -2f, -5.2f, 0.3f, 0f, 0.2f));
		PartDefinition rhinoR = body.addOrReplaceChild("rhino_r", CubeListBuilder.create()
				.texOffs(0, 15).addBox(-0.5f, -2f, -0.5f, 1f, 2f, 1f), PartPose.offsetAndRotation(-0.8f, -2f, -5.2f, 0.3f, 0f, -0.2f));
		PartDefinition gills = body.addOrReplaceChild("gills", CubeListBuilder.create()
				.texOffs(5, 15).addBox(-1.5f, -2f, -1f, 3f, 2f, 2f)
				.texOffs(16, 15).addBox(-0.5f, -3f, -0.5f, 1f, 1f, 1f), PartPose.offsetAndRotation(0f, -2f, 4.5f, -0.3f, 0f, 0f));
		return LayerDefinition.create(mesh, 64, 32);
	}

	@Override
	public void setupAnim(NudibranchRenderState state) {
		super.setupAnim(state);
		float t = state.ageInTicks;
		boolean dance = state.dancing;
		// A wave runs from head to tail along the skirt; swimming makes it big and fast.
		float amp = dance ? 0.85f : 0.12f;
		float rate = dance ? 0.35f : 0.08f;
		float lift = dance ? 0.1f : 0.2f;
		for (int i = 0; i < 3; i++) {
			float w = (float) Math.sin(t * rate - i * 1.1f) * amp;
			this.skirtLeft[i].zRot = -lift + w;
			this.skirtRight[i].zRot = lift - w;
		}
		// The whole body flexes up and down while it swims.
		this.body.xRot = dance ? (float) Math.sin(t * rate * 0.5f) * 0.18f : 0.0f;
		this.body.y = 24.0f + (dance ? (float) Math.sin(t * rate) * 0.6f : 0.0f);
		this.gills.xRot = -0.3f + (float) Math.sin(t * 0.06f) * 0.08f;
	}
}
