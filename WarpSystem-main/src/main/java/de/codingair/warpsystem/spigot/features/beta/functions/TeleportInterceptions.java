package de.codingair.warpsystem.spigot.features.beta.functions;

import de.codingair.warpsystem.spigot.base.WarpSystem;
import de.codingair.warpsystem.spigot.base.listeners.TeleportInterceptionListener;
import org.bukkit.Bukkit;

public class TeleportInterceptions implements Beta {
    @Override
    public boolean load(boolean loader) {
        Bukkit.getPluginManager().registerEvents(new TeleportInterceptionListener(), WarpSystem.getInstance());
        return true;
    }

    @Override
    public void save(boolean saver) {
    }

    @Override
    public void destroy() {
        //onDisable() automatically removes all listeners from WarpSystem -> ignore this
    }

    @Override
    public String getFinalConfigTag() {
        return "WarpSystem.Teleport.Teleport_Interceptions";
    }

    @Override
    public boolean active() {
        return true;
    }
}
