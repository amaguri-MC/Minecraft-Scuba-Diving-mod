package net.scubadive.client.creature.vanilla;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.scubadive.ScubaDive;

/**
 * White-spotted puffer on the vanilla pufferfish, one model per puff state (0 normal, 1 half, 2 fully inflated).
 * Puffers row with their small fins: the pectorals flutter fast, dorsal and anal fin scull, the tail steers.
 */
public class VanillaPufferfishModel extends EntityModel<VanillaPufferfishRenderState> {
	public static final ModelLayerLocation SMALL = new ModelLayerLocation(ScubaDive.id("vanilla_pufferfish"), "small");
	public static final ModelLayerLocation MID = new ModelLayerLocation(ScubaDive.id("vanilla_pufferfish"), "mid");
	public static final ModelLayerLocation BIG = new ModelLayerLocation(ScubaDive.id("vanilla_pufferfish"), "big");

	private final ModelPart pecRight;
	private final ModelPart pecLeft;
	private final ModelPart tail;
	private final ModelPart dorsal;
	private final ModelPart anal;

	public VanillaPufferfishModel(ModelPart root) {
		super(root);
		ModelPart body = root.getChild("body");
		this.pecRight = body.getChild("pec_r");
		this.pecLeft = body.getChild("pec_l");
		this.tail = body.getChild("tail");
		this.dorsal = body.getChild("dorsal");
		this.anal = body.getChild("anal");
	}

	@Override
	public void setupAnim(VanillaPufferfishRenderState state) {
		super.setupAnim(state);
		float t = state.ageInTicks + state.phase;
		float flutter = Mth.sin(t * (state.puffState == 2 ? 1.3f : 0.9f)) * 0.4f;
		this.pecRight.yRot = flutter;
		this.pecLeft.yRot = -flutter;
		float scull = Mth.sin(t * 0.35f);
		this.dorsal.yRot = scull * 0.3f;
		this.anal.yRot = scull * 0.3f;
		this.tail.yRot = Mth.sin(t * 0.2f + 1.0f) * 0.25f;
	}
}
