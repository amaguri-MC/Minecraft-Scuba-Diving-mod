package net.scubadive.client.creature.critter;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

import net.scubadive.ScubaDive;

/** Long-spined urchin: round dark test with long thin spines spread over the upper hemisphere. Texture 32x32. */
public class UrchinModel extends EntityModel<CritterRenderState> {
	public static final ModelLayerLocation LAYER = new ModelLayerLocation(ScubaDive.id("sea_urchin"), "main");
	private static final int SPINES = 30;

	private final ModelPart[] spines = new ModelPart[SPINES];
	private final float[] baseX = new float[SPINES];

	public UrchinModel(ModelPart root) {
		super(root);
		for (int i = 0; i < SPINES; i++) {
			this.spines[i] = root.getChild("spine" + i);
			this.baseX[i] = this.spines[i].xRot;
		}
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();
		root.addOrReplaceChild("test", CubeListBuilder.create()
				.texOffs(0, 0).addBox(-2.5f, -3.5f, -2.5f, 5.0f, 3.5f, 5.0f)
				.texOffs(0, 9).addBox(-1.0f, -4.0f, -1.0f, 2.0f, 0.5f, 2.0f), PartPose.offset(0.0f, 24.0f, 0.0f));
		double golden = Math.PI * (3.0 - Math.sqrt(5.0));
		for (int i = 0; i < SPINES; i++) {
			// Fibonacci spread over elevations from slightly below horizontal to straight up.
			double f = (i + 0.5) / SPINES;
			float elev = (float) (-0.15 + f * (Math.PI / 2 + 0.15));
			float yaw = (float) (i * golden);
			float len = 6.0f + (i * 7 % 5) * 0.6f;
			root.addOrReplaceChild("spine" + i, CubeListBuilder.create().texOffs(0, 14)
							.addBox(-0.25f, -0.25f, 1.5f, 0.5f, 0.5f, len),
					PartPose.offsetAndRotation(0.0f, 22.0f, 0.0f, elev, yaw, 0.0f));
		}
		return LayerDefinition.create(mesh, 32, 32);
	}

	@Override
	public void setupAnim(CritterRenderState state) {
		super.setupAnim(state);
		float t = state.ageInTicks;
		for (int i = 0; i < SPINES; i++) {
			this.spines[i].xRot = this.baseX[i] + (float) Math.sin(t * 0.05f + i) * 0.05f;
		}
	}
}
