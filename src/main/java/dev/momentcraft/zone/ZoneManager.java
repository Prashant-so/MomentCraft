package dev.momentcraft.zone;

import dev.momentcraft.plugin.MomentCraftPlugin;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public final class ZoneManager {

    private final MomentCraftPlugin plugin;
    private final File zonesFolder;
    private final Map<String, Zone> zones = new LinkedHashMap<>();

    public ZoneManager(MomentCraftPlugin plugin) {
        this.plugin = plugin;
        this.zonesFolder = new File(plugin.getDataFolder(), "zones");
    }

    public void loadAll() {
        zones.clear();

        if (!zonesFolder.exists()) {
            zonesFolder.mkdirs();
            return;
        }

        File[] files = zonesFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null) {
            return;
        }

        for (File file : files) {
            Zone zone = readZone(file);
            if (zone != null) {
                zones.put(zone.id(), zone);
            }
        }

        plugin.getLogger().info("Loaded " + zones.size() + " zone(s).");
    }

    private Zone readZone(File file) {
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);

        String id = yaml.getString("id");
        String world = yaml.getString("world");

        if (id == null || world == null) {
            plugin.getLogger().warning("Skipping invalid zone file: " + file.getName());
            return null;
        }

        int minX = yaml.getInt("bounds.min.x");
        int minY = yaml.getInt("bounds.min.y");
        int minZ = yaml.getInt("bounds.min.z");
        int maxX = yaml.getInt("bounds.max.x");
        int maxY = yaml.getInt("bounds.max.y");
        int maxZ = yaml.getInt("bounds.max.z");
        boolean enabled = yaml.getBoolean("enabled", true);

        return new Zone(id, world, minX, minY, minZ, maxX, maxY, maxZ, enabled);
    }

    public boolean create(Zone zone) {
        if (zones.containsKey(zone.id())) {
            return false;
        }

        zones.put(zone.id(), zone);
        save(zone);
        return true;
    }

    public boolean delete(String id) {
        Zone removed = zones.remove(id);
        if (removed == null) {
            return false;
        }

        File file = fileFor(id);
        if (file.exists()) {
            file.delete();
        }
        return true;
    }

    public Optional<Zone> get(String id) {
        return Optional.ofNullable(zones.get(id));
    }

    public Collection<Zone> all() {
        return zones.values();
    }

    public void save(Zone zone) {
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.set("id", zone.id());
        yaml.set("world", zone.world());
        yaml.set("enabled", zone.enabled());
        yaml.set("bounds.min.x", zone.minX());
        yaml.set("bounds.min.y", zone.minY());
        yaml.set("bounds.min.z", zone.minZ());
        yaml.set("bounds.max.x", zone.maxX());
        yaml.set("bounds.max.y", zone.maxY());
        yaml.set("bounds.max.z", zone.maxZ());

        try {
            if (!zonesFolder.exists()) {
                zonesFolder.mkdirs();
            }
            yaml.save(fileFor(zone.id()));
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save zone '" + zone.id() + "': " + e.getMessage());
        }
    }

    private File fileFor(String id) {
        return new File(zonesFolder, id + ".yml");
    }
}
