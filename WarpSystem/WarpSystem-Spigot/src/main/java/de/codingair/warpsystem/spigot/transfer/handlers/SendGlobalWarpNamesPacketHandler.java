package de.codingair.warpsystem.spigot.transfer.handlers;

import de.codingair.packetmanagement.handlers.PacketHandler;
import de.codingair.packetmanagement.utils.Direction;
import de.codingair.packetmanagement.utils.Proxy;
import de.codingair.warpsystem.core.transfer.packets.proxy.SendGlobalWarpNamesPacket;
import de.codingair.warpsystem.spigot.base.WarpSystem;
import de.codingair.warpsystem.spigot.features.FeatureType;
import de.codingair.warpsystem.spigot.features.globalwarps.managers.GlobalWarpManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SendGlobalWarpNamesPacketHandler implements PacketHandler<SendGlobalWarpNamesPacket> {
    @Override
    public void process(@NotNull SendGlobalWarpNamesPacket packet, @NotNull Proxy proxy, @Nullable Object connection, @NotNull Direction direction) {
        GlobalWarpManager gwManager = WarpSystem.getInstance().getDataManager().getManager(FeatureType.GLOBAL_WARPS);
        if (packet.isReset()) gwManager.getGlobalWarps().clear();
        gwManager.getGlobalWarps().put(packet.getWarp(), packet.getServer());
    }
}
