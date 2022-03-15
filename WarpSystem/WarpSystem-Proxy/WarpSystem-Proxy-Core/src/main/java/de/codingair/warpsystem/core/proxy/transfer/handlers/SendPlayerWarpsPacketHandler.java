package de.codingair.warpsystem.core.proxy.transfer.handlers;

import de.codingair.packetmanagement.handlers.PacketHandler;
import de.codingair.packetmanagement.utils.Direction;
import de.codingair.packetmanagement.utils.Proxy;
import de.codingair.warpsystem.core.proxy.Core;
import de.codingair.warpsystem.core.proxy.features.PlayerWarpHandler;
import de.codingair.warpsystem.core.transfer.packets.general.SendPlayerWarpsPacket;
import de.codingair.warpsystem.core.transfer.packets.spigot.utils.PlayerWarpData;
import de.codingair.warpsystem.core.transfer.utils.serializeable.Serializable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SendPlayerWarpsPacketHandler implements PacketHandler<SendPlayerWarpsPacket> {
    @Override
    public void process(@NotNull SendPlayerWarpsPacket packet, @NotNull Proxy proxy, @Nullable Object connection, @NotNull Direction direction) {
        PlayerWarpHandler handler = Core.getPlugin().getHandler(PlayerWarpHandler.class);
        List<PlayerWarpData> l = packet.getData();

        for (Serializable s : l) {
            PlayerWarpData w = (PlayerWarpData) s;
            handler.updateWarp(w);
        }

        //forwarding
        handler.interactWithServers(s -> {
            if (s.equals(connection)) return;
            Core.getPlugin().dataHandler().send(packet, s, Direction.DOWN);
        });
    }
}
