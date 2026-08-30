package dev.momentcraft.zone;

import org.bukkit.Location;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class SelectionManager {

    private final Map<UUID, Selection> selections = new HashMap<>();

    public void setCorner1(UUID playerId, Location location) {
        selections.computeIfAbsent(playerId, id -> new Selection()).corner1 = location;
    }

    public void setCorner2(UUID playerId, Location location) {
        selections.computeIfAbsent(playerId, id -> new Selection()).corner2 = location;
    }

    public Location getCorner1(UUID playerId) {
        Selection sel = selections.get(playerId);
        return sel != null ? sel.corner1 : null;
    }

    public Location getCorner2(UUID playerId) {
        Selection sel = selections.get(playerId);
        return sel != null ? sel.corner2 : null;
    }

    public boolean hasCompleteSelection(UUID playerId) {
        Selection sel = selections.get(playerId);
        return sel != null && sel.isComplete();
    }

    public void clear(UUID playerId) {
        selections.remove(playerId);
    }
}
