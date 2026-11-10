package net.scubadive.dive;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.minecraft.world.item.equipment.Equippable;
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
	/** Persistent per-player dive state, synced to the owning client only. */
	public static final AttachmentType<DiveData> DIVE = AttachmentRegistry.create(
			ScubaDive.id("dive"),
			builder -> builder
					.initializer(() -> DiveData.DEFAULT)
					.persistent(DiveData.CODEC)
					.copyOnDeath()
					.syncWith(DiveData.STREAM_CODEC, AttachmentSyncPredicate.targetOnly())
	);

	/** BCD air (0..1) shown on the vest, synced to every other player tracking this one (the owner uses its own DIVE data). Not persisted. */
	public static final AttachmentType<Float> BCD_AIR = AttachmentRegistry.create(
			ScubaDive.id("bcd_air"),
			builder -> builder
					.initializer(() -> 0.0f)
					.syncWith(net.minecraft.network.codec.ByteBufCodecs.FLOAT, AttachmentSyncPredicate.allButTarget())
	);

	/** Mask (head), BCD with built-in cylinder (chest), fins (feet). */
	public static Item DIVE_MASK;
	public static Item BCD;
	public static Item DIVE_FINS;
	/** Wetsuit (legs). */
	public static Item WETSUIT;
	public static Item DIVE_LIGHT;
	/** Dive computer: purely informational, its HUD is drawn client-side while it is held. */
	public static Item DIVE_COMPUTER;

	private static final int UPDATE_INTERVAL = 2;
	/** Update steps (of UPDATE_INTERVAL ticks) an old light stays after the new one is placed. */
	private static final int PENDING_TICKS = 3;

	private record LightPos(ResourceKey<Level> dimension, BlockPos pos) {
	}

	private static final class Pending {
		final LightPos pos;
		int ticksLeft;

		Pending(LightPos pos, int ticksLeft) {
			this.pos = pos;
			this.ticksLeft = ticksLeft;
		}
	}

	private static final Map<UUID, LightPos> ACTIVE = new HashMap<>();
	private static final Map<UUID, Pending> PENDING = new HashMap<>();
	private static int tickCounter;

	private ScubaDiveGear() {
	}

	public static void init() {
		ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, ScubaDive.id("dive_light"));
		DIVE_LIGHT = Registry.register(BuiltInRegistries.ITEM, key, new Item(new Item.Properties().setId(key).stacksTo(1)));
		ResourceKey<Item> dcKey = ResourceKey.create(Registries.ITEM, ScubaDive.id("dive_computer"));
		DIVE_COMPUTER = Registry.register(BuiltInRegistries.ITEM, dcKey, new Item(new Item.Properties().setId(dcKey).stacksTo(1)));

		DIVE_MASK = registerWearable("dive_mask", EquipmentSlot.HEAD, null);
		BCD = registerWearable("bcd", EquipmentSlot.CHEST, null);
		WETSUIT = registerWearable("wetsuit", EquipmentSlot.LEGS, null);
		DIVE_FINS = registerWearable("dive_fins", EquipmentSlot.FEET, ItemAttributeModifiers.builder()
				.add(Attributes.WATER_MOVEMENT_EFFICIENCY,
						new AttributeModifier(ScubaDive.id("dive_fins_water_movement"), 0.5, AttributeModifier.Operation.ADD_VALUE),
						EquipmentSlotGroup.FEET)
				.build());
		DiveServer.init();

		ServerTickEvents.END_SERVER_TICK.register(ScubaDiveGear::onServerTick);
		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> removeLight(server, handler.getPlayer().getUUID()));
		ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
			for (UUID id : new java.util.ArrayList<>(ACTIVE.keySet())) {
				removeLight(server, id);
			}
			for (UUID id : new java.util.ArrayList<>(PENDING.keySet())) {
				removeLight(server, id);
			}
		});
	}

	private static Item registerWearable(String name, EquipmentSlot slot, ItemAttributeModifiers attributes) {
		ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, ScubaDive.id(name));
		ResourceKey<EquipmentAsset> asset = ResourceKey.create(EquipmentAssets.ROOT_ID, ScubaDive.id(name));
		Item.Properties props = new Item.Properties().setId(key).stacksTo(1)
				.component(DataComponents.EQUIPPABLE, Equippable.builder(slot).setAsset(asset).build());
		if (attributes != null) {
			props.attributes(attributes);
		}
		return Registry.register(BuiltInRegistries.ITEM, key, new Item(props));
	}

	public static boolean isWearing(net.minecraft.world.entity.player.Player player, EquipmentSlot slot, Item item) {
		return player.getItemBySlot(slot).is(item);
	}

	/** True if the player can breathe underwater: mask + BCD and a non-empty tank. Used by the breath mixin; works on both sides. */
	public static boolean canBreathe(net.minecraft.world.entity.player.Player player) {
		return canBreathe(player, DiveApi.pressure(player));
	}

	static boolean canBreathe(net.minecraft.world.entity.player.Player player, float pressure) {
		return pressure > 0f && isWearing(player, EquipmentSlot.HEAD, DIVE_MASK) && DiveApi.hasTank(player);
	}

	public static boolean isHoldingLight(net.minecraft.world.entity.player.Player player) {
		return player.getItemInHand(InteractionHand.MAIN_HAND).is(DIVE_LIGHT)
				|| player.getItemInHand(InteractionHand.OFF_HAND).is(DIVE_LIGHT);
	}

	public static boolean isHoldingDiveComputer(net.minecraft.world.entity.player.Player player) {
		return player.getItemInHand(InteractionHand.MAIN_HAND).is(DIVE_COMPUTER)
				|| player.getItemInHand(InteractionHand.OFF_HAND).is(DIVE_COMPUTER);
	}

	/** True if the player wears mask, BCD, wetsuit and fins. Hostile mobs ignore such players unless provoked. */
	public static boolean hasFullSet(net.minecraft.world.entity.player.Player player) {
		return isWearing(player, EquipmentSlot.HEAD, DIVE_MASK)
				&& isWearing(player, EquipmentSlot.CHEST, BCD)
				&& isWearing(player, EquipmentSlot.LEGS, WETSUIT)
				&& isWearing(player, EquipmentSlot.FEET, DIVE_FINS);
	}

	private static void onServerTick(MinecraftServer server) {
		tickCounter++;
		if (tickCounter % UPDATE_INTERVAL != 0) {
			return;
		}

		// Clean up entries whose player is gone or no longer eligible.
		for (UUID id : new java.util.ArrayList<>(ACTIVE.keySet())) {
			ServerPlayer p = server.getPlayerList().getPlayer(id);
			if (p == null || !p.isAlive() || p.isRemoved() || !isHoldingLight(p) || !p.isEyeInFluid(FluidTags.WATER)) {
				removeLight(server, id);
			}
		}
		// Fade out old lights a few ticks after the new one is in place.
		for (UUID id : new java.util.ArrayList<>(PENDING.keySet())) {
			Pending pd = PENDING.get(id);
			if (--pd.ticksLeft <= 0) {
				PENDING.remove(id);
				if (!pd.pos.equals(ACTIVE.get(id))) {
					restore(server, pd.pos);
				}
			}
		}

		for (ServerPlayer player : server.getPlayerList().getPlayers()) {
			if (!player.isAlive() || !isHoldingLight(player) || !player.isEyeInFluid(FluidTags.WATER)) {
				continue;
			}
			if (!(player.level() instanceof ServerLevel level)) {
				continue;
			}

			// The light lives in the water block at the head; it only moves when the head crosses a block boundary.
			BlockPos target = BlockPos.containing(player.getEyePosition());
			LightPos current = ACTIVE.get(player.getUUID());
			if (current != null) {
				if (current.dimension().equals(level.dimension()) && level.getBlockState(current.pos()).is(Blocks.LIGHT)) {
					if (current.pos().equals(target)) {
						continue;
					}
				} else {
					ACTIVE.remove(player.getUUID());
					current = null;
				}
			}
			// No source water here: keep the current light instead of re-placing.
			if (!isSourceWater(level, target) || isTargetedByOther(player.getUUID(), level, target)) {
				continue;
			}
			BlockState light = Blocks.LIGHT.defaultBlockState()
					.setValue(BlockStateProperties.LEVEL, 15)
					.setValue(BlockStateProperties.WATERLOGGED, true);
			// Place the new light first; the old one stays a few ticks (at most 2 lights per player).
			level.setBlock(target, light, Block.UPDATE_ALL);
			ACTIVE.put(player.getUUID(), new LightPos(level.dimension(), target));
			if (current != null) {
				Pending older = PENDING.put(player.getUUID(), new Pending(current, PENDING_TICKS));
				if (older != null && !older.pos.equals(ACTIVE.get(player.getUUID()))) {
					restore(server, older.pos);
				}
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

	private static boolean isSourceWater(ServerLevel level, BlockPos pos) {
		BlockState state = level.getBlockState(pos);
		return state.is(Blocks.WATER) && level.getFluidState(pos).isSource();
	}

	/** Restores the tracked light (and a pending old one) to water. */
	private static void removeLight(MinecraftServer server, UUID id) {
		LightPos lp = ACTIVE.remove(id);
		if (lp != null) {
			restore(server, lp);
		}
		Pending pd = PENDING.remove(id);
		if (pd != null) {
			restore(server, pd.pos);
		}
	}

	/** Restores the block to water, but only if it is still our light block. */
	private static void restore(MinecraftServer server, LightPos lp) {
		ServerLevel level = server.getLevel(lp.dimension());
		if (level == null) {
			return;
		}
		if (level.getBlockState(lp.pos()).is(Blocks.LIGHT)) {
			level.setBlock(lp.pos(), Blocks.WATER.defaultBlockState(), Block.UPDATE_ALL);
		}
	}
}
