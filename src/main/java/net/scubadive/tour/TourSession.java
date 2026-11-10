package net.scubadive.tour;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import net.scubadive.ScubaDive;
import net.scubadive.dive.DiveApi;
import net.scubadive.species.Species;
import net.scubadive.tour.site.DiveSites;

/** One running tour: loading, diving, results, return and clean-up. */
final class TourSession {
	enum Phase { LOADING, DIVING, RESULTS, CLEANUP }

	/** Safety limit: 20 minutes from the start of the dive. */
	private static final int MAX_DIVE_TICKS = 20 * 60 * 20;
	private static final int RESULT_TICKS = 100;
	private static final int STEPS_PER_TICK = 3;
	private static final int CHUNK_WAIT_LIMIT = 600;
	private static final int SPAWN_INTERVAL = 40;
	private static final int FIRST_GROUPS_MIN = 30;
	private static final int FIRST_GROUPS_MAX = 50;

	/** Per-player best scores. */
	static final class Stat {
		final String name;
		final Map<Species, Integer> best = new EnumMap<>(Species.class);
		Species bestShotSpecies;
		int bestShot;

		Stat(String name) {
			this.name = name;
		}

		int total() {
			int sum = 0;
			for (int v : best.values()) {
				sum += v;
			}
			return sum;
		}
	}

	final MinecraftServer server;
	final int slot;
	final BlockPos origin;
	final DivePoint point;
	final long seed;
	final List<UUID> members = new ArrayList<>();
	final Map<UUID, Stat> stats = new LinkedHashMap<>();
	final Set<UUID> waiting = new HashSet<>();
	Phase phase = Phase.LOADING;

	private int ticks;
	private boolean chunksForced;
	private boolean chunksReady;
	private int chunkWait;
	private int lastPercent = -1;
	private final boolean preClear;
	private final int clearSteps = DiveSites.clearSteps();
	private final int buildSteps;
	private int work;
	private int diveTicks;
	private int resultTicks;
	private int cleanStep;
	private boolean entitiesCleared;
	boolean finished;

	TourSession(MinecraftServer server, int slot, DivePoint point, boolean preClear) {
		this.server = server;
		this.slot = slot;
		this.origin = new BlockPos(slot * TourManager.SLOT_SPACING, DiveSites.SURFACE_Y, 0);
		this.point = point;
		this.seed = server.overworld().getRandom().nextLong();
		this.preClear = preClear;
		this.buildSteps = Math.max(1, DiveSites.buildSteps(point));
	}

	ServerLevel level() {
		return server.getLevel(TourApi.DIMENSION);
	}

	private ServerPlayer player(UUID id) {
		return server.getPlayerList().getPlayer(id);
	}

	private void forEachMember(java.util.function.Consumer<ServerPlayer> action) {
		for (UUID id : new ArrayList<>(members)) {
			ServerPlayer p = player(id);
			if (p != null) {
				action.accept(p);
			}
		}
	}

	// ---- chunks ----

	private int chunkMin(int c) {
		return (c - DiveSites.RADIUS) >> 4;
	}

	private int chunkMax(int c) {
		return (c + DiveSites.RADIUS) >> 4;
	}

	private void setForced(ServerLevel level, boolean forced) {
		for (int cx = chunkMin(origin.getX()); cx <= chunkMax(origin.getX()); cx++) {
			for (int cz = chunkMin(origin.getZ()); cz <= chunkMax(origin.getZ()); cz++) {
				level.setChunkForced(cx, cz, forced);
			}
		}
	}

	private boolean allChunksLoaded(ServerLevel level) {
		for (int cx = chunkMin(origin.getX()); cx <= chunkMax(origin.getX()); cx++) {
			for (int cz = chunkMin(origin.getZ()); cz <= chunkMax(origin.getZ()); cz++) {
				if (!level.hasChunk(cx, cz)) {
					return false;
				}
			}
		}
		return true;
	}

	// ---- membership ----

	/** Records the player's position and joins them to the session (loading phase). */
	void addMember(ServerPlayer p) {
		p.setAttached(TourManager.ORIGIN, new TourOrigin(p.level().dimension(), p.getX(), p.getY(), p.getZ(), p.getYRot(), p.getXRot()));
		members.add(p.getUUID());
		stats.put(p.getUUID(), new Stat(p.getName().getString()));
		TourManager.DIVERS.put(p.getUUID(), this);
	}

	/** Drops a player from the session (disconnect or left the dimension). */
	void removeMember(UUID id) {
		members.remove(id);
		waiting.remove(id);
		TourManager.DIVERS.remove(id, this);
		if (phase == Phase.DIVING) {
			checkAllDone();
		}
	}

	// ---- ticking ----

	void tick() {
		ServerLevel level = level();
		if (level == null) {
			abort();
			return;
		}
		ticks++;
		switch (phase) {
			case LOADING -> tickLoading(level);
			case DIVING -> tickDiving(level);
			case RESULTS -> tickResults(level);
			case CLEANUP -> tickCleanup(level);
		}
	}

	private void tickLoading(ServerLevel level) {
		if (members.isEmpty()) {
			startCleanup();
			return;
		}
		if (!chunksForced) {
			setForced(level, true);
			chunksForced = true;
		}
		if (!chunksReady) {
			chunkWait++;
			chunksReady = allChunksLoaded(level) || chunkWait > CHUNK_WAIT_LIMIT;
			sendProgress(chunksReady ? 20 : Math.min(19, chunkWait * 20 / 100));
			return;
		}
		int totalWork = (preClear ? clearSteps : 0) + buildSteps;
		try {
			for (int i = 0; i < STEPS_PER_TICK && work < totalWork; i++, work++) {
				if (work < (preClear ? clearSteps : 0)) {
					DiveSites.clearStep(level, origin, work);
				} else {
					DiveSites.buildStep(level, origin, point, seed, work - (preClear ? clearSteps : 0));
				}
			}
		} catch (RuntimeException e) {
			ScubaDive.LOGGER.error("Tour site build failed", e);
			forEachMember(p -> p.sendSystemMessage(Component.translatable("tour.scubadive.error")));
			abort();
			return;
		}
		sendProgress(20 + work * 80 / totalWork);
		if (work >= totalWork) {
			beginDive(level);
		}
	}

	private void sendProgress(int percent) {
		if (percent == lastPercent) {
			return;
		}
		lastPercent = percent;
		TourLoadingPayload payload = new TourLoadingPayload(percent, point.langKey());
		forEachMember(p -> ServerPlayNetworking.send(p, payload));
	}

	private void beginDive(ServerLevel level) {
		TourSpawner.spawnGroups(level, origin, point,
				FIRST_GROUPS_MIN + level.getRandom().nextInt(FIRST_GROUPS_MAX - FIRST_GROUPS_MIN + 1), false);
		phase = Phase.DIVING;
		diveTicks = 0;
		int index = 0;
		for (UUID id : new ArrayList<>(members)) {
			ServerPlayer p = player(id);
			if (p == null || !p.isAlive()) {
				removeMember(id);
				continue;
			}
			ServerPlayNetworking.send(p, new TourLoadingPayload(-1, point.langKey()));
			teleportToBoat(p, level, index++);
			DiveApi.refill(p);
			p.connection.send(new ClientboundSetTitlesAnimationPacket(10, 70, 20));
			p.connection.send(new ClientboundSetSubtitleTextPacket(Component.translatable("tour.scubadive.start.subtitle")));
			p.connection.send(new ClientboundSetTitleTextPacket(Component.translatable("tour.scubadive.start.title", Component.translatable(point.langKey()))));
		}
		if (members.isEmpty()) {
			startCleanup();
		}
	}

	private void teleportToBoat(ServerPlayer p, ServerLevel level, int index) {
		BlockPos spot = DiveSites.boatSpawn(origin, index);
		p.teleportTo(level, spot.getX() + 0.5, spot.getY(), spot.getZ() + 0.5, Set.of(), p.getYRot(), 0.0f, true);
		p.resetFallDistance();
	}

	private int indexOf(UUID id) {
		return Math.max(0, stats.keySet().stream().toList().indexOf(id));
	}

	private void tickDiving(ServerLevel level) {
		diveTicks++;
		for (UUID id : new ArrayList<>(members)) {
			ServerPlayer p = player(id);
			if (p == null) {
				continue;
			}
			if (!p.level().dimension().equals(TourApi.DIMENSION)) {
				// Left the tour dimension by other means: no longer part of the tour.
				p.removeAttached(TourManager.ORIGIN);
				removeMember(id);
				continue;
			}
			if (waiting.contains(id)) {
				if ((diveTicks + p.getId()) % 5 == 0 && !DiveSites.onBoat(origin, p.blockPosition())) {
					teleportToBoat(p, level, indexOf(id));
				}
				if ((diveTicks + p.getId()) % 20 == 0) {
					p.sendOverlayMessage(Component.translatable("tour.scubadive.waiting"));
				}
			}
		}
		if (phase != Phase.DIVING) {
			return;
		}
		if (diveTicks % SPAWN_INTERVAL == 0) {
			TourSpawner.spawnGroups(level, origin, point, 3, true);
		}
		if (diveTicks >= MAX_DIVE_TICKS) {
			endDive();
		} else {
			checkAllDone();
		}
	}

	/** The diver's tank is empty: send them to the boat and make them wait. */
	void outOfAir(ServerPlayer p) {
		if (phase != Phase.DIVING || !waiting.add(p.getUUID())) {
			return;
		}
		ServerLevel level = level();
		if (level != null) {
			teleportToBoat(p, level, indexOf(p.getUUID()));
		}
		p.sendOverlayMessage(Component.translatable("tour.scubadive.waiting"));
		checkAllDone();
	}

	/** A diver died in the tour dimension (death is cancelled by the manager): treat as waiting. */
	void died(ServerPlayer p) {
		p.setHealth(p.getMaxHealth());
		p.setAirSupply(p.getMaxAirSupply());
		p.extinguishFire();
		waiting.add(p.getUUID());
		ServerLevel level = level();
		if (level != null) {
			teleportToBoat(p, level, indexOf(p.getUUID()));
		}
		p.sendOverlayMessage(Component.translatable("tour.scubadive.waiting"));
		if (phase == Phase.DIVING) {
			checkAllDone();
		}
	}

	private void checkAllDone() {
		if (phase != Phase.DIVING) {
			return;
		}
		if (members.isEmpty() || waiting.containsAll(members)) {
			endDive();
		}
	}

	// ---- scoring ----

	void recordPhoto(ServerPlayer p, Species species, int score) {
		Stat stat = stats.computeIfAbsent(p.getUUID(), k -> new Stat(p.getName().getString()));
		stat.best.merge(species, score, Math::max);
		if (score > stat.bestShot) {
			stat.bestShot = score;
			stat.bestShotSpecies = species;
		}
	}

	private void endDive() {
		phase = Phase.RESULTS;
		resultTicks = RESULT_TICKS;
		broadcastResults();
	}

	private void broadcastResults() {
		List<Component> lines = new ArrayList<>();
		lines.add(Component.translatable("tour.scubadive.result.header", Component.translatable(point.langKey())));

		lines.add(Component.translatable("tour.scubadive.result.best_shot"));
		List<Stat> shots = stats.values().stream().filter(s -> s.bestShotSpecies != null)
				.sorted(Comparator.comparingInt((Stat s) -> s.bestShot).reversed()).limit(3).toList();
		if (shots.isEmpty()) {
			lines.add(Component.translatable("tour.scubadive.result.none"));
		}
		for (int i = 0; i < shots.size(); i++) {
			Stat s = shots.get(i);
			lines.add(Component.translatable("tour.scubadive.result.best_shot_line", i + 1, s.name,
					Component.translatable(s.bestShotSpecies.langKey()), s.bestShot));
		}

		lines.add(Component.translatable("tour.scubadive.result.total"));
		List<Stat> totals = stats.values().stream().filter(s -> !s.best.isEmpty())
				.sorted(Comparator.comparingInt(Stat::total).reversed()).limit(3).toList();
		if (totals.isEmpty()) {
			lines.add(Component.translatable("tour.scubadive.result.none"));
		}
		for (int i = 0; i < totals.size(); i++) {
			Stat s = totals.get(i);
			lines.add(Component.translatable("tour.scubadive.result.total_line", i + 1, s.name, s.total()));
		}

		Set<Species> all = new HashSet<>();
		for (Stat s : stats.values()) {
			all.addAll(s.best.keySet());
		}
		lines.add(Component.translatable("tour.scubadive.result.species", all.size()));
		forEachMember(p -> {
			for (Component line : lines) {
				p.sendSystemMessage(line);
			}
		});
	}

	// ---- results / return / clean-up ----

	private void tickResults(ServerLevel level) {
		if (--resultTicks > 0) {
			return;
		}
		forEachMember(TourManager::returnPlayer);
		for (UUID id : new ArrayList<>(members)) {
			TourManager.DIVERS.remove(id, this);
		}
		members.clear();
		waiting.clear();
		startCleanup();
	}

	/** Cancels a session that cannot continue; members go back to where they were. */
	void abort() {
		forEachMember(p -> {
			ServerPlayNetworking.send(p, new TourLoadingPayload(-1, point.langKey()));
			TourManager.returnPlayer(p);
		});
		for (UUID id : new ArrayList<>(members)) {
			TourManager.DIVERS.remove(id, this);
		}
		members.clear();
		waiting.clear();
		startCleanup();
	}

	private void startCleanup() {
		phase = Phase.CLEANUP;
		cleanStep = 0;
	}

	private void tickCleanup(ServerLevel level) {
		if (!entitiesCleared) {
			entitiesCleared = true;
			if (chunksForced) {
				for (Entity e : level.getEntitiesOfClass(Entity.class, TourSpawner.siteBox(origin), e -> !(e instanceof Player))) {
					e.discard();
				}
			}
		}
		boolean touched = chunksForced && chunksReady && work > 0;
		// An untouched slot stays as it was (clean unless it needed the pre-clear).
		boolean clean = !preClear;
		if (touched) {
			clean = true;
			try {
				for (int i = 0; i < STEPS_PER_TICK && cleanStep < clearSteps; i++, cleanStep++) {
					DiveSites.clearStep(level, origin, cleanStep);
				}
			} catch (RuntimeException e) {
				ScubaDive.LOGGER.error("Tour site clear failed", e);
				clean = false;
				cleanStep = clearSteps;
			}
			if (cleanStep < clearSteps) {
				return;
			}
		}
		if (chunksForced) {
			setForced(level, false);
		}
		TourManager.slotFreed(slot, clean);
		finished = true;
	}
}
