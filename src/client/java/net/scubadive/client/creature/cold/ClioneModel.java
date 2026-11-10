package net.scubadive.client.creature.cold;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.util.Mth;
import net.scubadive.ScubaDive;

/**
 * Clione (sea angel): upright translucent body tapering to a small tail, a round head with two tiny
 * horns, the red-orange gut glowing inside the body and two wing-like parapodia at the neck that flap.
 * Built on a fine grid and drawn small by {@link ClioneRenderer}. Texture 32x32 with alpha.
 */
@SuppressWarnings("unused")
public class ClioneModel extends EntityModel<ClioneRenderState> {
	public static final ModelLayerLocation LAYER = new ModelLayerLocation(ScubaDive.id("clione"), "main");

	private final ModelPart body;
	private final ModelPart head;
	private final ModelPart leftWing;
	private final ModelPart rightWing;

	public ClioneModel(ModelPart root) {
		super(root, RenderTypes::entityTranslucent);
		this.body = root.getChild("body");
		this.head = this.body.getChild("head");
		this.leftWing = this.body.getChild("left_wing");
		this.rightWing = this.body.getChild("right_wing");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();
		PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create()
				.texOffs(0, 0).addBox(-1.5f, -7f, -1.5f, 3f, 5f, 3f)
				.texOffs(0, 8).addBox(-1f, -2f, -1f, 2f, 1.5f, 2f)
				.texOffs(8, 8).addBox(-0.5f, -0.6f, -0.5f, 1f, 0.6f, 1f), PartPose.offsetAndRotation(0f, 24f, 0f, 0f, 0f, 0f));
		PartDefinition gut = body.addOrReplaceChild("gut", CubeListBuilder.create()
				.texOffs(16, 0).addBox(-0.8f, -6.2f, -0.8f, 1.6f, 3.2f, 1.6f), PartPose.offsetAndRotation(0f, 0f, 0f, 0f, 0f, 0f));
		PartDefinition head = body.addOrReplaceChild("head", CubeListBuilder.create()
				.texOffs(0, 16).addBox(-1.5f, -3f, -1.5f, 3f, 3f, 3f)
				.texOffs(24, 0).addBox(-1.3f, -3.7f, -0.4f, 0.8f, 0.8f, 0.8f)
				.texOffs(24, 0).addBox(0.5f, -3.7f, -0.4f, 0.8f, 0.8f, 0.8f), PartPose.offsetAndRotation(0f, -7.2f, 0f, 0f, 0f, 0f));
		PartDefinition leftWing = body.addOrReplaceChild("left_wing", CubeListBuilder.create()
				.texOffs(14, 12).addBox(0f, -0.1f, -1.5f, 4f, 0.2f, 3f), PartPose.offsetAndRotation(1.3f, -6.4f, 0f, 0f, 0f, -0.2f));
		PartDefinition rightWing = body.addOrReplaceChild("right_wing", CubeListBuilder.create()
				.texOffs(14, 17).addBox(-4f, -0.1f, -1.5f, 4f, 0.2f, 3f), PartPose.offsetAndRotation(-1.3f, -6.4f, 0f, 0f, 0f, 0.2f));
		return LayerDefinition.create(mesh, 32, 32);
	}

	@Override
	public void setupAnim(ClioneRenderState state) {
		super.setupAnim(state);
		float t = state.ageInTicks + state.phase;
		// Quick flutter: the parapodia beat up and down like little wings.
		float flap = Mth.sin(t * 0.35f);
		this.leftWing.zRot = -0.15f - flap * 0.65f;
		this.rightWing.zRot = 0.15f + flap * 0.65f;
		// The body bobs and sways a little with each stroke.
		this.body.y = 24.0f + Mth.sin(t * 0.35f - 0.8f) * 0.25f;
		this.body.xRot = Mth.sin(t * 0.05f) * 0.08f;
		this.body.zRot = Mth.cos(t * 0.04f) * 0.06f;
		this.head.xRot = Mth.sin(t * 0.07f) * 0.06f;
	}
}
