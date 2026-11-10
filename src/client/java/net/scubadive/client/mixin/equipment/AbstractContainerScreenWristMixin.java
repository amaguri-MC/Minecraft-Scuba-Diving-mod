package net.scubadive.client.mixin.equipment;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;

import net.scubadive.equipment.WristEquipSlot;

/** Draws the wrist slot frame (absent from the vanilla background) and an empty-slot tooltip. */
@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenWristMixin {
	@Shadow
	protected Slot hoveredSlot;

	@Inject(method = "extractSlot(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/world/inventory/Slot;II)V", at = @At("HEAD"))
	private void scubadive$wristFrame(GuiGraphicsExtractor graphics, Slot slot, int mouseX, int mouseY, CallbackInfo ci) {
		if (slot instanceof WristEquipSlot) {
			graphics.blitSprite(RenderPipelines.GUI_TEXTURED, WristEquipSlot.FRAME, slot.x - 1, slot.y - 1, 18, 18);
		}
	}

	@Inject(method = "extractTooltip(Lnet/minecraft/client/gui/GuiGraphicsExtractor;II)V", at = @At("TAIL"))
	private void scubadive$wristTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY, CallbackInfo ci) {
		AbstractContainerScreen<?> self = (AbstractContainerScreen<?>) (Object) this;
		if (this.hoveredSlot instanceof WristEquipSlot && !this.hoveredSlot.hasItem() && self.getMenu().getCarried().isEmpty()) {
			graphics.setTooltipForNextFrame(Component.translatable("container.scubadive.wrist_slot"), mouseX, mouseY);
		}
	}
}
