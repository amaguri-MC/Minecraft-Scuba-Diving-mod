package net.scubadive.guide;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.network.Filterable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.WrittenBookItem;
import net.minecraft.world.item.component.WrittenBookContent;

import net.scubadive.ScubaDive;

/** In-game guide book. Pages are translation keys, so the book reads in the player's language. */
public final class DiveManual {
	public static final int PAGES = 14;
	public static Item DIVE_MANUAL;

	private DiveManual() {
	}

	public static void init() {
		List<Filterable<Component>> pages = new ArrayList<>();
		for (int i = 1; i <= PAGES; i++) {
			pages.add(Filterable.passThrough(Component.translatable("book.scubadive.manual.p" + i)));
		}
		WrittenBookContent content = new WrittenBookContent(Filterable.passThrough("Scuba Dive Guide"), "Scuba Dive", 0, pages, true);
		ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, ScubaDive.id("dive_manual"));
		DIVE_MANUAL = Registry.register(BuiltInRegistries.ITEM, key,
				new WrittenBookItem(new Item.Properties().setId(key).stacksTo(1).component(DataComponents.WRITTEN_BOOK_CONTENT, content)));
	}
}
