package net.scubadive.client.creature.critter;

import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
import net.minecraft.client.renderer.entity.EntityRenderers;

import net.scubadive.ScubaDive;
import net.scubadive.creature.critter.Critters;

/** Client registration for sea stars, the urchin and jellyfish. */
public final class CrittersClient {
	private CrittersClient() {
	}

	public static void init() {
		ModelLayerRegistry.registerModelLayer(StarfishModel.BLUE, StarfishModel::blue);
		ModelLayerRegistry.registerModelLayer(StarfishModel.CUSHION, StarfishModel::cushion);
		ModelLayerRegistry.registerModelLayer(StarfishModel.CROWN, StarfishModel::crown);
		ModelLayerRegistry.registerModelLayer(UrchinModel.LAYER, UrchinModel::createBodyLayer);
		ModelLayerRegistry.registerModelLayer(JellyfishModel.MOON, JellyfishModel::moon);
		ModelLayerRegistry.registerModelLayer(JellyfishModel.NETTLE, JellyfishModel::nettle);
		ModelLayerRegistry.registerModelLayer(JellyfishModel.SPOTTED, JellyfishModel::spotted);
		ModelLayerRegistry.registerModelLayer(BoxJellyModel.LAYER, BoxJellyModel::createBodyLayer);
		ModelLayerRegistry.registerModelLayer(CombJellyModel.LAYER, CombJellyModel::createBodyLayer);
		ModelLayerRegistry.registerModelLayer(LionsManeModel.LAYER, LionsManeModel::createBodyLayer);
		ModelLayerRegistry.registerModelLayer(FriedEggJellyModel.LAYER, FriedEggJellyModel::createBodyLayer);
		ModelLayerRegistry.registerModelLayer(EchizenJellyModel.LAYER, EchizenJellyModel::createBodyLayer);
		ModelLayerRegistry.registerModelLayer(IndonesianSeaNettleModel.LAYER, IndonesianSeaNettleModel::createBodyLayer);
		ModelLayerRegistry.registerModelLayer(UpsideDownJellyModel.LAYER, UpsideDownJellyModel::createBodyLayer);
		ModelLayerRegistry.registerModelLayer(CannonballJellyModel.LAYER, CannonballJellyModel::createBodyLayer);
		ModelLayerRegistry.registerModelLayer(FlowerHatJellyModel.LAYER, FlowerHatJellyModel::createBodyLayer);
		ModelLayerRegistry.registerModelLayer(BlueBlubberModel.LAYER, BlueBlubberModel::createBodyLayer);

		EntityRenderers.register(Critters.BLUE_STARFISH, ctx -> new CritterRenderer<>(ctx,
				new StarfishModel(ctx.bakeLayer(StarfishModel.BLUE), 5), 0.1f, 0.4f, ScubaDive.id("textures/entity/blue_starfish.png")));
		EntityRenderers.register(Critters.CUSHION_STAR, ctx -> new CritterRenderer<>(ctx,
				new StarfishModel(ctx.bakeLayer(StarfishModel.CUSHION), 5), 0.1f, 0.4f, ScubaDive.id("textures/entity/cushion_star.png")));
		EntityRenderers.register(Critters.CROWN_OF_THORNS, ctx -> new CritterRenderer<>(ctx,
				new StarfishModel(ctx.bakeLayer(StarfishModel.CROWN), 13), 0.12f, 0.5f, ScubaDive.id("textures/entity/crown_of_thorns.png")));
		EntityRenderers.register(Critters.SEA_URCHIN, ctx -> new CritterRenderer<>(ctx,
				new UrchinModel(ctx.bakeLayer(UrchinModel.LAYER)), 0.1f, 0.6f, ScubaDive.id("textures/entity/sea_urchin.png")));
		EntityRenderers.register(Critters.MOON_JELLY, ctx -> new CritterRenderer<>(ctx,
				new JellyfishModel(ctx.bakeLayer(JellyfishModel.MOON), 16), 0.0f, 0.5f, ScubaDive.id("textures/entity/moon_jelly.png")));
		EntityRenderers.register(Critters.SEA_NETTLE, ctx -> new CritterRenderer<>(ctx,
				new JellyfishModel(ctx.bakeLayer(JellyfishModel.NETTLE), 12), 0.0f, 0.5f, ScubaDive.id("textures/entity/sea_nettle.png")));
		EntityRenderers.register(Critters.SPOTTED_JELLY, ctx -> new CritterRenderer<>(ctx,
				new JellyfishModel(ctx.bakeLayer(JellyfishModel.SPOTTED), 0), 0.0f, 0.6f, ScubaDive.id("textures/entity/spotted_jelly.png")));
		EntityRenderers.register(Critters.BOX_JELLYFISH, ctx -> new CritterRenderer<>(ctx,
				new BoxJellyModel(ctx.bakeLayer(BoxJellyModel.LAYER)), 0.0f, 0.45f, ScubaDive.id("textures/entity/box_jellyfish.png")));
		EntityRenderers.register(Critters.COMB_JELLY, ctx -> new GlowCritterRenderer<>(ctx,
				new CombJellyModel(ctx.bakeLayer(CombJellyModel.LAYER)), 0.0f, 0.6f, ScubaDive.id("textures/entity/comb_jelly.png"),
				ScubaDive.id("textures/entity/comb_jelly_glow.png")));
		EntityRenderers.register(Critters.LIONS_MANE_JELLY, ctx -> new CritterRenderer<>(ctx,
				new LionsManeModel(ctx.bakeLayer(LionsManeModel.LAYER)), 0.0f, 1.1f, ScubaDive.id("textures/entity/lions_mane_jelly.png")));
		EntityRenderers.register(Critters.FRIED_EGG_JELLY, ctx -> new CritterRenderer<>(ctx,
				new FriedEggJellyModel(ctx.bakeLayer(FriedEggJellyModel.LAYER)), 0.0f, 0.6f, ScubaDive.id("textures/entity/fried_egg_jelly.png")));
		EntityRenderers.register(Critters.ECHIZEN_JELLY, ctx -> new CritterRenderer<>(ctx,
				new EchizenJellyModel(ctx.bakeLayer(EchizenJellyModel.LAYER)), 0.0f, 2.0f, ScubaDive.id("textures/entity/echizen_jelly.png")));
		EntityRenderers.register(Critters.INDONESIAN_SEA_NETTLE, ctx -> new CritterRenderer<>(ctx,
				new IndonesianSeaNettleModel(ctx.bakeLayer(IndonesianSeaNettleModel.LAYER)), 0.0f, 0.55f,
				ScubaDive.id("textures/entity/indonesian_sea_nettle.png")));
		EntityRenderers.register(Critters.UPSIDE_DOWN_JELLY, ctx -> new CritterRenderer<>(ctx,
				new UpsideDownJellyModel(ctx.bakeLayer(UpsideDownJellyModel.LAYER)), 0.2f, 0.6f,
				ScubaDive.id("textures/entity/upside_down_jelly.png")));
		EntityRenderers.register(Critters.CANNONBALL_JELLY, ctx -> new CritterRenderer<>(ctx,
				new CannonballJellyModel(ctx.bakeLayer(CannonballJellyModel.LAYER)), 0.0f, 0.6f,
				ScubaDive.id("textures/entity/cannonball_jelly.png")));
		EntityRenderers.register(Critters.FLOWER_HAT_JELLY, ctx -> new SoftGlowRenderer<>(ctx,
				new FlowerHatJellyModel(ctx.bakeLayer(FlowerHatJellyModel.LAYER)), 0.0f, 0.5f,
				ScubaDive.id("textures/entity/flower_hat_jelly.png"), ScubaDive.id("textures/entity/flower_hat_jelly_glow.png")));
		EntityRenderers.register(Critters.BLUE_BLUBBER_JELLY, ctx -> new CritterRenderer<>(ctx,
				new BlueBlubberModel(ctx.bakeLayer(BlueBlubberModel.LAYER)), 0.0f, 0.6f,
				ScubaDive.id("textures/entity/blue_blubber_jelly.png")));
	}
}
