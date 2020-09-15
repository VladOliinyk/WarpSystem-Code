package de.codingair.warpsystem.spigot.api;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class PAPI {
    private static Boolean papi;

    public static boolean papi() {
        if(papi == null) papi = Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI");
        return papi;
    }

    public static String convert(String s, Player player) {
        if(s == null) return null;

        if(papi()) {
            return PlaceholderAPI.setPlaceholders(player, s);
        } else return s;
    }

    public static void register() {
        if(papi()) {
            //register here
        }
    }
}
