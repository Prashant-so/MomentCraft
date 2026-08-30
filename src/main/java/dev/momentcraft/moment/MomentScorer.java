package dev.momentcraft.moment;

import dev.momentcraft.config.ConfigManager;

public final class MomentScorer {

    private final ConfigManager config;

    public MomentScorer(ConfigManager config) {
        this.config = config;
    }

    public int score(MomentEvent event) {
        int score = switch (event.type()) {
            case PLAYER_KILL -> config.getScoreBaseKill();
            case BOSS_KILL -> config.getScoreBaseBossKill();
            case PLAYER_DEATH -> 0; // deaths alone aren't inherently a "moment" worth keeping
            case EXPLOSION_SURVIVED -> config.getScoreBaseKill() / 2;
        };

        // Lower health at the moment of the event = more of a "clutch" —
        // scales linearly up to the configured max bonus.
        double lowHealthFactor = 1.0 - event.primaryHealthFraction();
        score += (int) Math.round(lowHealthFactor * config.getScoreLowHealthBonusMax());

        if (event.killstreak() > 1) {
            score += (event.killstreak() - 1) * config.getScoreKillstreakPerKill();
        }

        if (event.dangerousEnvironment()) {
            score += config.getScoreDangerBonus();
        }

        if (event.nearbyPlayerCount() > 0) {
            score += event.nearbyPlayerCount() * config.getScoreNearbyPlayerBonus();
        }

        return Math.max(0, score);
    }
}
