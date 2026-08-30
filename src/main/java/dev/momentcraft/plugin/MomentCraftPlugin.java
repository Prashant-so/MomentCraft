package dev.momentcraft.plugin;

import org.bukkit.plugin.java.JavaPlugin;

public final class MomentCraftPlugin extends JavaPlugin {

    private static MomentCraftPlugin instance;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();

        getLogger().info("MomentCraft enabled.");
    }

    @Override
    public void onDisable() {
        getLogger().info("MomentCraft disabled.");
        instance = null;
    }

    public static MomentCraftPlugin get() {
        return instance;
    }
}
