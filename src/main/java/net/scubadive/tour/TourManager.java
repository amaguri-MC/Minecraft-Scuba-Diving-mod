package net.scubadive.tour;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.storage.LevelData;

import net.scubadive.ScubaDive;
import net.scubadive.species.Species;

/** Tour sessions: loading, boat, diving, results, return. Owner: tour session module (H2). */
public final class TourManager {
	/** Distance between the origins of two sessions along x. */
	static final int SLOT_SPACING = 1024;
	private static final int MAX_SLOTS = 16;

	/** Where the player came from; persisted so a diver is never stranded in the tour dimension. */
	static final AttachmentType<TourOrigin> ORIGIN = AttachmentRegistry.create(
			ScubaDive.id("tour_origin"),
			builder -> builder.persistent(TourOrigin.CODEC));

	/** Divers of running sessions (loading until the return). */
	static final Map<UUID, TourSession> DIVERS = new HashMap<>();
	private static final List<TourSession> SESSIONS = new ArrayList<>();
	private static final Set<Integer> USED_SLOTS = new HashSet<>();
	/** Slots known to be empty; after a server start nothing is known, so each slot is cleared on first use. */
	private static final Set<Integer> CLEAN_SLOTS = new HashSet<>();

	private TourManager() {
	}

	public static void init() {
		PayloadTypeRegistry.clientboundPlay().register(TourLoadingPayload.TYPE, TourLoadingPayload.CODEC);

		ServerTickEvents.END_SERVER_TICK.register(TourManager::tick);
		ServerLifecycleEvents.SERVER_STARTED.register(TourManager::onStarted);
		ServerLifecycleEvents.SERVER_STOPPED.register(server -> reset());
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			ServerPlayer player = handler.getPlayer();
			server.execute(() -> onJoin(server, player));
		});
		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
			TourSession session = DIVERS.get(handler.getPlayer().getUUID());
			if (session != null) {
				session.removeMember(handler.getPlayer().getUUID());
			}
		});

		// A diver who would die stays alive and waits on the boat instead.
		ServerLivingEntityEvents.ALLOW_DEATH.register((entity, source, amount) -> {
			if (entity instanceof ServerPlayer player && player.level().dimension().equals(TourApi.DIMENSION)) {
				TourSession session = DIVERS.get(player.getUUID());
				if (session != null && (session.phase == TourSession.Phase.DIVING || session.phase == TourSession.Phase.RESULTS)) {
					session.died(player);
					return false;
				}
			}
			return true;
		});

		// No building or breaking anywhere in the tour dimension.
		PlayerBlockBreakEvents.BEFORE.register((level, player, pos, state, blockEntity) -> !level.dimension().equals(TourApi.DIMENSION));
		UseBlockCallback.EVENT.register((player, level, hand, hit) -> {
			if (level.dimension().equals(TourApi.DIMENSION)) {
				Item item = player.getItemInHand(hand).getItem();
				if (item instanceof BlockItem || item instanceof BucketItem) {
					return InteractionResult.FAIL;
				}
			}
			return InteractionResult.PASS;
		});
	}

	// ---- API entry points ----

	static boolean isDiver(Player player) {
		return DIVERS.containsKey(player.getUUID());
	}

	static void recordPhoto(ServerPlayer player, Species species, int score) {
		TourSession session = DIVERS.get(player.getUUID());
		if (session != null) {
			session.recordPhoto(player, species, score);
		}
	}

	static void outOfAir(ServerPlayer player) {
		TourSession session = DIVERS.get(player.getUUID());
		if (session != null) {
			session.outOfAir(player);
		}
	}

	static boolean start(ServerPlayer host, DivePoint point, List<ServerPlayer> members) {
		MinecraftServer server = host.level().getServer();
		if (server == null || point == null || members.isEmpty() || server.getLevel(TourApi.DIMENSION) == null) {
			return false;
		}
		for (ServerPlayer p : members) {
			if (DIVERS.containsKey(p.getUUID()) || !p.isAlive()) {
				return false;
			}
		}
		int slot = -1;
		for (int i = 0; i < MAX_SLOTS; i++) {
			if (!USED_SLOTS.contains(i)) {
				slot = i;
				break;
			}
		}
		if (slot < 0) {
			return false;
		}
		USED_SLOTS.add(slot);
		TourSession session = new TourSession(server, slot, point, !CLEAN_SLOTS.contains(slot));
		for (ServerPlayer p : members) {
			session.addMember(p);
		}
		SESSIONS.add(session);
		return true;
	}

	static void slotFreed(int slot, boolean clean) {
		USED_SLOTS.remove(slot);
		if (clean) {
			CLEAN_SLOTS.add(slot);
		} else {
			CLEAN_SLOTS.remove(slot);
		}
	}

	// ---- internals ----

	private static void tick(MinecraftServer server) {
		if (SESSIONS.isEmpty()) {
			return;
		}
		for (TourSession session : new ArrayList<>(SESSIONS)) {
			try {
				session.tick();
			} catch (RuntimeException e) {
				ScubaDive.LOGGER.error("Tour session failed", e);
				session.finished = true;
				for (UUID id : new ArrayList<>(session.members)) {
					DIVERS.remove(id, session);
				}
				USED_SLOTS.remove(session.slot);
				CLEAN_SLOTS.remove(session.slot);
			}
			if (session.finished) {
				SESSIONS.remove(session);
			}
		}
	}

	private static void reset() {
		DIVERS.clear();
		SESSIONS.clear();
		USED_SLOTS.clear();
		CLEAN_SLOTS.clear();
	}

	/** After a crash or stop, force-loaded chunks of the tour dimension may linger: release them. */
	private static void onStarted(MinecraftServer server) {
		reset();
		ServerLevel level = server.getLevel(TourApi.DIMENSION);
		if (level == null) {
			return;
		}
		for (long packed : new ArrayList<>(level.getForceLoadedChunks())) {
			level.setChunkForced(ChunkPos.getX(packed), ChunkPos.getZ(packed), false);
		}
	}

	/** Brings back a player who is left over in the tour dimension (or has a stale origin). */
	private static void onJoin(MinecraftServer server, ServerPlayer player) {
		if (isDiver(player)) {
			return;
		}
		boolean inTour = player.level().dimension().equals(TourApi.DIMENSION);
		if (inTour) {
			returnPlayer(player);
		} else {
			player.removeAttached(ORIGIN);
		}
	}

	/** Teleports the player back to the recorded origin (or the world spawn) and forgets it. */
	static void returnPlayer(ServerPlayer player) {
		MinecraftServer server = player.level().getServer();
		TourOrigin origin = player.getAttached(ORIGIN);
		player.removeAttached(ORIGIN);
		ServerLevel level = null;
		double x;
		double y;
		double z;
		float yaw = player.getYRot();
		float pitch = 0.0f;
		if (origin != null) {
			level = server.getLevel(origin.dimension());
		}
		if (level != null && !level.dimension().equals(TourApi.DIMENSION)) {
			x = origin.x();
			y = origin.y();
			z = origin.z();
			yaw = origin.yaw();
			pitch = origin.pitch();
		} else {
			LevelData.RespawnData spawn = server.getRespawnData();
			level = server.getLevel(spawn.dimension());
			if (level == null) {
				level = server.overworld();
			}
			x = spawn.pos().getX() + 0.5;
			y = spawn.pos().getY();
			z = spawn.pos().getZ() + 0.5;
		}
		player.teleportTo(level, x, y, z, Set.of(), yaw, pitch, true);
		player.resetFallDistance();
	}
}
