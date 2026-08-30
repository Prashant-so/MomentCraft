package dev.momentcraft.plugin;

import dev.momentcraft.capture.CaptureManager;
import dev.momentcraft.command.MomentCraftCommand;
import dev.momentcraft.config.ConfigManager;
import dev.momentcraft.performance.PerformanceGuard;
import dev.momentcraft.zone.SelectionManager;
import dev.momentcraft.zone.WandListener;
import dev.momentcraft.zone.ZoneManager;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;

public final class MomentCraftPlugin extends JavaPlugin {

    private static MomentCraftPlugin instance;

    private ConfigManager configManager;
    private ZoneManager zoneManager;
    private SelectionManager selectionManager;
    private CaptureManager captureManager;
    private PerformanceGuard performanceGuard;
    private NamespacedKey wandKey;

    @Override
    public void onEnable() {
        instance = this;

        printBanner();

        configManager = new ConfigManager(this);
        configManager.load();

        wandKey = new NamespacedKey(this, "wand");
        selectionManager = new SelectionManager();
        zoneManager = new ZoneManager(this);
        zoneManager.loadAll();

        performanceGuard = new PerformanceGuard(this);
        performanceGuard.start();

        captureManager = new CaptureManager(this);
        captureManager.start();

        getServer().getPluginManager().registerEvents(new WandListener(selectionManager, wandKey), this);

        registerCommands();

        getComponentLogger().info(MiniMessage.miniMessage().deserialize(
            "<green>MomentCraft enabled.</green> <gray>(v<version>)</gray>",
            net.kyori.adventure.text.minimessage.tag.resolver.Placeholder.unparsed(
                "version", getPluginMeta().getVersion())
        ));
    }

    private void printBanner() {
        MiniMessage mm = MiniMessage.miniMessage();
        getComponentLogger().info(mm.deserialize("<gradient:gold:yellow>▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬</gradient>"));
        getComponentLogger().info(mm.deserialize("<gradient:gold:yellow><bold>            MomentCraft</bold></gradient>"));
        getComponentLogger().info(mm.deserialize("<gray>      cinematic moment detection, self-hosted</gray>"));
        getComponentLogger().info(mm.deserialize("<gradient:gold:yellow>▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬</gradient>"));
    }

    @Override
    public void onDisable() {
        if (captureManager != null) {
            captureManager.stop();
        }
        if (performanceGuard != null) {
            performanceGuard.stop();
        }
        getComponentLogger().info(MiniMessage.miniMessage().deserialize("<red>MomentCraft disabled.</red>"));
        instance = null;
    }

    private void registerCommands() {
        MomentCraftCommand command = new MomentCraftCommand(this);
        var pluginCommand = getCommand("momentcraft");
        if (pluginCommand != null) {
            pluginCommand.setExecutor(command);
            pluginCommand.setTabCompleter(command);
        } else {
            getLogger().severe("Failed to register /momentcraft — check plugin.yml.");
        }
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public ZoneManager getZoneManager() {
        return zoneManager;
    }

    public SelectionManager getSelectionManager() {
        return selectionManager;
    }

    public CaptureManager getCaptureManager() {
        return captureManager;
    }

    public PerformanceGuard getPerformanceGuard() {
        return performanceGuard;
    }

    public NamespacedKey getWandKey() {
        return wandKey;
    }

    public static MomentCraftPlugin get() {
        return instance;
    }
}
