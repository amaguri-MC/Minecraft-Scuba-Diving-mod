package net.scubadive.client.guide;

import java.util.List;

import net.minecraft.resources.Identifier;

import net.scubadive.ScubaDive;

/** Contents of the guide book. Each page has a heading, an illustration and body text (translation keys). */
final class GuidePages {
	private GuidePages() {
	}

	/** What is drawn between the heading and the body text. */
	sealed interface Art permits None, Picture, Items, Creatures, Toc, Sites {
	}

	record None() implements Art {
	}

	/** A 140x56 illustration in textures/gui/guide/, with optional captions spread evenly below it. */
	record Picture(Identifier texture, int captions) implements Art {
	}

	/** A row of item icons. */
	record Items(List<String> ids) implements Art {
	}

	/** Slowly turning 3D creatures with their names below. */
	record Creatures(List<String> ids) implements Art {
	}

	/** The contents list (chapters, clickable). */
	record Toc() implements Art {
	}

	/** The dive site list built from {@link net.scubadive.tour.DivePoint}. */
	record Sites() implements Art {
	}

	/**
	 * @param id      key part: guide.scubadive.page.&lt;id&gt;.title / .body / .c1..
	 * @param chapter whether the page starts a chapter (listed in the contents)
	 */
	record Page(String id, boolean chapter, Art art) {
		String titleKey() {
			return "guide.scubadive.page." + id + ".title";
		}

		String bodyKey() {
			return "guide.scubadive.page." + id + ".body";
		}

		String captionKey(int i) {
			return "guide.scubadive.page." + id + ".c" + (i + 1);
		}
	}

	static Identifier tex(String name) {
		return ScubaDive.id("textures/gui/guide/" + name + ".png");
	}

	private static Picture pic(String name) {
		return new Picture(tex(name), 0);
	}

	private static Picture pic(String name, int captions) {
		return new Picture(tex(name), captions);
	}

	/** Index of the contents page (the bookmark jumps to its spread). */
	static final int TOC_PAGE = 1;

	static final List<Page> PAGES = List.of(
			new Page("intro", true, pic("cover")),
			new Page("toc", false, new Toc()),
			new Page("gear", true, new Items(List.of("scubadive:dive_mask", "scubadive:bcd", "scubadive:wetsuit", "scubadive:dive_fins"))),
			new Page("bcd", true, pic("bcd", 3)),
			new Page("tank", true, pic("gauge")),
			new Page("ascent", true, pic("ascent", 2)),
			new Page("computer", true, new Items(List.of("scubadive:dive_computer"))),
			new Page("light", true, new Items(List.of("scubadive:dive_light"))),
			new Page("camera", true, pic("camera_keys")),
			new Page("score", false, pic("photo_score")),
			new Page("hazards", true, new Creatures(List.of("scubadive:moray_eel", "scubadive:sea_nettle", "scubadive:crown_of_thorns"))),
			new Page("guide", true, new Creatures(List.of("scubadive:clownfish", "scubadive:nudibranch", "minecraft:turtle"))),
			new Page("guide2", false, new Creatures(List.of("scubadive:manta_ray", "scubadive:octopus", "scubadive:pygmy_seahorse"))),
			new Page("center", true, pic("dive_center")),
			new Page("tour", true, pic("tour_flow", 5)),
			new Page("tour_rules", false, new None()),
			new Page("sites", true, new Sites()),
			new Page("commands", true, new None()));

	/** Item shown next to each dive site, in {@link net.scubadive.tour.DivePoint} order. */
	static final List<String> SITE_ICONS = List.of(
			"minecraft:brain_coral", "minecraft:packed_ice", "minecraft:lily_pad",
			"minecraft:chest", "minecraft:prismarine", "minecraft:hanging_roots");
}
