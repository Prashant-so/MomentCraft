package dev.momentcraft.config;

import dev.momentcraft.plugin.MomentCraftPlugin;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

public final class ConfigManager {

    private static final long TICKS_PER_SECOND = 20L;

    private final MomentCraftPlugin plugin;
    private FileConfiguration config;

    public ConfigManager(MomentCraftPlugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        config = plugin.getConfig();

        int version = config.getInt("config-version", 0);
        if (version != 1) {
            plugin.getLogger().warning(
                "config.yml has config-version " + version + ", expected 1. " +
                "Some settings may not apply correctly until the file is updated."
            );
        }
    }

    public void reload() {
        plugin.reloadConfig();
        config = plugin.getConfig();
        plugin.getLogger().info("Configuration reloaded.");
    }

    public boolean isDebug() {
        return config.getBoolean("general.debug", false);
    }

    public Material getWandMaterial() {
        String name = config.getString("zones.wand-material", "STICK");
        Material material = Material.matchMaterial(name);

        if (material == null) {
            plugin.getLogger().warning(
                "zones.wand-material '" + name + "' is not a valid material. Falling back to STICK.");
            return Material.STICK;
        }

        return material;
    }

    public long getCaptureSampleIntervalTicks() {
        return Math.max(1L, config.getLong("capture.sample-interval-ticks", 10));
    }

    public int getCaptureBufferCapacity() {
        long interval = getCaptureSampleIntervalTicks();
        int bufferSeconds = Math.max(1, config.getInt("capture.buffer-seconds", 15));
        return (int) Math.max(1, (bufferSeconds * TICKS_PER_SECOND) / interval);
    }

    public double getPerformanceThrottleTps() {
        return config.getDouble("performance.throttle-below-tps", 18.0);
    }

    public double getPerformancePauseTps() {
        return config.getDouble("performance.pause-below-tps", 15.0);
    }

    public double getPerformanceMinFreeMemoryRatio() {
        return config.getDouble("performance.min-free-memory-percent", 10.0) / 100.0;
    }

    public int getScoreThreshold() {
        return config.getInt("scoring.threshold", 55);
    }

    public int getScoreCooldownSeconds() {
        return config.getInt("scoring.cooldown-seconds", 30);
    }

    public int getScoreBaseKill() {
        return config.getInt("scoring.weights.base-kill", 5);
    }

    public int getScoreBaseBossKill() {
        return config.getInt("scoring.weights.base-boss-kill", 50);
    }

    public int getScoreLowHealthBonusMax() {
        return config.getInt("scoring.weights.low-health-bonus-max", 30);
    }

    public int getScoreKillstreakPerKill() {
        return config.getInt("scoring.weights.killstreak-per-kill", 8);
    }

    public int getScoreDangerBonus() {
        return config.getInt("scoring.weights.danger-bonus", 10);
    }

    public int getScoreNearbyPlayerBonus() {
        return config.getInt("scoring.weights.nearby-player-bonus", 4);
    }

    public int getScoreNearbyPlayerCap() {
        return config.getInt("scoring.nearby-player-cap", 3);
    }

    public double getScoreNearbyPlayerRadius() {
        return config.getDouble("scoring.nearby-player-radius", 20.0);
    }

    public FileConfiguration raw() {
        return config;
    }
}
