package net.scubadive.tour.center;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityType;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

import net.scubadive.ScubaDive;

/** Beach dive centers, shop clerk, guide and lobby, fish buckets. Owner: dive center module (H1). */
public final class DiveCenter {
	public static final ResourceKey<EntityType<?>> CLERK_KEY = ResourceKey.create(Registries.ENTITY_TYPE, ScubaDive.id("dive_shop_clerk"));
	public static final ResourceKey<EntityType<?>> GUIDE_KEY = ResourceKey.create(Registries.ENTITY_TYPE, ScubaDive.id("dive_guide"));

	public static final EntityType<DiveClerk> CLERK = FabricEntityType.Builder.createMob(DiveClerk::new, MobCategory.MISC,
					mob -> mob.defaultAttributes(DiveCenter::npcAttributes))
			.sized(0.6f, 1.95f)
			.clientTrackingRange(10)
			.build(CLERK_KEY);

	public static final EntityType<DiveGuide> GUIDE = FabricEntityType.Builder.createMob(DiveGuide::new, MobCategory.MISC,
					mob -> mob.defaultAttributes(DiveCenter::npcAttributes))
			.sized(0.6f, 1.95f)
			.clientTrackingRange(10)
			.build(GUIDE_KEY);

	public static final Feature<NoneFeatureConfiguration> FEATURE = new DiveCenterFeature(NoneFeatureConfiguration.CODEC);

	/** The bucket item; set in {@link #init()}. */
	public static Item FISH_BUCKET;

	private DiveCenter() {
	}

	private static AttributeSupplier.Builder npcAttributes() {
		// Standing NPCs: they never walk, so the movement speed is zero.
		return Mob.createMobAttributes()
				.add(Attributes.MAX_HEALTH, 20.0)
				.add(Attributes.MOVEMENT_SPEED, 0.0);
	}

	public static void init() {
		ResourceKey<Item> bucketKey = ResourceKey.create(Registries.ITEM, ScubaDive.id("fish_bucket"));
		FISH_BUCKET = Registry.register(BuiltInRegistries.ITEM, bucketKey, new FishBucketItem(new Item.Properties().setId(bucketKey).stacksTo(1)));
		FishBuckets.init();

		Registry.register(BuiltInRegistries.ENTITY_TYPE, CLERK_KEY, CLERK);
		Registry.register(BuiltInRegistries.ENTITY_TYPE, GUIDE_KEY, GUIDE);

		PayloadTypeRegistry.clientboundPlay().register(GuideStatePayload.TYPE, GuideStatePayload.CODEC);
		PayloadTypeRegistry.serverboundPlay().register(GuideActionPayload.TYPE, GuideActionPayload.CODEC);
		DiveLobby.init();

		// Beach huts are a structure (worldgen/structure + structure_set dive_center); the feature stays for /place only.
		Registry.register(BuiltInRegistries.FEATURE, ScubaDive.id("dive_center"), FEATURE);
		DiveCenterStructure.register();
	}
}
