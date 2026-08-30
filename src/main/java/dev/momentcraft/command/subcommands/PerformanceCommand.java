package dev.momentcraft.command.subcommands;

import dev.momentcraft.command.SubCommand;
import dev.momentcraft.plugin.MomentCraftPlugin;
import dev.momentcraft.util.Messages;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

public final class PerformanceCommand implements SubCommand {

    private final MomentCraftPlugin plugin;

    public PerformanceCommand(MomentCraftPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public String name() {
        return "performance";
    }

    @Override
    public String description() {
        return "Shows the performance guard state";
    }

    @Override
    public String usage() {
        return "/momentcraft performance";
    }

    @Override
    public String permission() {
        return "momentcraft.admin.reload";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        double tps = Math.min(Bukkit.getServer().getTPS()[0], 20.0);
        var state = plugin.getPerformanceGuard().state();

        String color = switch (state) {
            case NORMAL -> "green";
            case THROTTLED -> "yellow";
            case PAUSED -> "red";
        };

        Messages.raw(sender, Messages.DIVIDER);
        Messages.raw(sender, " <gray>State</gray>  <" + color + "><bold><state></bold></" + color + ">",
            Messages.ph("state", state));
        Messages.raw(sender, " <gray>TPS</gray>    <white><tps></white>",
            Messages.ph("tps", String.format("%.1f", tps)));
        Messages.raw(sender, Messages.DIVIDER);
    }
}
