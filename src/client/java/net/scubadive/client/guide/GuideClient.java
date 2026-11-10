package net.scubadive.client.guide;

import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionResult;

import net.scubadive.guide.DiveManual;

/** Opens the guide book screen when the dive manual is used. Client side only; the server just sees a plain item. */
public final class GuideClient {
	private GuideClient() {
	}

	public static void init() {
		UseItemCallback.EVENT.register((player, level, hand) -> {
			// The callback also runs on the integrated server thread: only react on the client.
			if (!level.isClientSide() || player.getItemInHand(hand).getItem() != DiveManual.DIVE_MANUAL) {
				return InteractionResult.PASS;
			}
			Minecraft.getInstance().gui.setScreen(new GuideBookScreen());
			return InteractionResult.SUCCESS;
		});
	}
}
