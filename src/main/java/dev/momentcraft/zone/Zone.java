package dev.momentcraft.zone;

public final class Zone {

    private final String id;
    private final String world;
    private final int minX, minY, minZ;
    private final int maxX, maxY, maxZ;
    private boolean enabled;

    public Zone(String id, String world, int minX, int minY, int minZ,
                int maxX, int maxY, int maxZ, boolean enabled) {
        this.id = id;
        this.world = world;
        this.minX = minX;
        this.minY = minY;
        this.minZ = minZ;
        this.maxX = maxX;
        this.maxY = maxY;
        this.maxZ = maxZ;
        this.enabled = enabled;
    }

    public String id() {
        return id;
    }

    public String world() {
        return world;
    }

    public int minX() {
        return minX;
    }

    public int minY() {
        return minY;
    }

    public int minZ() {
        return minZ;
    }

    public int maxX() {
        return maxX;
    }

    public int maxY() {
        return maxY;
    }

    public int maxZ() {
        return maxZ;
    }

    public boolean enabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean contains(String world, double x, double y, double z) {
        if (!this.world.equals(world)) {
            return false;
        }
        return x >= minX && x <= maxX
            && y >= minY && y <= maxY
            && z >= minZ && z <= maxZ;
    }
}
