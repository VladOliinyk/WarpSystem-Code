package de.codingair.warpsystem.spigot.base.listeners;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import de.codingair.codingapi.server.AsyncCatcher;
import de.codingair.codingapi.server.events.PlayerWalkEvent;
import de.codingair.codingapi.tools.Location;
import de.codingair.warpsystem.spigot.base.WarpSystem;
import de.codingair.warpsystem.spigot.base.language.Lang;
import de.codingair.warpsystem.spigot.base.managers.TeleportManager;
import de.codingair.warpsystem.spigot.base.utils.teleport.TeleportOptions;
import de.codingair.warpsystem.spigot.base.utils.teleport.destinations.Destination;
import de.codingair.warpsystem.spigot.base.utils.teleport.destinations.adapters.EmptyAdapter;
import de.codingair.warpsystem.spigot.base.utils.teleport.v2.Teleport;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class TeleportListener implements Listener {
    public static final HashMap<Player, org.bukkit.Location> TELEPORTS = new HashMap<>();
    private static final Cache<String, TeleportOptions> teleport = CacheBuilder.newBuilder().expireAfterWrite(10, TimeUnit.SECONDS).build();

    public static void setSpawnPositionOrTeleport(String name, TeleportOptions options) {
        if(options == null) return;
        options.setSkip(true);
        Player player = Bukkit.getPlayer(name);

        options.setSkip(true);

        if(player != null && player.isOnline()) {
            //teleport
            AsyncCatcher.runSync(WarpSystem.getInstance(), () -> WarpSystem.getInstance().getTeleportManager().teleport(player, options));
        } else {
            teleport.put(name.toLowerCase(), options);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onTeleport(PlayerTeleportEvent e) {
        org.bukkit.Location loc = TELEPORTS.remove(e.getPlayer());

        if(loc != null) {
            e.setCancelled(false);
            e.setTo(loc);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onSpawn(PlayerJoinEvent e) {
        TeleportOptions options = teleport.getIfPresent(e.getPlayer().getName().toLowerCase());

        if(options != null) {
            teleport.invalidate(e.getPlayer().getName().toLowerCase());

            options.setCanMove(true);
            options.setSilent(true);
            options.setSkip(true);

            Bukkit.getScheduler().runTaskLater(WarpSystem.getInstance(), () -> WarpSystem.getInstance().getTeleportManager().teleport(e.getPlayer(), options), 2L);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSpawn(PlayerSpawnLocationEvent e) {
        TeleportOptions options = teleport.getIfPresent(e.getPlayer().getName().toLowerCase());

        if(options != null) {
            teleport.invalidate(e.getPlayer().getName().toLowerCase());
            org.bukkit.Location l = options.buildLocation();

            if(l.getYaw() == -420 && l.getPitch() == -420) {
                org.bukkit.Location p = e.getPlayer().getLocation();
                l.setYaw(p.getYaw());
                l.setPitch(p.getPitch());
            }

            if(l == null || l.getWorld() == null) {
                String world = l instanceof Location ? ((Location) l).getWorldName() : null;
                Bukkit.getScheduler().runTaskLater(WarpSystem.getInstance(), () -> e.getPlayer().sendMessage(new String[] {" ", Lang.getPrefix() + "§4World " + (world == null ? "" : "'" + world + "' ") + "is missing. Please contact an admin!", " "}), 2L);
                return;
            }

            e.setSpawnLocation(l);

            options.setCanMove(true);
            options.setSilent(true);
            options.setSkip(true);
            options.setDestination(new Destination(new EmptyAdapter()));

            Bukkit.getScheduler().runTaskLater(WarpSystem.getInstance(), () -> WarpSystem.getInstance().getTeleportManager().teleport(e.getPlayer(), options), 2L);
        }
    }

    @EventHandler
    public void onMove(PlayerWalkEvent e) {
        Player p = e.getPlayer();

        Teleport t = TeleportManager.getInstance().getTeleport(p);
        if(t == null || t.isCanMove()) return;

        double diff = Math.abs(e.getFrom().getX() - e.getTo().getX()) + Math.abs(e.getFrom().getZ() - e.getTo().getZ());
        double diffY = Math.abs(e.getFrom().getY() - e.getTo().getY());

        if(diff > 0.01 || diffY >= 0.11) WarpSystem.getInstance().getTeleportManager().cancelTeleport(p);
    }
}
