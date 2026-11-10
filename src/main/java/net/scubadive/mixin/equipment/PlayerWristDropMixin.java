package net.scubadive.mixin.equipment;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.gamerules.GameRules;

import net.scubadive.equipment.WristSlot;

/** On death without keepInventory, the wrist slot contents drop like the rest of the inventory. */
@Mixin(Player.class)
public abstract class PlayerWristDropMixin {
	@Inject(method = "dropEquipment(Lnet/minecraft/server/level/ServerLevel;)V", at = @At("TAIL"))
	private void scubadive$dropWrist(ServerLevel level, CallbackInfo ci) {
		if (level.getGameRules().get(GameRules.KEEP_INVENTORY)) {
			return;
		}
		Player self = (Player) (Object) this;
		ItemStack stack = WristSlot.take(self);
		if (!stack.isEmpty()) {
			self.drop(stack, true, false);
		}
	}
}
