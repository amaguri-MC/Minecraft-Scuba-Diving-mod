package net.scubadive.client.creature.vanilla;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.util.Mth;
import net.scubadive.ScubaDive;

/**
 * Green sea turtle on the vanilla turtle. Swimming: the long front flippers beat up and down like wings while
 * feathering back and forth, the rear flippers steer. On land: the flippers drag the body forward in turns; while
 * laying eggs the rear flippers dig. The egg belly shows while the turtle carries eggs.
 */
public class VanillaTurtleModel extends EntityModel<VanillaTurtleRenderState> {
	public static final ModelLayerLocation LAYER = new ModelLayerLocation(ScubaDive.id("vanilla_turtle"), "main");

	private final ModelPart body;
	private final ModelPart head;
	private final ModelPart frontRight;
	private final ModelPart frontLeft;
	private final ModelPart rearRight;
	private final ModelPart rearLeft;
	private final ModelPart egg;

	public VanillaTurtleModel(ModelPart root) {
		super(root);
		this.body = root.getChild("body");
		this.head = this.body.getChild("head");
		this.frontRight = this.body.getChild("fl_r");
		this.frontLeft = this.body.getChild("fl_l");
		this.rearRight = this.body.getChild("rl_r");
		this.rearLeft = this.body.getChild("rl_l");
		this.egg = this.body.getChild("egg");
	}

	public static LayerDefinition createLayer() {
		return TurtleGeometry.createLayer();
	}

	@Override
	public void setupAnim(VanillaTurtleRenderState state) {
		super.setupAnim(state);
		float t = state.ageInTicks + state.phase;
		this.head.yRot = Mth.clamp(state.yRot, -60.0f, 60.0f) * Mth.DEG_TO_RAD * 0.7f;
		this.head.xRot = Mth.clamp(state.xRot, -40.0f, 40.0f) * Mth.DEG_TO_RAD * 0.7f;
		this.egg.visible = state.hasEgg && !state.isBaby;

		if (state.isOnLand) {
			// Crawl: the front flippers swing forward and back in turns, the rear ones push opposite.
			float w = state.walkAnimationPos * 0.9f;
			float amount = Math.min(state.walkAnimationSpeed, 1.0f);
			float swing = Mth.cos(w) * 0.55f * amount;
			this.frontRight.yRot = swing;
			this.frontLeft.yRot = swing;
			this.frontRight.zRot = 0.15f;
			this.frontLeft.zRot = -0.15f;
			this.rearRight.yRot = -swing * 0.6f;
			this.rearLeft.yRot = -swing * 0.6f;
			if (state.isLayingEgg) {
				float dig = Mth.sin(t * 0.8f) * 0.45f;
				this.rearRight.zRot = 0.2f + dig;
				this.rearLeft.zRot = -0.2f + dig;
				this.rearRight.yRot = 0.25f;
				this.rearLeft.yRot = -0.25f;
				this.head.xRot += 0.15f;
			}
			return;
		}
		// Swimming: slow wing-like strokes, faster when the turtle is on its way somewhere.
		float speed = 0.11f + 0.2f * Math.min(state.walkAnimationSpeed, 1.0f);
		float phase = t * speed;
		float beat = Mth.sin(phase);
		float feather = Mth.cos(phase);
		this.frontRight.zRot = beat * 0.5f;
		this.frontLeft.zRot = -beat * 0.5f;
		this.frontRight.yRot = feather * 0.25f;
		this.frontLeft.yRot = -feather * 0.25f;
		float steer = Mth.sin(phase + 1.0f) * 0.15f;
		this.rearRight.yRot = steer;
		this.rearLeft.yRot = -steer;
		this.body.y += beat * 0.3f;
	}
}
