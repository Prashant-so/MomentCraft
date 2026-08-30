package dev.momentcraft.moment;

import java.util.UUID;

/**
 * A normalized representation of something that happened in-game, independent
 * of whichever raw Bukkit event produced it. Scoring and (eventually) export
 * only ever need to look at this shape, not at EntityDeathEvent vs
 * EntityDamageEvent vs whatever else.
 */
public record MomentEvent(
    MomentType type,
    String world,
    double x, double y, double z,
    UUID primaryPlayerId,
    String primaryPlayerName,
    double primaryHealthFraction,
    int killstreak,
    int nearbyPlayerCount,
    boolean dangerousEnvironment,
    long timestampMillis
) {
}
