package net.scubadive.tour.center;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/** The dive guide: right click opens the tour reception screen. The reception state lives in {@link DiveLobby}. */
public class DiveGuide extends PathfinderMob {
	public DiveGuide(EntityType<? extends DiveGuide> type, Level level) {
		super(type, level);
		this.setPersistenceRequired();
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(0, new FloatGoal(this));
		this.goalSelector.addGoal(1, new LookAtPlayerGoal(this, Player.class, 8.0f));
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
	public InteractionResult mobInteract(Player player, InteractionHand hand) {
		if (!this.isAlive()) {
			return InteractionResult.PASS;
		}
		if (player instanceof ServerPlayer serverPlayer) {
			DiveLobby.open(serverPlayer, this);
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	public void die(DamageSource source) {
		super.die(source);
		if (this.level() instanceof ServerLevel level) {
			DiveLobby.onGuideDied(level.getServer(), this.getUUID());
		}
	}
}
