package dev.momentcraft.moment;

import dev.momentcraft.plugin.MomentCraftPlugin;
import dev.momentcraft.util.Messages;
import org.bukkit.Bukkit;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class MomentManager {

    private static final int HISTORY_CAPACITY = 20;

    private final MomentCraftPlugin plugin;
    private final MomentScorer scorer;
    private final KillstreakTracker killstreakTracker = new KillstreakTracker();
    private final Deque<ScoredMoment> history = new ArrayDeque<>();
    private final Map<UUID, Long> lastMomentAt = new HashMap<>();

    public MomentManager(MomentCraftPlugin plugin) {
        this.plugin = plugin;
        this.scorer = new MomentScorer(plugin.getConfigManager());
    }

    public KillstreakTracker killstreakTracker() {
        return killstreakTracker;
    }

    public void submit(MomentEvent event) {
        int score = scorer.score(event);
        ScoredMoment scored = new ScoredMoment(event, score);
        int threshold = effectiveThreshold(event);

        if (plugin.getConfigManager().isDebug()) {
            plugin.getLogger().info(
                "[moment] " + event.type() + " by " + event.primaryPlayerName() +
                " scored " + score + " (threshold " + threshold + ")"
            );
        }

        if (score < threshold) {
            return;
        }

        if (isOnCooldown(event.primaryPlayerId())) {
            if (plugin.getConfigManager().isDebug()) {
                plugin.getLogger().info(
                    "[moment] Suppressed — " + event.primaryPlayerName() + " is still on moment cooldown."
                );
            }
            return;
        }

        lastMomentAt.put(event.primaryPlayerId(), System.currentTimeMillis());

        recordHistory(scored);
        notifyAdmins(scored);

        Bukkit.getPluginManager().callEvent(new MomentDetectedEvent(scored));
    }

    private int effectiveThreshold(MomentEvent event) {
        // Boss kills are rare enough on their own merit — they shouldn't
        // need bonus conditions stacked on top just to clear the same bar
        // as an ordinary player kill.
        if (event.type() == MomentType.BOSS_KILL) {
            return Math.min(plugin.getConfigManager().getScoreThreshold(),
                plugin.getConfigManager().getScoreBaseBossKill());
        }
        return plugin.getConfigManager().getScoreThreshold();
    }

    private boolean isOnCooldown(UUID playerId) {
        Long last = lastMomentAt.get(playerId);
        if (last == null) {
            return false;
        }
        long cooldownMillis = plugin.getConfigManager().getScoreCooldownSeconds() * 1000L;
        return (System.currentTimeMillis() - last) < cooldownMillis;
    }

    private void recordHistory(ScoredMoment scored) {
        history.addLast(scored);
        while (history.size() > HISTORY_CAPACITY) {
            history.removeFirst();
        }
    }

    private void notifyAdmins(ScoredMoment scored) {
        MomentEvent event = scored.event();
        for (var player : Bukkit.getOnlinePlayers()) {
            if (!player.hasPermission("momentcraft.admin.notify")) {
                continue;
            }
            Messages.send(player,
                "<gold>Moment detected:</gold> <white><type></white> <gray>by</gray> <white><player></white> <gray>(score <score>)</gray>",
                Messages.ph("type", formatType(event.type())),
                Messages.ph("player", event.primaryPlayerName()),
                Messages.ph("score", scored.score())
            );
        }
    }

    private String formatType(MomentType type) {
        return switch (type) {
            case PLAYER_KILL -> "Kill";
            case PLAYER_DEATH -> "Death";
            case BOSS_KILL -> "Boss Kill";
            case EXPLOSION_SURVIVED -> "Explosion Survived";
        };
    }

    public List<ScoredMoment> recentHistory() {
        return Collections.unmodifiableList(new ArrayList<>(history));
    }
}
