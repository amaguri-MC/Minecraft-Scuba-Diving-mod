package net.scubadive.client.creature.fresh;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.scubadive.ScubaDive;
import net.scubadive.client.creature.bottom.AnemoneShrimpModel;
import net.scubadive.client.creature.bottom.BottomRenderer;
import net.scubadive.client.creature.reef.ReefFishModel;
import net.scubadive.client.creature.reef.ReefFishRenderer;
import net.scubadive.creature.fresh.FreshwaterCreatures;

/**
 * Client registration for the fresh-water creatures. They reuse the reef fish body shapes and the shrimp
 * model (layers registered by ReefFishClient / BottomDwellersClient) with their own textures and sizes.
 */
public final class FreshwaterCreaturesClient {
	private static final Identifier CRAYFISH_TEX = ScubaDive.id("textures/entity/crayfish.png");

	private FreshwaterCreaturesClient() {
	}

	public static void init() {
		// Body lengths in blocks: trout ~0.45, carp ~0.6, catfish ~0.7, cave fish ~0.15, crayfish ~0.25.
		fish(FreshwaterCreatures.RAINBOW_TROUT, ReefFishModel.SNAPPER, "rainbow_trout", 0.24f, 0.28f, 0.24f, 0.08f);
		fish(FreshwaterCreatures.CARP, ReefFishModel.PARROT, "carp", 0.48f, 0.14f, 0.12f, 0.12f);
		fish(FreshwaterCreatures.CATFISH, ReefFishModel.GROUPER, "catfish", 0.42f, 0.09f, 0.08f, 0.15f);
		fish(FreshwaterCreatures.BLIND_CAVEFISH, ReefFishModel.SLENDER, "blind_cavefish", 0.26f, 0.25f, 0.22f, 0.04f);
		EntityRenderers.register(FreshwaterCreatures.CRAYFISH, ctx ->
				new BottomRenderer<>(ctx, new AnemoneShrimpModel(ctx.bakeLayer(AnemoneShrimpModel.LAYER)), 0.08f, 0.38f, s -> CRAYFISH_TEX));
	}

	private static <T extends Mob> void fish(EntityType<T> type, ModelLayerLocation layer, String id, float scale,
			float tailSpeed, float flapSpeed, float shadow) {
		EntityRenderers.register(type, context -> new ReefFishRenderer<T>(context, layer, id, scale, tailSpeed, flapSpeed, shadow));
	}
}
