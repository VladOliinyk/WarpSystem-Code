package de.codingair.warpsystem.velocity.transfer;

import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import de.codingair.codingapi.transfer.core.PacketListener;
import de.codingair.codingapi.transfer.packets.utils.Packet;

public interface VelocityPacketListener extends PacketListener<RegisteredServer> {
    @Override
    default void onReceive(Packet packet, RegisteredServer server) {
        onReceive(packet, server.getServerInfo());
    }

    void onReceive(Packet packet, ServerInfo server);
}
