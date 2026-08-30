package dev.momentcraft.capture;

import java.util.UUID;

public record PlayerSnapshot(
    UUID playerId,
    String playerName,
    double x,
    double y,
    double z,
    float yaw,
    float pitch,
    double health,
    int foodLevel
) {
}
