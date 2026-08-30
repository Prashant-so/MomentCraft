package dev.momentcraft.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.command.CommandSender;

public final class Messages {

    private static final MiniMessage MM = MiniMessage.miniMessage();

    private static final String PREFIX =
        "<gradient:gold:yellow><bold>MomentCraft</bold></gradient><dark_gray> » </dark_gray>";

    public static final String DIVIDER =
        "<gradient:gold:yellow:gold>▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬</gradient>";

    private Messages() {
    }

    private static Component parse(String template, TagResolver... resolvers) {
        return MM.deserialize(template, resolvers);
    }

    /** Sends a message with the MomentCraft prefix attached. */
    public static void send(CommandSender sender, String template, TagResolver... resolvers) {
        sender.sendMessage(parse(PREFIX + template, resolvers));
    }

    /** Sends a message with no prefix — used for menus, dividers, list rows. */
    public static void raw(CommandSender sender, String template, TagResolver... resolvers) {
        sender.sendMessage(parse(template, resolvers));
    }

    public static void success(CommandSender sender, String message, TagResolver... resolvers) {
        send(sender, "<green>" + message + "</green>", resolvers);
    }

    public static void error(CommandSender sender, String message, TagResolver... resolvers) {
        send(sender, "<red>" + message + "</red>", resolvers);
    }

    public static void warn(CommandSender sender, String message, TagResolver... resolvers) {
        send(sender, "<yellow>" + message + "</yellow>", resolvers);
    }

    public static void info(CommandSender sender, String message, TagResolver... resolvers) {
        send(sender, "<gray>" + message + "</gray>", resolvers);
    }

    /** Safe placeholder for dynamic values (zone names, coordinates, etc.) — prevents tag injection from user input. */
    public static TagResolver ph(String key, Object value) {
        return Placeholder.unparsed(key, String.valueOf(value));
    }
}
