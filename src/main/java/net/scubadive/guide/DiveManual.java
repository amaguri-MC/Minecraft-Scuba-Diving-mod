package net.scubadive.guide;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;

import net.scubadive.ScubaDive;

/**
 * In-game guide book. A plain item: the client opens its own illustrated book screen on use
 * (net.scubadive.client.guide.GuideClient, via Fabric's UseItemCallback), so nothing happens server-side.
 */
public final class DiveManual {
	public static Item DIVE_MANUAL;

	private DiveManual() {
	}

	public static void init() {
		ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, ScubaDive.id("dive_manual"));
		DIVE_MANUAL = Registry.register(BuiltInRegistries.ITEM, key, new Item(new Item.Properties().setId(key).stacksTo(1)));
	}
}
