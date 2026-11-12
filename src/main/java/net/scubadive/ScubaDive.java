package net.scubadive;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScubaDive implements ModInitializer {
	public static final String MOD_ID = "scubadive";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static final ResourceKey<CreativeModeTab> TAB_KEY = ResourceKey.create(Registries.CREATIVE_MODE_TAB, id("main"));

	@Override
	public void onInitialize() {
		net.scubadive.creature.ScubaCreatures.init();
		net.scubadive.world.ScubaWorldgen.init();
		net.scubadive.dive.ScubaDiveGear.init();
		net.scubadive.camera.ScubaCamera.init();
		net.scubadive.reef.ScubaReefs.init();
		net.scubadive.silt.SiltClouds.init();
		net.scubadive.creature.big.BigFish.init();
		net.scubadive.creature.reef.ReefFish.init();
		net.scubadive.creature.bottom.BottomDwellers.init();
		net.scubadive.creature.critter.Critters.init();
		net.scubadive.creature.cold.ColdCreatures.init();
		net.scubadive.creature.fresh.FreshwaterCreatures.init();
		net.scubadive.creature.temperate.TemperateFish.init();
		net.scubadive.creature.cephalopod.Cephalopods.init();
		net.scubadive.guide.DiveManual.init();
		net.scubadive.equipment.WristSlot.init();
		net.scubadive.creature.spawn.HotspotSpawner.init();
		net.scubadive.tour.TourManager.init();
		net.scubadive.tour.center.DiveCenter.init();

		registerCreativeTabs();
		LOGGER.info("ScubaDive loaded");
	}

	/** Creative inventory tabs: gear and blocks, then spawn eggs grouped like the journal (small groups merged). */
	private enum CreativeGroup {
		GEAR("gear", "camera"),
		FISH("fish", "clownfish_spawn_egg"),
		BOTTOM("bottom", "octopus_spawn_egg"),
		NUDIBRANCH("nudibranch", "nudibranch_spawn_egg"),
		JELLY("jelly", "moon_jelly_spawn_egg"),
		COOL_FRESH("cool_fresh", "rainbow_trout_spawn_egg");

		final String key;
		final String icon;

		CreativeGroup(String key, String icon) {
			this.key = key;
			this.icon = icon;
		}

		static CreativeGroup of(Identifier item) {
			String path = item.getPath();
			if (!path.endsWith("_spawn_egg")) {
				return GEAR;
			}
			net.scubadive.species.Species s = net.scubadive.species.Species.byId(path.substring(0, path.length() - "_spawn_egg".length()));
			if (s == null) {
				return FISH;
			}
			return switch (s.category) {
				case REEF_FISH, BIG -> FISH;
				case BOTTOM, CEPHALOPOD, INVERT -> BOTTOM;
				case NUDIBRANCH -> NUDIBRANCH;
				case JELLY -> JELLY;
				case COLD, FRESH -> COOL_FRESH;
			};
		}
	}

	private static void registerCreativeTabs() {
		for (CreativeGroup group : CreativeGroup.values()) {
			ResourceKey<CreativeModeTab> key = group == CreativeGroup.GEAR ? TAB_KEY
					: ResourceKey.create(Registries.CREATIVE_MODE_TAB, id(group.key));
			Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, key, FabricCreativeModeTab.builder()
					.title(Component.translatable("itemGroup.scubadive." + (group == CreativeGroup.GEAR ? "main" : group.key)))
					.icon(() -> {
						var item = BuiltInRegistries.ITEM.getValue(id(group.icon));
						return new ItemStack(item == Items.AIR ? Items.TURTLE_HELMET : item);
					})
					.displayItems((context, entries) -> BuiltInRegistries.ITEM.entrySet().stream()
							.filter(e -> e.getKey().identifier().getNamespace().equals(MOD_ID)
									&& CreativeGroup.of(e.getKey().identifier()) == group)
							.forEach(e -> entries.accept(e.getValue())))
					.build());
		}
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
