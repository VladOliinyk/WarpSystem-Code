package de.codingair.warpsystem.core.proxy.transfer.handlers;

import de.codingair.packetmanagement.handlers.PacketHandler;
import de.codingair.packetmanagement.utils.Direction;
import de.codingair.packetmanagement.utils.Proxy;
import de.codingair.warpsystem.core.proxy.Core;
import de.codingair.warpsystem.core.proxy.features.RandomTPHandler;
import de.codingair.warpsystem.core.proxy.utils.Server;
import de.codingair.warpsystem.core.transfer.packets.spigot.QueueRTPUsagePacket;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class QueueRTPUsagePacketHandler implements PacketHandler<QueueRTPUsagePacket> {
    @Override
    public void process(@NotNull QueueRTPUsagePacket packet, @NotNull Proxy proxy, @Nullable Object connection, @NotNull Direction direction) {
        RandomTPHandler handler = Core.getPlugin().getHandler(RandomTPHandler.class);
        Server<?> server = Core.getPlugin().getServer(packet.getServer());
        if (!server.isEmpty()) Core.getPlugin().dataHandler().send(packet, server, Direction.DOWN);
        else handler.addQueueEntry(packet.getIdOnce(), packet.getServer());
    }
}
