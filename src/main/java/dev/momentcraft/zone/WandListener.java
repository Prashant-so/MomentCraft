package dev.momentcraft.zone;

import dev.momentcraft.util.Messages;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public final class WandListener implements Listener {

    private final SelectionManager selectionManager;
    private final NamespacedKey wandKey;

    public WandListener(SelectionManager selectionManager, NamespacedKey wandKey) {
        this.selectionManager = selectionManager;
        this.wandKey = wandKey;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        ItemStack item = event.getItem();
        if (item == null || !isWand(item)) {
            return;
        }

        Block clicked = event.getClickedBlock();
        if (clicked == null) {
            return;
        }

        Player player = event.getPlayer();

        switch (event.getAction()) {
            case LEFT_CLICK_BLOCK -> {
                selectionManager.setCorner1(player.getUniqueId(), clicked.getLocation());
                Messages.send(player, "<yellow>Corner 1</yellow> <gray>set at</gray> <white><coords></white>",
                    Messages.ph("coords", format(clicked.getLocation())));
                event.setCancelled(true);
            }
            case RIGHT_CLICK_BLOCK -> {
                selectionManager.setCorner2(player.getUniqueId(), clicked.getLocation());
                Messages.send(player, "<yellow>Corner 2</yellow> <gray>set at</gray> <white><coords></white>",
                    Messages.ph("coords", format(clicked.getLocation())));
                event.setCancelled(true);
            }
            default -> {
            }
        }
    }

    private boolean isWand(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return false;
        }
        return Boolean.TRUE.equals(meta.getPersistentDataContainer().get(wandKey, PersistentDataType.BOOLEAN));
    }

    private String format(Location loc) {
        return loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ();
    }
}
