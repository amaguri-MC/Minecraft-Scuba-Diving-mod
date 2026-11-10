package net.scubadive.tour.center;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

/** A bucket holding one small fish. Use it on water to release the fish (the bucket comes back empty). */
public class FishBucketItem extends Item {
	/** Name always follows the species inside, so shop trades also show "Clownfish in a bucket" etc. */
	@Override
	public net.minecraft.network.chat.Component getName(ItemStack stack) {
		net.minecraft.network.chat.Component species = FishBuckets.speciesName(FishBuckets.speciesOf(stack));
		return species == null ? super.getName(stack)
				: net.minecraft.network.chat.Component.translatable("item.scubadive.fish_bucket.of", species);
	}

	public FishBucketItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		String speciesId = FishBuckets.speciesOf(stack);
		if (!FishBuckets.isCatchable(speciesId)) {
			return InteractionResult.PASS;
		}
		BlockHitResult hit = getPlayerPOVHitResult(level, player, ClipContext.Fluid.SOURCE_ONLY);
		if (hit.getType() != HitResult.Type.BLOCK) {
			return InteractionResult.PASS;
		}
		BlockPos pos = hit.getBlockPos();
		if (!level.getFluidState(pos).is(FluidTags.WATER)) {
			pos = pos.relative(hit.getDirection());
			if (!level.getFluidState(pos).is(FluidTags.WATER)) {
				return InteractionResult.PASS;
			}
		}
		if (level instanceof ServerLevel serverLevel) {
			EntityType<?> type = FishBuckets.entityType(speciesId);
			if (type == null || type.spawn(serverLevel, pos, EntitySpawnReason.BUCKET) == null) {
				return InteractionResult.PASS;
			}
			level.playSound(null, pos, SoundEvents.BUCKET_EMPTY_FISH, SoundSource.NEUTRAL, 1.0f, 1.0f);
		}
		if (player.hasInfiniteMaterials()) {
			return InteractionResult.SUCCESS;
		}
		return InteractionResult.SUCCESS.heldItemTransformedTo(new ItemStack(Items.BUCKET));
	}
}
