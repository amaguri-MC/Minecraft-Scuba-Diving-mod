package net.scubadive.tour.center;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import net.scubadive.ScubaDive;
import net.scubadive.dive.ScubaDiveGear;
import net.scubadive.tour.DivePoint;
import net.scubadive.tour.TourApi;

/**
 * Server-side tour receptions, one per guide entity. Everything is validated here; the screen only sends
 * button presses. Receptions are kept in memory (they last 5 minutes) and refund every payment when they end
 * without a start.
 */
public final class DiveLobby {
	/** A reception cancels itself after 5 minutes. */
	public static final int EXPIRE_TICKS = 5 * 60 * 20;
	public static final int MAX_MEMBERS = 8;
	private static final double REACH_SQ = 10.0 * 10.0;

	private static final Map<UUID, Lobby> LOBBIES = new HashMap<>();
	/** Player -> guide whose reception the player is in. */
	private static final Map<UUID, UUID> PLAYER_LOBBY = new HashMap<>();

	private static final class Lobby {
		final UUID guide;
		final DivePoint point;
		final UUID host;
		final List<UUID> members = new ArrayList<>();
		final long expiresAt;
		int guideId;

		Lobby(UUID guide, int guideId, DivePoint point, UUID host, long expiresAt) {
			this.guide = guide;
			this.guideId = guideId;
			this.point = point;
			this.host = host;
			this.expiresAt = expiresAt;
		}
	}

	private DiveLobby() {
	}

	public static void init() {
		ServerPlayNetworking.registerGlobalReceiver(GuideActionPayload.TYPE, (payload, context) -> handle(context.player(), payload));
		net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.END_SERVER_TICK.register(DiveLobby::tick);
		net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents.DISCONNECT.register(
				(handler, server) -> onDisconnect(handler.getPlayer(), server));
		net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
			for (Lobby lobby : new ArrayList<>(LOBBIES.values())) {
				cancel(server, lobby, Component.translatable("message.scubadive.center.cancelled"), null);
			}
		});
	}

	// ---- entry points -------------------------------------------------------------------------------

	/** A player right clicked the guide: send the state and ask the client to open the screen. */
	public static void open(ServerPlayer player, DiveGuide guide) {
		Lobby lobby = LOBBIES.get(guide.getUUID());
		if (lobby != null) {
			lobby.guideId = guide.getId();
		}
		send(player, guide.getId(), lobby, true);
	}

	public static void onGuideDied(MinecraftServer server, UUID guideUuid) {
		Lobby lobby = LOBBIES.get(guideUuid);
		if (lobby != null) {
			cancel(server, lobby, Component.translatable("message.scubadive.center.guide_gone"), null);
		}
	}

	private static void tick(MinecraftServer server) {
		if (LOBBIES.isEmpty() || server.getTickCount() % 20 != 0) {
			return;
		}
		long now = server.getTickCount();
		for (Lobby lobby : new ArrayList<>(LOBBIES.values())) {
			if (now >= lobby.expiresAt) {
				cancel(server, lobby, Component.translatable("message.scubadive.center.expired"), null);
			}
		}
	}

	/** A logged-out member is removed and refunded; a logged-out host cancels the whole reception. */
	private static void onDisconnect(ServerPlayer player, MinecraftServer server) {
		UUID guide = PLAYER_LOBBY.get(player.getUUID());
		Lobby lobby = guide == null ? null : LOBBIES.get(guide);
		if (lobby == null) {
			PLAYER_LOBBY.remove(player.getUUID());
			return;
		}
		if (lobby.host.equals(player.getUUID())) {
			cancel(server, lobby, Component.translatable("message.scubadive.center.host_left"), player);
			return;
		}
		lobby.members.remove(player.getUUID());
		PLAYER_LOBBY.remove(player.getUUID());
		give(player, lobby.point.cost);
		broadcast(server, lobby);
	}

	// ---- actions ------------------------------------------------------------------------------------

	private static void handle(ServerPlayer player, GuideActionPayload payload) {
		Entity entity = player.level().getEntity(payload.guideId());
		if (!(entity instanceof DiveGuide guide) || !guide.isAlive() || player.distanceToSqr(entity) > REACH_SQ) {
			send(player, payload.guideId(), null, false, GuideStatePayload.MODE_CLOSE);
			return;
		}
		MinecraftServer server = player.level().getServer();
		Lobby lobby = LOBBIES.get(guide.getUUID());
		switch (payload.action()) {
			case GuideActionPayload.CREATE -> create(server, player, guide, lobby, payload.arg());
			case GuideActionPayload.JOIN -> join(server, player, guide, lobby);
			case GuideActionPayload.START -> start(server, player, guide, lobby);
			case GuideActionPayload.CANCEL -> {
				if (lobby != null && lobby.host.equals(player.getUUID())) {
					cancel(server, lobby, Component.translatable("message.scubadive.center.cancelled"), null);
				} else {
					send(player, guide.getId(), lobby, false);
				}
			}
			case GuideActionPayload.LEAVE -> leave(server, player, guide, lobby);
			default -> send(player, guide.getId(), lobby, false);
		}
	}

	private static void create(MinecraftServer server, ServerPlayer player, DiveGuide guide, Lobby lobby, String pointId) {
		DivePoint point = DivePoint.byId(pointId);
		if (lobby != null || point == null || !canEnter(player)) {
			send(player, guide.getId(), lobby, false);
			return;
		}
		if (!take(player, point.cost)) {
			player.sendSystemMessage(Component.translatable("message.scubadive.center.not_enough", point.cost));
			send(player, guide.getId(), null, false);
			return;
		}
		Lobby created = new Lobby(guide.getUUID(), guide.getId(), point, player.getUUID(), server.getTickCount() + EXPIRE_TICKS);
		created.members.add(player.getUUID());
		LOBBIES.put(guide.getUUID(), created);
		PLAYER_LOBBY.put(player.getUUID(), guide.getUUID());
		warnGear(player);
		broadcast(server, created);
	}

	private static void join(MinecraftServer server, ServerPlayer player, DiveGuide guide, Lobby lobby) {
		if (lobby == null || lobby.members.contains(player.getUUID()) || !canEnter(player)) {
			send(player, guide.getId(), lobby, false);
			return;
		}
		if (lobby.members.size() >= MAX_MEMBERS) {
			player.sendSystemMessage(Component.translatable("message.scubadive.center.full"));
			send(player, guide.getId(), lobby, false);
			return;
		}
		int cost = lobby.point.cost;
		if (!take(player, cost)) {
			player.sendSystemMessage(Component.translatable("message.scubadive.center.not_enough", cost));
			send(player, guide.getId(), lobby, false);
			return;
		}
		lobby.members.add(player.getUUID());
		PLAYER_LOBBY.put(player.getUUID(), guide.getUUID());
		warnGear(player);
		Component joined = Component.translatable("message.scubadive.center.joined", player.getName());
		for (UUID id : lobby.members) {
			ServerPlayer member = server.getPlayerList().getPlayer(id);
			if (member != null && member != player) {
				member.sendSystemMessage(joined);
			}
		}
		broadcast(server, lobby);
	}

	private static void leave(MinecraftServer server, ServerPlayer player, DiveGuide guide, Lobby lobby) {
		if (lobby == null || lobby.host.equals(player.getUUID()) || !lobby.members.remove(player.getUUID())) {
			send(player, guide.getId(), lobby, false);
			return;
		}
		PLAYER_LOBBY.remove(player.getUUID());
		give(player, lobby.point.cost);
		player.sendSystemMessage(Component.translatable("message.scubadive.center.refunded", lobby.point.cost));
		send(player, guide.getId(), lobby, false, GuideStatePayload.MODE_CLOSE);
		broadcast(server, lobby);
	}

	private static void start(MinecraftServer server, ServerPlayer player, DiveGuide guide, Lobby lobby) {
		if (lobby == null || !lobby.host.equals(player.getUUID())) {
			send(player, guide.getId(), lobby, false);
			return;
		}
		List<ServerPlayer> members = new ArrayList<>();
		for (UUID id : new ArrayList<>(lobby.members)) {
			ServerPlayer member = server.getPlayerList().getPlayer(id);
			if (member == null) {
				continue;
			}
			if (!gearReady(member)) {
				// Took gear off after paying: leaves the tour with a refund.
				if (member.getUUID().equals(lobby.host)) {
					send(player, guide.getId(), lobby, false);
					return;
				}
				lobby.members.remove(id);
				PLAYER_LOBBY.remove(id);
				give(member, lobby.point.cost);
				member.sendSystemMessage(Component.translatable("message.scubadive.center.refunded", lobby.point.cost));
				continue;
			}
			members.add(member);
		}
		boolean started;
		try {
			started = TourApi.start(player, lobby.point, members);
		} catch (RuntimeException e) {
			ScubaDive.LOGGER.error("Tour start failed", e);
			started = false;
		}
		if (started) {
			// Payments are kept; the tour owns the divers now.
			LOBBIES.remove(lobby.guide);
			for (UUID id : lobby.members) {
				PLAYER_LOBBY.remove(id);
				ServerPlayer member = server.getPlayerList().getPlayer(id);
				if (member != null) {
					send(member, lobby.guideId, lobby, false, GuideStatePayload.MODE_CLOSE);
				}
			}
		} else {
			cancel(server, lobby, Component.translatable("message.scubadive.center.start_failed"), null);
		}
	}

	/** Ends a reception and refunds everyone. {@code leaving} is a player who is just logging out (no packets to them). */
	private static void cancel(MinecraftServer server, Lobby lobby, Component reason, ServerPlayer leaving) {
		LOBBIES.remove(lobby.guide);
		for (UUID id : lobby.members) {
			PLAYER_LOBBY.remove(id);
			ServerPlayer member = leaving != null && leaving.getUUID().equals(id) ? leaving : server.getPlayerList().getPlayer(id);
			if (member == null) {
				continue;
			}
			give(member, lobby.point.cost);
			if (member != leaving) {
				member.sendSystemMessage(reason);
				member.sendSystemMessage(Component.translatable("message.scubadive.center.refunded", lobby.point.cost));
				send(member, lobby.guideId, lobby, false, GuideStatePayload.MODE_CLOSE);
			}
		}
	}

	// ---- helpers ------------------------------------------------------------------------------------

	private static boolean canEnter(ServerPlayer player) {
		if (TourApi.isInTour(player)) {
			player.sendSystemMessage(Component.translatable("message.scubadive.center.in_tour"));
			return false;
		}
		if (PLAYER_LOBBY.containsKey(player.getUUID())) {
			player.sendSystemMessage(Component.translatable("message.scubadive.center.already"));
			return false;
		}
		return gearReady(player);
	}

	/** Tours need the full kit; tells the player what is missing. */
	private static boolean gearReady(ServerPlayer player) {
		List<Component> missing = net.scubadive.tour.TourGear.missing(player);
		if (missing.isEmpty()) {
			return true;
		}
		player.sendSystemMessage(Component.translatable("message.scubadive.center.missing_gear",
				net.minecraft.network.chat.ComponentUtils.formatList(missing, Component.literal(", "))));
		return false;
	}

	private static void warnGear(ServerPlayer player) {
		boolean mask = ScubaDiveGear.isWearing(player, EquipmentSlot.HEAD, ScubaDiveGear.DIVE_MASK);
		boolean bcd = ScubaDiveGear.isWearing(player, EquipmentSlot.CHEST, ScubaDiveGear.BCD);
		if (!mask || !bcd) {
			player.sendSystemMessage(Component.translatable("message.scubadive.center.need_gear"));
		}
	}

	private static void broadcast(MinecraftServer server, Lobby lobby) {
		for (UUID id : lobby.members) {
			ServerPlayer member = server.getPlayerList().getPlayer(id);
			if (member != null) {
				send(member, lobby.guideId, lobby, false);
			}
		}
	}

	private static void send(ServerPlayer to, int guideId, Lobby lobby, boolean open) {
		send(to, guideId, lobby, open, -1);
	}

	/** modeOverride < 0 computes the mode from the lobby and the viewer. */
	private static void send(ServerPlayer to, int guideId, Lobby lobby, boolean open, int modeOverride) {
		int mode = GuideStatePayload.MODE_SELECT;
		String point = "";
		int seconds = 0;
		List<String> names = new ArrayList<>();
		if (lobby != null) {
			MinecraftServer server = to.level().getServer();
			point = lobby.point.id;
			seconds = (int) Math.max(0L, (lobby.expiresAt - server.getTickCount()) / 20L);
			for (UUID id : lobby.members) {
				ServerPlayer member = server.getPlayerList().getPlayer(id);
				names.add(member == null ? "?" : member.getName().getString());
			}
			if (lobby.host.equals(to.getUUID())) {
				mode = GuideStatePayload.MODE_HOST;
			} else if (lobby.members.contains(to.getUUID())) {
				mode = GuideStatePayload.MODE_MEMBER;
			} else {
				mode = GuideStatePayload.MODE_JOIN;
			}
		}
		if (modeOverride >= 0) {
			mode = modeOverride;
		}
		ServerPlayNetworking.send(to, new GuideStatePayload(guideId, open, mode, point, seconds, names));
	}

	private static int count(Player player) {
		int total = 0;
		Inventory inventory = player.getInventory();
		for (int i = 0; i < inventory.getContainerSize(); i++) {
			ItemStack stack = inventory.getItem(i);
			if (stack.is(Items.EMERALD)) {
				total += stack.getCount();
			}
		}
		return total;
	}

	private static boolean take(Player player, int amount) {
		if (count(player) < amount) {
			return false;
		}
		int left = amount;
		Inventory inventory = player.getInventory();
		for (int i = 0; i < inventory.getContainerSize() && left > 0; i++) {
			ItemStack stack = inventory.getItem(i);
			if (stack.is(Items.EMERALD)) {
				int used = Math.min(left, stack.getCount());
				stack.shrink(used);
				left -= used;
			}
		}
		inventory.setChanged();
		return true;
	}

	/** Returns emeralds to the inventory; whatever does not fit drops at the player's feet. */
	private static void give(Player player, int amount) {
		int left = amount;
		while (left > 0) {
			int part = Math.min(64, left);
			player.getInventory().placeItemBackInInventory(new ItemStack(Items.EMERALD, part));
			left -= part;
		}
	}
}
