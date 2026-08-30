package dev.momentcraft.moment;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class KillstreakTracker {

    private final Map<UUID, Integer> streaks = new HashMap<>();

    public int onKill(UUID killerId) {
        int updated = streaks.merge(killerId, 1, Integer::sum);
        return updated;
    }

    public void onDeath(UUID victimId) {
        streaks.remove(victimId);
    }

    public int current(UUID playerId) {
        return streaks.getOrDefault(playerId, 0);
    }
}
