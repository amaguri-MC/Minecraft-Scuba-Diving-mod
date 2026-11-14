package net.scubadive.client.creature.vanilla;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

/** Vanilla cod state plus a per-entity phase so a school does not beat in step. */
public class VanillaCodRenderState extends LivingEntityRenderState {
	public float phase;
}
