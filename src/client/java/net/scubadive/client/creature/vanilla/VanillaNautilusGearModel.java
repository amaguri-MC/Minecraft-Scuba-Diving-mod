package net.scubadive.client.creature.vanilla;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.scubadive.ScubaDive;

/** Saddle or armour fitted to the renewed nautilus shell (same root pivot and pose as the body model). */
public class VanillaNautilusGearModel extends EntityModel<VanillaNautilusRenderState> {
	public static final ModelLayerLocation SADDLE = new ModelLayerLocation(ScubaDive.id("vanilla_nautilus"), "saddle");
	public static final ModelLayerLocation ARMOR = new ModelLayerLocation(ScubaDive.id("vanilla_nautilus"), "armor");

	private final ModelPart main;

	public VanillaNautilusGearModel(ModelPart root) {
		super(root);
		this.main = root.getChild("main");
	}

	@Override
	public void setupAnim(VanillaNautilusRenderState state) {
		super.setupAnim(state);
		VanillaNautilusModel.poseShell(this.main, state);
	}
}
