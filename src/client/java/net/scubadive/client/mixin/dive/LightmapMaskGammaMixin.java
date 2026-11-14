package net.scubadive.client.mixin.dive;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.renderer.LightmapRenderStateExtractor;
import net.minecraft.client.renderer.state.LightmapRenderState;

import net.scubadive.client.dive.MaskGamma;

/** Raises the lightmap brightness (the gamma setting) while the dive mask is on under water. */
@Mixin(LightmapRenderStateExtractor.class)
public abstract class LightmapMaskGammaMixin {
	@Inject(method = "extract", at = @At("TAIL"))
	private void scubadive$maskGamma(LightmapRenderState state, float partialTick, CallbackInfo ci) {
		float boost = MaskGamma.boost();
		if (boost > state.brightness) {
			state.brightness = boost;
			state.needsUpdate = true;
		}
	}
}
