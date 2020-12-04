package de.codingair.warpsystem.velocity.features.teleport.listeners;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import de.codingair.warpsystem.base.transfer.packets.bungee.PerformCommandOnSpigotPacket;
import de.codingair.warpsystem.base.transfer.packets.spigot.ToggleForceTeleportsPacket;
import de.codingair.warpsystem.velocity.base.WarpSystem;
import de.codingair.warpsystem.velocity.base.events.ServerInitializeEvent;
import de.codingair.warpsystem.velocity.features.teleport.managers.TeleportManager;
import de.codingair.warpsystem.velocity.features.teleport.utils.TeleportCommandOptions;

import java.util.Optional;

public class TeleportCommandListener {

    @Subscribe
    public void onQuit(DisconnectEvent e) {
        TeleportManager.getInstance().setDenyForceTps(e.getPlayer(), false);
        TeleportManager.getInstance().setDenyForceTpRequests(e.getPlayer(), false);
    }

    @Subscribe
    public void onSwitch(ServerConnectedEvent e) {
        boolean tp, tpa;
        if((tp = TeleportManager.getInstance().deniesForceTps(e.getPlayer())) | (tpa = TeleportManager.getInstance().deniesForceTpRequests(e.getPlayer())))
            WarpSystem.getInstance().getDataHandler().send(new ToggleForceTeleportsPacket(e.getPlayer().getUsername(), tp, tpa), e.getServer());
    }

    @Subscribe
    public void onSwitch(ServerInitializeEvent e) {
        TeleportManager.getInstance().removeOptions(e.getServer());
    }

    @Subscribe
    public void onPreProcess(PlayerChatEvent e) {
        if(e.getMessage() == null) return;
        if(!e.getMessage().startsWith("/")) return;
        String cmd = e.getMessage().substring(1);
        if(cmd.contains(" ")) cmd = cmd.split(" ")[0];

        Optional<ServerConnection> con = e.getPlayer().getCurrentServer();
        if(!con.isPresent()) return;

        if(WarpSystem.proxy().getCommandManager().hasCommand(cmd) && !isEnabled(con.get().getServer(), cmd)) {
            WarpSystem.getInstance().getDataHandler().send(new PerformCommandOnSpigotPacket(e.getPlayer().getUsername(), e.getMessage().substring(1)), con.get().getServer());
            e.setResult(PlayerChatEvent.ChatResult.denied());
        }
    }

    public boolean isEnabled(RegisteredServer server, String command) {
        TeleportCommandOptions options = TeleportManager.getInstance().getOptions(server);

        switch(command.toLowerCase()) {
            case "tphere":
            case "teleport":
                return options != null && options.isTp();
            case "tpa":
                return options != null && options.isTpa();
            case "tpaall":
                return options != null && options.isTpaAll();
            case "tpahere":
                return options != null && options.isTpaHere();
            case "tpall":
                return options != null && options.isTpAll();
            case "tpatoggle":
                return options != null && options.isTpaToggle();
            case "tptoggle":
                return options != null && options.isTpToggle();
        }

        return true;
    }
}
