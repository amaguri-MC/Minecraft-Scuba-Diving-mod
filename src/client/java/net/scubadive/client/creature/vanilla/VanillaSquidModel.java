package net.scubadive.client.creature.vanilla;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.util.Mth;
import net.scubadive.ScubaDive;

/**
 * Bigfin reef squid on the vanilla squid, drawn translucent. A wave runs along the fins that line the whole mantle;
 * the arms open with the vanilla jet cycle (tentacle angle) and sway, the two long tentacles trail.
 */
public class VanillaSquidModel extends EntityModel<VanillaSquidRenderState> {
	public static final ModelLayerLocation LAYER = new ModelLayerLocation(ScubaDive.id("vanilla_squid"), "main");

	private final ModelPart[] arms;
	private final ModelPart[] finsRight;
	private final ModelPart[] finsLeft;

	public VanillaSquidModel(ModelPart root) {
		super(root, RenderTypes::entityTranslucent);
		ModelPart body = root.getChild("body");
		this.arms = new ModelPart[SquidGeometry.ARMS.length];
		for (int i = 0; i < this.arms.length; i++) {
			this.arms[i] = body.getChild(SquidGeometry.ARMS[i]);
		}
		this.finsRight = new ModelPart[SquidGeometry.FINS_R.length];
		this.finsLeft = new ModelPart[SquidGeometry.FINS_L.length];
		for (int i = 0; i < this.finsRight.length; i++) {
			this.finsRight[i] = body.getChild(SquidGeometry.FINS_R[i]);
			this.finsLeft[i] = body.getChild(SquidGeometry.FINS_L[i]);
		}
	}

	@Override
	public void setupAnim(VanillaSquidRenderState state) {
		super.setupAnim(state);
		float t = state.ageInTicks + state.phase;
		// Fin wave from the head (index 0) to the tail; a little stronger while jetting.
		float amp = 0.22f + 0.25f * state.tentacleAngle;
		for (int i = 0; i < this.finsRight.length; i++) {
			float w = Mth.sin(t * 0.32f - i * 0.8f) * amp;
			this.finsRight[i].zRot = w;
			this.finsLeft[i].zRot = -w;
		}
		// Arms open with the jet cycle (tentacleAngle 0..pi/4) and sway a little.
		float open = state.tentacleAngle * 0.7f;
		for (int i = 0; i < this.arms.length; i++) {
			boolean tentacle = i >= 8;
			float spread = (tentacle ? 0.3f : 1.0f) * open + 0.05f;
			float sway = Mth.sin(t * 0.18f + i * 1.3f) * (tentacle ? 0.06f : 0.08f);
			ModelPart p = this.arms[i];
			p.xRot += spread * SquidGeometry.ARM_DY[i] + sway;
			p.yRot += -spread * SquidGeometry.ARM_DX[i];
		}
	}
}
