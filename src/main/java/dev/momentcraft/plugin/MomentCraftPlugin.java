package dev.momentcraft.plugin;

import dev.momentcraft.command.MomentCraftCommand;
import dev.momentcraft.config.ConfigManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class MomentCraftPlugin extends JavaPlugin {

    private static MomentCraftPlugin instance;

    private ConfigManager configManager;

    @Override
    public void onEnable() {
        instance = this;

        configManager = new ConfigManager(this);
        configManager.load();

        registerCommands();

        getLogger().info("MomentCraft enabled.");
    }

    @Override
    public void onDisable() {
        getLogger().info("MomentCraft disabled.");
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

    public static MomentCraftPlugin get() {
        return instance;
    }
}
