package de.codingair.warpsystem.bungee.features.teleport.listeners;

import de.codingair.warpsystem.bungee.api.Players;
import de.codingair.warpsystem.bungee.base.WarpSystem;
import de.codingair.warpsystem.bungee.features.teleport.managers.TeleportManager;
import de.codingair.warpsystem.bungee.features.teleport.utils.TeleportCommandOptions;
import de.codingair.warpsystem.base.features.TeleportTabCompleteKeys;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.TabCompleteResponseEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class TabCompleterListener implements Listener {

    private void finish(TabCompleteResponseEvent e) {
        if(e.getSuggestions().isEmpty()) e.setCancelled(true); //important to avoid error message on client
    }

    @EventHandler
    public void onResponse(TabCompleteResponseEvent e) {
        if(e.getSuggestions().size() != 2) return;

        boolean tp = false, tpa = false, tpaHere = false, tpHere = false;
        if((tp = e.getSuggestions().remove(TeleportTabCompleteKeys.ID_TP)) || (tpa = e.getSuggestions().remove(TeleportTabCompleteKeys.ID_TPA)) || (tpaHere = e.getSuggestions().remove(TeleportTabCompleteKeys.ID_TPA_HERE)) || (tpHere = e.getSuggestions().remove(TeleportTabCompleteKeys.ID_TP_HERE))) {
            String cursor = e.getSuggestions().remove(0);
            if(cursor.length() >= 2) {
                if(cursor.charAt(0) == '"') cursor = cursor.substring(1);
                if(cursor.charAt(cursor.length() - 1) == '"') cursor = cursor.substring(0, cursor.length() - 1);
            }

            String[] args = cursor.split(" ");

            ProxiedPlayer receiver = (ProxiedPlayer) e.getReceiver();

            String last = args[args.length - 1];
            e.getSuggestions().clear();

            if(tp) {
                int deep = args.length - 1;

                if(cursor.endsWith(" ")) {
                    if(deep == 1 && Character.isDigit(args[1].charAt(0)) && Players.getPlayer(args[1]) == null) {
                        finish(e);
                        return;
                    }
                    if(deep == 0 || deep == 1) {
                        for(ServerInfo server : WarpSystem.proxy().getServers().values()) {
                            for(ProxiedPlayer player : server.getPlayers()) {
                                e.getSuggestions().add(player.getName());
                            }
                        }
                    }
                } else {
                    if(deep == 1 || deep == 2) {
                        for(ServerInfo server : WarpSystem.proxy().getServers().values()) {
                            for(ProxiedPlayer player : server.getPlayers()) {
                                if(!player.getName().toLowerCase().startsWith(last.toLowerCase())) continue;
                                e.getSuggestions().add(player.getName());
                            }
                        }
                    }
                }
            } else if(tpa) {
                for(ServerInfo server : WarpSystem.proxy().getServers().values()) {
                    for(ProxiedPlayer player : server.getPlayers()) {
                        if(player.getName().equals(receiver.getName())) continue;
                        if(!cursor.endsWith(" ") && !player.getName().toLowerCase().startsWith(last.toLowerCase())) continue;
                        if(!WarpSystem.getVanishManager().isVanished(player.getName())) e.getSuggestions().add(player.getName()); //check vanished player names
                    }
                }
            } else if(tpaHere) {
                for(ServerInfo server : WarpSystem.proxy().getServers().values()) {
                    for(ProxiedPlayer player : server.getPlayers()) {
                        if(player.getName().equals(receiver.getName())) continue;
                        if(!cursor.endsWith(" ") && !player.getName().toLowerCase().startsWith(last.toLowerCase())) continue;
                        if(!WarpSystem.getVanishManager().isVanished(player.getName())) e.getSuggestions().add(player.getName()); //check vanished player names
                    }
                }
            } else if(tpHere) {
                for(ServerInfo server : WarpSystem.proxy().getServers().values()) {
                    TeleportCommandOptions access = TeleportManager.getInstance().getOptions(server);
                    if(access != null && access.isTp()) {
                        for(ProxiedPlayer player : server.getPlayers()) {
                            if(!cursor.endsWith(" ") && !player.getName().toLowerCase().startsWith(last.toLowerCase())) continue;
                            e.getSuggestions().add(player.getName());
                        }
                    }
                }
            }
            finish(e);
        }
    }
}
