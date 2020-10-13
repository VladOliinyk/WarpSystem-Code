package de.codingair.warpsystem.spigot.features.simplewarps.commands;

import de.codingair.codingapi.server.commands.builder.BaseComponent;
import de.codingair.codingapi.server.commands.builder.CommandComponent;
import de.codingair.codingapi.server.commands.builder.special.MultiCommandComponent;
import de.codingair.codingapi.server.commands.builder.special.NaturalCommandComponent;
import de.codingair.codingapi.tools.Location;
import de.codingair.warpsystem.spigot.api.WSCommandBuilder;
import de.codingair.warpsystem.spigot.base.WarpSystem;
import de.codingair.warpsystem.spigot.base.language.Lang;
import de.codingair.warpsystem.spigot.features.FeatureType;
import de.codingair.warpsystem.spigot.features.simplewarps.SimpleWarp;
import de.codingair.warpsystem.spigot.features.simplewarps.guis.GEditWarp;
import de.codingair.warpsystem.spigot.features.simplewarps.managers.SimpleWarpManager;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class CModifyWarp extends WSCommandBuilder {
    public CModifyWarp() {
        super("ModifyWarp", new BaseComponent(WarpSystem.PERMISSION_MODIFY_SIMPLE_WARPS) {
            @Override
            public void noPermission(CommandSender sender, String label, CommandComponent child) {
                sender.sendMessage(Lang.getPrefix() + Lang.get("No_Permission"));
            }

            @Override
            public void onlyFor(boolean player, CommandSender sender, String label, CommandComponent child) {
            }

            @Override
            public void unknownSubCommand(CommandSender sender, String label, String[] args) {
                sender.sendMessage(Lang.getPrefix() + WarpSystem.opt().cmdSug() + Lang.get("Use") + ": /" + label + " " + WarpSystem.opt().cmdArg() + "<warp>");
            }

            @Override
            public boolean runCommand(CommandSender sender, String label, String[] args) {
                sender.sendMessage(Lang.getPrefix() + WarpSystem.opt().cmdSug() + Lang.get("Use") + ": /" + label + " " + WarpSystem.opt().cmdArg() + "<warp>");
                return true;
            }
        });

        SimpleWarpManager m = WarpSystem.getInstance().getDataManager().getManager(FeatureType.SIMPLE_WARPS);

        getBaseComponent().addChild(new MultiCommandComponent() {
            @Override
            public void addArguments(CommandSender sender, String[] args, List<String> suggestions) {
                for(SimpleWarp value : m.getWarps().values()) {
                    suggestions.add(value.getName(true));
                }
            }

            @Override
            public boolean runCommand(CommandSender sender, String label, String argument, String[] args) {
                if(!m.existsWarp(argument)) {
                    sender.sendMessage(Lang.getPrefix() + Lang.get("WARP_DOES_NOT_EXISTS"));
                    return true;
                }

                sender.sendMessage(Lang.getPrefix() + WarpSystem.opt().cmdSug() + Lang.get("Use") + ": /" + label + " " + WarpSystem.opt().cmdArg() + argument + " <world> <x> <y> <z> [<yaw> <pitch>]");

                SimpleWarp warp = m.getWarp(argument);
                new GEditWarp((Player) sender, warp).open();
                ((Player) sender).updateInventory();
                return true;
            }
        });

        getBaseComponent().getChild(null).addChild(new MultiCommandComponent() {
            @Override
            public void addArguments(CommandSender sender, String[] args, List<String> suggestions) {
                for(World w : Bukkit.getWorlds()) {
                    suggestions.add(w.getName());
                }
            }

            @Override
            public boolean runCommand(CommandSender sender, String label, String argument, String[] args) {
                World w = Bukkit.getWorld(argument);

                if(w == null) {
                    sender.sendMessage(Lang.getPrefix() + Lang.get("World_Not_Exists"));
                    return true;
                }

                sender.sendMessage(Lang.getPrefix() + WarpSystem.opt().cmdSug() + Lang.get("Use") + ": /" + label + " " + WarpSystem.opt().cmdArg() + args[0] + " " + argument + " <x> <y> <z> [<yaw> <pitch>]");
                return true;
            }
        });

        getBaseComponent().getChild(null).getChild(null).addChild(new NaturalCommandComponent() {
            @Override
            public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
                List<String> l = new ArrayList<>();

                if(sender instanceof Player) {
                    Player p = (Player) sender;

                    if(args.length == 3) l.add(cut(p.getLocation().getX()) + "");
                    else if(args.length == 4) l.add(cut(p.getLocation().getY()) + "");
                    else if(args.length == 5) l.add(cut(p.getLocation().getZ()) + "");
                    else if(args.length == 6) l.add(cut(p.getLocation().getYaw()) + "");
                    else if(args.length == 7) l.add(cut(p.getLocation().getPitch()) + "");
                }

                return l;
            }

            @Override
            public boolean runCommand(CommandSender sender, String label, String[] args) {
                if(args.length == 5 || args.length == 7) {
                    SimpleWarp warp = m.getWarp(args[0]);

                    if(warp == null) {
                        sender.sendMessage(Lang.getPrefix() + Lang.get("WARP_DOES_NOT_EXISTS"));
                        return true;
                    }

                    World w = Bukkit.getWorld(args[1]);

                    if(w == null) {
                        sender.sendMessage(Lang.getPrefix() + Lang.get("World_Not_Exists"));
                        return true;
                    }

                    try {
                        //without yaw + pitch
                        double x = Double.parseDouble(args[2].replace(",", "."));
                        double y = Double.parseDouble(args[3].replace(",", "."));
                        double z = Double.parseDouble(args[4].replace(",", "."));

                        float yaw = 0;
                        float pitch = 0;

                        if(args.length == 7) {
                            //with yaw + pitch
                            yaw = Float.parseFloat(args[5].replace(",", "."));
                            pitch = Float.parseFloat(args[6].replace(",", "."));
                        }

                        warp.setLocation(new Location(w, x, y, z, yaw, pitch));
                        sender.sendMessage(Lang.getPrefix() + "§a" + Lang.get("Changes_have_been_saved"));
                        return true;
                    } catch(NumberFormatException ignored) {
                    }
                }

                sender.sendMessage(Lang.getPrefix() + WarpSystem.opt().cmdSug() + Lang.get("Use") + ": /" + label + " " + WarpSystem.opt().cmdArg() + args[0] + " " + args[1] + " <x> <y> <z> [<yaw> <pitch>]");
                return true;
            }
        });
    }

    private static double cut(double d) {
        return ((double) (int) (d * 100)) / 100;
    }
}
