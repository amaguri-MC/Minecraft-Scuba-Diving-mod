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
 * Echizen (Nomura's) jelly, the rare giant: a thick, domed cream-to-tan bell speckled with brown warts, a short
 * stout column underneath and eight heavy two-segment oral arms, each with frilled flaps, plus a fringe of tiny
 * marginal tentacles. Slow, heavy pulses. Drawn at 2x scale (bell about two blocks across).
 * Texture 128x64 with alpha (tools/K3_jelly_tex.py).
 */
public class EchizenJellyModel extends EntityModel<CritterRenderState> {
	public static final ModelLayerLocation LAYER = new ModelLayerLocation(ScubaDive.id("echizen_jelly"), "main");
	static final float PULSE = 90.0f;
	private static final int ARMS = 8;
	private static final int FRINGE = 16;
	private static final float SEGMENT = 7.0f;

	private final ModelPart bell;
	private final ModelPart column;
	private final ModelPart[] bases = new ModelPart[ARMS];
	private final ModelPart[] tips = new ModelPart[ARMS];
	private final ModelPart[] fringe = new ModelPart[FRINGE];

	public EchizenJellyModel(ModelPart root) {
		super(root, RenderTypes::entityTranslucent);
		this.bell = root.getChild("bell");
		this.column = root.getChild("column");
		for (int i = 0; i < ARMS; i++) {
			this.bases[i] = root.getChild("arm" + i);
			this.tips[i] = this.bases[i].getChild("tip");
		}
		for (int i = 0; i < FRINGE; i++) {
			this.fringe[i] = this.bell.getChild("fringe" + i);
		}
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();
		float rimY = 24.0f - 3.0f - SEGMENT * 2;
		PartDefinition bell = root.addOrReplaceChild("bell", CubeListBuilder.create()
				.texOffs(0, 0).addBox(-8.0f, -1.0f, -8.0f, 16.0f, 1.0f, 16.0f)
				.texOffs(0, 17).addBox(-7.5f, -4.0f, -7.5f, 15.0f, 3.0f, 15.0f)
				.texOffs(0, 35).addBox(-6.0f, -6.0f, -6.0f, 12.0f, 2.0f, 12.0f)
				.texOffs(48, 35).addBox(-4.0f, -7.0f, -4.0f, 8.0f, 1.0f, 8.0f), PartPose.offset(0.0f, rimY, 0.0f));
		for (int i = 0; i < FRINGE; i++) {
			float a = (float) (i * 2.0 * Math.PI / FRINGE);
			bell.addOrReplaceChild("fringe" + i, CubeListBuilder.create().texOffs(112, 0)
							.addBox(-0.2f, 0.0f, -0.2f, 0.4f, 2.0f, 0.4f),
					PartPose.offset((float) Math.cos(a) * 7.7f, 0.0f, (float) Math.sin(a) * 7.7f));
		}
		root.addOrReplaceChild("column", CubeListBuilder.create().texOffs(64, 0)
				.addBox(-3.0f, 0.0f, -3.0f, 6.0f, 4.0f, 6.0f), PartPose.offset(0.0f, rimY, 0.0f));
		for (int i = 0; i < ARMS; i++) {
			float a = (float) (i * 2.0 * Math.PI / ARMS + Math.PI / ARMS);
			float yaw = (float) Math.PI / 2 - a;
			PartDefinition arm = root.addOrReplaceChild("arm" + i, CubeListBuilder.create()
							.texOffs(64, 10).addBox(-1.5f, 0.0f, -1.5f, 3.0f, SEGMENT, 3.0f)
							.texOffs(88, 0).addBox(-2.25f, 1.0f, -0.25f, 4.5f, 5.0f, 0.5f),
					PartPose.offsetAndRotation((float) Math.cos(a) * 2.4f, rimY + 3.0f, (float) Math.sin(a) * 2.4f, 0.22f, yaw, 0.0f));
			arm.addOrReplaceChild("tip", CubeListBuilder.create()
					.texOffs(64, 20).addBox(-1.25f, 0.0f, -1.25f, 2.5f, SEGMENT, 2.5f)
					.texOffs(88, 6).addBox(-2.0f, 0.5f, -0.25f, 4.0f, 6.0f, 0.5f)
					.texOffs(100, 0).addBox(-0.25f, SEGMENT, -0.25f, 0.5f, 2.0f, 0.5f), PartPose.offset(0.0f, SEGMENT, 0.0f));
		}
		return LayerDefinition.create(mesh, 128, 64);
	}

	@Override
	public void setupAnim(CritterRenderState state) {
		super.setupAnim(state);
		float t = state.ageInTicks;
		float squeeze = JellyPulse.squeeze(t, PULSE);
		JellyPulse.bell(this.bell, squeeze, 0.1f);
		this.column.xScale = 1.0f - 0.06f * squeeze;
		this.column.zScale = 1.0f - 0.06f * squeeze;
		for (int i = 0; i < ARMS; i++) {
			JellyPulse.sway(this.bases[i], this.tips[i], t, i * 2, 0.025f, 0.08f, squeeze);
		}
		for (int i = 0; i < FRINGE; i++) {
			this.fringe[i].xRot = (float) Math.sin(t * 0.07f + i) * 0.25f;
		}
	}
}
