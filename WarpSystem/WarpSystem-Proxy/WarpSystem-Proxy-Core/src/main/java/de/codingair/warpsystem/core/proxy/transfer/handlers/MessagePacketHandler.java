package de.codingair.warpsystem.core.proxy.transfer.handlers;

import de.codingair.packetmanagement.handlers.PacketHandler;
import de.codingair.packetmanagement.utils.Direction;
import de.codingair.packetmanagement.utils.Proxy;
import de.codingair.warpsystem.core.proxy.Core;
import de.codingair.warpsystem.core.proxy.utils.Player;
import de.codingair.warpsystem.core.transfer.packets.spigot.MessagePacket;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MessagePacketHandler implements PacketHandler<MessagePacket> {
    @Override
    public void process(@NotNull MessagePacket packet, @NotNull Proxy proxy, @Nullable Object connection, @NotNull Direction direction) {
        Player player = Core.getPlugin().getPlayer(packet.getPlayer());

        if (player != null) player.sendGrayMessage(packet.getMessage());
        else if(direction == Direction.DOWN) Core.getPlugin().dataHandler().send(packet, null, Direction.UP);
    }
}
