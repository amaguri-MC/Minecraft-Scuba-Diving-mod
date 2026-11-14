package net.scubadive.client.creature.vanilla;

import net.minecraft.client.renderer.entity.state.SquidRenderState;

/** Vanilla squid state (tentacle angle, body pitch and roll) plus a per-entity phase. */
public class VanillaSquidRenderState extends SquidRenderState {
	public float phase;
}
