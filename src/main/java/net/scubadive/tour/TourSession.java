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
	/** Time budget for site build/clear slices per tick. */
	private static final long STEP_BUDGET_NANOS = 15_000_000L;
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
		/** Points lost for breaking corals. */
		int penalty;
		int coralsBroken;
		/** Ran the tank dry underwater: the dive total is halved. */
		boolean emptyTank;
		/** Surfaced with decompression sickness: the dive total is cut by a quarter. */
		boolean bent;

		Stat(String name) {
			this.name = name;
		}

		int total() {
			int sum = -penalty;
			for (int v : best.values()) {
				sum += v;
			}
			sum = Math.max(0, sum);
			if (emptyTank) {
				sum /= 2;
			}
			if (bent) {
				sum = sum * 3 / 4;
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
	private boolean leftoversCleared;
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
		if (preClear && !leftoversCleared) {
			leftoversCleared = true;
			discardEntities(level);
		}
		try {
			long end = System.nanoTime() + STEP_BUDGET_NANOS;
			do {
				if (work < (preClear ? clearSteps : 0)) {
					DiveSites.clearStep(level, origin, work);
				} else {
					DiveSites.buildStep(level, origin, point, seed, work - (preClear ? clearSteps : 0));
				}
				work++;
			} while (work < totalWork && System.nanoTime() < end);
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
		// Entities load asynchronously after the chunks; clear any leftovers that arrived late.
		discardEntities(level);
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
			DiveApi.setPressure(p, TourApi.TOUR_TANK);
			stats.computeIfAbsent(p.getUUID(), k -> new Stat(p.getName().getString()));
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
			if (!waiting.contains(id)) {
				checkPersonalEnd(p, level);
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

	/** Ticks spent under water per diver; a diver counts as having dived after DIVED_TICKS. */
	private final Map<UUID, Integer> underTicks = new java.util.HashMap<>();
	private static final int DIVED_TICKS = 100;
	/** Surfacing with this little air left ends the diver's dive. */
	private static final float RESERVE_BAR = 10f;

	/** Ends a diver's own dive when they surface on reserve or climb back onto the boat. */
	private void checkPersonalEnd(ServerPlayer p, ServerLevel level) {
		if (phase != Phase.DIVING) {
			return;
		}
		UUID id = p.getUUID();
		if (p.isUnderWater()) {
			underTicks.merge(id, 1, Integer::sum);
			return;
		}
		boolean dived = underTicks.getOrDefault(id, 0) >= DIVED_TICKS;
		if (!dived) {
			return;
		}
		if (DiveSites.onBoat(origin, p.blockPosition())) {
			finishDiver(p, level, "tour.scubadive.end.boat");
		} else if (net.scubadive.dive.DiveApi.pressure(p) <= RESERVE_BAR) {
			finishDiver(p, level, "tour.scubadive.end.reserve");
		}
	}

	private void finishDiver(ServerPlayer p, ServerLevel level, String messageKey) {
		if (!waiting.add(p.getUUID())) {
			return;
		}
		p.sendSystemMessage(Component.translatable(messageKey));
		teleportToBoat(p, level, indexOf(p.getUUID()));
		p.sendOverlayMessage(Component.translatable("tour.scubadive.waiting"));
		checkAllDone();
	}

	/** A diver broke a coral during the tour: environmental damage costs points. */
	void coralBroken(ServerPlayer p) {
		if (phase != Phase.DIVING || waiting.contains(p.getUUID())) {
			return;
		}
		Stat stat = stats.computeIfAbsent(p.getUUID(), k -> new Stat(p.getName().getString()));
		stat.penalty += CORAL_PENALTY;
		stat.coralsBroken++;
		p.sendOverlayMessage(Component.translatable("tour.scubadive.coral_penalty", CORAL_PENALTY)
				.withStyle(net.minecraft.ChatFormatting.RED));
	}

	private static final int CORAL_PENALTY = 20;

	/** Decompression sickness during the tour: a milder penalty than running out of air. */
	void bent(ServerPlayer p) {
		if (phase != Phase.DIVING) {
			return;
		}
		stats.computeIfAbsent(p.getUUID(), k -> new Stat(p.getName().getString())).bent = true;
		p.sendSystemMessage(Component.translatable("tour.scubadive.bent_penalty").withStyle(net.minecraft.ChatFormatting.RED));
	}

	/** The diver's tank is empty under water: big penalty, back to the boat. */
	void outOfAir(ServerPlayer p) {
		if (phase != Phase.DIVING || !waiting.add(p.getUUID())) {
			return;
		}
		stats.computeIfAbsent(p.getUUID(), k -> new Stat(p.getName().getString())).emptyTank = true;
		p.sendSystemMessage(Component.translatable("tour.scubadive.end.empty").withStyle(net.minecraft.ChatFormatting.RED));
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

		lines.add(Component.translatable("tour.scubadive.result.species"));
		for (UUID id : stats.keySet()) {
			Stat s = stats.get(id);
			lines.add(Component.translatable("tour.scubadive.result.species_line", s.name, s.best.size()));
			if (s.coralsBroken > 0) {
				lines.add(Component.translatable("tour.scubadive.result.coral_line", s.name, s.coralsBroken, s.penalty));
			}
			if (s.emptyTank) {
				lines.add(Component.translatable("tour.scubadive.result.empty_line", s.name));
			}
			if (s.bent) {
				lines.add(Component.translatable("tour.scubadive.result.bent_line", s.name));
			}
		}
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

	private void discardEntities(ServerLevel level) {
		for (Entity e : level.getEntitiesOfClass(Entity.class, TourSpawner.siteBox(origin), e -> !(e instanceof Player))) {
			e.discard();
		}
	}

	/** Safe abort after an unexpected error: everything is guarded so that nobody stays stranded. */
	void emergency() {
		for (UUID id : new ArrayList<>(members)) {
			try {
				ServerPlayer p = player(id);
				if (p != null) {
					ServerPlayNetworking.send(p, new TourLoadingPayload(-1, point.langKey()));
					TourManager.returnPlayer(p);
				}
			} catch (RuntimeException e) {
				ScubaDive.LOGGER.error("Tour emergency return failed", e);
			}
		}
		members.clear();
		waiting.clear();
		TourManager.DIVERS.values().removeIf(s -> s == this);
		ServerLevel level = level();
		if (level != null) {
			try {
				discardEntities(level);
			} catch (RuntimeException e) {
				ScubaDive.LOGGER.error("Tour emergency entity clear failed", e);
			}
			try {
				if (chunksForced) {
					setForced(level, false);
				}
			} catch (RuntimeException e) {
				ScubaDive.LOGGER.error("Tour emergency unforce failed", e);
			}
		}
		TourManager.slotFreed(slot, false);
		finished = true;
	}

	private void startCleanup() {
		phase = Phase.CLEANUP;
		cleanStep = 0;
	}

	private void tickCleanup(ServerLevel level) {
		if (!entitiesCleared) {
			entitiesCleared = true;
			if (chunksForced) {
				discardEntities(level);
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
