package de.codingair.warpsystem.core.proxy.transfer.handlers;

import de.codingair.packetmanagement.handlers.PacketHandler;
import de.codingair.packetmanagement.utils.Direction;
import de.codingair.packetmanagement.utils.Proxy;
import de.codingair.warpsystem.core.proxy.Core;
import de.codingair.warpsystem.core.proxy.features.RandomTPHandler;
import de.codingair.warpsystem.core.proxy.utils.Server;
import de.codingair.warpsystem.core.transfer.packets.spigot.RandomTPWorldsPacket;
import org.jetbrains.annotations.NotNull;

public class RandomTPWorldsPacketHandler implements PacketHandler<RandomTPWorldsPacket> {
    @Override
    public void process(@NotNull RandomTPWorldsPacket packet, @NotNull Proxy proxy, Object connection, @NotNull Direction direction) {
        RandomTPHandler handler = Core.getPlugin().getHandler(RandomTPHandler.class);
        handler.addWorldData(((Server<?>) connection), packet);
    }
}
