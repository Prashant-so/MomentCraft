package dev.momentcraft.performance;

import dev.momentcraft.plugin.MomentCraftPlugin;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

public final class PerformanceGuard {

    private final MomentCraftPlugin plugin;
    private PerformanceState state = PerformanceState.NORMAL;
    private BukkitTask task;

    public PerformanceGuard(MomentCraftPlugin plugin) {
        this.plugin = plugin;
    }

    public void start() {
        // Checked every second (20 ticks) — this doesn't need to be
        // fine-grained, it just needs to react before things get bad.
        task = Bukkit.getScheduler().runTaskTimer(plugin, this::evaluate, 20L, 20L);
    }

    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    private void evaluate() {
        double tps = currentTps();
        double freeMemoryRatio = freeMemoryRatio();

        double throttleTps = plugin.getConfigManager().getPerformanceThrottleTps();
        double pauseTps = plugin.getConfigManager().getPerformancePauseTps();
        double minFreeMemoryRatio = plugin.getConfigManager().getPerformanceMinFreeMemoryRatio();

        PerformanceState previous = state;

        if (tps < pauseTps || freeMemoryRatio < minFreeMemoryRatio) {
            state = PerformanceState.PAUSED;
        } else if (tps < throttleTps) {
            state = PerformanceState.THROTTLED;
        } else {
            state = PerformanceState.NORMAL;
        }

        if (state != previous) {
            plugin.getLogger().info(
                "Performance state changed: " + previous + " -> " + state +
                " (TPS " + String.format("%.1f", tps) + ", free memory " +
                String.format("%.0f%%", freeMemoryRatio * 100) + ")"
            );
        }
    }

    private double currentTps() {
        double[] tps = Bukkit.getServer().getTPS();
        // Index 0 is the last-1-minute average — most responsive without
        // being as noisy as an instant per-tick reading.
        return tps.length > 0 ? Math.min(tps[0], 20.0) : 20.0;
    }

    private double freeMemoryRatio() {
        Runtime runtime = Runtime.getRuntime();
        long max = runtime.maxMemory();
        long used = runtime.totalMemory() - runtime.freeMemory();
        if (max <= 0) {
            return 1.0;
        }
        return 1.0 - ((double) used / (double) max);
    }

    public PerformanceState state() {
        return state;
    }

    public boolean isPaused() {
        return state == PerformanceState.PAUSED;
    }

    public boolean isThrottled() {
        return state == PerformanceState.THROTTLED;
    }
}
