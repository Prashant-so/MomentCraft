package dev.momentcraft.capture;

import dev.momentcraft.performance.PerformanceState;
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
    private int throttleSkipCounter = 0;

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
        PerformanceState state = plugin.getPerformanceGuard().state();

        if (state == PerformanceState.PAUSED) {
            // Do nothing at all — not even the throttle counter — this is
            // the cheapest possible state, by design.
            return;
        }

        if (state == PerformanceState.THROTTLED) {
            // Only sample on every other tick instead of skipping entirely,
            // so there's still *some* lead-up footage rather than none.
            throttleSkipCounter++;
            if (throttleSkipCounter % 2 != 0) {
                return;
            }
        }

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
