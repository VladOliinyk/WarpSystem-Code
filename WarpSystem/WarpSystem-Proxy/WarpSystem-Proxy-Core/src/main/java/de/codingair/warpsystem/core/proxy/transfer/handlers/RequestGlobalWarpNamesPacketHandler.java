package de.codingair.warpsystem.core.proxy.transfer.handlers;

import de.codingair.packetmanagement.handlers.PacketHandler;
import de.codingair.packetmanagement.utils.Direction;
import de.codingair.packetmanagement.utils.Proxy;
import de.codingair.warpsystem.core.proxy.Core;
import de.codingair.warpsystem.core.proxy.features.GlobalWarpHandler;
import de.codingair.warpsystem.core.proxy.utils.Server;
import de.codingair.warpsystem.core.transfer.packets.spigot.RequestGlobalWarpNamesPacket;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RequestGlobalWarpNamesPacketHandler implements PacketHandler<RequestGlobalWarpNamesPacket> {
    @Override
    public void process(@NotNull RequestGlobalWarpNamesPacket packet, @NotNull Proxy proxy, @Nullable Object connection, @NotNull Direction direction) {
        GlobalWarpHandler handler = Core.getPlugin().getHandler(GlobalWarpHandler.class);
        handler.synchronize((Server<?>) connection);
    }
}
