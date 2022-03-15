package de.codingair.warpsystem.core.proxy.transfer.handlers;

import de.codingair.packetmanagement.handlers.PacketHandler;
import de.codingair.packetmanagement.utils.Direction;
import de.codingair.packetmanagement.utils.Proxy;
import de.codingair.warpsystem.core.proxy.Core;
import de.codingair.warpsystem.core.proxy.features.TeleportHandler;
import de.codingair.warpsystem.core.proxy.utils.Player;
import de.codingair.warpsystem.core.proxy.utils.Players;
import de.codingair.warpsystem.core.transfer.packets.spigot.ToggleForceTeleportsPacket;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ToggleForceTeleportsPacketHandler implements PacketHandler<ToggleForceTeleportsPacket> {
    @Override
    public void process(@NotNull ToggleForceTeleportsPacket packet, @NotNull Proxy proxy, @Nullable Object connection, @NotNull Direction direction) {
        TeleportHandler handler = Core.getPlugin().getHandler(TeleportHandler.class);
        Player player = Players.getPlayer(packet.getPlayer());
        if (player != null) {
            handler.setDenyForceTps(player, packet.isAutoDenyTp());
            handler.setDenyForceTpRequests(player, packet.isAutoDenyTpa());
        }
    }
}
