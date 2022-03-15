package de.codingair.warpsystem.core.proxy.transfer.handlers;

import de.codingair.packetmanagement.handlers.ResponsiblePacketHandler;
import de.codingair.packetmanagement.utils.Direction;
import de.codingair.packetmanagement.utils.Proxy;
import de.codingair.warpsystem.core.proxy.utils.Player;
import de.codingair.warpsystem.core.proxy.utils.Players;
import de.codingair.warpsystem.core.transfer.packets.proxy.SendUUIDPacket;
import de.codingair.warpsystem.core.transfer.packets.spigot.RequestUUIDPacket;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class SendUUIDPacketHandler implements ResponsiblePacketHandler<RequestUUIDPacket, SendUUIDPacket> {
    @Override
    public @NotNull CompletableFuture<SendUUIDPacket> response(@NotNull RequestUUIDPacket packet, @NotNull Proxy proxy, @Nullable Object connection, @NotNull Direction direction) {
        Player pp = Players.getPlayer(packet.getPlayer());
        return CompletableFuture.completedFuture(new SendUUIDPacket(pp == null ? null : pp.getUniqueId()));
    }
}
