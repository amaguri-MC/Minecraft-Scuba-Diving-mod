package net.scubadive.creature.reef;

/**
 * Per-species behaviour numbers.
 *
 * @param fleeRadius distance at which a player makes the fish bolt
 * @param fleeSpeed  speed modifier while bolting
 * @param homeRange  how far it may drift from its home before swimming back
 * @param schoolSize max school size (1 = loner, 2 = pair)
 * @param hide       dart into nearby coral instead of simply swimming away
 */
public record ReefProfile(String id, double fleeRadius, double fleeSpeed, double homeRange, int schoolSize, boolean hide) {
}
