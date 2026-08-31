package dev.momentcraft.job;

import dev.momentcraft.moment.MomentDetectedEvent;
import dev.momentcraft.plugin.MomentCraftPlugin;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public final class JobExportListener implements Listener {

    private final MomentCraftPlugin plugin;
    private final JobWriter jobWriter;

    public JobExportListener(MomentCraftPlugin plugin) {
        this.plugin = plugin;
        this.jobWriter = new JobWriter(plugin);
    }

    @EventHandler
    public void onMomentDetected(MomentDetectedEvent event) {
        // File I/O off the main thread — writing to disk has no business
        // blocking the server tick, even for a small file like this.
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> jobWriter.write(event.scoredMoment()));
    }
}
