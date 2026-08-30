package dev.momentcraft.moment;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Fired whenever a moment's score clears the configured threshold.
 * This is the hand-off point for later phases (job export, etc.) — they
 * should listen for this rather than reaching into MomentManager directly.
 */
public final class MomentDetectedEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final ScoredMoment scoredMoment;

    public MomentDetectedEvent(ScoredMoment scoredMoment) {
        this.scoredMoment = scoredMoment;
    }

    public ScoredMoment scoredMoment() {
        return scoredMoment;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
