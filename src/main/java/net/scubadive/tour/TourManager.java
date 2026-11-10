package net.scubadive.tour;

import java.util.List;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import net.scubadive.species.Species;

/** Tour sessions: loading, boat, diving, results, return. Owner: tour session module (H2). */
public final class TourManager {
	private TourManager() {
	}

	public static void init() {
	}

	static boolean isDiver(Player player) {
		return false;
	}

	static void recordPhoto(ServerPlayer player, Species species, int score) {
	}

	static void outOfAir(ServerPlayer player) {
	}

	static boolean start(ServerPlayer host, DivePoint point, List<ServerPlayer> members) {
		return false;
	}
}
