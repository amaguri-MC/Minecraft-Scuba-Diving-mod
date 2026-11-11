package net.scubadive.client.creature.critter;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.rendertype.RenderTypes;

import net.scubadive.ScubaDive;

/**
 * Comb jelly: clear oval body (rounded by end caps) with a faint gut inside and eight vertical comb rows, two on
 * each side. The rows beat in a wave and the body turns slowly; the rows glow through {@link ShimmerLayer}.
 * Texture 32x32 with alpha plus a glow texture of the same layout (tools/K3_jelly_tex.py).
 */
public class CombJellyModel extends EntityModel<CritterRenderState> {
	public static final ModelLayerLocation LAYER = new ModelLayerLocation(ScubaDive.id("comb_jelly"), "main");
	private static final int ROWS = 8;

	private final ModelPart body;
	private final ModelPart[] rows = new ModelPart[ROWS];

	public CombJellyModel(ModelPart root) {
		super(root, RenderTypes::entityTranslucent);
		this.body = root.getChild("body");
		for (int i = 0; i < ROWS; i++) {
			this.rows[i] = this.body.getChild("row" + i);
		}
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();
		PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create()
				.texOffs(0, 0).addBox(-2.5f, -3.5f, -2.5f, 5.0f, 7.0f, 5.0f)
				.texOffs(0, 12).addBox(-1.5f, -4.5f, -1.5f, 3.0f, 1.0f, 3.0f)
				.texOffs(0, 16).addBox(-1.5f, 3.5f, -1.5f, 3.0f, 1.0f, 3.0f)
				.texOffs(20, 0).addBox(-0.75f, -2.5f, -0.75f, 1.5f, 5.0f, 1.5f), PartPose.offset(0.0f, 19.5f, 0.0f));
		for (int i = 0; i < ROWS; i++) {
			// Two rows per side, at x = -1.2 and +1.2 on that side.
			float yaw = (float) ((i / 2) * Math.PI / 2);
			float side = (i % 2 == 0) ? -1.2f : 1.2f;
			// Pivot on the body surface (same rotation as the renderer's Y turn), so the beat bulges outward.
			float px = (float) (side * Math.cos(yaw) + 2.45 * Math.sin(yaw));
			float pz = (float) (-side * Math.sin(yaw) + 2.45 * Math.cos(yaw));
			body.addOrReplaceChild("row" + i, CubeListBuilder.create().texOffs(26, 0)
							.addBox(-0.4f, -3.2f, 0.0f, 0.8f, 6.4f, 0.4f),
					PartPose.offsetAndRotation(px, 0.0f, pz, 0.0f, yaw, 0.0f));
		}
		return LayerDefinition.create(mesh, 32, 32);
	}

	@Override
	public void setupAnim(CritterRenderState state) {
		super.setupAnim(state);
		float t = state.ageInTicks;
		this.body.yRot = t * 0.012f;
		// Gentle breathing instead of a bell pulse: comb jellies glide on their beating combs.
		float breathe = (float) Math.sin(t * 0.05f) * 0.04f;
		this.body.xScale = 1.0f + breathe;
		this.body.zScale = 1.0f + breathe;
		this.body.yScale = 1.0f - breathe * 0.5f;
		this.body.zRot = (float) Math.sin(t * 0.021f) * 0.08f;
		for (int i = 0; i < ROWS; i++) {
			// The combs beat in a metachronal wave running round the body.
			float beat = (float) Math.sin(t * 0.5f - i * 0.8f);
			this.rows[i].zScale = 1.0f + 0.7f * Math.max(0.0f, beat);
		}
	}
}
