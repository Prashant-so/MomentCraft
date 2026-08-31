package dev.momentcraft.job;

import dev.momentcraft.plugin.MomentCraftPlugin;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;

public final class JobCleanupTask {

    private final MomentCraftPlugin plugin;
    private final File jobsFolder;
    private BukkitTask task;

    public JobCleanupTask(MomentCraftPlugin plugin) {
        this.plugin = plugin;
        this.jobsFolder = new File(plugin.getDataFolder(), "jobs");
    }

    public void start() {
        // Runs every 5 minutes — cleanup doesn't need to be frequent or precise.
        long interval = 20L * 60L * 5L;
        task = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::cleanup, interval, interval);
    }

    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    private void cleanup() {
        if (!jobsFolder.exists()) {
            return;
        }

        File[] files = jobsFolder.listFiles((dir, name) -> name.endsWith(".json"));
        if (files == null || files.length == 0) {
            return;
        }

        long maxAgeMillis = plugin.getConfigManager().getJobMaxAgeMinutes() * 60L * 1000L;
        long now = System.currentTimeMillis();
        int maxCount = plugin.getConfigManager().getJobMaxCount();

        for (File file : files) {
            if (now - file.lastModified() > maxAgeMillis) {
                file.delete();
            }
        }

        // Re-list after age-based cleanup, then enforce the hard count cap
        // by deleting the oldest first — bounded storage, per spec.
        File[] remaining = jobsFolder.listFiles((dir, name) -> name.endsWith(".json"));
        if (remaining == null || remaining.length <= maxCount) {
            return;
        }

        Arrays.sort(remaining, Comparator.comparingLong(File::lastModified));
        int excess = remaining.length - maxCount;
        for (int i = 0; i < excess; i++) {
            remaining[i].delete();
        }
    }
}
