package de.codingair.warpsystem.core.proxy.transfer.handlers;

import de.codingair.packetmanagement.handlers.PacketHandler;
import de.codingair.packetmanagement.utils.Direction;
import de.codingair.packetmanagement.utils.Proxy;
import de.codingair.warpsystem.core.proxy.Core;
import de.codingair.warpsystem.core.proxy.features.PlayerWarpHandler;
import de.codingair.warpsystem.core.proxy.utils.Server;
import de.codingair.warpsystem.core.transfer.packets.general.SendPlayerWarpUpdatePacket;
import de.codingair.warpsystem.core.transfer.packets.spigot.utils.PlayerWarpData;
import de.codingair.warpsystem.core.transfer.packets.spigot.utils.PlayerWarpUpdate;
import org.jetbrains.annotations.NotNull;

public class SendPlayerWarpUpdatePacketHandler implements PacketHandler<SendPlayerWarpUpdatePacket> {
    @Override
    public void process(@NotNull SendPlayerWarpUpdatePacket packet, @NotNull Proxy proxy, Object connection, @NotNull Direction direction) {
        PlayerWarpHandler handler = Core.getPlugin().getHandler(PlayerWarpHandler.class);
        PlayerWarpUpdate update = packet.getUpdate();
        PlayerWarpData w = handler.getWarp(update.getId(), update.getOriginName());
        w.apply(update);

        //forwarding
        handler.interactWithServers(s -> {
            if (s.getName().equalsIgnoreCase(((Server<?>) connection).getName())) return;
            Core.getPlugin().dataHandler().send(packet, s, Direction.DOWN);
        });
        update.destroy();
    }
}
