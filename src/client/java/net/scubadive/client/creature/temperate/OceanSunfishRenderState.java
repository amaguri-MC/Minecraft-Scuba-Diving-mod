package net.scubadive.client.creature.temperate;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

public class OceanSunfishRenderState extends LivingEntityRenderState {
	/** 0 upright .. 1 lying on its side (basking or stranded). */
	public float roll;
	/** Per-entity animation offset so two sunfish do not scull in step. */
	public float phase;
}
