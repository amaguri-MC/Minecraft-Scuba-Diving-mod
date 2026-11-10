package net.scubadive.dive;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;

import net.scubadive.ScubaDive;

/**
 * Dive light item. While held underwater, a waterlogged minecraft:light block (level 15)
 * is placed in a water source block a few blocks ahead of the player. Every placed block is
 * tracked and restored to water when the player moves the light, switches items, leaves
 * the water, dies, disconnects, or the server stops.
 */
public final class ScubaDiveGear {
	public static Item DIVE_LIGHT;
	/** Dive computer: purely informational, its HUD is drawn client-side while it is held. */
	public static Item DIVE_COMPUTER;

	private static final int UPDATE_INTERVAL = 2;
	private static final int MAX_DISTANCE = 5;

	private record LightPos(ResourceKey<Level> dimension, BlockPos pos) {
	}

	private static final Map<UUID, LightPos> ACTIVE = new HashMap<>();
	private static int tickCounter;

	private ScubaDiveGear() {
	}

	public static void init() {
		ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, ScubaDive.id("dive_light"));
		DIVE_LIGHT = Registry.register(BuiltInRegistries.ITEM, key, new Item(new Item.Properties().setId(key).stacksTo(1)));
		ResourceKey<Item> dcKey = ResourceKey.create(Registries.ITEM, ScubaDive.id("dive_computer"));
		DIVE_COMPUTER = Registry.register(BuiltInRegistries.ITEM, dcKey, new Item(new Item.Properties().setId(dcKey).stacksTo(1)));

		ServerTickEvents.END_SERVER_TICK.register(ScubaDiveGear::onServerTick);
		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> removeLight(server, handler.getPlayer().getUUID()));
		ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
			for (UUID id : new java.util.ArrayList<>(ACTIVE.keySet())) {
				removeLight(server, id);
			}
		});
	}

	public static boolean isHoldingLight(net.minecraft.world.entity.player.Player player) {
		return player.getItemInHand(InteractionHand.MAIN_HAND).is(DIVE_LIGHT)
				|| player.getItemInHand(InteractionHand.OFF_HAND).is(DIVE_LIGHT);
	}

	public static boolean isHoldingDiveComputer(net.minecraft.world.entity.player.Player player) {
		return player.getItemInHand(InteractionHand.MAIN_HAND).is(DIVE_COMPUTER)
				|| player.getItemInHand(InteractionHand.OFF_HAND).is(DIVE_COMPUTER);
	}

	private static void onServerTick(MinecraftServer server) {
		tickCounter++;
		if (tickCounter % UPDATE_INTERVAL != 0) {
			return;
		}

		// Clean up entries whose player is gone or no longer eligible.
		Iterator<UUID> it = new java.util.ArrayList<>(ACTIVE.keySet()).iterator();
		while (it.hasNext()) {
			UUID id = it.next();
			ServerPlayer p = server.getPlayerList().getPlayer(id);
			if (p == null || !p.isAlive() || p.isRemoved() || !isHoldingLight(p) || !p.isEyeInFluid(FluidTags.WATER)) {
				removeLight(server, id);
			}
		}

		for (ServerPlayer player : server.getPlayerList().getPlayers()) {
			if (!player.isAlive() || !isHoldingLight(player) || !player.isEyeInFluid(FluidTags.WATER)) {
				continue;
			}
			if (!(player.level() instanceof ServerLevel level)) {
				continue;
			}

			BlockPos target = findTarget(level, player);
			LightPos current = ACTIVE.get(player.getUUID());
			if (target == null) {
				if (current != null) {
					removeLight(server, player.getUUID());
				}
				continue;
			}
			if (current != null && current.dimension().equals(level.dimension()) && current.pos().equals(target)) {
				continue;
			}
			if (current != null) {
				removeLight(server, player.getUUID());
			}
			if (isSourceWater(level, target) && !isTargetedByOther(player.getUUID(), level, target)) {
				BlockState light = Blocks.LIGHT.defaultBlockState()
						.setValue(BlockStateProperties.LEVEL, 15)
						.setValue(BlockStateProperties.WATERLOGGED, true);
				level.setBlock(target, light, Block.UPDATE_ALL);
				ACTIVE.put(player.getUUID(), new LightPos(level.dimension(), target));
			}
		}
	}

	private static boolean isTargetedByOther(UUID self, ServerLevel level, BlockPos pos) {
		for (Map.Entry<UUID, LightPos> e : ACTIVE.entrySet()) {
			if (!e.getKey().equals(self) && e.getValue().dimension().equals(level.dimension()) && e.getValue().pos().equals(pos)) {
				return true;
			}
		}
		return false;
	}

	/** Walks along the view ray and returns the farthest water source block before the ray leaves the water. */
	private static BlockPos findTarget(ServerLevel level, ServerPlayer player) {
		Vec3 eye = player.getEyePosition();
		Vec3 look = player.getLookAngle();
		BlockPos best = null;
		for (int d = 1; d <= MAX_DISTANCE; d++) {
			BlockPos p = BlockPos.containing(eye.x + look.x * d, eye.y + look.y * d, eye.z + look.z * d);
			if (!level.getFluidState(p).is(FluidTags.WATER)) {
				break;
			}
			if (isSourceWater(level, p)) {
				best = p;
			}
		}
		return best;
	}

	private static boolean isSourceWater(ServerLevel level, BlockPos pos) {
		BlockState state = level.getBlockState(pos);
		return state.is(Blocks.WATER) && level.getFluidState(pos).isSource();
	}

	/** Restores the tracked block to water, but only if it is still our light block. */
	private static void removeLight(MinecraftServer server, UUID id) {
		LightPos lp = ACTIVE.remove(id);
		if (lp == null) {
			return;
		}
		ServerLevel level = server.getLevel(lp.dimension());
		if (level == null) {
			return;
		}
		if (level.getBlockState(lp.pos()).is(Blocks.LIGHT)) {
			level.setBlock(lp.pos(), Blocks.WATER.defaultBlockState(), Block.UPDATE_ALL);
		}
	}
}
