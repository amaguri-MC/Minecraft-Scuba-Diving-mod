package net.scubadive.client.creature.reef;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

/** Garden eel render state: how far it is pulled into the sand and a per-eel sway phase. */
public class GardenEelRenderState extends LivingEntityRenderState {
	public float retract;
	public float phase;
}
