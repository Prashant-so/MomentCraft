package dev.momentcraft.command.subcommands;

import dev.momentcraft.command.SubCommand;
import dev.momentcraft.plugin.MomentCraftPlugin;
import dev.momentcraft.util.Messages;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

public final class WandCommand implements SubCommand {

    private final MomentCraftPlugin plugin;

    public WandCommand(MomentCraftPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public String name() {
        return "wand";
    }

    @Override
    public String description() {
        return "Gives the zone selection tool";
    }

    @Override
    public String usage() {
        return "/momentcraft wand";
    }

    @Override
    public String permission() {
        return "momentcraft.admin.zone";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            Messages.error(sender, "Only players can use the wand.");
            return;
        }

        ItemStack wand = new ItemStack(plugin.getConfigManager().getWandMaterial());
        ItemMeta meta = wand.getItemMeta();

        meta.displayName(Component.text("✦ MomentCraft Wand", NamedTextColor.GOLD)
            .decoration(TextDecoration.ITALIC, false)
            .decoration(TextDecoration.BOLD, true));
        meta.lore(List.of(
            Component.empty(),
            Component.text("Left click", NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false)
                .append(Component.text("  →  corner 1", NamedTextColor.GRAY)),
            Component.text("Right click", NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false)
                .append(Component.text("  →  corner 2", NamedTextColor.GRAY))
        ));
        meta.getPersistentDataContainer().set(plugin.getWandKey(), PersistentDataType.BOOLEAN, true);

        wand.setItemMeta(meta);

        player.getInventory().addItem(wand);
        Messages.success(sender, "You received the zone selection wand.");
    }
}
