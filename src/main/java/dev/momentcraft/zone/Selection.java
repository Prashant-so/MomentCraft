package dev.momentcraft.zone;

import org.bukkit.Location;

final class Selection {

    Location corner1;
    Location corner2;

    boolean isComplete() {
        return corner1 != null && corner2 != null;
    }
}
