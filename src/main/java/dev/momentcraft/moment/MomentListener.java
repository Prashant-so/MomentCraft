package dev.momentcraft.moment;

import dev.momentcraft.plugin.MomentCraftPlugin;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wither;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;

import java.util.UUID;

public final class MomentListener implements Listener {

    private final MomentCraftPlugin plugin;

    public MomentListener(MomentCraftPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity victim = event.getEntity();
        Player killer = victim.getKiller();

        if (victim instanceof Player playerVictim) {
            plugin.getMomentManager().killstreakTracker().onDeath(playerVictim.getUniqueId());
            plugin.getMomentManager().submit(buildEvent(
                MomentType.PLAYER_DEATH, playerVictim, playerVictim.getLocation(), 0
            ));
        }

        if (killer == null) {
            return;
        }

        boolean isBoss = victim instanceof Wither || victim instanceof EnderDragon;
        MomentType type = isBoss ? MomentType.BOSS_KILL : MomentType.PLAYER_KILL;

        // Only score PLAYER_KILL when the victim was actually a player — a
        // regular mob kill isn't a "moment" on its own, boss kills always are.
        if (type == MomentType.PLAYER_KILL && !(victim instanceof Player)) {
            return;
        }

        int killstreak = plugin.getMomentManager().killstreakTracker().onKill(killer.getUniqueId());
        plugin.getMomentManager().submit(buildEvent(type, killer, killer.getLocation(), killstreak));
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        boolean isExplosion = event.getCause() == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION
            || event.getCause() == EntityDamageEvent.DamageCause.BLOCK_EXPLOSION;

        if (!isExplosion) {
            return;
        }

        double healthAfter = player.getHealth() - event.getFinalDamage();
        boolean survived = healthAfter > 0;
        boolean wasCloseCall = event.getFinalDamage() >= (player.getHealth() * 0.5);

        if (!survived || !wasCloseCall) {
            return;
        }

        plugin.getMomentManager().submit(buildEvent(
            MomentType.EXPLOSION_SURVIVED, player, player.getLocation(), 0
        ));
    }

    private MomentEvent buildEvent(MomentType type, Player primary, Location loc, int killstreak) {
        double healthFraction = clamp(primary.getHealth() / getMaxHealth(primary));
        int nearby = countNearbyPlayers(primary, loc);
        boolean dangerous = isDangerousEnvironment(primary.getWorld(), loc);

        return new MomentEvent(
            type,
            loc.getWorld().getName(),
            loc.getX(), loc.getY(), loc.getZ(),
            primary.getUniqueId(),
            primary.getName(),
            healthFraction,
            killstreak,
            nearby,
            dangerous,
            System.currentTimeMillis()
        );
    }

    private double getMaxHealth(Player player) {
        var attribute = player.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH);
        return attribute != null ? attribute.getValue() : 20.0;
    }

    private double clamp(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }

    private int countNearbyPlayers(Player exclude, Location loc) {
        double radius = plugin.getConfigManager().getScoreNearbyPlayerRadius();
        int count = 0;
        for (Player other : loc.getWorld().getPlayers()) {
            if (other.getUniqueId().equals(exclude.getUniqueId())) {
                continue;
            }
            if (other.getLocation().distanceSquared(loc) <= radius * radius) {
                count++;
            }
        }
        return count;
    }

    private boolean isDangerousEnvironment(World world, Location loc) {
        if (world.getEnvironment() == World.Environment.NETHER
            || world.getEnvironment() == World.Environment.THE_END) {
            return true;
        }

        return loc.getBlock().getType().name().contains("LAVA")
            || loc.getBlock().getRelative(0, -1, 0).getType().name().contains("LAVA");
    }
}
