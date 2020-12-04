package de.codingair.warpsystem.velocity.transfer;

import com.velocitypowered.api.proxy.server.ServerInfo;
import de.codingair.codingapi.transfer.packets.utils.Packet;
import de.codingair.codingapi.transfer.utils.PacketListener;

public interface VelocityPacketListener extends PacketListener {
    @Override
    default void onReceive(Packet packet, Object server) {
        onReceive(packet, (ServerInfo) server);
    }

    void onReceive(Packet packet, ServerInfo server);
}
