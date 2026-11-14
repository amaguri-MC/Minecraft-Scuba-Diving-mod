package net.scubadive.gametest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mojang.datafixers.util.Pair;
import org.lwjgl.glfw.GLFW;

import net.minecraft.client.CameraType;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.levelgen.structure.Structure;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;

import net.scubadive.client.guide.GuideBookScreen;
import net.scubadive.tour.center.DiveCenterStructure;

/**
 * End-to-end smoke test run in CI on a real client (software rendering):
 * a normal-preset world, terrain generation cost, /locate of the dive center, held light, fins, the guide book
 * and a stay under water. Everything is logged with the prefix SCUBA_TEST and screenshots go to run/screenshots.
 */
public class ScubaClientGameTest implements FabricClientGameTest {
	private static void log(String fmt, Object... args) {
		System.out.println("SCUBA_TEST " + String.format(fmt, args));
	}

	@Override
	public void runTest(ClientGameTestContext context) {
		try (TestSingleplayerContext sp = context.worldBuilder()
				.setUseConsistentSettings(false)
				.adjustSettings(c -> {
					c.setSeed("20261111");
					c.setGameMode(WorldCreationUiState.SelectedGameMode.CREATIVE);
					c.setAllowCommands(true);
				})
				.create()) {
			var server = sp.getServer();
			sp.getConnection().waitForChunksRender();
			server.runCommand("gamerule advance_time false");
			server.runCommand("time set noon");
			server.runCommand("gamerule advance_weather false");
			server.runCommand("weather clear");
			server.runCommand("gamerule send_command_feedback false");
			context.runOnClient(mc -> mc.options.chatVisibility().set(net.minecraft.world.entity.player.ChatVisiblity.HIDDEN));
			context.takeScreenshot("00_spawn");

			terrainCost(context, sp);
			BlockPos center = locate(context, sp);
			gear(context, sp, center);
			book(context);
			showcase(context, sp);
			journal(context);
			zoo(context, sp);
			underwater(context, sp);
			tour(context, sp);
		}
	}

	/** Teleports across fresh terrain and measures chunk loading time and the dive center search cost. */
	private static void terrainCost(ClientGameTestContext context, TestSingleplayerContext sp) {
		var server = sp.getServer();
		long calls0 = DiveCenterStructure.SEARCH_CALLS.get();
		long nanos0 = DiveCenterStructure.SEARCH_NANOS.get();
		long samples0 = DiveCenterStructure.HEIGHT_SAMPLES.get();
		long totalWait = 0;
		for (int i = 1; i <= 5; i++) {
			int x = i * 600;
			server.runCommand("tp @p " + x + " 120 " + (i % 2 == 0 ? 300 : -300));
			long t = System.nanoTime();
			context.waitTicks(5);
			sp.getConnection().waitForChunksRender(false, 20 * 120);
			long ms = (System.nanoTime() - t) / 1_000_000;
			totalWait += ms;
			long avgTick = server.computeOnServer(s -> s.getAverageTickTimeNanos()) / 1000;
			long maxTick = server.computeOnServer(s -> maxTick(s)) / 1000;
			log("terrain hop %d: chunks ready in %d ms, server avg tick %d us, max tick %d us, fps %d",
					i, ms, avgTick, maxTick, context.computeOnClient(mc -> mc.getFps()));
		}
		long calls = DiveCenterStructure.SEARCH_CALLS.get() - calls0;
		long nanos = DiveCenterStructure.SEARCH_NANOS.get() - nanos0;
		long samples = DiveCenterStructure.HEIGHT_SAMPLES.get() - samples0;
		log("terrain total wait %d ms; dive center searches %d, total %d ms, avg %d us/search, %d height samples (%d/search)",
				totalWait, calls, nanos / 1_000_000, calls == 0 ? 0 : nanos / calls / 1000, samples, calls == 0 ? 0 : samples / calls);
		context.takeScreenshot("01_terrain");
	}

	private static long maxTick(MinecraftServer s) {
		long max = 0;
		for (long t : s.getTickTimesNanos()) {
			max = Math.max(max, t);
		}
		return max;
	}

	/** Same search as /locate structure scubadive:dive_center, timed. */
	private static BlockPos locate(ClientGameTestContext context, TestSingleplayerContext sp) {
		var server = sp.getServer();
		long calls0 = DiveCenterStructure.SEARCH_CALLS.get();
		long nanos0 = DiveCenterStructure.SEARCH_NANOS.get();
		long t = System.nanoTime();
		BlockPos found = server.computeOnServer(s -> {
			ServerLevel level = s.overworld();
			Holder<Structure> holder = s.registryAccess().lookupOrThrow(Registries.STRUCTURE)
					.getOrThrow(ResourceKey.create(Registries.STRUCTURE, Identifier.fromNamespaceAndPath("scubadive", "dive_center")));
			Pair<BlockPos, Holder<Structure>> r = level.getChunkSource().getGenerator()
					.findNearestMapStructure(level, HolderSet.direct(holder), BlockPos.ZERO, 100, false);
			return r == null ? null : r.getFirst();
		});
		long ms = (System.nanoTime() - t) / 1_000_000;
		long n = DiveCenterStructure.SEARCH_CALLS.get() - calls0;
		log("locate dive_center from 0,0: %s in %d ms (%d searches, %d ms inside the site search)", found, ms, n,
				(DiveCenterStructure.SEARCH_NANOS.get() - nanos0) / 1_000_000);
		if (found == null) {
			throw new AssertionError("dive center not found within 100 chunks");
		}
		if (ms > 60_000) {
			throw new AssertionError("locate took " + ms + " ms");
		}
		server.runCommand("tp @p " + found.getX() + " 90 " + found.getZ() + " 0 30");
		long w = System.nanoTime();
		context.waitTicks(5);
		sp.getConnection().waitForChunksRender(false, 20 * 120);
		log("dive center chunks ready in %d ms", (System.nanoTime() - w) / 1_000_000);
		context.waitTicks(20);
		context.takeScreenshot("02_dive_center_above");
		// the hut from four sides (roof shape check): find the roof by its spruce stairs near the located spot
		BlockPos roof = server.computeOnServer(s -> {
			ServerLevel level = s.overworld();
			long sx = 0, sy = 0, sz = 0, cnt = 0;
			BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos();
			for (int x = found.getX() - 64; x <= found.getX() + 64; x++) {
				for (int z = found.getZ() - 64; z <= found.getZ() + 64; z++) {
					for (int y = 60; y <= 90; y++) {
						if (level.getBlockState(m.set(x, y, z)).is(net.minecraft.world.level.block.Blocks.SPRUCE_STAIRS)) {
							sx += x; sy += y; sz += z; cnt++;
						}
					}
				}
			}
			return cnt == 0 ? null : new BlockPos((int) (sx / cnt), (int) (sy / cnt), (int) (sz / cnt));
		});
		log("dive center roof at %s", roof);
		if (roof != null) {
			int[][] sides = {{0, -12, 0}, {12, 0, 90}, {0, 12, 180}, {-12, 0, 270}};
			for (int[] d : sides) {
				server.runCommand("tp @p " + (roof.getX() + d[0]) + " " + (roof.getY() + 3) + " " + (roof.getZ() + d[1]) + " " + d[2] + " 12");
				context.waitTicks(30);
				context.takeScreenshot("02b_dive_center_yaw" + d[2]);
			}
		}
		return found;
	}

	/** Held light in first and third person, full kit with gray fins. */
	private static void gear(ClientGameTestContext context, TestSingleplayerContext sp, BlockPos center) {
		var server = sp.getServer();
		int top = server.computeOnServer(s -> s.overworld().getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING,
				center.getX(), center.getZ()));
		server.runCommand("tp @p " + center.getX() + " " + top + " " + center.getZ());
		server.runCommand("item replace entity @p armor.head with scubadive:dive_mask");
		server.runCommand("item replace entity @p armor.chest with scubadive:bcd");
		server.runCommand("item replace entity @p armor.legs with scubadive:wetsuit");
		server.runCommand("item replace entity @p armor.feet with scubadive:dive_fins");
		server.runCommand("item replace entity @p weapon.mainhand with scubadive:dive_light");
		server.runCommand("item replace entity @p weapon.offhand with scubadive:dive_light");
		sp.getConnection().waitForChunksRender(false, 20 * 60);
		context.waitTicks(10);
		context.getInput().lookAt(0, 10);
		context.waitTicks(5);
		context.takeScreenshot("03_light_first_person");
		context.runOnClient(mc -> mc.options.setCameraType(CameraType.THIRD_PERSON_FRONT));
		context.waitTicks(5);
		context.takeScreenshot("04_kit_third_person_front");
		context.runOnClient(mc -> mc.options.setCameraType(CameraType.THIRD_PERSON_BACK));
		context.getInput().lookAt(90, 10);
		context.waitTicks(5);
		context.takeScreenshot("05_kit_third_person_side");
		context.runOnClient(mc -> mc.options.setCameraType(CameraType.FIRST_PERSON));
		server.runCommand("item replace entity @p weapon.offhand with minecraft:air");
	}

	/** Opens the guide book and turns every page (this crashed before). */
	private static void book(ClientGameTestContext context) {
		context.setScreen(GuideBookScreen::new);
		context.waitForScreen(GuideBookScreen.class);
		context.getInput().pressKey(GLFW.GLFW_KEY_HOME);
		int spreads = GuideBookScreen.spreadCount();
		long t = System.nanoTime();
		for (int i = 0; i < spreads; i++) {
			context.waitTicks(3);
			if (i < 4 || i % 5 == 0 || i == spreads - 1) {
				context.takeScreenshot(String.format("06_book_%02d", i));
			}
			context.getInput().pressKey(GLFW.GLFW_KEY_RIGHT);
		}
		log("guide book: %d spreads turned in %d ms, fps %d", spreads, (System.nanoTime() - t) / 1_000_000,
				context.computeOnClient(mc -> mc.getFps()));
		context.setScreen(() -> null);
	}

	/** On a platform in the sky: full kit with gray fins, brain coral colours and the barracuda, close up. */
	private static void showcase(ClientGameTestContext context, TestSingleplayerContext sp) {
		var server = sp.getServer();
		int bx = 40, by = 200, bz = 40;
		server.runCommand("forceload add " + (bx - 8) + " " + (bz - 8) + " " + (bx + 8) + " " + (bz + 12));
		context.waitTicks(40);
		server.runCommand("tp @p " + bx + ".5 " + (by + 2) + " " + bz + ".5 0 15");
		context.waitTicks(10);
		sp.getConnection().waitForChunksRender(false, 20 * 60);
		server.runCommand("fill " + (bx - 6) + " " + (by - 1) + " " + (bz - 6) + " " + (bx + 6) + " " + (by - 1) + " " + (bz + 10) + " minecraft:smooth_stone");
		for (int i = 0; i < 9; i++) {
			int x = bx - 4 + i;
			server.runCommand("setblock " + x + " " + by + " " + (bz + 6) + " scubadive:brain_coral");
			server.runCommand("setblock " + x + " " + (by + 1) + " " + (bz + 6 + (i % 2)) + " scubadive:brain_coral");
		}
		server.runCommand("item replace entity @p weapon.mainhand with scubadive:dive_light");
		server.runCommand("tp @p " + bx + ".5 " + by + " " + bz + ".5 0 15");
		context.runOnClient(mc -> mc.options.setCameraType(CameraType.THIRD_PERSON_FRONT));
		context.getInput().lookAt(180, 25);
		context.waitTicks(40);
		context.takeScreenshot("09_kit_on_platform_front");
		context.runOnClient(mc -> mc.options.setCameraType(CameraType.FIRST_PERSON));
		server.runCommand("item replace entity @p weapon.mainhand with minecraft:air");
		server.runCommand("summon scubadive:barracuda " + (bx - 1) + ".5 " + (by + 1) + " " + (bz + 3) + ".5 {NoAI:1b,NoGravity:1b,Rotation:[90f,0f]}");
		server.runCommand("summon scubadive:barracuda " + (bx + 2) + ".5 " + (by + 1) + " " + (bz + 3) + ".5 {NoAI:1b,NoGravity:1b,Rotation:[-60f,0f]}");
		context.getInput().lookAt(0, 20);
		context.waitTicks(40);
		context.takeScreenshot("10_showcase_coral_barracuda");
		context.runOnClient(mc -> {
			var state = BuiltInRegistries.BLOCK.getValue(Identifier.fromNamespaceAndPath("scubadive", "brain_coral")).defaultBlockState();
			var model = mc.getModelManager().getBlockStateModelSet().get(state);
			List<net.minecraft.client.renderer.block.dispatch.BlockStateModelPart> parts = new ArrayList<>();
			model.collectParts(net.minecraft.util.RandomSource.create(1), parts);
			for (var part : parts) {
				for (var quad : part.getQuads(net.minecraft.core.Direction.NORTH)) {
					log("brain coral north quad: %s", quad.materialInfo());
				}
			}
		});
		server.runCommand("tp @p " + (bx - 1) + ".5 " + (by + 1) + " " + (bz + 1) + ".5");
		context.getInput().lookAt(0, 25);
		context.waitTicks(40);
		context.takeScreenshot("11_barracuda_closeup");
		server.runCommand("kill @e[type=scubadive:barracuda]");
		server.runCommand("tp @p " + (bx + 0.5) + " " + by + " " + (bz + 4) + ".2");
		context.getInput().lookAt(0, 0);
		context.waitTicks(40);
		context.takeScreenshot("12_coral_macro");
		context.runOnClient(mc -> {
			var state = BuiltInRegistries.BLOCK.getValue(Identifier.fromNamespaceAndPath("scubadive", "brain_coral")).defaultBlockState();
			var sources = mc.getBlockColors().getTintSources(state);
			BlockPos p = new BlockPos(bx, by, bz + 6);
			StringBuilder b = new StringBuilder();
			for (var src : sources) {
				b.append(String.format(" %06X", src.colorInWorld(state, mc.level, p) & 0xFFFFFF));
			}
			log("brain coral tints at %s: %d sources:%s", p, sources.size(), b);
		});
		server.runCommand("item replace entity @p weapon.mainhand with scubadive:dive_light");
	}

	/** Journal: every category tab, with unknown species showing their hints. */
	private static void journal(ClientGameTestContext context) {
		context.setScreen(net.scubadive.client.camera.JournalScreen::new);
		context.waitForScreen(net.scubadive.client.camera.JournalScreen.class);
		context.waitTicks(5);
		context.takeScreenshot("13_journal_all");
		for (net.scubadive.species.Species.Category c : net.scubadive.species.Species.Category.values()) {
			context.runOnClient(mc -> ((net.scubadive.client.camera.JournalScreen) mc.gui.screen()).selectCategory(c));
			context.waitTicks(3);
			context.takeScreenshot("14_journal_" + c.name().toLowerCase(java.util.Locale.ROOT));
		}
		context.setScreen(() -> null);
	}

	/** New species of update-10: one close-up each. */
	private static final java.util.Set<String> CLOSE_UP = java.util.Set.of(
			"yellow_tang", "cleaner_wrasse", "mandarinfish", "napoleon_wrasse", "garden_eel",
			"sardine", "red_sea_bream", "striped_beakfish", "ocean_sunfish", "arctic_char",
			"box_jellyfish", "comb_jelly", "lions_mane_jelly", "fried_egg_jelly", "echizen_jelly",
			"gentoo_penguin", "rockhopper_penguin", "cape_penguin", "nudibranch", "sea_bunny", "spanish_dancer", "blue_dragon", "leaf_sheep", "phyllidia", "thecacera",
			"blue_nudibranch", "bornella", "ceratosoma", "indonesian_sea_nettle", "upside_down_jelly", "cannonball_jelly",
			"flower_hat_jelly", "blue_blubber_jelly");

	/**
	 * Creature gallery on a sky platform (in air, frozen, so nothing blurs them): every category as a lineup, then a
	 * close-up of each new species.
	 */
	private static void zoo(ClientGameTestContext context, TestSingleplayerContext sp) {
		var server = sp.getServer();
		int x0 = 200, y0 = 150, z0 = 200;
		String area = "x=" + x0 + ",y=" + y0 + ",z=" + z0 + ",distance=..40";
		server.runCommand("forceload add " + (x0 - 20) + " " + (z0 - 20) + " " + (x0 + 20) + " " + (z0 + 30));
		context.waitTicks(40);
		server.runCommand("fill " + (x0 - 12) + " " + (y0 - 1) + " " + (z0 - 8) + " " + (x0 + 12) + " " + (y0 - 1) + " " + (z0 + 12) + " minecraft:smooth_stone");
		server.runCommand("tp @p " + x0 + ".5 " + y0 + " " + (z0 - 6) + ".5 0 12");
		context.waitTicks(10);
		sp.getConnection().waitForChunksRender(false, 20 * 60);
		server.runCommand("item replace entity @p weapon.mainhand with minecraft:air");
		server.runCommand("item replace entity @p armor.head with minecraft:air");
		for (net.scubadive.species.Species.Category c : net.scubadive.species.Species.Category.values()) {
			clearGallery(server, x0, y0, z0);
			List<net.scubadive.species.Species> list = new ArrayList<>();
			for (net.scubadive.species.Species s : net.scubadive.species.Species.values()) {
				if (s.category == c && !s.isBlock) list.add(s);
			}
			for (int i = 0; i < list.size(); i++) {
				net.scubadive.species.Species s = list.get(i);
				double x = x0 - 4.5 + (i % 4) * 3 + 0.5;
				double y = y0 + 0.2 + (i / 4) * 1.6;
				double z = z0 + 1.5 + (s.lengthCm > 150 ? 4 : 0);
				summonFrozen(server, s.target.toString(), x, y, z, 90f);
			}
			context.waitTicks(30);
			context.takeScreenshot("15_zoo_" + c.name().toLowerCase(java.util.Locale.ROOT));
			log("zoo %s: %d species", c, list.size());
		}
		for (net.scubadive.species.Species s : net.scubadive.species.Species.values()) {
			if (!CLOSE_UP.contains(s.id)) continue;
			clearGallery(server, x0, y0, z0);
			double back = s.lengthCm >= 150 ? 6 : (s.lengthCm >= 40 ? 2.5 : 1.2);
			summonFrozen(server, s.target.toString(), x0 + 0.5, y0 + 1.2, z0 - 6 + 0.5 + back, 60f);
			context.waitTicks(25);
			context.takeScreenshot("16_close_" + s.id);
		}
		clearGallery(server, x0, y0, z0);
		// penguins in a water box in front of the camera: streamlined swimming pose
		server.runCommand("fill " + (x0 - 3) + " " + y0 + " " + (z0 - 4) + " " + (x0 + 3) + " " + (y0 + 3) + " " + (z0 + 1) + " minecraft:water");
		summonFrozen(server, "scubadive:gentoo_penguin", x0 - 1.2, y0 + 1.2, z0 - 3.0, 90f);
		summonFrozen(server, "scubadive:rockhopper_penguin", x0 + 1.4, y0 + 0.6, z0 - 2.5, 60f);
		summonFrozen(server, "scubadive:cape_penguin", x0 + 0.3, y0 + 2.0, z0 - 1.5, 120f);
		server.runCommand("tp @p " + x0 + ".5 " + (y0 + 1) + " " + (z0 - 7) + ".5 0 5");
		context.waitTicks(40);
		context.takeScreenshot("19_penguins_swim_pose");
		clearGallery(server, x0, y0, z0);
		// renewed vanilla creatures in the same water box
		server.runCommand("fill " + (x0 - 6) + " " + y0 + " " + (z0 - 5) + " " + (x0 + 6) + " " + (y0 + 5) + " " + (z0 + 4) + " minecraft:water");
		summonFrozen(server, "minecraft:turtle", x0 - 3.5, y0 + 1.0, z0 - 1.0, 90f);
		summonFrozen(server, "minecraft:dolphin", x0 + 3.0, y0 + 3.2, z0 + 1.0, 90f);
		summonFrozen(server, "minecraft:nautilus", x0 + 2.5, y0 + 0.8, z0 - 2.5, 60f);
		summonFrozen(server, "minecraft:squid", x0 - 2.0, y0 + 3.4, z0 - 2.0, 90f);
		server.runCommand(String.format(java.util.Locale.ROOT, "summon minecraft:pufferfish %.1f %.1f %.1f {NoAI:1b,NoGravity:1b,Invulnerable:1b,PuffState:0}", x0 - 0.5, y0 + 1.5, z0 - 3.5));
		server.runCommand(String.format(java.util.Locale.ROOT, "summon minecraft:pufferfish %.1f %.1f %.1f {NoAI:1b,NoGravity:1b,Invulnerable:1b,PuffState:1}", x0 + 0.5, y0 + 1.5, z0 - 3.5));
		server.runCommand(String.format(java.util.Locale.ROOT, "summon minecraft:pufferfish %.1f %.1f %.1f {NoAI:1b,NoGravity:1b,Invulnerable:1b,PuffState:2}", x0 + 1.6, y0 + 1.5, z0 - 3.5));
		server.runCommand("tp @p " + x0 + ".5 " + (y0 + 2) + " " + (z0 - 7) + ".5 0 10");
		context.waitTicks(40);
		context.takeScreenshot("20_vanilla_renewal");
		server.runCommand("tp @p " + x0 + ".5 " + (y0 + 1) + " " + (z0 - 5) + ".5 0 10");
		context.waitTicks(20);
		context.takeScreenshot("20b_vanilla_renewal_close");
		// nautilus with saddle and armor, ridden, seen from the side
		clearGallery(server, x0, y0, z0);
		server.runCommand(String.format(java.util.Locale.ROOT, "summon minecraft:nautilus %.1f %.1f %.1f {NoAI:1b,Invulnerable:1b,Tame:1b,Rotation:[90f,0f]}", x0 + 0.5, y0 + 1.5, z0 - 1.5));
		server.runCommand("item replace entity @e[type=minecraft:nautilus,limit=1] saddle with minecraft:saddle");
		server.runCommand("item replace entity @e[type=minecraft:nautilus,limit=1] armor.body with minecraft:golden_nautilus_armor");
		server.runCommand("ride @p mount @e[type=minecraft:nautilus,limit=1]");
		context.runOnClient(mc -> mc.options.setCameraType(CameraType.THIRD_PERSON_FRONT));
		context.getInput().lookAt(90, 10);
		context.waitTicks(40);
		context.takeScreenshot("21_nautilus_ridden");
		context.runOnClient(mc -> mc.options.setCameraType(CameraType.FIRST_PERSON));
		server.runCommand("ride @p dismount");
		clearGallery(server, x0, y0, z0);
	}

	/** A real tour at the warm-sea dive point: 40 s in the tour dimension with every creature ticking. */
	private static void tour(ClientGameTestContext context, TestSingleplayerContext sp) {
		var server = sp.getServer();
		server.runCommand("gamemode survival @p");
		server.runCommand("item replace entity @p armor.head with scubadive:dive_mask");
		server.runCommand("item replace entity @p armor.chest with scubadive:bcd");
		server.runCommand("item replace entity @p armor.legs with scubadive:wetsuit");
		server.runCommand("item replace entity @p armor.feet with scubadive:dive_fins");
		boolean started = server.computeOnServer(s -> {
			var player = s.getPlayerList().getPlayers().get(0);
			return net.scubadive.tour.TourApi.start(player, net.scubadive.tour.DivePoint.WARM_SEA, List.of(player));
		});
		log("tour started: %s", started);
		for (int i = 1; i <= 4; i++) {
			context.waitTicks(200);
			long avg = server.computeOnServer(s -> s.getAverageTickTimeNanos()) / 1000;
			int count = server.computeOnServer(s -> {
				var level = s.getLevel(net.scubadive.tour.TourApi.DIMENSION);
				int n = 0;
				if (level != null) for (Entity e : level.getAllEntities()) n++;
				return n;
			});
			String dim = context.computeOnClient(mc -> mc.level == null ? "none" : mc.level.dimension().identifier().toString());
			log("tour %ds: dimension %s, server avg tick %d us, entities in tour %d", i * 10, dim, avg, count);
			if (i == 2) {
				server.runCommand("execute as @p at @s run tp @s ~ ~-6 ~");
			}
		}
		context.takeScreenshot("17_tour");
	}

	/** Removes the gallery creatures without death particles. */
	private static void clearGallery(net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext server, int x0, int y0, int z0) {
		server.runOnServer(s -> {
			var box = new net.minecraft.world.phys.AABB(x0 - 40, y0 - 20, z0 - 40, x0 + 40, y0 + 40, z0 + 40);
			for (Entity e : s.overworld().getEntitiesOfClass(Entity.class, box, e -> !(e instanceof net.minecraft.world.entity.player.Player))) {
				e.discard();
			}
		});
	}

	private static void summonFrozen(net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext server, String id,
			double x, double y, double z, float yaw) {
		server.runCommand(String.format(java.util.Locale.ROOT,
				"summon %s %.2f %.2f %.2f {NoAI:1b,NoGravity:1b,Invulnerable:1b,Silent:1b,PersistenceRequired:1b,Rotation:[%.1ff,0f]}", id, x, y, z, yaw));
	}

	/** Stays in the ocean for a while: server tick time, client fps and how many mod creatures are around. */
	private static void underwater(ClientGameTestContext context, TestSingleplayerContext sp) {
		var server = sp.getServer();
		BlockPos ocean = server.computeOnServer(s -> {
			ServerLevel level = s.overworld();
			var found = level.findClosestBiome3d(h -> h.unwrapKey().map(k -> k.identifier().getPath().contains("warm_ocean")
							|| k.identifier().getPath().equals("lukewarm_ocean")).orElse(false),
					BlockPos.ZERO, 6400, 32, 64);
			return found == null ? null : found.getFirst();
		});
		log("warm ocean at %s", ocean);
		if (ocean == null) {
			return;
		}
		server.runCommand("tp @p " + ocean.getX() + " 55 " + ocean.getZ());
		sp.getConnection().waitForChunksRender(false, 20 * 120);
		server.runCommand("gamemode survival @p");
		server.runCommand("effect give @p minecraft:water_breathing 600 0 true");
		server.runCommand("effect give @p minecraft:resistance 600 4 true");
		context.getInput().lookAt(0, 45);
		context.waitTicks(40);
		context.takeScreenshot("07_underwater_light");
		// mask gamma: same view without and with the dive mask
		server.runCommand("item replace entity @p weapon.mainhand with minecraft:air");
		server.runCommand("item replace entity @p armor.head with minecraft:air");
		context.waitTicks(10);
		context.takeScreenshot("07a_no_mask");
		server.runCommand("item replace entity @p armor.head with scubadive:dive_mask");
		context.waitTicks(10);
		context.takeScreenshot("07b_with_mask");
		// penguins in the water: they must take the streamlined swimming pose
		server.runCommand("execute as @p at @s run summon scubadive:gentoo_penguin ^-1 ^ ^3 {NoAI:1b,Invulnerable:1b,Rotation:[90f,0f]}");
		server.runCommand("execute as @p at @s run summon scubadive:rockhopper_penguin ^1 ^ ^3 {NoAI:1b,Invulnerable:1b,Rotation:[90f,0f]}");
		server.runCommand("execute as @p at @s run summon scubadive:cape_penguin ^ ^1 ^4 {NoAI:1b,Invulnerable:1b,Rotation:[90f,0f]}");
		context.getInput().lookAt(0, 10);
		server.runCommand("effect give @p minecraft:night_vision 600 0 true");
		context.waitTicks(30);
		context.takeScreenshot("18_penguins_in_water");
		for (int phase = 1; phase <= 3; phase++) {
			context.waitTicks(200);
			long avg = server.computeOnServer(s -> s.getAverageTickTimeNanos()) / 1000;
			long max = server.computeOnServer(s -> maxTick(s)) / 1000;
			Map<String, Integer> counts = server.computeOnServer(s -> {
				Map<String, Integer> m = new HashMap<>();
				for (Entity e : s.overworld().getAllEntities()) {
					Identifier id = BuiltInRegistries.ENTITY_TYPE.getKey(e.getType());
					if ("scubadive".equals(id.getNamespace())) {
						m.merge(id.getPath(), 1, Integer::sum);
					}
				}
				return m;
			});
			int total = counts.values().stream().mapToInt(Integer::intValue).sum();
			List<String> top = new ArrayList<>();
			counts.entrySet().stream().sorted((a, b) -> b.getValue() - a.getValue()).limit(8)
					.forEach(e -> top.add(e.getKey() + "=" + e.getValue()));
			log("underwater %ds: server avg tick %d us, max %d us, fps %d, mod entities %d %s",
					phase * 10, avg, max, context.computeOnClient(mc -> mc.getFps()), total, top);
		}
		context.takeScreenshot("08_underwater_after_30s");
	}
}
