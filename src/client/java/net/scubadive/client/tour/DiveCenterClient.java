package net.scubadive.client.tour;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.resources.Identifier;

import net.scubadive.ScubaDive;
import net.scubadive.tour.center.DiveCenter;
import net.scubadive.tour.center.DiveClerk;
import net.scubadive.tour.center.DiveGuide;
import net.scubadive.tour.center.GuideStatePayload;

/** Client side of dive centers (NPC renderers, guide screen). Owner: dive center module (H1). */
public final class DiveCenterClient {
	/** Humanoid body with the 64x64 player skin layout. */
	public static final ModelLayerLocation NPC_LAYER = new ModelLayerLocation(ScubaDive.id("dive_npc"), "main");

	private static final Identifier CLERK_TEXTURE = ScubaDive.id("textures/entity/dive_shop_clerk.png");
	private static final Identifier GUIDE_TEXTURE = ScubaDive.id("textures/entity/dive_guide.png");

	private DiveCenterClient() {
	}

	public static void init() {
		ModelLayerRegistry.registerModelLayer(NPC_LAYER,
				() -> LayerDefinition.create(HumanoidModel.createMesh(CubeDeformation.NONE, 0.0f), 64, 64));
		EntityRenderers.register(DiveCenter.CLERK, context -> new DiveCenterNpcRenderer<DiveClerk>(context, CLERK_TEXTURE));
		EntityRenderers.register(DiveCenter.GUIDE, context -> new DiveCenterNpcRenderer<DiveGuide>(context, GUIDE_TEXTURE));
		ClientPlayNetworking.registerGlobalReceiver(GuideStatePayload.TYPE,
				(payload, context) -> DiveCenterGuideScreen.onState(context.client(), payload));
	}
}
