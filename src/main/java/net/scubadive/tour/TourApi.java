package net.scubadive.tour;

import java.util.List;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import net.scubadive.ScubaDive;
import net.scubadive.species.Species;

/**
 * Cross-module contract for dive tours. Signatures must not change.
 * Implemented by the tour session module (TourManager); other modules only call these.
 */
public final class TourApi {
	/** The tour dimension. Each tour session uses its own far-apart area inside it. */
	public static final ResourceKey<Level> DIMENSION = ResourceKey.create(Registries.DIMENSION, ScubaDive.id("tour"));

	private TourApi() {
	}

	/** True while the player is a diver of a running tour session (server side). */
	public static boolean isInTour(Player player) {
		return TourManager.isDiver(player);
	}

	/** True if the tank must not refill at the surface (anywhere in the tour dimension). Both sides. */
	public static boolean noRefill(Player player) {
		return player.level().dimension().equals(DIMENSION);
	}

	/** Called by the camera after a verified shot taken during a tour. */
	public static void onPhoto(ServerPlayer player, Species species, int score) {
		TourManager.recordPhoto(player, species, score);
	}

	/** Called by the dive module once when a tour diver's tank reaches 0 bar. */
	public static void onOutOfAir(ServerPlayer player) {
		TourManager.outOfAir(player);
	}

	/** Called by the guide when the host presses start. Members include the host; all have paid. */
	public static boolean start(ServerPlayer host, DivePoint point, List<ServerPlayer> members) {
		return TourManager.start(host, point, members);
	}
}
