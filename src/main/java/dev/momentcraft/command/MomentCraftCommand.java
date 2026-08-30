package dev.momentcraft.command;

import dev.momentcraft.command.subcommands.PerformanceCommand;
import dev.momentcraft.command.subcommands.ReloadCommand;
import dev.momentcraft.command.subcommands.VersionCommand;
import dev.momentcraft.command.subcommands.WandCommand;
import dev.momentcraft.command.subcommands.ZoneCommand;
import dev.momentcraft.plugin.MomentCraftPlugin;
import dev.momentcraft.util.Messages;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class MomentCraftCommand implements CommandExecutor, TabCompleter {

    private final Map<String, SubCommand> subCommands = new LinkedHashMap<>();

    public MomentCraftCommand(MomentCraftPlugin plugin) {
        register(new ReloadCommand(plugin));
        register(new VersionCommand(plugin));
        register(new WandCommand(plugin));
        register(new ZoneCommand(plugin));
        register(new PerformanceCommand(plugin));
    }

    private void register(SubCommand subCommand) {
        subCommands.put(subCommand.name().toLowerCase(), subCommand);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        SubCommand sub = subCommands.get(args[0].toLowerCase());
        if (sub == null) {
            Messages.error(sender, "Unknown subcommand. Run <white>/momentcraft</white> for a list.");
            return true;
        }

        if (sub.permission() != null && !sender.hasPermission(sub.permission())) {
            Messages.error(sender, "You don't have permission to do that.");
            return true;
        }

        String[] rest = args.length > 1 ? Arrays.copyOfRange(args, 1, args.length) : new String[0];
        sub.execute(sender, rest);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            String prefix = args[0].toLowerCase();
            return subCommands.keySet().stream()
                .filter(name -> name.startsWith(prefix))
                .collect(Collectors.toList());
        }

        if (args.length > 1) {
            SubCommand sub = subCommands.get(args[0].toLowerCase());
            if (sub != null) {
                String[] rest = Arrays.copyOfRange(args, 1, args.length);
                return sub.tabComplete(sender, rest);
            }
        }

        return List.of();
    }

    private void sendHelp(CommandSender sender) {
        Messages.raw(sender, Messages.DIVIDER);
        Messages.raw(sender, "<gradient:gold:yellow><bold>              MomentCraft</bold></gradient>");
        Messages.raw(sender, Messages.DIVIDER);

        for (SubCommand sub : subCommands.values()) {
            String usage = sub.usage();
            String padded = usage + " ".repeat(Math.max(1, 34 - usage.length()));
            Messages.raw(sender,
                "<yellow> ▸ </yellow><white>" + padded + "</white><gray>" + sub.description() + "</gray>");
        }

        Messages.raw(sender, Messages.DIVIDER);
    }
}
