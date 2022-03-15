package de.codingair.warpsystem.spigot.transfer.handlers;

import de.codingair.packetmanagement.handlers.PacketHandler;
import de.codingair.packetmanagement.utils.Direction;
import de.codingair.packetmanagement.utils.Proxy;
import de.codingair.warpsystem.core.transfer.packets.proxy.ProvidePlayerDataPacket;
import de.codingair.warpsystem.spigot.base.WarpSystem;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ProvidePlayerDataPacketHandler implements PacketHandler<ProvidePlayerDataPacket> {
    @Override
    public void process(@NotNull ProvidePlayerDataPacket packet, @NotNull Proxy proxy, Object connection, @NotNull Direction direction) {
        WarpSystem.getInstance().getPlayerDataManager().apply(packet.getData(), (Player) connection);
    }
}
