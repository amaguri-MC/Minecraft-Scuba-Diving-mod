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
 * Cannonball jelly: hard, ball-like thick hemisphere bell, cream with a brown rim band, almost no tentacles,
 * just a short lumpy cluster of oral arms underneath. Quick bouncy pulses.
 * Texture 64x64 with alpha (tools/K6_jelly_tex.py).
 */
public class CannonballJellyModel extends EntityModel<CritterRenderState> {
	public static final ModelLayerLocation LAYER = new ModelLayerLocation(ScubaDive.id("cannonball_jelly"), "main");
	static final float PULSE = 14.0f;
	private static final float ARM = 4.0f;

	private final ModelPart bell;
	private final ModelPart[] lumps = new ModelPart[4];

	public CannonballJellyModel(ModelPart root) {
		super(root, RenderTypes::entityTranslucent);
		this.bell = root.getChild("bell");
		for (int i = 0; i < 4; i++) {
			this.lumps[i] = root.getChild("lump" + i);
		}
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();
		float rimY = 24.0f - ARM;
		root.addOrReplaceChild("bell", CubeListBuilder.create()
				.texOffs(0, 0).addBox(-5.0f, -2.0f, -5.0f, 10.0f, 2.0f, 10.0f)
				.texOffs(0, 12).addBox(-4.5f, -5.0f, -4.5f, 9.0f, 3.0f, 9.0f)
				.texOffs(0, 24).addBox(-3.5f, -7.0f, -3.5f, 7.0f, 2.0f, 7.0f)
				.texOffs(0, 34).addBox(-2.0f, -8.0f, -2.0f, 4.0f, 1.0f, 4.0f), PartPose.offset(0.0f, rimY, 0.0f));
		root.addOrReplaceChild("arms", CubeListBuilder.create().texOffs(40, 0)
				.addBox(-2.0f, 0.0f, -2.0f, 4.0f, 3.0f, 4.0f), PartPose.offset(0.0f, rimY, 0.0f));
		for (int i = 0; i < 4; i++) {
			float a = (float) (i * Math.PI / 2 + Math.PI / 4);
			root.addOrReplaceChild("lump" + i, CubeListBuilder.create().texOffs(40, 8)
							.addBox(-0.75f, 0.0f, -0.75f, 1.5f, 2.5f, 1.5f),
					PartPose.offsetAndRotation((float) Math.cos(a) * 1.8f, rimY + 1.5f, (float) Math.sin(a) * 1.8f,
							0.3f, (float) Math.PI / 2 - a, 0.0f));
		}
		return LayerDefinition.create(mesh, 64, 64);
	}

	@Override
	public void setupAnim(CritterRenderState state) {
		super.setupAnim(state);
		float t = state.ageInTicks;
		float squeeze = JellyPulse.squeeze(t, PULSE);
		// Stiff bell: only a small squeeze.
		JellyPulse.bell(this.bell, squeeze, 0.1f);
		for (int i = 0; i < 4; i++) {
			JellyPulse.sway(this.lumps[i], null, t, i, 0.08f, 0.05f, squeeze);
		}
	}
}
