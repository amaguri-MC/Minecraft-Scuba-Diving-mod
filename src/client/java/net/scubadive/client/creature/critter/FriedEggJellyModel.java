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
 * Fried egg jelly: flat creamy-white bell with a raised yellow dome in the middle (the "yolk"), and a cluster of
 * eight short oral arms underneath, each ending in a violet knob. Calm pulses.
 * Texture 64x64 with alpha (tools/K3_jelly_tex.py).
 */
public class FriedEggJellyModel extends EntityModel<CritterRenderState> {
	public static final ModelLayerLocation LAYER = new ModelLayerLocation(ScubaDive.id("fried_egg_jelly"), "main");
	static final float PULSE = 50.0f;
	private static final int ARMS = 8;
	private static final float ARM = 5.0f;

	private final ModelPart bell;
	private final ModelPart[] arms = new ModelPart[ARMS];

	public FriedEggJellyModel(ModelPart root) {
		super(root, RenderTypes::entityTranslucent);
		this.bell = root.getChild("bell");
		for (int i = 0; i < ARMS; i++) {
			this.arms[i] = root.getChild("arm" + i);
		}
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();
		float rimY = 24.0f - ARM - 1.5f;
		root.addOrReplaceChild("bell", CubeListBuilder.create()
				.texOffs(0, 0).addBox(-6.0f, -1.0f, -6.0f, 12.0f, 1.0f, 12.0f)
				.texOffs(0, 13).addBox(-5.5f, -2.5f, -5.5f, 11.0f, 1.5f, 11.0f)
				.texOffs(0, 26).addBox(-3.0f, -4.5f, -3.0f, 6.0f, 2.0f, 6.0f), PartPose.offset(0.0f, rimY, 0.0f));
		for (int i = 0; i < ARMS; i++) {
			float a = (float) (i * 2.0 * Math.PI / ARMS);
			float r = (i % 2 == 0) ? 1.4f : 2.6f;
			PartDefinition arm = root.addOrReplaceChild("arm" + i, CubeListBuilder.create().texOffs(48, 0)
							.addBox(-1.0f, 0.0f, -1.0f, 2.0f, ARM - (i % 2), 2.0f),
					PartPose.offsetAndRotation((float) Math.cos(a) * r, rimY, (float) Math.sin(a) * r, 0.15f, (float) Math.PI / 2 - a, 0.0f));
			arm.addOrReplaceChild("knob", CubeListBuilder.create().texOffs(48, 7)
					.addBox(-1.25f, 0.0f, -1.25f, 2.5f, 1.5f, 2.5f), PartPose.offset(0.0f, ARM - (i % 2), 0.0f));
		}
		return LayerDefinition.create(mesh, 64, 64);
	}

	@Override
	public void setupAnim(CritterRenderState state) {
		super.setupAnim(state);
		float t = state.ageInTicks;
		float squeeze = JellyPulse.squeeze(t, PULSE);
		JellyPulse.bell(this.bell, squeeze, 0.15f);
		for (int i = 0; i < ARMS; i++) {
			JellyPulse.sway(this.arms[i], null, t, i, 0.05f, 0.08f, squeeze);
		}
	}
}
