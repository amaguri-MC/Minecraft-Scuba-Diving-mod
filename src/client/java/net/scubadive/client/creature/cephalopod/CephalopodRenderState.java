package net.scubadive.client.creature.cephalopod;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

public class CephalopodRenderState extends LivingEntityRenderState {
	/** Per-entity animation offset so a school does not move in lockstep. */
	public float phase;
	public boolean jetting;
	/** Cuttlefish only: the black-and-white warning display. */
	public boolean display;
}
