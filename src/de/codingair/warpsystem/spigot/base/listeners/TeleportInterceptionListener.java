package de.codingair.warpsystem.spigot.base.listeners;

import de.codingair.warpsystem.spigot.base.WarpSystem;
import de.codingair.warpsystem.spigot.base.managers.TeleportManager;
import de.codingair.warpsystem.spigot.base.utils.teleport.Origin;
import de.codingair.warpsystem.spigot.base.utils.teleport.TeleportOptions;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;

public class TeleportInterceptionListener implements Listener {

    @EventHandler(priority = EventPriority.HIGH)
    public void onTeleport(PlayerTeleportEvent e) {
        if(!e.getPlayer().isOnline()) return; //catch fake players
        if(!WarpSystem.opt().isTeleportInterceptions() || TeleportManager.getInstance().isTeleporting(e.getPlayer())) return;

        e.setCancelled(true);
        Location to = e.getTo();

        TeleportOptions options = new TeleportOptions(to, null);
        options.setMessage(null);
        options.setOrigin(Origin.TeleportInterception);

        TeleportManager.getInstance().teleport(e.getPlayer(), options);
    }

}
