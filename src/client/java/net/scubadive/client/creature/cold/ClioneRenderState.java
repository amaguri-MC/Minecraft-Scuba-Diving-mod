package net.scubadive.client.creature.cold;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

public class ClioneRenderState extends LivingEntityRenderState {
	/** Per-entity animation offset so a group does not flap in lockstep (matches the server stroke timing). */
	public float phase;
}
