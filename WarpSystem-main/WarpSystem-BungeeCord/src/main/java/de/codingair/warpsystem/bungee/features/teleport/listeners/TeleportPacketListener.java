package de.codingair.warpsystem.bungee.features.teleport.listeners;

import de.codingair.warpsystem.bungee.api.Players;
import de.codingair.warpsystem.bungee.base.WarpSystem;
import de.codingair.warpsystem.bungee.features.teleport.managers.TeleportManager;
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
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class TeleportPacketListener extends PacketListener {
    @Override
    public void onReceive(Packet packet, String extra) {
        if(packet.getType() == PacketType.ToggleForceTeleportsPacket) {
            ToggleForceTeleportsPacket tpPacket = (ToggleForceTeleportsPacket) packet;

            ProxiedPlayer player = Players.getPlayer(tpPacket.getPlayer());
            if(player != null) {
                TeleportManager.getInstance().setDenyForceTps(player, tpPacket.isAutoDenyTp());
                TeleportManager.getInstance().setDenyForceTpRequests(player, tpPacket.isAutoDenyTpa());
            }
        } else if(packet.getType() == PacketType.GetOnlineCountPacket) {
            GetOnlineCountPacket tpPacket = (GetOnlineCountPacket) packet;
            ServerInfo origin = WarpSystem.proxy().getServerInfo(extra);

            IntegerPacket answer = new IntegerPacket(WarpSystem.proxy().getPlayers().size());
            tpPacket.applyAsAnswer(answer);
            WarpSystem.getInstance().getDataHandler().send(answer, origin);
        }

    }

    @Override
    public boolean onSend(Packet packet) {
        return false;
    }
}
