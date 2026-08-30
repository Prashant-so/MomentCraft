package dev.momentcraft.command.subcommands;

import dev.momentcraft.command.SubCommand;
import dev.momentcraft.plugin.MomentCraftPlugin;
import dev.momentcraft.util.Messages;
import dev.momentcraft.zone.SelectionManager;
import dev.momentcraft.zone.Zone;
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
        return "Manages capture zones";
    }

    @Override
    public String usage() {
        return "/momentcraft zone <action> [name]";
    }

    @Override
    public String permission() {
        return "momentcraft.admin.zone";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            Messages.error(sender, "Usage: <white><usage></white>", Messages.ph("usage", usage()));
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
            default -> Messages.error(sender, "Unknown action. Usage: <white><usage></white>", Messages.ph("usage", usage()));
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
            Messages.error(sender, "Only players can create zones — a physical selection is required.");
            return;
        }

        if (args.length < 1) {
            Messages.error(sender, "Usage: <white>/momentcraft zone create <name></white>");
            return;
        }

        String id = args[0].toLowerCase();
        if (plugin.getZoneManager().get(id).isPresent()) {
            Messages.error(sender, "A zone named <white><name></white> already exists.", Messages.ph("name", id));
            return;
        }

        SelectionManager selection = plugin.getSelectionManager();
        UUID playerId = player.getUniqueId();

        if (!selection.hasCompleteSelection(playerId)) {
            Messages.error(sender, "Set both corners with the wand first. Run <white>/mc wand</white>.");
            return;
        }

        Location c1 = selection.getCorner1(playerId);
        Location c2 = selection.getCorner2(playerId);

        if (!c1.getWorld().equals(c2.getWorld())) {
            Messages.error(sender, "Both corners must be in the same world.");
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

        Messages.success(sender, "Zone <white><name></white> created.", Messages.ph("name", id));
    }

    private void delete(CommandSender sender, String[] args) {
        if (args.length < 1) {
            Messages.error(sender, "Usage: <white>/momentcraft zone delete <name></white>");
            return;
        }

        String id = args[0].toLowerCase();
        boolean removed = plugin.getZoneManager().delete(id);
        if (removed) {
            Messages.success(sender, "Zone <white><name></white> deleted.", Messages.ph("name", id));
        } else {
            Messages.error(sender, "No zone named <white><name></white> exists.", Messages.ph("name", id));
        }
    }

    private void list(CommandSender sender) {
        var zones = plugin.getZoneManager().all();

        Messages.raw(sender, Messages.DIVIDER);
        Messages.raw(sender, "<gradient:gold:yellow><bold>              Capture Zones</bold></gradient>");
        Messages.raw(sender, Messages.DIVIDER);

        if (zones.isEmpty()) {
            Messages.raw(sender, " <gray><italic>No zones defined yet.</italic></gray>");
        } else {
            for (Zone zone : zones) {
                String dot = zone.enabled() ? "<green>●</green>" : "<dark_gray>●</dark_gray>";
                Messages.raw(sender, " " + dot + " <white><name></white> <gray>(<world>)</gray>",
                    Messages.ph("name", zone.id()), Messages.ph("world", zone.world()));
            }
        }

        Messages.raw(sender, Messages.DIVIDER);
    }

    private void info(CommandSender sender, String[] args) {
        if (args.length < 1) {
            Messages.error(sender, "Usage: <white>/momentcraft zone info <name></white>");
            return;
        }

        plugin.getZoneManager().get(args[0].toLowerCase()).ifPresentOrElse(zone -> {
            String statusColor = zone.enabled() ? "green" : "red";
            String statusText = zone.enabled() ? "Enabled" : "Disabled";

            Messages.raw(sender, Messages.DIVIDER);
            Messages.raw(sender, " <gradient:gold:yellow><bold><name></bold></gradient>", Messages.ph("name", zone.id()));
            Messages.raw(sender, Messages.DIVIDER);
            Messages.raw(sender, " <gray>World</gray>   <white><world></white>", Messages.ph("world", zone.world()));
            Messages.raw(sender, " <gray>Min</gray>     <white><min></white>",
                Messages.ph("min", zone.minX() + ", " + zone.minY() + ", " + zone.minZ()));
            Messages.raw(sender, " <gray>Max</gray>     <white><max></white>",
                Messages.ph("max", zone.maxX() + ", " + zone.maxY() + ", " + zone.maxZ()));
            Messages.raw(sender, " <gray>Status</gray>  <" + statusColor + "><status></" + statusColor + ">",
                Messages.ph("status", statusText));
            Messages.raw(sender, Messages.DIVIDER);
        }, () -> Messages.error(sender, "No zone named <white><name></white> exists.", Messages.ph("name", args[0])));
    }

    private void setEnabled(CommandSender sender, String[] args, boolean enabled) {
        if (args.length < 1) {
            Messages.error(sender, "Usage: <white>/momentcraft zone " + (enabled ? "enable" : "disable") + " <name></white>");
            return;
        }

        plugin.getZoneManager().get(args[0].toLowerCase()).ifPresentOrElse(zone -> {
            zone.setEnabled(enabled);
            plugin.getZoneManager().save(zone);
            String verb = enabled ? "enabled" : "disabled";
            Messages.success(sender, "Zone <white><name></white> " + verb + ".", Messages.ph("name", zone.id()));
        }, () -> Messages.error(sender, "No zone named <white><name></white> exists.", Messages.ph("name", args[0])));
    }

    private void buffer(CommandSender sender, String[] args) {
        if (args.length < 1) {
            Messages.error(sender, "Usage: <white>/momentcraft zone buffer <name></white>");
            return;
        }

        String id = args[0].toLowerCase();
        if (plugin.getZoneManager().get(id).isEmpty()) {
            Messages.error(sender, "No zone named <white><name></white> exists.", Messages.ph("name", id));
            return;
        }

        int size = plugin.getCaptureManager().bufferSize(id);
        Messages.info(sender, "Zone <white><name></white> buffer: <aqua><count></aqua> snapshot(s).",
            Messages.ph("name", id), Messages.ph("count", size));
    }
}
