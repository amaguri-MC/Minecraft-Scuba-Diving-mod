package net.scubadive.client.creature.penguin;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

public class PenguinRenderState extends LivingEntityRenderState {
	/** 0 = standing on land, 1 = swimming (body horizontal). */
	public float swimAmount;
	/** Per-entity time offset so a group does not shake heads in step. */
	public float phase;
}
