package net.scubadive.tour.center;

import java.util.List;

import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;

import net.scubadive.species.Species;

/** Fish bucket helpers: which fish can be caught, the bucket stack format and the catch handler. */
public final class FishBuckets {
	/** Small reef and bottom fish that fit in a bucket. Sharks, rays, eels, octopus and groupers are too big. */
	public static final List<String> SPECIES = List.of(
			"scubadive:blue_damsel", "scubadive:chromis", "scubadive:anthias", "scubadive:blue_tang",
			"scubadive:butterflyfish", "scubadive:moorish_idol", "scubadive:lionfish", "scubadive:parrotfish",
			"scubadive:boxfish", "scubadive:triggerfish", "scubadive:bluestripe_snapper",
			"scubadive:clownfish", "scubadive:anemone_shrimp", "scubadive:goby", "scubadive:frogfish");

	private static final String TAG = "species";

	private FishBuckets() {
	}

	public static boolean isCatchable(String entityId) {
		return SPECIES.contains(entityId);
	}

	/** Builds a filled bucket. The custom data holds only the species so equal buckets stack-compare equal. */
	public static ItemStack create(String entityId) {
		ItemStack stack = new ItemStack(DiveCenter.FISH_BUCKET);
		CompoundTag tag = new CompoundTag();
		tag.putString(TAG, entityId);
		stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
		Species species = species(entityId);
		if (species != null) {
			stack.set(DataComponents.ITEM_NAME, Component.translatable("item.scubadive.fish_bucket.of", Component.translatable(species.langKey())));
		}
		return stack;
	}

	/** The CustomData of a bucket holding this species (for exact trade matching). */
	public static CustomData data(String entityId) {
		CompoundTag tag = new CompoundTag();
		tag.putString(TAG, entityId);
		return CustomData.of(tag);
	}

	/** Entity id stored in a bucket, or "" if empty or not a bucket. */
	public static String speciesOf(ItemStack stack) {
		CustomData data = stack.get(DataComponents.CUSTOM_DATA);
		if (data == null) {
			return "";
		}
		return data.copyTag().getStringOr(TAG, "");
	}

	/** Display name of the species in a bucket, or null if unknown. */
	public static Component speciesName(String entityId) {
		Species species = species(entityId);
		return species == null ? null : Component.translatable(species.langKey());
	}

	private static Species species(String entityId) {
		Identifier id = Identifier.tryParse(entityId);
		return id == null ? null : Species.byTarget(id, false);
	}

	/** Emeralds the clerk pays for a bucket of this species: 1 to 6 by the photo base points. */
	public static int emeraldValue(String entityId) {
		Species species = species(entityId);
		int points = species == null ? 10 : species.basePoints;
		return Math.max(1, Math.min(6, Math.round(points / 10.0f)));
	}

	public static void init() {
		// Right click a small fish with a water bucket to catch it.
		UseEntityCallback.EVENT.register((player, level, hand, entity, hit) -> {
			if (player.isSpectator()) {
				return InteractionResult.PASS;
			}
			ItemStack held = player.getItemInHand(hand);
			if (!held.is(Items.WATER_BUCKET) || !entity.isAlive()) {
				return InteractionResult.PASS;
			}
			Identifier id = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
			if (!isCatchable(id.toString())) {
				return InteractionResult.PASS;
			}
			if (level.isClientSide()) {
				return InteractionResult.SUCCESS;
			}
			ItemStack bucket = create(id.toString());
			entity.discard();
			level.playSound(null, entity.blockPosition(), SoundEvents.BUCKET_FILL_FISH, SoundSource.NEUTRAL, 1.0f, 1.0f);
			player.setItemInHand(hand, ItemUtils.createFilledResult(held, player, bucket, true));
			return InteractionResult.SUCCESS;
		});
	}

	/** Looks the entity type up without the default-entry fallback of the registry. */
	static EntityType<?> entityType(String entityId) {
		Identifier id = Identifier.tryParse(entityId);
		return id == null ? null : BuiltInRegistries.ENTITY_TYPE.getOptional(id).orElse(null);
	}
}
