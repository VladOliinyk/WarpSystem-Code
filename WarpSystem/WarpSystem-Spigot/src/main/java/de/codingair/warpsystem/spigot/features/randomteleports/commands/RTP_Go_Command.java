package de.codingair.warpsystem.spigot.features.randomteleports.commands;

import de.codingair.codingapi.server.commands.builder.special.NaturalCommandComponent;
import de.codingair.codingapi.tools.Callback;
import de.codingair.warpsystem.spigot.base.WarpSystem;
import de.codingair.warpsystem.spigot.base.utils.Lang;
import de.codingair.warpsystem.spigot.base.utils.Permissions;
import de.codingair.warpsystem.spigot.base.utils.teleport.Origin;
import de.codingair.warpsystem.spigot.features.randomteleports.managers.RandomTeleportManager;
import de.codingair.warpsystem.spigot.versionfactory.VFac;
import de.codingair.warpsystem.spigot.versionfactory.VKey;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class RTP_Go_Command extends NaturalCommandComponent {
    public RTP_Go_Command(String permission) {
        super(permission);
    }

    private boolean checkOther(CommandSender sender) {
        return sender.hasPermission(Permissions.PERMISSION_RANDOM_TELEPORT_SELECTION_OTHER);
    }

    @Override
    public boolean runCommand(@NotNull CommandSender sender, String label, String[] args) {
        if (args.length >= 1 && args[0].equalsIgnoreCase("go")) {
            if (sender instanceof Player && WarpSystem.cooldown().checkPlayer((Player) sender, Origin.RandomTP)) return false;

            StringBuilder builder = new StringBuilder();
            for (int i = 1; i < args.length; i++) {
                if (i > 1) builder.append(" ");

                String s = args[i];
                builder.append(s);
            }

            String cmd = builder.toString().trim();
            boolean specifyWorld = cmd.contains("]");

            String player;
            if (specifyWorld) player = cmd.split("]", 2)[1].trim();
            else player = args[args.length - 1];
            if (player.isEmpty()) player = null;

            if (player != null && !checkOther(sender)) {
                getBase().noPermission(sender, label, this);
                return false;
            }

            if (player == null && !(sender instanceof Player)) {
                sender.sendMessage(Lang.getPrefix() + WarpSystem.opt().cmdSug() + Lang.get("Use") + ": /" + label + " go " + WarpSystem.opt().cmdArg() + "[server-1, server-2, ...; world-1, world-2, ...] <player>");
                return false;
            }

            //cut the name out of the command
            if (player != null) cmd = cmd.substring(0, cmd.length() - player.length()).trim();

            if (player == null) player = sender.getName();
            final String finalPlayer = player;

            boolean teleportingSelf = player.equalsIgnoreCase(sender.getName());

            if (cmd.startsWith("[") && cmd.endsWith("]")) cmd = cmd.substring(1, cmd.length() - 1).replace(" ", "").toLowerCase();
            else if (teleportingSelf) {
                sender.sendMessage(Lang.getPrefix() + WarpSystem.opt().cmdSug() + Lang.get("Use") + ": /" + label + " go " + WarpSystem.opt().cmdArg() + "[server-1, server-2, ...; world-1, world-2, ...]" + (checkOther(sender) ? " [player]" : ""));
                return false;
            }

            if (cmd.isEmpty() && teleportingSelf) {
                sender.sendMessage(Lang.getPrefix() + WarpSystem.opt().cmdSug() + Lang.get("Use") + ": /" + label + " go " + WarpSystem.opt().cmdArg() + "[server-1, server-2, ...; world-1, world-2, ...]" + (checkOther(sender) ? " [player]" : ""));
                return false;
            }

            String[] data = cmd.isEmpty() ? new String[0] : cmd.split(";");

            Player p = Bukkit.getPlayer(player);
            if (p == null) {
                sender.sendMessage(Lang.getPrefix() + Lang.get("Player_is_not_online"));
                return false;
            }

            //If initiated by another player/console, the player should be teleported
            if (teleportingSelf && !RandomTeleportManager.getInstance().canTeleport(p)) {
                sender.sendMessage(Lang.getPrefix() + Lang.get("RandomTP_No_Teleports_Left"));
                return false;
            }

            if (data.length == 1) {
                //only on local server
                //test@test1, test@test2, test@test3
                String[] worlds = data[0].replaceAll("\\p{Blank}*[a-z]*@", "").split(",");

                World target = Bukkit.getWorld(worlds[(int) (Math.random() * worlds.length)]);
                if (processTarget(sender, player, finalPlayer, target, !teleportingSelf)) return false;
            } else if (data.length == 2) {
                //different servers
                if (!WarpSystem.getInstance().isProxyConnected()) {
                    sender.sendMessage(Lang.getPrefix() + WarpSystem.opt().cmdSug() + Lang.get("Use") + ": /" + label + " go " + WarpSystem.opt().cmdArg() + "[world-1, world-2, ...] [player]");
                    return false;
                }

                List<String> worlds = new ArrayList<>(Arrays.asList(data[1].split(",")));
                String targetWorld = worlds.get((int) (Math.random() * worlds.size()));
                data = targetWorld.split("@");
                String targetServer = data[0];
                targetWorld = data[1];

                if (!targetServer.equalsIgnoreCase(WarpSystem.getInstance().getCurrentServer())) {
                    VFac.build(VKey.RTP_Go_Command_Handler, player, targetServer, targetWorld, finalPlayer, sender, p);
                    return false;
                }

                World target = Bukkit.getWorld(targetWorld);
                if (processTarget(sender, player, finalPlayer, target, !teleportingSelf)) return false;
            } else {
                Player target = Bukkit.getPlayer(finalPlayer);
                if (target == null) {
                    sender.sendMessage(Lang.getPrefix() + Lang.get("Player_is_not_online"));
                    return false;
                }

                if (processTarget(sender, player, finalPlayer, target.getWorld(), !teleportingSelf)) return false;
            }
        }

        return false;
    }

    private boolean processTarget(@NotNull CommandSender sender, String player, String finalPlayer, World target, boolean force) {
        if (target == null) {
            sender.sendMessage(Lang.getPrefix() + Lang.get("World_Not_Exists"));
            return true;
        }

        RandomTeleportManager.getInstance().tryToTeleport(player, target, force, new Callback<Integer>() {
            @Override
            public void accept(Integer result) {
                if (!finalPlayer.equalsIgnoreCase(sender.getName())) {
                    if (result == 0) sender.sendMessage(Lang.getPrefix() + Lang.get("RandomTP_Teleported_Other").replace("%PLAYER%", finalPlayer));
                    else if (result == 1) sender.sendMessage(Lang.getPrefix() + Lang.get("Player_is_not_online"));
                    else if (result == 2) sender.sendMessage(Lang.getPrefix() + Lang.get("RandomTP_No_Location_Found"));
                    else if (result == 4) sender.sendMessage(Lang.getPrefix() + Lang.get("RandomTP_Other_No_Teleports_Left").replace("%PLAYER%", finalPlayer));
                } else if (result == 0 && sender instanceof Player) {
                    WarpSystem.cooldown().register((Player) sender, Origin.RandomTP);
                }

                if (result == 3) sender.sendMessage(Lang.getPrefix() + Lang.get("Server_Is_Not_Online"));
            }
        });
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> sug = new ArrayList<>();
        if (args.length == 1) {
            //add old
            sug.add("go");
        } else if (args.length >= 2 && args[0].equalsIgnoreCase("go")) {
            StringBuilder builder = new StringBuilder();
            for (int i = 1; i < args.length; i++) {
                builder.append(args[i]);
                builder.append(" ");
            }
            String cmd = builder.toString();
            cmd = cmd.substring(0, cmd.length() - 1);

            if (WarpSystem.getInstance().isProxyConnected()) {
                applyProxySuggestions(sender, cmd, args, sug);
            } else {
                applySuggestions(sender, cmd, sug);
            }
        }
        return sug;
    }

    private void applyProxySuggestions(CommandSender commandSender, String command, String[] args, List<String> sug) {
        boolean other = checkOther(commandSender);
        if (command.startsWith("\"") && command.endsWith("\"")) command = command.substring(1, command.length() - 1);

        if (!RandomTeleportManager.getInstance().hasRegisteredServers()) return;

        boolean editingLast = !command.endsWith(" ");

        int start = command.indexOf('[');
        int semi = command.indexOf(';');
        int end = command.lastIndexOf(']');

        if (start == -1 || end == -1) {
            //data
            if (semi == -1) {
                String last = args.length == 0 ? "" : args[args.length - 1];

                //server + local worlds
                String startSug = start == -1 || args.length <= 1 ? "[" : "";

                int count = count(command, ',') - (editingLast ? 1 : 0);

                Set<String> serverList = RandomTeleportManager.getInstance().getServer();
                int max = serverList.size();
                for (String s : serverList) {
                    if (!command.contains(s + ",") && !command.contains(s + ";")) {
                        if (max > 1 && count + 1 < max) {
                            add(last, startSug + s + ",", sug);
                        }

                        add(last, startSug + s + ";", sug);
                    }
                }
                serverList.clear();
            } else if (start != -1) {
                //worlds of given servers
                command = command.substring(start + 1).replace(" ", ""); //remove first bracket

                String[] data = command.split(";");
                String[] servers = data[0].split(",");
                String worlds = data.length == 1 ? "" : data[1];

                args = worlds.split(",", -1);
                String last = args[args.length - 1];

                int max = 0;
                for (String server : servers) {
                    List<String> worldList = RandomTeleportManager.getInstance().getWorlds(server);
                    max += worldList.size();
                }

                for (String server : servers) {
                    List<String> worldList = RandomTeleportManager.getInstance().getWorlds(server);
                    int count = count(worlds, ',') - (editingLast ? 1 : 0);

                    for (String world : worldList) {
                        String w = server + "@" + world;
                        if (!worlds.contains(w + ",") && !worlds.contains(w + "]")) {
                            if (max > 1 && count + 1 < max) {
                                add(last, w + ",", sug);
                            }

                            add(last, w + "]", sug);
                        }
                    }
                }
            }
        } else if (other && command.length() > end + 1) {
            //player
            String rest = command.substring(end + 2);

            if (rest.contains(" ")) return;
            WarpSystem.getInstance().getPlayerDataManager().getCached().forEach(c -> {
                if (rest.isEmpty() || c.getName().toLowerCase().startsWith(rest)) sug.add(c.getName());
            });
        }
    }

    private void add(String last, String argument, List<String> sug) {
        if (last.isEmpty() || argument.startsWith(last)) {
            sug.add(argument);
        }
    }

    private void applySuggestions(CommandSender sender, String command, List<String> suggestions) {
        boolean editingLast = !command.endsWith(" ");
        String[] args = command.split(" ", -1);

        int start = command.indexOf('[');
        int end = command.lastIndexOf(']');

        if (start == -1 || end == -1) {
            //data
            command = command.replace(" ", ""); //remove first bracket
            String startSug = start == -1 || args.length <= 1 ? "[" : "";

            args = command.split(",", -1);
            String last = args[args.length - 1];

            int max = Bukkit.getWorlds().size();
            int count = count(command, ',') - (editingLast ? 1 : 0);

            for (World world : Bukkit.getWorlds()) {
                String w = world.getName();
                if (!command.contains(w + ",") && !command.contains(w + "]")) {
                    if (max > 1 && count + 1 < max) {
                        add(suggestions, last, startSug + w + ",");
                    }

                    add(suggestions, last, startSug + w + "]");
                }
            }
        } else if (checkOther(sender) && command.length() > end + 1) {
            //player
            String rest = command.substring(end + 2);

            if (rest.contains(" ")) return;
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (rest.isEmpty() || player.getName().toLowerCase().startsWith(rest)) suggestions.add(player.getName());
            }
        }
    }

    private int count(String s, char c) {
        int i = 0;
        for (char c1 : s.toCharArray()) {
            if (c1 == c) i++;
        }
        return i;
    }

    private void add(List<String> suggestions, String last, String argument) {
        if (last.isEmpty() || argument.startsWith(last)) {
            suggestions.add(argument);
        }
    }
}
