package de.codingair.warpsystem.velocity.features.teleport.listeners;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import de.codingair.codingapi.tools.Callback;
import de.codingair.codingapi.utils.Value;
import de.codingair.warpsystem.base.transfer.packets.bungee.TeleportPlayerToCoordsPacket;
import de.codingair.warpsystem.base.transfer.packets.bungee.TeleportPlayerToPlayerPacket;
import de.codingair.warpsystem.base.transfer.packets.general.IntegerPacket;
import de.codingair.warpsystem.base.transfer.packets.general.LongPacket;
import de.codingair.warpsystem.base.transfer.packets.general.StartTeleportToPlayerPacket;
import de.codingair.warpsystem.base.transfer.packets.spigot.*;
import de.codingair.warpsystem.base.transfer.packets.utils.Packet;
import de.codingair.warpsystem.base.transfer.packets.utils.PacketType;
import de.codingair.warpsystem.base.transfer.utils.PacketListener;
import de.codingair.warpsystem.transfer.packets.spigot.GetOnlineCountPacket;
import de.codingair.warpsystem.velocity.api.Players;
import de.codingair.warpsystem.velocity.base.WarpSystem;
import de.codingair.warpsystem.velocity.features.teleport.managers.TeleportManager;

import java.util.Optional;

public class TeleportPacketListener extends PacketListener {
    @Override
    public void onReceive(Packet packet, String extra) {
        Optional<RegisteredServer> oS = WarpSystem.proxy().getServer(extra);
        if(!oS.isPresent()) return;
        RegisteredServer source = oS.get();

        if(packet.getType() == PacketType.ToggleForceTeleportsPacket) {
            ToggleForceTeleportsPacket tpPacket = (ToggleForceTeleportsPacket) packet;

            Player player = Players.getPlayer(tpPacket.getPlayer());
            if(player != null) {
                TeleportManager.getInstance().setDenyForceTps(player, tpPacket.isAutoDenyTp());
                TeleportManager.getInstance().setDenyForceTpRequests(player, tpPacket.isAutoDenyTpa());
            }
        } else if(packet.getType() == PacketType.GetOnlineCountPacket) {
            GetOnlineCountPacket tpPacket = (GetOnlineCountPacket) packet;

            IntegerPacket answer = new IntegerPacket(WarpSystem.proxy().getPlayerCount());
            tpPacket.applyAsAnswer(answer);
            WarpSystem.getInstance().getDataHandler().send(answer, source);
        }
    }

    @Override
    public boolean onSend(Packet packet) {
        return false;
    }
}
