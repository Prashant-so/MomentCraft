package dev.momentcraft.capture;

import dev.momentcraft.plugin.MomentCraftPlugin;
import dev.momentcraft.zone.Zone;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class CaptureManager {

    private final MomentCraftPlugin plugin;
    private final Map<String, CaptureBuffer> buffers = new HashMap<>();
    private BukkitTask task;

    public CaptureManager(MomentCraftPlugin plugin) {
        this.plugin = plugin;
    }

    public void start() {
        long interval = plugin.getConfigManager().getCaptureSampleIntervalTicks();
        task = Bukkit.getScheduler().runTaskTimer(plugin, this::tick, interval, interval);
    }

    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    private void tick() {
        for (Zone zone : plugin.getZoneManager().all()) {
            if (!zone.enabled()) {
                continue;
            }

            World world = Bukkit.getWorld(zone.world());
            if (world == null) {
                continue;
            }

            List<PlayerSnapshot> inside = playersInside(zone, world);
            if (inside.isEmpty()) {
                // Nothing in the zone right now — skip entirely, don't even
                // touch the buffer map for this zone.
                continue;
            }

            CaptureBuffer buffer = buffers.computeIfAbsent(zone.id(),
                id -> new CaptureBuffer(plugin.getConfigManager().getCaptureBufferCapacity()));
            buffer.add(new Snapshot(zone.id(), System.currentTimeMillis(), inside));
        }
    }

    private List<PlayerSnapshot> playersInside(Zone zone, World world) {
        List<PlayerSnapshot> result = new ArrayList<>();

        for (Player player : world.getPlayers()) {
            Location loc = player.getLocation();
            if (!zone.contains(zone.world(), loc.getX(), loc.getY(), loc.getZ())) {
                continue;
            }

            result.add(new PlayerSnapshot(
                player.getUniqueId(),
                player.getName(),
                loc.getX(), loc.getY(), loc.getZ(),
                loc.getYaw(), loc.getPitch(),
                player.getHealth(),
                player.getFoodLevel()
            ));
        }

        return result;
    }

    public int bufferSize(String zoneId) {
        CaptureBuffer buffer = buffers.get(zoneId);
        return buffer != null ? buffer.size() : 0;
    }

    public List<Snapshot> snapshotsFor(String zoneId) {
        CaptureBuffer buffer = buffers.get(zoneId);
        return buffer != null ? buffer.snapshots() : List.of();
    }
}
