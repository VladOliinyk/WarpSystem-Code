package de.codingair.warpsystem.velocity.features.teleport.listeners;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.TabCompleteEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import de.codingair.warpsystem.base.features.TeleportTabCompleteKeys;
import de.codingair.warpsystem.velocity.api.Players;
import de.codingair.warpsystem.velocity.base.WarpSystem;
import de.codingair.warpsystem.velocity.features.teleport.managers.TeleportManager;
import de.codingair.warpsystem.velocity.features.teleport.utils.TeleportCommandOptions;

import java.util.Optional;

public class TabCompleterListener {

    @Subscribe
    public void onResponse(TabCompleteEvent e) {
        if(e.getSuggestions().size() != 2) return;

        boolean tp = false, tpa = false, tpaHere = false, tpHere = false;
        if((tp = e.getSuggestions().remove(TeleportTabCompleteKeys.ID_TP)) || (tpa = e.getSuggestions().remove(TeleportTabCompleteKeys.ID_TPA)) || (tpaHere = e.getSuggestions().remove(TeleportTabCompleteKeys.ID_TPA_HERE)) || (tpHere = e.getSuggestions().remove(TeleportTabCompleteKeys.ID_TP_HERE))) {
            String cursor = e.getSuggestions().remove(0);
            if(cursor.length() >= 2) {
                if(cursor.charAt(0) == '"') cursor = cursor.substring(1);
                if(cursor.charAt(cursor.length() - 1) == '"') cursor = cursor.substring(0, cursor.length() - 1);
            }

            String[] args = cursor.split(" ");

            Player receiver = e.getPlayer();
            Optional<ServerConnection> info = receiver.getCurrentServer();
            if(!info.isPresent()) return;
            RegisteredServer serverInfo = info.get().getServer();
            TeleportCommandOptions options = TeleportManager.getInstance().getOptions(serverInfo);

            String last = args[args.length - 1];

            if(tp) {
                if(options == null || !options.isTp()) {
                    for(Player player : serverInfo.getPlayersConnected()) {
                        if(!cursor.endsWith(" ") && !player.getUsername().toLowerCase().startsWith(last.toLowerCase())) continue;

                        e.getSuggestions().add(player.getUsername());
                    }
                    return;
                }

                int deep = args.length - 1;

                if(cursor.endsWith(" ")) {
                    if(deep == 1 && Character.isDigit(args[1].charAt(0)) && Players.getPlayer(args[1]) == null) return;
                    if(deep == 0 || deep == 1) {
                        for(RegisteredServer server : WarpSystem.proxy().getAllServers()) {
                            TeleportCommandOptions access = TeleportManager.getInstance().getOptions(server);
                            if(access != null && access.isTp()) {
                                for(Player player : server.getPlayersConnected()) {
                                    if(!cursor.endsWith(" ") && !player.getUsername().toLowerCase().startsWith(last.toLowerCase())) continue;
                                    e.getSuggestions().add(player.getUsername());
                                }
                            }
                        }
                    }
                } else {
                    if(deep == 1 || deep == 2) {
                        for(RegisteredServer server : WarpSystem.proxy().getAllServers()) {
                            TeleportCommandOptions access = TeleportManager.getInstance().getOptions(server);
                            if(access != null && access.isTp()) {
                                for(Player player : server.getPlayersConnected()) {
                                    if(!player.getUsername().toLowerCase().startsWith(last.toLowerCase())) continue;
                                    e.getSuggestions().add(player.getUsername());
                                }
                            }
                        }
                    }
                }
            } else if(tpa) {
                if(options == null || !options.isTpa()) {
                    for(Player player : serverInfo.getPlayersConnected()) {
                        if(player.getUsername().equals(receiver.getUsername())) continue;
                        if(!cursor.endsWith(" ") && !player.getUsername().toLowerCase().startsWith(last.toLowerCase())) continue;
                        if(!WarpSystem.getVanishManager().isVanished(player.getUsername())) e.getSuggestions().add(player.getUsername()); //check vanished player names
                    }
                    return;
                }

                for(RegisteredServer server : WarpSystem.proxy().getAllServers()) {
                    TeleportCommandOptions access = TeleportManager.getInstance().getOptions(server);
                    if(access != null && access.isTpa()) {
                        for(Player player : server.getPlayersConnected()) {
                            if(player.getUsername().equals(receiver.getUsername())) continue;
                            if(!cursor.endsWith(" ") && !player.getUsername().toLowerCase().startsWith(last.toLowerCase())) continue;
                            if(!WarpSystem.getVanishManager().isVanished(player.getUsername())) e.getSuggestions().add(player.getUsername()); //check vanished player names
                        }
                    }
                }
            } else if(tpaHere) {
                if(options == null || !options.isTpaHere()) {
                    for(Player player : serverInfo.getPlayersConnected()) {
                        if(player.getUsername().equals(receiver.getUsername())) continue;
                        if(!cursor.endsWith(" ") && !player.getUsername().toLowerCase().startsWith(last.toLowerCase())) continue;
                        if(!WarpSystem.getVanishManager().isVanished(player.getUsername())) e.getSuggestions().add(player.getUsername()); //check vanished player names
                    }
                    return;
                }

                for(RegisteredServer server : WarpSystem.proxy().getAllServers()) {
                    TeleportCommandOptions access = TeleportManager.getInstance().getOptions(server);
                    if(access != null && access.isTpaHere()) {
                        for(Player player : server.getPlayersConnected()) {
                            if(player.getUsername().equals(receiver.getUsername())) continue;
                            if(!cursor.endsWith(" ") && !player.getUsername().toLowerCase().startsWith(last.toLowerCase())) continue;
                            if(!WarpSystem.getVanishManager().isVanished(player.getUsername())) e.getSuggestions().add(player.getUsername()); //check vanished player names
                        }
                    }
                }
            } else if(tpHere) {
                if(options == null || !options.isTp()) {
                    for(Player player : serverInfo.getPlayersConnected()) {
                        if(!cursor.endsWith(" ") && !player.getUsername().toLowerCase().startsWith(last.toLowerCase())) continue;
                        e.getSuggestions().add(player.getUsername());
                    }
                    return;
                }

                for(RegisteredServer server : WarpSystem.proxy().getAllServers()) {
                    TeleportCommandOptions access = TeleportManager.getInstance().getOptions(server);
                    if(access != null && access.isTp()) {
                        for(Player player : server.getPlayersConnected()) {
                            if(!cursor.endsWith(" ") && !player.getUsername().toLowerCase().startsWith(last.toLowerCase())) continue;
                            e.getSuggestions().add(player.getUsername());
                        }
                    }
                }
            }
        }
    }
}
