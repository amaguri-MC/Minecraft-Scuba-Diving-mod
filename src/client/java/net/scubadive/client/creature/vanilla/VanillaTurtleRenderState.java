package net.scubadive.client.creature.vanilla;

import net.minecraft.client.renderer.entity.state.TurtleRenderState;

/** Vanilla turtle state plus a per-entity phase so turtles do not paddle in step. */
public class VanillaTurtleRenderState extends TurtleRenderState {
	public float phase;
}
