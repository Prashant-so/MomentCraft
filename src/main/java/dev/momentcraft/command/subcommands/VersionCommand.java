package dev.momentcraft.command.subcommands;

import dev.momentcraft.command.SubCommand;
import dev.momentcraft.plugin.MomentCraftPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;

public final class VersionCommand implements SubCommand {

    private final MomentCraftPlugin plugin;

    public VersionCommand(MomentCraftPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public String name() {
        return "version";
    }

    @Override
    public String description() {
        return "Shows the running MomentCraft version.";
    }

    @Override
    public String usage() {
        return "/momentcraft version";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        String version = plugin.getPluginMeta().getVersion();
        sender.sendMessage(Component.text("MomentCraft v" + version, NamedTextColor.AQUA));
    }
}
