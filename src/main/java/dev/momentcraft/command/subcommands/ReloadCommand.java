package dev.momentcraft.command.subcommands;

import dev.momentcraft.command.SubCommand;
import dev.momentcraft.plugin.MomentCraftPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;

public final class ReloadCommand implements SubCommand {

    private final MomentCraftPlugin plugin;

    public ReloadCommand(MomentCraftPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public String name() {
        return "reload";
    }

    @Override
    public String description() {
        return "Reloads the MomentCraft configuration.";
    }

    @Override
    public String usage() {
        return "/momentcraft reload";
    }

    @Override
    public String permission() {
        return "momentcraft.admin.reload";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        plugin.getConfigManager().reload();
        sender.sendMessage(Component.text("MomentCraft configuration reloaded.", NamedTextColor.GREEN));
    }
}
