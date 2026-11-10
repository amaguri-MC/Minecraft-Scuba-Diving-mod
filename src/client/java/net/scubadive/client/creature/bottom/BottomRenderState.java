package net.scubadive.client.creature.bottom;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

/** Shared render state for the bottom dwellers: colour variant and an "agitated" flag (eel gape). */
public class BottomRenderState extends LivingEntityRenderState {
	public int variant;
	public boolean agitated;
}
