package de.codingair.warpsystem.core.proxy.transfer.handlers;

import de.codingair.packetmanagement.handlers.PacketHandler;
import de.codingair.packetmanagement.utils.Direction;
import de.codingair.packetmanagement.utils.Proxy;
import de.codingair.warpsystem.core.proxy.Core;
import de.codingair.warpsystem.core.proxy.utils.Server;
import de.codingair.warpsystem.core.transfer.packets.spigot.RequestInitialPacket;
import org.jetbrains.annotations.NotNull;

public class RequestInitialPacketHandler implements PacketHandler<RequestInitialPacket> {
    @Override
    public void process(@NotNull RequestInitialPacket packet, @NotNull Proxy proxy, Object connection, @NotNull Direction direction) {
        if(direction == Direction.DOWN) {
            Core.getServerManager().sendInitialPacket((Server<?>) connection);
        }
    }
}
