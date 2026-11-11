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
 * Flower hat jelly: small clear dome bell with pink gut and green canals. Short tentacles stick out of the rim and
 * the bell's side and curl outward, each tipped purple, yellow or black (neon-like, lit by a soft glow layer).
 * Texture 64x64 with alpha, glow texture flower_hat_jelly_glow.png (tools/K6_jelly_tex.py).
 */
public class FlowerHatJellyModel extends EntityModel<CritterRenderState> {
	public static final ModelLayerLocation LAYER = new ModelLayerLocation(ScubaDive.id("flower_hat_jelly"), "main");
	static final float PULSE = 36.0f;
	private static final int RIM = 16;
	private static final int SIDE = 8;
	private static final int ALL = RIM + SIDE;

	private final ModelPart bell;
	private final ModelPart[] bases = new ModelPart[ALL];
	private final ModelPart[] tips = new ModelPart[ALL];

	public FlowerHatJellyModel(ModelPart root) {
		super(root, RenderTypes::entityTranslucent);
		this.bell = root.getChild("bell");
		for (int i = 0; i < ALL; i++) {
			this.bases[i] = root.getChild("tentacle" + i);
			this.tips[i] = this.bases[i].getChild("tip");
		}
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();
		float rimY = 19.5f;
		root.addOrReplaceChild("bell", CubeListBuilder.create()
				.texOffs(0, 0).addBox(-4.0f, -1.0f, -4.0f, 8.0f, 1.0f, 8.0f)
				.texOffs(0, 10).addBox(-3.75f, -4.0f, -3.75f, 7.5f, 3.0f, 7.5f)
				.texOffs(0, 22).addBox(-3.0f, -6.0f, -3.0f, 6.0f, 2.0f, 6.0f)
				.texOffs(0, 31).addBox(-1.75f, -7.0f, -1.75f, 3.5f, 1.0f, 3.5f), PartPose.offset(0.0f, rimY, 0.0f));
		root.addOrReplaceChild("gut", CubeListBuilder.create().texOffs(34, 0)
				.addBox(-1.0f, -3.0f, -1.0f, 2.0f, 3.0f, 2.0f), PartPose.offset(0.0f, rimY + 1.0f, 0.0f));
		for (int i = 0; i < ALL; i++) {
			boolean rim = i < RIM;
			float a = rim ? (float) (i * 2.0 * Math.PI / RIM) : (float) ((i - RIM) * 2.0 * Math.PI / SIDE + Math.PI / SIDE);
			float r = rim ? 3.8f : 3.6f;
			float y = rim ? rimY : rimY - 2.5f;
			// Rim tentacles splay out and down, side tentacles stick straight out; tips curl further out and up.
			float out = rim ? 0.8f + (i % 2) * 0.35f : 1.7f;
			int tipU = 48 + (i % 3) * 4;
			PartDefinition base = root.addOrReplaceChild("tentacle" + i, CubeListBuilder.create().texOffs(44, 0)
							.addBox(-0.25f, 0.0f, -0.25f, 0.5f, 3.0f, 0.5f),
					PartPose.offsetAndRotation((float) Math.cos(a) * r, y, (float) Math.sin(a) * r, out, (float) Math.PI / 2 - a, 0.0f));
			base.addOrReplaceChild("tip", CubeListBuilder.create().texOffs(tipU, 0)
					.addBox(-0.25f, 0.0f, -0.25f, 0.5f, 2.5f, 0.5f), PartPose.offsetAndRotation(0.0f, 3.0f, 0.0f, 0.7f, 0.0f, 0.0f));
		}
		return LayerDefinition.create(mesh, 64, 64);
	}

	@Override
	public void setupAnim(CritterRenderState state) {
		super.setupAnim(state);
		float t = state.ageInTicks;
		float squeeze = JellyPulse.squeeze(t, PULSE);
		JellyPulse.bell(this.bell, squeeze, 0.16f);
		for (int i = 0; i < ALL; i++) {
			JellyPulse.sway(this.bases[i], this.tips[i], t, i, 0.06f, 0.07f, squeeze);
		}
	}
}
