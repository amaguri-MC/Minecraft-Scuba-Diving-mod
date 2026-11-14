package net.scubadive.client.creature.vanilla;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityAttachment;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.nautilus.Nautilus;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.phys.Vec3;
import net.scubadive.ScubaDive;

/**
 * Renewed look for the vanilla (tameable, rideable) nautilus; the zombie nautilus keeps its vanilla renderer.
 * Saddle and armour are drawn by {@link VanillaNautilusGearLayer} on models fitted to the new shell, and the whole
 * model is shifted so the saddle seat meets the vanilla passenger attachment point (where the rider sits).
 */
public class VanillaNautilusRenderer extends MobRenderer<Nautilus, VanillaNautilusRenderState, VanillaNautilusModel> {
	private static final Identifier TEXTURE = ScubaDive.id("textures/entity/vanilla/nautilus.png");
	private static final String[] ARMOR_MATERIALS = {"copper", "iron", "gold", "diamond", "netherite"};
	private static final float MAX_LIFT = 0.4f;
	private static final float MAX_SINK = 0.25f;

	public VanillaNautilusRenderer(EntityRendererProvider.Context context) {
		super(context, new VanillaNautilusModel(context.bakeLayer(VanillaNautilusModel.LAYER)), 0.7f);
		this.addLayer(new VanillaNautilusGearLayer(this,
				new VanillaNautilusGearModel(context.bakeLayer(VanillaNautilusGearModel.SADDLE)),
				new VanillaNautilusGearModel(context.bakeLayer(VanillaNautilusGearModel.ARMOR))));
	}

	@Override
	public VanillaNautilusRenderState createRenderState() {
		return new VanillaNautilusRenderState();
	}

	@Override
	public void extractRenderState(Nautilus entity, VanillaNautilusRenderState state, float partialTick) {
		super.extractRenderState(entity, state, partialTick);
		state.phase = entity.getId() * 7.0f;
		state.dashing = entity.isDashing();
		state.hasSaddle = !entity.getItemBySlot(EquipmentSlot.SADDLE).isEmpty();
		state.armorTexture = armorTexture(entity.getBodyArmorItem());
		state.seatLift = 0.0f;
		if (!state.isBaby) {
			Vec3 seat = entity.getAttachments().getNullable(EntityAttachment.PASSENGER, 0, 0.0f);
			if (seat != null) {
				float lift = (float) seat.y - VanillaNautilusModel.SEAT_PX / 16.0f * state.ageScale;
				state.seatLift = Mth.clamp(lift, -MAX_SINK, MAX_LIFT);
			}
		}
	}

	/** One texture per vanilla nautilus armour material (by the item's equipment asset); iron for anything else. */
	private static Identifier armorTexture(ItemStack armor) {
		if (armor.isEmpty()) {
			return null;
		}
		String material = "iron";
		Equippable equippable = armor.get(DataComponents.EQUIPPABLE);
		if (equippable != null && equippable.assetId().isPresent()) {
			String path = equippable.assetId().get().identifier().getPath();
			for (String m : ARMOR_MATERIALS) {
				if (m.equals(path)) {
					material = m;
				}
			}
		}
		return ScubaDive.id("textures/entity/vanilla/nautilus_armor_" + material + ".png");
	}

	@Override
	public Identifier getTextureLocation(VanillaNautilusRenderState state) {
		return TEXTURE;
	}

	@Override
	protected void scale(VanillaNautilusRenderState state, PoseStack poseStack) {
		super.scale(state, poseStack);
		poseStack.scale(state.ageScale, state.ageScale, state.ageScale);
	}

	@Override
	protected float getShadowRadius(VanillaNautilusRenderState state) {
		return super.getShadowRadius(state) * state.ageScale;
	}

	@Override
	protected void setupRotations(VanillaNautilusRenderState state, PoseStack poseStack, float bodyRot, float scale) {
		super.setupRotations(state, poseStack, bodyRot, scale);
		poseStack.translate(0.0f, state.seatLift, 0.0f);
	}
}
