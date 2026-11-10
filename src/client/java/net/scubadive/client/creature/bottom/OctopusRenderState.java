package net.scubadive.client.creature.bottom;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

/** Render state for the octopus: camouflage texture index and the jetting pose flag. */
public class OctopusRenderState extends LivingEntityRenderState {
	public int camo;
	public boolean jetting;
}
