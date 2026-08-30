package dev.momentcraft.command.subcommands;

import dev.momentcraft.command.SubCommand;
import dev.momentcraft.moment.ScoredMoment;
import dev.momentcraft.plugin.MomentCraftPlugin;
import dev.momentcraft.util.Messages;
import org.bukkit.command.CommandSender;

import java.util.List;

public final class MomentsCommand implements SubCommand {

    private final MomentCraftPlugin plugin;

    public MomentsCommand(MomentCraftPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public String name() {
        return "moments";
    }

    @Override
    public String description() {
        return "Shows recently detected moments";
    }

    @Override
    public String usage() {
        return "/momentcraft moments";
    }

    @Override
    public String permission() {
        return "momentcraft.admin.notify";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        List<ScoredMoment> history = plugin.getMomentManager().recentHistory();

        Messages.raw(sender, Messages.DIVIDER);
        Messages.raw(sender, "<gradient:gold:yellow><bold>          Recent Moments</bold></gradient>");
        Messages.raw(sender, Messages.DIVIDER);

        if (history.isEmpty()) {
            Messages.raw(sender, " <gray><italic>Nothing detected yet.</italic></gray>");
        } else {
            for (ScoredMoment scored : history) {
                var event = scored.event();
                Messages.raw(sender,
                    " <yellow><type></yellow> <gray>—</gray> <white><player></white> <gray>(score <score>)</gray>",
                    Messages.ph("type", event.type()),
                    Messages.ph("player", event.primaryPlayerName()),
                    Messages.ph("score", scored.score())
                );
            }
        }

        Messages.raw(sender, Messages.DIVIDER);
    }
}
