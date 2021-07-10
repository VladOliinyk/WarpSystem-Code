package de.codingair.warpsystem.spigot.features.warps.commands;

import de.codingair.codingapi.player.gui.inventory.v2.exceptions.AlreadyOpenedException;
import de.codingair.codingapi.player.gui.inventory.v2.exceptions.IsWaitingException;
import de.codingair.codingapi.player.gui.inventory.v2.exceptions.NoPageException;
import de.codingair.codingapi.server.commands.builder.BaseComponent;
import de.codingair.codingapi.server.commands.builder.CommandComponent;
import de.codingair.warpsystem.spigot.api.WSCommandBuilder;
import de.codingair.warpsystem.spigot.base.utils.Lang;
import de.codingair.warpsystem.spigot.base.utils.Permissions;
import net.nitrado.event.warppanel.warpgui.WarpPanel;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CWarps extends WSCommandBuilder {
    public CWarps() {
        super("Warps", new BaseComponent(Permissions.PERMISSION_USE_WARP_GUI) {
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
            }

            @Override
            public boolean runCommand(CommandSender sender, String label, String[] args) {
                try {
                    new WarpPanel((Player) sender).open();
                } catch (AlreadyOpenedException | NoPageException | IsWaitingException e) {
                    e.printStackTrace();
                }
                return false;
            }
        }.setOnlyPlayers(true));
    }
}
