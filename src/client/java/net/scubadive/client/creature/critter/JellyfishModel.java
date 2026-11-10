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
 * Jellyfish: stacked translucent bell, four frilly oral arms and a ring of thin tentacles that sway.
 * The bell pulses. Texture 64x64 with alpha.
 */
public class JellyfishModel extends EntityModel<CritterRenderState> {
	public static final ModelLayerLocation MOON = new ModelLayerLocation(ScubaDive.id("moon_jelly"), "main");
	public static final ModelLayerLocation NETTLE = new ModelLayerLocation(ScubaDive.id("sea_nettle"), "main");
	public static final ModelLayerLocation SPOTTED = new ModelLayerLocation(ScubaDive.id("spotted_jelly"), "main");

	private final ModelPart bell;
	private final ModelPart[] arms = new ModelPart[4];
	private final ModelPart[] tentacles;

	public JellyfishModel(ModelPart root, int tentacleCount) {
		super(root, RenderTypes::entityTranslucent);
		this.bell = root.getChild("bell");
		for (int i = 0; i < 4; i++) {
			this.arms[i] = root.getChild("arm" + i);
		}
		this.tentacles = new ModelPart[tentacleCount];
		for (int i = 0; i < tentacleCount; i++) {
			this.tentacles[i] = root.getChild("tentacle" + i);
		}
	}

	public static LayerDefinition moon() {
		return create(4.0f, 3.0f, 16, 5.0f);
	}

	public static LayerDefinition nettle() {
		return create(9.0f, 4.0f, 12, 16.0f);
	}

	public static LayerDefinition spotted() {
		return create(5.0f, 1.6f, 0, 0.0f);
	}

	/** armLength / armWidth for the oral arms, tentacle count and length. The longest part ends at the feet (y = 24). */
	private static LayerDefinition create(float armLength, float armWidth, int tentacleCount, float tentacleLength) {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();
		float rimY = 24.0f - Math.max(armLength, tentacleLength);
		root.addOrReplaceChild("bell", CubeListBuilder.create()
				.texOffs(0, 0).addBox(-5.0f, -1.0f, -5.0f, 10.0f, 1.0f, 10.0f)
				.texOffs(0, 12).addBox(-4.5f, -4.0f, -4.5f, 9.0f, 3.0f, 9.0f)
				.texOffs(0, 26).addBox(-3.0f, -6.0f, -3.0f, 6.0f, 2.0f, 6.0f), PartPose.offset(0.0f, rimY, 0.0f));
		for (int i = 0; i < 4; i++) {
			float a = (float) (i * Math.PI / 2 + Math.PI / 4);
			root.addOrReplaceChild("arm" + i, CubeListBuilder.create().texOffs(40, 0)
							.addBox(-armWidth / 2, 0.0f, -armWidth / 4, armWidth, armLength, armWidth / 2),
					PartPose.offsetAndRotation((float) Math.cos(a) * 1.2f, rimY, (float) Math.sin(a) * 1.2f, 0.0f, -a, 0.0f));
		}
		for (int i = 0; i < tentacleCount; i++) {
			float a = (float) (i * 2.0 * Math.PI / tentacleCount);
			root.addOrReplaceChild("tentacle" + i, CubeListBuilder.create().texOffs(56, 0)
							.addBox(-0.25f, 0.0f, -0.25f, 0.5f, tentacleLength, 0.5f),
					PartPose.offset((float) Math.cos(a) * 4.6f, rimY, (float) Math.sin(a) * 4.6f));
		}
		return LayerDefinition.create(mesh, 64, 64);
	}

	@Override
	public void setupAnim(CritterRenderState state) {
		super.setupAnim(state);
		float t = state.ageInTicks;
		// Contract the bell for a moment once per swim pulse (50 ticks), then relax.
		float phase = (t % 50.0f) / 50.0f;
		float squeeze = phase < 0.2f ? (float) Math.sin(phase / 0.2f * Math.PI) : 0.0f;
		this.bell.xScale = 1.0f - 0.18f * squeeze;
		this.bell.zScale = 1.0f - 0.18f * squeeze;
		this.bell.yScale = 1.0f + 0.12f * squeeze;
		for (int i = 0; i < this.arms.length; i++) {
			this.arms[i].zRot = (float) Math.sin(t * 0.06f + i) * 0.12f;
		}
		for (int i = 0; i < this.tentacles.length; i++) {
			this.tentacles[i].xRot = (float) Math.sin(t * 0.05f + i * 0.7f) * 0.18f + squeeze * 0.15f;
			this.tentacles[i].zRot = (float) Math.cos(t * 0.04f + i * 0.9f) * 0.18f;
		}
	}
}
