package dev.momentcraft.capture;

import java.util.List;

public record Snapshot(String zoneId, long timestampMillis, List<PlayerSnapshot> players) {
}
