package net.scubadive.camera;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;

import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

import net.scubadive.ScubaDive;
import net.scubadive.species.Species;

public final class ScubaCamera {
	/** Persistent per-player journal, synced to the owning client only. */
	public static final AttachmentType<Journal> JOURNAL = AttachmentRegistry.create(
			ScubaDive.id("journal"),
			builder -> builder
					.initializer(() -> Journal.EMPTY)
					.persistent(Journal.CODEC)
					.copyOnDeath()
					.syncWith(Journal.STREAM_CODEC, AttachmentSyncPredicate.targetOnly())
	);

	public static Item CAMERA;

	private static final int MIN_SHOT_INTERVAL_TICKS = 10;
	private static final Map<UUID, Long> LAST_SHOT = new HashMap<>();

	private ScubaCamera() { }

	public static void init() {
		ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, ScubaDive.id("camera"));
		CAMERA = Registry.register(BuiltInRegistries.ITEM, key, new Item(new Item.Properties().setId(key).stacksTo(1)));

		PayloadTypeRegistry.serverboundPlay().register(CapturePayload.TYPE, CapturePayload.CODEC);
		PayloadTypeRegistry.clientboundPlay().register(CaptureResultPayload.TYPE, CaptureResultPayload.CODEC);

		ServerPlayNetworking.registerGlobalReceiver(CapturePayload.TYPE, (payload, context) -> handleCapture(context.player(), payload));
	}

	private static void handleCapture(ServerPlayer player, CapturePayload payload) {
		long now = player.level().getGameTime();
		Long last = LAST_SHOT.get(player.getUUID());
		if (last != null && now - last < MIN_SHOT_INTERVAL_TICKS) {
			reject(player);
			return;
		}
		LAST_SHOT.put(player.getUUID(), now);

		if (!player.getMainHandItem().is(CAMERA) && !player.getOffhandItem().is(CAMERA)) {
			reject(player);
			return;
		}
		Species species = Species.byId(payload.species());
		if (species == null) {
			reject(player);
			return;
		}
		int score = CameraLogic.verify(player, species, payload.entityId(), new BlockPos(payload.x(), payload.y(), payload.z()), payload.fov());
		if (score <= 0) {
			reject(player);
			return;
		}

		Journal journal = player.getAttachedOrElse(JOURNAL, Journal.EMPTY);
		boolean isNew = !journal.has(species.id);
		player.setAttached(JOURNAL, journal.withShot(species.id, score));
		ServerPlayNetworking.send(player, new CaptureResultPayload(species.id, score, isNew));
	}

	private static void reject(ServerPlayer player) {
		ServerPlayNetworking.send(player, new CaptureResultPayload("", 0, false));
	}

	public static Journal journalOf(Player player) {
		return player.getAttachedOrElse(JOURNAL, Journal.EMPTY);
	}
}
