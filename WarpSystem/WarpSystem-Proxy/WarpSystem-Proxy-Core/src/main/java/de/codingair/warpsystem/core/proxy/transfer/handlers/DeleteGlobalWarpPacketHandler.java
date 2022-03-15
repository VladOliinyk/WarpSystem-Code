package de.codingair.warpsystem.core.proxy.transfer.handlers;

import de.codingair.packetmanagement.handlers.ResponsiblePacketHandler;
import de.codingair.packetmanagement.packets.impl.BooleanPacket;
import de.codingair.packetmanagement.utils.Direction;
import de.codingair.packetmanagement.utils.Proxy;
import de.codingair.warpsystem.core.proxy.Core;
import de.codingair.warpsystem.core.proxy.features.GlobalWarpHandler;
import de.codingair.warpsystem.core.transfer.packets.proxy.UpdateGlobalWarpPacket;
import de.codingair.warpsystem.core.transfer.packets.spigot.DeleteGlobalWarpPacket;
import de.codingair.warpsystem.core.transfer.utils.serializeable.SGlobalWarp;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class DeleteGlobalWarpPacketHandler implements ResponsiblePacketHandler<DeleteGlobalWarpPacket, BooleanPacket> {

    @Override
    public @NotNull CompletableFuture<BooleanPacket> response(@NotNull DeleteGlobalWarpPacket packet, @NotNull Proxy proxy, @Nullable Object connection, @NotNull Direction direction) {
        GlobalWarpHandler handler = Core.getPlugin().getHandler(GlobalWarpHandler.class);
        SGlobalWarp warp = handler.get(packet.getWarp());

        if (warp == null) return CompletableFuture.completedFuture(new BooleanPacket(false));
        else {
            handler.remove(warp.getName());
            handler.synchronize(warp, UpdateGlobalWarpPacket.Action.DELETE);
            return CompletableFuture.completedFuture(new BooleanPacket(true));
        }
    }
}
