package net.scubadive.tour.center;

import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;

import net.scubadive.camera.ScubaCamera;
import net.scubadive.dive.ScubaDiveGear;

/** The dive shop clerk: buys fish and fish buckets for emeralds, sells dive gear. Stands still. */
public class DiveClerk extends AbstractVillager {
	/** Gear stock is refilled this often (ticks). */
	private static final int RESTOCK_INTERVAL = 6000;

	public DiveClerk(EntityType<? extends DiveClerk> type, Level level) {
		super(type, level);
		this.setPersistenceRequired();
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(0, new FloatGoal(this));
		this.goalSelector.addGoal(1, new LookAtPlayerGoal(this, Player.class, 8.0f));
	}

	@Override
	public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob partner) {
		return null;
	}

	@Override
	public boolean isPushable() {
		return false;
	}

	@Override
	public boolean removeWhenFarAway(double distanceSqr) {
		return false;
	}

	@Override
	public boolean showProgressBar() {
		return false;
	}

	@Override
	protected void rewardTradeXp(MerchantOffer offer) {
	}

	@Override
	public InteractionResult mobInteract(Player player, InteractionHand hand) {
		if (!this.isAlive()) {
			return InteractionResult.PASS;
		}
		if (!this.level().isClientSide()) {
			Player trader = this.getTradingPlayer();
			if (trader != null && trader != player) {
				return InteractionResult.CONSUME;
			}
			if (this.getOffers().isEmpty()) {
				return InteractionResult.CONSUME;
			}
			this.setTradingPlayer(player);
			this.openTradingScreen(player, this.getDisplayName(), 1);
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	public void aiStep() {
		super.aiStep();
		if (!this.level().isClientSide() && this.offers != null && this.tickCount % RESTOCK_INTERVAL == 0) {
			for (MerchantOffer offer : this.offers) {
				offer.resetUses();
			}
		}
	}

	@Override
	protected void updateTrades(ServerLevel level) {
		if (this.offers == null) {
			this.offers = new MerchantOffers();
		}
		MerchantOffers list = this.offers;
		// Buying from the player (stock is effectively unlimited).
		buy(list, new ItemCost(Items.COD, 6));
		buy(list, new ItemCost(Items.SALMON, 5));
		buy(list, new ItemCost(Items.TROPICAL_FISH, 3));
		buy(list, new ItemCost(Items.PUFFERFISH, 3));
		for (String id : FishBuckets.SPECIES) {
			ItemCost cost = new ItemCost(DiveCenter.FISH_BUCKET).withComponents(
					b -> b.expect(DataComponents.CUSTOM_DATA, FishBuckets.data(id)));
			list.add(new MerchantOffer(cost, new ItemStack(Items.EMERALD, FishBuckets.emeraldValue(id)), 9999, 0, 0.0f));
		}
		// Selling dive gear (limited stock, refilled over time).
		sell(list, ScubaDiveGear.DIVE_MASK, 3, 4);
		sell(list, ScubaDiveGear.BCD, 8, 3);
		sell(list, ScubaDiveGear.WETSUIT, 6, 3);
		sell(list, ScubaDiveGear.DIVE_FINS, 4, 4);
		sell(list, ScubaDiveGear.DIVE_LIGHT, 2, 6);
		sell(list, ScubaDiveGear.DIVE_COMPUTER, 5, 3);
		sell(list, ScubaCamera.CAMERA, 7, 3);
	}

	private static void buy(MerchantOffers list, ItemCost cost) {
		list.add(new MerchantOffer(cost, new ItemStack(Items.EMERALD), 9999, 0, 0.0f));
	}

	private static void sell(MerchantOffers list, Item gear, int emeralds, int stock) {
		list.add(new MerchantOffer(new ItemCost(Items.EMERALD, emeralds), new ItemStack(gear), stock, 0, 0.0f));
	}
}
