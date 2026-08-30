package dev.momentcraft.moment;

import dev.momentcraft.plugin.MomentCraftPlugin;
import dev.momentcraft.util.Messages;
import org.bukkit.Bukkit;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;

public final class MomentManager {

    private static final int HISTORY_CAPACITY = 20;

    private final MomentCraftPlugin plugin;
    private final MomentScorer scorer;
    private final KillstreakTracker killstreakTracker = new KillstreakTracker();
    private final Deque<ScoredMoment> history = new ArrayDeque<>();

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

        if (plugin.getConfigManager().isDebug()) {
            plugin.getLogger().info(
                "[moment] " + event.type() + " by " + event.primaryPlayerName() +
                " scored " + score + " (threshold " + plugin.getConfigManager().getScoreThreshold() + ")"
            );
        }

        if (score < plugin.getConfigManager().getScoreThreshold()) {
            return;
        }

        recordHistory(scored);
        notifyAdmins(scored);

        Bukkit.getPluginManager().callEvent(new MomentDetectedEvent(scored));
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
