package de.codingair.warpsystem.core.proxy.transfer.handlers;

import de.codingair.packetmanagement.handlers.PacketHandler;
import de.codingair.packetmanagement.utils.Direction;
import de.codingair.packetmanagement.utils.Proxy;
import de.codingair.warpsystem.core.proxy.Core;
import de.codingair.warpsystem.core.proxy.features.PlayerWarpHandler;
import de.codingair.warpsystem.core.proxy.utils.Server;
import de.codingair.warpsystem.core.transfer.packets.spigot.PlayerWarpTeleportProcessPacket;
import de.codingair.warpsystem.core.transfer.packets.spigot.utils.PlayerWarpData;
import org.jetbrains.annotations.NotNull;

public class PlayerWarpTeleportProcessPacketHandler implements PacketHandler<PlayerWarpTeleportProcessPacket> {
    @Override
    public void process(@NotNull PlayerWarpTeleportProcessPacket packet, @NotNull Proxy proxy, Object connection, @NotNull Direction direction) {
        PlayerWarpHandler handler = Core.getPlugin().getHandler(PlayerWarpHandler.class);
        PlayerWarpData w = handler.getWarp(packet.getId(), packet.getName());

        if (packet.increaseSales()) w.increaseInactiveSales();
        if (packet.resetSales()) w.setInactiveSales((byte) 0);
        if (packet.increasePerformed()) w.increasePerformed();

        //forwarding
        handler.interactWithServers(s -> {
            if (s.getName().equalsIgnoreCase(((Server<?>) connection).getName())) return;
            Core.getPlugin().dataHandler().send(packet, s, Direction.DOWN);
        });
    }
}
