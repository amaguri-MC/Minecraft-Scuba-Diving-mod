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

/** Sea star: central disc and radial arms, each in two tapering segments; optional spines on top. Texture 32x32. */
public class StarfishModel extends EntityModel<CritterRenderState> {
	public static final ModelLayerLocation BLUE = new ModelLayerLocation(ScubaDive.id("blue_starfish"), "main");
	public static final ModelLayerLocation CUSHION = new ModelLayerLocation(ScubaDive.id("cushion_star"), "main");
	public static final ModelLayerLocation CROWN = new ModelLayerLocation(ScubaDive.id("crown_of_thorns"), "main");

	private final ModelPart[] arms;

	public StarfishModel(ModelPart root, int armCount) {
		super(root);
		this.arms = new ModelPart[armCount];
		for (int i = 0; i < armCount; i++) {
			this.arms[i] = root.getChild("arm" + i);
		}
	}

	public static LayerDefinition blue() {
		return create(5, 2.2f, 7.0f, 1.5f, 3.0f, false);
	}

	public static LayerDefinition cushion() {
		return create(5, 4.0f, 3.0f, 3.0f, 3.0f, false);
	}

	public static LayerDefinition crown() {
		return create(13, 1.4f, 6.0f, 1.5f, 4.0f, true);
	}

	private static LayerDefinition create(int armCount, float width, float length, float thick, float disc, boolean spines) {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();
		root.addOrReplaceChild("disc", CubeListBuilder.create().texOffs(0, 0)
				.addBox(-disc, -thick - 0.5f, -disc, disc * 2, thick + 0.5f, disc * 2), PartPose.offset(0.0f, 24.0f, 0.0f));
		for (int i = 0; i < armCount; i++) {
			float yaw = (float) (i * 2.0 * Math.PI / armCount);
			float inner = disc * 0.7f;
			float half = length * 0.55f;
			CubeListBuilder arm = CubeListBuilder.create()
					.texOffs(0, 12).addBox(-width / 2, -thick, inner, width, thick, half)
					.texOffs(0, 12).addBox(-width * 0.32f, -thick * 0.75f, inner + half, width * 0.64f, thick * 0.75f, length - half);
			if (spines) {
				for (int s = 0; s < 3; s++) {
					arm.texOffs(0, 24).addBox(-0.4f, -thick - 1.0f, inner + 1.0f + s * (length / 3.0f), 0.8f, 1.0f, 0.8f);
				}
			}
			root.addOrReplaceChild("arm" + i, arm, PartPose.offsetAndRotation(0.0f, 24.0f, 0.0f, 0.0f, yaw, 0.0f));
		}
		return LayerDefinition.create(mesh, 32, 32);
	}

	@Override
	public void setupAnim(CritterRenderState state) {
		super.setupAnim(state);
		float t = state.ageInTicks;
		for (int i = 0; i < this.arms.length; i++) {
			// Arm tips lift a hair, out of phase, so the star looks alive.
			this.arms[i].xRot = 0.04f + (float) Math.sin(t * 0.03f + i * 1.3f) * 0.04f;
		}
	}
}
