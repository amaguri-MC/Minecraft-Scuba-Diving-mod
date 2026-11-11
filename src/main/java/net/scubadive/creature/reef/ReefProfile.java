package net.scubadive.creature.reef;

/**
 * Per-species behaviour numbers.
 *
 * @param fleeRadius distance at which a player makes the fish bolt
 * @param fleeSpeed  speed modifier while bolting
 * @param homeRange  how far it may drift from its home before swimming back
 * @param schoolSize max school size (1 = loner, 2 = pair)
 * @param hide       dart into nearby coral instead of simply swimming away
 * @param trait      extra behaviour (grazing, territorial chase, shelter lurking)
 * @param moveSpeed  movement speed attribute override, 0 keeps the fish default
 */
public record ReefProfile(String id, double fleeRadius, double fleeSpeed, double homeRange, int schoolSize, boolean hide,
		Trait trait, double moveSpeed) {
	public enum Trait {
		NONE,
		/** Nibbles at nearby coral. */
		GRAZER,
		/** Chases a close diver for a moment, harmlessly. */
		TERRITORIAL,
		/** Picks a sheltered spot (rock overhang) as home and stays there. */
		LURKER,
		/** Always lives in a school: loners rejoin, followers hold slots around the leader. */
		SCHOOLING,
		/** Cleaner wrasse: swims up to big fish or divers and fusses around them for a few seconds. */
		CLEANER,
		/** Keeps low over the floor near coral, creeping in short slow hops. */
		BOTTOM,
		/** Unafraid: now and then drifts over to a diver and looks at them. */
		CURIOUS
	}

	public ReefProfile(String id, double fleeRadius, double fleeSpeed, double homeRange, int schoolSize, boolean hide) {
		this(id, fleeRadius, fleeSpeed, homeRange, schoolSize, hide, Trait.NONE, 0.0);
	}
}
