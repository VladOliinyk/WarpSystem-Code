package de.codingair.warpsystem.spigot.features.portals.commands;

import de.codingair.codingapi.player.gui.anvil.*;
import de.codingair.codingapi.server.commands.builder.BaseComponent;
import de.codingair.codingapi.server.commands.builder.CommandComponent;
import de.codingair.codingapi.server.commands.builder.special.MultiCommandComponent;
import de.codingair.codingapi.tools.Callback;
import de.codingair.codingapi.tools.Location;
import de.codingair.codingapi.tools.items.ItemBuilder;
import de.codingair.codingapi.utils.ChatColor;
import de.codingair.warpsystem.api.destinations.utils.Result;
import de.codingair.warpsystem.spigot.api.WSCommandBuilder;
import de.codingair.warpsystem.spigot.base.WarpSystem;
import de.codingair.warpsystem.spigot.base.managers.TeleportManager;
import de.codingair.warpsystem.spigot.base.utils.Lang;
import de.codingair.warpsystem.spigot.base.utils.Permissions;
import de.codingair.warpsystem.spigot.base.utils.teleport.TeleportOptions;
import de.codingair.warpsystem.spigot.features.portals.guis.PortalEditor;
import de.codingair.warpsystem.spigot.features.portals.managers.PortalManager;
import de.codingair.warpsystem.spigot.features.portals.utils.Portal;
import de.codingair.warpsystem.spigot.features.portals.utils.PortalFactory;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.List;

public class CPortals extends WSCommandBuilder {
    public CPortals() {
        super("Portals", new BaseComponent(Permissions.PERMISSION_MODIFY_PORTALS) {
            @Override
            public void noPermission(CommandSender sender, String label, CommandComponent child) {
                sender.sendMessage(Lang.getPrefix() + Lang.get("No_Permission"));
            }

            @Override
            public void onlyFor(boolean player, CommandSender sender, String label, CommandComponent child) {
                sender.sendMessage(Lang.getPrefix() + Lang.get("Only_For_Players"));
            }

            @Override
            public void unknownSubCommand(CommandSender sender, String label, String[] args) {
                sender.sendMessage(Lang.getPrefix() + WarpSystem.opt().cmdSug() + Lang.get("Use") + ": /" + label + " " + WarpSystem.opt().cmdArg() + "<create, edit, delete, visit>");
            }

            @Override
            public boolean runCommand(CommandSender sender, String label, String[] args) {
                sender.sendMessage(Lang.getPrefix() + WarpSystem.opt().cmdSug() + Lang.get("Use") + ": /" + label + " " + WarpSystem.opt().cmdArg() + "<create, edit, delete, visit>");
                return true;
            }
        }.setOnlyPlayers(true));

        getBaseComponent().addChild(new CommandComponent("create") {
            @Override
            public boolean runCommand(CommandSender sender, String label, String[] args) {
                AnvilGUI.openAnvil(WarpSystem.getInstance(), (Player) sender, new AnvilListener() {
                    @Override
                    public void onClick(AnvilClickEvent e) {
                        if (!e.getSlot().equals(AnvilSlot.OUTPUT)) return;

                        String input = e.getInput();

                        if (input == null) {
                            e.getPlayer().sendMessage(Lang.getPrefix() + Lang.get("Enter_Name"));
                            return;
                        }

                        if (PortalManager.getInstance().existsPortal(input)) {
                            e.getPlayer().sendMessage(Lang.getPrefix() + Lang.get("Name_Already_Exists"));
                            return;
                        }

                        e.setClose(true);
                    }

                    @Override
                    public void onClose(AnvilCloseEvent e) {
                        if (e.isSubmitted()) {
                            String name = e.getSubmittedText();

                            Portal portal = PortalFactory.build(name);
                            portal.setSpawn(new Location(((Player) sender).getLocation()));
                            e.setPost(() -> new PortalEditor((Player) sender, portal).open());
                        }
                    }
                }, new ItemBuilder(Material.PAPER).setName(Lang.get("Name") + "...").getItem());

                return true;
            }
        });

        getComponent("create").addChild(new MultiCommandComponent() {
            @Override
            public void addArguments(CommandSender sender, String[] args, List<String> suggestions) {
            }

            @Override
            public boolean runCommand(CommandSender sender, String label, String argument, String[] args) {
                if (PortalManager.getInstance().existsPortal(argument)) {
                    sender.sendMessage(Lang.getPrefix() + Lang.get("Name_Already_Exists"));
                    return true;
                }

                Portal portal = PortalFactory.build(argument);
                portal.setSpawn(new Location(((Player) sender).getLocation()));
                new PortalEditor((Player) sender, portal).open();
                return true;
            }
        });

        getBaseComponent().addChild(new CommandComponent("delete") {
            @Override
            public boolean runCommand(CommandSender sender, String label, String[] args) {
                PortalManager.getInstance().setGoingToEdit((Player) sender, 0);
                PortalManager.getInstance().setGoingToDelete((Player) sender, 30);
                sender.sendMessage(Lang.getPrefix() + Lang.get("Go_To_Portal"));
                return true;
            }
        });

        getBaseComponent().addChild(new CommandComponent("edit") {
            @Override
            public boolean runCommand(CommandSender sender, String label, String[] args) {
                PortalManager.getInstance().setGoingToDelete((Player) sender, 0);
                PortalManager.getInstance().setGoingToEdit((Player) sender, 30);
                sender.sendMessage(Lang.getPrefix() + Lang.get("Go_To_Portal"));
                return true;
            }
        });

        getComponent("edit").addChild(new MultiCommandComponent() {
            @Override
            public void addArguments(CommandSender sender, String[] args, List<String> suggestions) {
                for (Portal portal : PortalManager.getInstance().getPortals()) {
                    String name = ChatColor.stripColor(ChatColor.translateAll('&', portal.getDisplayName()));
                    if (!suggestions.contains(name)) suggestions.add(name);
                }
            }

            @Override
            public boolean runCommand(CommandSender sender, String label, String argument, String[] args) {
                Player p = (Player) sender;
                Portal portal = PortalManager.getInstance().getPortal(ChatColor.translateAll('&', argument));

                if (portal == null) {
                    p.sendMessage(Lang.getPrefix() + Lang.get("Portal_Does_Not_Exist"));
                    return true;
                }

                org.bukkit.Location spawn;
                boolean velocity = true;

                if (portal.getSpawn() != null) {
                    spawn = portal.getSpawn();
                    velocity = false;
                } else spawn = portal.getAbsoluteMid();

                if (spawn != null) {
                    PortalManager.getInstance().getNoTeleport().add(p);
                    TeleportOptions options = new TeleportOptions(spawn, null);
                    options.setSkip(true);
                    options.setMessage(null);

                    if (velocity) {
                        options.addCallback(new Callback<Result>() {
                            @Override
                            public void accept(Result object) {
                                p.setVelocity(new Vector(-0.5, 0, 0));
                            }
                        });
                    }

                    TeleportManager.getInstance().teleport(p, options, true);
                }

                Bukkit.getScheduler().runTaskLater(WarpSystem.getInstance(), () -> {
                    new PortalEditor(p, portal).open();
                    PortalManager.getInstance().getNoTeleport().remove(p);
                }, 4L);
                return true;
            }
        });

        getBaseComponent().addChild(new CommandComponent("visit") {
            @Override
            public boolean runCommand(CommandSender sender, String label, String[] args) {
                sender.sendMessage(Lang.getPrefix() + WarpSystem.opt().cmdSug() + Lang.get("Use") + ": /" + label + " visit " + WarpSystem.opt().cmdArg() + "<portal>");
                return true;
            }
        });

        getComponent("visit").addChild(new MultiCommandComponent() {
            @Override
            public void addArguments(CommandSender sender, String[] args, List<String> suggestions) {
                for (Portal portal : PortalManager.getInstance().getPortals()) {
                    String name = ChatColor.stripColor(ChatColor.translateAll('&', portal.getDisplayName()));
                    if (!suggestions.contains(name)) suggestions.add(name);
                }
            }

            @Override
            public boolean runCommand(CommandSender sender, String label, String argument, String[] args) {
                Player p = (Player) sender;
                Portal portal = PortalManager.getInstance().getPortal(ChatColor.translateAll('&', argument));

                if (portal == null) {
                    p.sendMessage(Lang.getPrefix() + Lang.get("Portal_Does_Not_Exist"));
                    return true;
                }

                org.bukkit.Location spawn;
                boolean velocity = true;

                if (portal.getSpawn() != null) {
                    spawn = portal.getSpawn();
                    velocity = false;
                } else spawn = portal.getAbsoluteMid();

                if (spawn != null) {
                    PortalManager.getInstance().getNoTeleport().add(p);
                    TeleportOptions options = new TeleportOptions(spawn, portal.getDisplayName());
                    options.setSkip(true);

                    if (velocity) {
                        options.addCallback(new Callback<Result>() {
                            @Override
                            public void accept(Result object) {
                                p.setVelocity(new Vector(-0.5, 0, 0));
                                PortalManager.getInstance().getNoTeleport().remove(p);
                            }
                        });
                    }

                    TeleportManager.getInstance().teleport(p, options, true);
                }
                return true;
            }
        });
    }
}
