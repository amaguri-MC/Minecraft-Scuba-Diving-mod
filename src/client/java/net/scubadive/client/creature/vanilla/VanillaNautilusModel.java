package net.scubadive.client.creature.vanilla;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.util.Mth;
import net.scubadive.ScubaDive;

/**
 * Chambered nautilus on the vanilla (rideable) nautilus. The shell pitches gently with the swimming direction, the
 * hood breathes and the many cirri sway in a slow wave; during a dash they are drawn together and trail back.
 */
public class VanillaNautilusModel extends EntityModel<VanillaNautilusRenderState> {
	public static final ModelLayerLocation LAYER = new ModelLayerLocation(ScubaDive.id("vanilla_nautilus"), "main");
	/** Height of the saddle seat above the feet in model px (shell crown + saddle). */
	static final float SEAT_PX = 24.0f - NautilusGeometry.SHELL_TOP_Y + 0.5f;

	private final ModelPart main;
	private final ModelPart soft;
	private final ModelPart[] tentacles;

	public VanillaNautilusModel(ModelPart root) {
		super(root);
		this.main = root.getChild("main");
		this.soft = this.main.getChild("soft");
		ModelPart arms = this.soft.getChild("arms");
		this.tentacles = new ModelPart[NautilusGeometry.TENTACLES.length];
		for (int i = 0; i < this.tentacles.length; i++) {
			this.tentacles[i] = arms.getChild(NautilusGeometry.TENTACLES[i]);
		}
	}

	public static LayerDefinition createLayer() {
		return NautilusGeometry.createLayer();
	}

	/** Shared with the saddle and armour models so the gear follows the shell. */
	static void poseShell(ModelPart main, VanillaNautilusRenderState state) {
		main.xRot = Mth.clamp(state.xRot, -50.0f, 50.0f) * Mth.DEG_TO_RAD * 0.6f;
	}

	@Override
	public void setupAnim(VanillaNautilusRenderState state) {
		super.setupAnim(state);
		float t = state.ageInTicks + state.phase;
		poseShell(this.main, state);
		this.soft.xRot = Mth.sin(t * 0.1f) * 0.035f;
		float spread = state.dashing ? -0.12f : 0.07f + 0.05f * Mth.sin(t * 0.09f);
		float droop = state.dashing ? -0.35f : 0.0f;
		for (int i = 0; i < this.tentacles.length; i++) {
			ModelPart p = this.tentacles[i];
			float wave = Mth.sin(t * 0.14f + i * 0.7f) * (state.dashing ? 0.04f : 0.12f);
			p.xRot += wave + droop + spread * NautilusGeometry.TENTACLE_DY[i];
			p.yRot += -spread * NautilusGeometry.TENTACLE_DX[i];
		}
	}
}
