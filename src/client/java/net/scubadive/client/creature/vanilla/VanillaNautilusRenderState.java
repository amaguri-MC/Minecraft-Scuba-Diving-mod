package net.scubadive.client.creature.vanilla;

import net.minecraft.client.renderer.entity.state.NautilusRenderState;
import net.minecraft.resources.Identifier;

/** Vanilla nautilus state plus what the renewed model and its gear layer need. */
public class VanillaNautilusRenderState extends NautilusRenderState {
	public float phase;
	public boolean dashing;
	public boolean hasSaddle;
	/** Armour texture for the worn nautilus armour, or null without armour. */
	public Identifier armorTexture;
	/** Vertical shift (blocks) that puts the saddle seat at the rider's attachment point. */
	public float seatLift;
	/** Extra size so the saddle seat meets the rider (1 = the modelled size). */
	public float gearScale = 1.0f;
}
