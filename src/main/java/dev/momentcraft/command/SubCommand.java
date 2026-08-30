package dev.momentcraft.command;

import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public interface SubCommand {

    String name();

    String description();

    String usage();

    /** Permission required to run this subcommand, or null if none is required. */
    default String permission() {
        return null;
    }

    void execute(CommandSender sender, String[] args);

    default List<String> tabComplete(CommandSender sender, String[] args) {
        return Collections.emptyList();
    }
}
