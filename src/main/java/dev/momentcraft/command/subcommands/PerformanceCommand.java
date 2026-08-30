package dev.momentcraft.command.subcommands;

import dev.momentcraft.command.SubCommand;
import dev.momentcraft.plugin.MomentCraftPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
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
        return "Shows the current performance guard state.";
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
        double tps = Bukkit.getServer().getTPS()[0];
        var state = plugin.getPerformanceGuard().state();

        NamedTextColor color = switch (state) {
            case NORMAL -> NamedTextColor.GREEN;
            case THROTTLED -> NamedTextColor.YELLOW;
            case PAUSED -> NamedTextColor.RED;
        };

        sender.sendMessage(Component.text("Performance state: " + state, color));
        sender.sendMessage(Component.text(
            "TPS: " + String.format("%.1f", Math.min(tps, 20.0)), NamedTextColor.GRAY));
    }
}
