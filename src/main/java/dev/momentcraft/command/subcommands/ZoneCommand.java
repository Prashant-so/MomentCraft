package dev.momentcraft.command.subcommands;

import dev.momentcraft.command.SubCommand;
import dev.momentcraft.plugin.MomentCraftPlugin;
import dev.momentcraft.zone.SelectionManager;
import dev.momentcraft.zone.Zone;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public final class ZoneCommand implements SubCommand {

    private final MomentCraftPlugin plugin;

    public ZoneCommand(MomentCraftPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public String name() {
        return "zone";
    }

    @Override
    public String description() {
        return "Manages manual capture zones.";
    }

    @Override
    public String usage() {
        return "/momentcraft zone <create|delete|list|info|enable|disable|buffer> [name]";
    }

    @Override
    public String permission() {
        return "momentcraft.admin.zone";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(Component.text("Usage: " + usage(), NamedTextColor.RED));
            return;
        }

        String action = args[0].toLowerCase();
        String[] rest = args.length > 1 ? Arrays.copyOfRange(args, 1, args.length) : new String[0];

        switch (action) {
            case "create" -> create(sender, rest);
            case "delete" -> delete(sender, rest);
            case "list" -> list(sender);
            case "info" -> info(sender, rest);
            case "enable" -> setEnabled(sender, rest, true);
            case "disable" -> setEnabled(sender, rest, false);
            case "buffer" -> buffer(sender, rest);
            default -> sender.sendMessage(Component.text("Unknown zone action. Usage: " + usage(), NamedTextColor.RED));
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return List.of("create", "delete", "list", "info", "enable", "disable", "buffer").stream()
                .filter(a -> a.startsWith(args[0].toLowerCase()))
                .toList();
        }

        if (args.length == 2 && !args[0].equalsIgnoreCase("create")) {
            return plugin.getZoneManager().all().stream()
                .map(Zone::id)
                .filter(id -> id.startsWith(args[1].toLowerCase()))
                .toList();
        }

        return List.of();
    }

    private void create(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Only players can create zones (a selection is required).", NamedTextColor.RED));
            return;
        }

        if (args.length < 1) {
            sender.sendMessage(Component.text("Usage: /momentcraft zone create <name>", NamedTextColor.RED));
            return;
        }

        String id = args[0].toLowerCase();
        if (plugin.getZoneManager().get(id).isPresent()) {
            sender.sendMessage(Component.text("A zone named '" + id + "' already exists.", NamedTextColor.RED));
            return;
        }

        SelectionManager selection = plugin.getSelectionManager();
        UUID playerId = player.getUniqueId();

        if (!selection.hasCompleteSelection(playerId)) {
            sender.sendMessage(Component.text(
                "You need to set both corners with the wand first. Use /momentcraft wand.", NamedTextColor.RED));
            return;
        }

        Location c1 = selection.getCorner1(playerId);
        Location c2 = selection.getCorner2(playerId);

        if (!c1.getWorld().equals(c2.getWorld())) {
            sender.sendMessage(Component.text("Both corners must be in the same world.", NamedTextColor.RED));
            return;
        }

        Zone zone = new Zone(
            id,
            c1.getWorld().getName(),
            Math.min(c1.getBlockX(), c2.getBlockX()),
            Math.min(c1.getBlockY(), c2.getBlockY()),
            Math.min(c1.getBlockZ(), c2.getBlockZ()),
            Math.max(c1.getBlockX(), c2.getBlockX()),
            Math.max(c1.getBlockY(), c2.getBlockY()),
            Math.max(c1.getBlockZ(), c2.getBlockZ()),
            true
        );

        plugin.getZoneManager().create(zone);
        selection.clear(playerId);

        sender.sendMessage(Component.text("Zone '" + id + "' created.", NamedTextColor.GREEN));
    }

    private void delete(CommandSender sender, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(Component.text("Usage: /momentcraft zone delete <name>", NamedTextColor.RED));
            return;
        }

        boolean removed = plugin.getZoneManager().delete(args[0].toLowerCase());
        if (removed) {
            sender.sendMessage(Component.text("Zone '" + args[0] + "' deleted.", NamedTextColor.GREEN));
        } else {
            sender.sendMessage(Component.text("No zone named '" + args[0] + "' exists.", NamedTextColor.RED));
        }
    }

    private void list(CommandSender sender) {
        var zones = plugin.getZoneManager().all();
        if (zones.isEmpty()) {
            sender.sendMessage(Component.text("No zones defined.", NamedTextColor.GRAY));
            return;
        }

        sender.sendMessage(Component.text("Zones:", NamedTextColor.GOLD));
        for (Zone zone : zones) {
            NamedTextColor color = zone.enabled() ? NamedTextColor.GREEN : NamedTextColor.GRAY;
            sender.sendMessage(Component.text("- " + zone.id() + " (" + zone.world() + ")", color));
        }
    }

    private void info(CommandSender sender, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(Component.text("Usage: /momentcraft zone info <name>", NamedTextColor.RED));
            return;
        }

        plugin.getZoneManager().get(args[0].toLowerCase()).ifPresentOrElse(zone -> {
            sender.sendMessage(Component.text("Zone: " + zone.id(), NamedTextColor.GOLD));
            sender.sendMessage(Component.text("World: " + zone.world(), NamedTextColor.GRAY));
            sender.sendMessage(Component.text(
                "Min: " + zone.minX() + ", " + zone.minY() + ", " + zone.minZ(), NamedTextColor.GRAY));
            sender.sendMessage(Component.text(
                "Max: " + zone.maxX() + ", " + zone.maxY() + ", " + zone.maxZ(), NamedTextColor.GRAY));
            sender.sendMessage(Component.text(
                "Enabled: " + zone.enabled(), zone.enabled() ? NamedTextColor.GREEN : NamedTextColor.RED));
        }, () -> sender.sendMessage(Component.text("No zone named '" + args[0] + "' exists.", NamedTextColor.RED)));
    }

    private void setEnabled(CommandSender sender, String[] args, boolean enabled) {
        if (args.length < 1) {
            sender.sendMessage(Component.text(
                "Usage: /momentcraft zone " + (enabled ? "enable" : "disable") + " <name>", NamedTextColor.RED));
            return;
        }

        plugin.getZoneManager().get(args[0].toLowerCase()).ifPresentOrElse(zone -> {
            zone.setEnabled(enabled);
            plugin.getZoneManager().save(zone);
            sender.sendMessage(Component.text(
                "Zone '" + zone.id() + "' " + (enabled ? "enabled" : "disabled") + ".", NamedTextColor.GREEN));
        }, () -> sender.sendMessage(Component.text("No zone named '" + args[0] + "' exists.", NamedTextColor.RED)));
    }

    private void buffer(CommandSender sender, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(Component.text("Usage: /momentcraft zone buffer <name>", NamedTextColor.RED));
            return;
        }

        String id = args[0].toLowerCase();
        if (plugin.getZoneManager().get(id).isEmpty()) {
            sender.sendMessage(Component.text("No zone named '" + id + "' exists.", NamedTextColor.RED));
            return;
        }

        int size = plugin.getCaptureManager().bufferSize(id);
        sender.sendMessage(Component.text(
            "Zone '" + id + "' buffer: " + size + " snapshot(s).", NamedTextColor.GRAY));
    }
}
