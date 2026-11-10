package net.scubadive.client.dive;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Unit;

import net.scubadive.ScubaDive;
import net.scubadive.dive.ScubaDiveGear;

/**
 * Draws the worn dive gear as 3D boxes on the player: mask (opaque frame + translucent lens),
 * BCD with an octagonal cylinder and valve on the back, and fins. The vanilla equipment textures
 * of these items are fully transparent, so nothing is drawn twice.
 */
public class DiveGearLayer extends RenderLayer<AvatarRenderState, PlayerModel> {
	private static final Identifier TEXTURE = ScubaDive.id("textures/entity/dive_gear.png");
	private static final float QUARTER = (float) (Math.PI / 4.0);

	private static final Model<Unit> MASK = new Model.Simple(mask(), RenderTypes::entityCutout);
	private static final Model<Unit> LENS = new Model.Simple(lens(), RenderTypes::entityTranslucent);
	private static final Model<Unit> BCD = new Model.Simple(bcd(), RenderTypes::entityCutout);
	private static final Model<Unit> FIN = new Model.Simple(fin(), RenderTypes::entityCutout);

	public DiveGearLayer(RenderLayerParent<AvatarRenderState, PlayerModel> parent) {
		super(parent);
	}

	@Override
	public void submit(PoseStack poseStack, SubmitNodeCollector nodes, int light, AvatarRenderState state, float limbAngle, float limbDistance) {
		if (state.isInvisible) {
			return;
		}
		PlayerModel model = this.getParentModel();
		if (state.headEquipment.is(ScubaDiveGear.DIVE_MASK)) {
			poseStack.pushPose();
			model.head.translateAndRotate(poseStack);
			draw(MASK, poseStack, nodes, light, state);
			draw(LENS, poseStack, nodes, light, state);
			poseStack.popPose();
		}
		if (state.chestEquipment.is(ScubaDiveGear.BCD)) {
			poseStack.pushPose();
			model.body.translateAndRotate(poseStack);
			draw(BCD, poseStack, nodes, light, state);
			poseStack.popPose();
		}
		if (state.feetEquipment.is(ScubaDiveGear.DIVE_FINS)) {
			for (ModelPart leg : new ModelPart[] {model.leftLeg, model.rightLeg}) {
				poseStack.pushPose();
				leg.translateAndRotate(poseStack);
				draw(FIN, poseStack, nodes, light, state);
				poseStack.popPose();
			}
		}
	}

	private static void draw(Model<Unit> model, PoseStack poseStack, SubmitNodeCollector nodes, int light, AvatarRenderState state) {
		nodes.submitModel(model, Unit.INSTANCE, poseStack, TEXTURE, light, OverlayTexture.NO_OVERLAY, state.outlineColor, null);
	}

	private static ModelPart mask() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();
		root.addOrReplaceChild("frame_top", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0f, -6.0f, -5.0f, 8.0f, 1.0f, 1.0f), PartPose.ZERO);
		root.addOrReplaceChild("frame_bottom", CubeListBuilder.create().texOffs(0, 2).addBox(-4.0f, -2.0f, -5.0f, 8.0f, 1.0f, 1.0f), PartPose.ZERO);
		root.addOrReplaceChild("frame_left", CubeListBuilder.create().texOffs(18, 0).addBox(3.0f, -5.0f, -5.0f, 1.0f, 3.0f, 1.0f), PartPose.ZERO);
		root.addOrReplaceChild("frame_right", CubeListBuilder.create().texOffs(18, 0).addBox(-4.0f, -5.0f, -5.0f, 1.0f, 3.0f, 1.0f), PartPose.ZERO);
		root.addOrReplaceChild("strap_left", CubeListBuilder.create().texOffs(0, 4).addBox(3.5f, -4.5f, -4.0f, 1.0f, 1.0f, 8.0f), PartPose.ZERO);
		root.addOrReplaceChild("strap_right", CubeListBuilder.create().texOffs(0, 4).addBox(-4.5f, -4.5f, -4.0f, 1.0f, 1.0f, 8.0f), PartPose.ZERO);
		root.addOrReplaceChild("strap_back", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0f, -4.5f, 3.5f, 8.0f, 1.0f, 1.0f), PartPose.ZERO);
		return LayerDefinition.create(mesh, 64, 64).bakeRoot();
	}

	private static ModelPart lens() {
		MeshDefinition mesh = new MeshDefinition();
		mesh.getRoot().addOrReplaceChild("lens", CubeListBuilder.create().texOffs(22, 0).addBox(-3.0f, -5.0f, -4.9f, 6.0f, 3.0f, 0.8f), PartPose.ZERO);
		return LayerDefinition.create(mesh, 64, 64).bakeRoot();
	}

	/** Octagonal prism = an axis-aligned square prism plus the same one turned 45 degrees. */
	private static void octagon(PartDefinition root, String name, int u, int v, float y, float h, float side, float z, CubeDeformation def) {
		float half = side / 2.0f;
		root.addOrReplaceChild(name + "_a", CubeListBuilder.create().texOffs(u, v).addBox(-half, y, -half, side, h, side, def), PartPose.offset(0.0f, 0.0f, z));
		root.addOrReplaceChild(name + "_b", CubeListBuilder.create().texOffs(u, v).addBox(-half, y, -half, side, h, side, def), PartPose.offsetAndRotation(0.0f, 0.0f, z, 0.0f, QUARTER, 0.0f));
	}

	private static ModelPart bcd() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();
		CubeDeformation vest = new CubeDeformation(0.8f);
		root.addOrReplaceChild("panel_left", CubeListBuilder.create().texOffs(0, 13).addBox(1.0f, 0.0f, -2.0f, 3.0f, 9.0f, 4.0f, vest), PartPose.ZERO);
		root.addOrReplaceChild("panel_right", CubeListBuilder.create().texOffs(0, 13).addBox(-4.0f, 0.0f, -2.0f, 3.0f, 9.0f, 4.0f, vest), PartPose.ZERO);
		root.addOrReplaceChild("cummerbund", CubeListBuilder.create().texOffs(14, 13).addBox(-4.0f, 9.0f, -2.0f, 8.0f, 2.0f, 4.0f, new CubeDeformation(0.85f)), PartPose.ZERO);
		// Cylinder on the back (+z), centred 4.5 px behind the body axis.
		float z = 4.5f;
		octagon(root, "tank", 0, 26, 0.5f, 12.0f, 4.0f, z, CubeDeformation.NONE);
		octagon(root, "band_top", 16, 26, 3.0f, 1.0f, 4.0f, z, new CubeDeformation(0.15f));
		octagon(root, "band_bottom", 16, 26, 9.0f, 1.0f, 4.0f, z, new CubeDeformation(0.15f));
		octagon(root, "cap", 16, 32, -0.5f, 1.0f, 3.0f, z, CubeDeformation.NONE);
		root.addOrReplaceChild("valve", CubeListBuilder.create().texOffs(28, 32).addBox(-0.5f, -2.5f, -0.5f, 1.0f, 2.0f, 1.0f), PartPose.offset(0.0f, 0.0f, z));
		root.addOrReplaceChild("handwheel", CubeListBuilder.create().texOffs(32, 32).addBox(-1.5f, -2.5f, -0.5f, 3.0f, 1.0f, 1.0f), PartPose.offset(0.0f, 0.0f, z));
		return LayerDefinition.create(mesh, 64, 64).bakeRoot();
	}

	private static ModelPart fin() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();
		root.addOrReplaceChild("boot", CubeListBuilder.create().texOffs(0, 42).addBox(-2.0f, 9.0f, -2.0f, 4.0f, 3.0f, 4.0f, new CubeDeformation(0.7f)), PartPose.ZERO);
		root.addOrReplaceChild("blade", CubeListBuilder.create().texOffs(16, 42).addBox(-2.5f, 11.5f, -10.0f, 5.0f, 1.0f, 8.0f), PartPose.ZERO);
		root.addOrReplaceChild("rail_left", CubeListBuilder.create().texOffs(0, 51).addBox(1.5f, 10.5f, -10.0f, 1.0f, 1.0f, 8.0f), PartPose.ZERO);
		root.addOrReplaceChild("rail_right", CubeListBuilder.create().texOffs(0, 51).addBox(-2.5f, 10.5f, -10.0f, 1.0f, 1.0f, 8.0f), PartPose.ZERO);
		return LayerDefinition.create(mesh, 64, 64).bakeRoot();
	}
}
