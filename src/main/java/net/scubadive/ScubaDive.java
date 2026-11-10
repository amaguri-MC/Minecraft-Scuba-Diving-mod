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
		net.scubadive.guide.DiveManual.init();
		net.scubadive.equipment.WristSlot.init();
		net.scubadive.creature.spawn.HotspotSpawner.init();
		net.scubadive.tour.TourManager.init();
		net.scubadive.tour.center.DiveCenter.init();

		// All items in the scubadive namespace are listed in this tab automatically.
		Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, TAB_KEY, FabricCreativeModeTab.builder()
				.title(Component.translatable("itemGroup.scubadive.main"))
				.icon(() -> {
					var camera = BuiltInRegistries.ITEM.getValue(id("camera"));
					return new ItemStack(camera == Items.AIR ? Items.TURTLE_HELMET : camera);
				})
				.displayItems((context, entries) -> BuiltInRegistries.ITEM.entrySet().stream()
						.filter(e -> e.getKey().identifier().getNamespace().equals(MOD_ID))
						.forEach(e -> entries.accept(e.getValue())))
				.build());
		LOGGER.info("ScubaDive loaded");
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
