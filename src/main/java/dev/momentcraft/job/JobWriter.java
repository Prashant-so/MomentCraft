package dev.momentcraft.job;

import dev.momentcraft.capture.PlayerSnapshot;
import dev.momentcraft.capture.Snapshot;
import dev.momentcraft.moment.MomentEvent;
import dev.momentcraft.moment.ScoredMoment;
import dev.momentcraft.plugin.MomentCraftPlugin;
import dev.momentcraft.zone.Zone;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

public final class JobWriter {

    private final MomentCraftPlugin plugin;
    private final File jobsFolder;

    public JobWriter(MomentCraftPlugin plugin) {
        this.plugin = plugin;
        this.jobsFolder = new File(plugin.getDataFolder(), "jobs");
    }

    public void write(ScoredMoment scored) {
        if (!jobsFolder.exists()) {
            jobsFolder.mkdirs();
        }

        String jobId = UUID.randomUUID().toString();
        File finalFile = new File(jobsFolder, jobId + ".json");
        File tempFile = new File(jobsFolder, jobId + ".json.tmp");

        String json = buildJson(jobId, scored);

        try (Writer writer = Files.newBufferedWriter(tempFile.toPath(), StandardCharsets.UTF_8)) {
            writer.write(json);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to write job file '" + jobId + "': " + e.getMessage());
            tempFile.delete();
            return;
        }

        try {
            // Atomic-ish rename: the worker should only ever see a fully
            // written .json file, never a partially written one.
            Files.move(tempFile.toPath(), finalFile.toPath(), StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to finalize job file '" + jobId + "': " + e.getMessage());
            tempFile.delete();
        }
    }

    private String buildJson(String jobId, ScoredMoment scored) {
        MomentEvent event = scored.event();
        List<Snapshot> snapshots = findSnapshotsFor(event);

        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"job_id\": ").append(quote(jobId)).append(",\n");
        sb.append("  \"created_at_millis\": ").append(System.currentTimeMillis()).append(",\n");
        sb.append("  \"moment_type\": ").append(quote(event.type().name())).append(",\n");
        sb.append("  \"score\": ").append(scored.score()).append(",\n");
        sb.append("  \"world\": ").append(quote(event.world())).append(",\n");
        sb.append("  \"location\": {\n");
        sb.append("    \"x\": ").append(event.x()).append(",\n");
        sb.append("    \"y\": ").append(event.y()).append(",\n");
        sb.append("    \"z\": ").append(event.z()).append("\n");
        sb.append("  },\n");
        sb.append("  \"primary_player\": {\n");
        sb.append("    \"uuid\": ").append(quote(event.primaryPlayerId().toString())).append(",\n");
        sb.append("    \"name\": ").append(quote(event.primaryPlayerName())).append("\n");
        sb.append("  },\n");
        sb.append("  \"health_fraction\": ").append(event.primaryHealthFraction()).append(",\n");
        sb.append("  \"killstreak\": ").append(event.killstreak()).append(",\n");
        sb.append("  \"nearby_player_count\": ").append(event.nearbyPlayerCount()).append(",\n");
        sb.append("  \"dangerous_environment\": ").append(event.dangerousEnvironment()).append(",\n");
        sb.append("  \"event_timestamp_millis\": ").append(event.timestampMillis()).append(",\n");
        sb.append("  \"snapshots\": [\n");
        appendSnapshots(sb, snapshots);
        sb.append("  ]\n");
        sb.append("}\n");

        return sb.toString();
    }

    private void appendSnapshots(StringBuilder sb, List<Snapshot> snapshots) {
        for (int i = 0; i < snapshots.size(); i++) {
            Snapshot snap = snapshots.get(i);
            sb.append("    {\n");
            sb.append("      \"timestamp_millis\": ").append(snap.timestampMillis()).append(",\n");
            sb.append("      \"players\": [\n");
            appendPlayers(sb, snap.players());
            sb.append("      ]\n");
            sb.append("    }");
            sb.append(i < snapshots.size() - 1 ? ",\n" : "\n");
        }
    }

    private void appendPlayers(StringBuilder sb, List<PlayerSnapshot> players) {
        for (int i = 0; i < players.size(); i++) {
            PlayerSnapshot p = players.get(i);
            sb.append("        {\n");
            sb.append("          \"uuid\": ").append(quote(p.playerId().toString())).append(",\n");
            sb.append("          \"name\": ").append(quote(p.playerName())).append(",\n");
            sb.append("          \"x\": ").append(p.x()).append(",\n");
            sb.append("          \"y\": ").append(p.y()).append(",\n");
            sb.append("          \"z\": ").append(p.z()).append(",\n");
            sb.append("          \"yaw\": ").append(p.yaw()).append(",\n");
            sb.append("          \"pitch\": ").append(p.pitch()).append(",\n");
            sb.append("          \"health\": ").append(p.health()).append(",\n");
            sb.append("          \"food_level\": ").append(p.foodLevel()).append("\n");
            sb.append("        }");
            sb.append(i < players.size() - 1 ? ",\n" : "\n");
        }
    }

    private List<Snapshot> findSnapshotsFor(MomentEvent event) {
        for (Zone zone : plugin.getZoneManager().all()) {
            if (zone.contains(event.world(), event.x(), event.y(), event.z())) {
                return plugin.getCaptureManager().snapshotsFor(zone.id());
            }
        }
        // Happened outside any defined zone — export with no lead-up
        // snapshots rather than failing to export at all.
        return List.of();
    }

    private String quote(String value) {
        String escaped = value
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n");
        return "\"" + escaped + "\"";
    }
}
