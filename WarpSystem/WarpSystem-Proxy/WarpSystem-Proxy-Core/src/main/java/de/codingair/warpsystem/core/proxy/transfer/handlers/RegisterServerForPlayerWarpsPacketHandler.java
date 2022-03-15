package de.codingair.warpsystem.core.proxy.transfer.handlers;

import de.codingair.packetmanagement.handlers.PacketHandler;
import de.codingair.packetmanagement.utils.Direction;
import de.codingair.packetmanagement.utils.Proxy;
import de.codingair.warpsystem.core.proxy.Core;
import de.codingair.warpsystem.core.proxy.features.PlayerWarpHandler;
import de.codingair.warpsystem.core.proxy.utils.Server;
import de.codingair.warpsystem.core.transfer.packets.general.SendPlayerWarpsPacket;
import de.codingair.warpsystem.core.transfer.packets.proxy.SendPlayerWarpOptionsPacket;
import de.codingair.warpsystem.core.transfer.packets.spigot.RegisterServerForPlayerWarpsPacket;
import de.codingair.warpsystem.core.transfer.packets.spigot.utils.PlayerWarpData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class RegisterServerForPlayerWarpsPacketHandler implements PacketHandler<RegisterServerForPlayerWarpsPacket> {
    @Override
    public void process(@NotNull RegisterServerForPlayerWarpsPacket packet, @NotNull Proxy proxy, @Nullable Object connection, @NotNull Direction direction) {
        PlayerWarpHandler handler = Core.getPlugin().getHandler(PlayerWarpHandler.class);
        List<List<PlayerWarpData>> uploads = new ArrayList<>();
        List<PlayerWarpData> l = new ArrayList<>();

        for (List<PlayerWarpData> value : handler.getWarps().values()) {
            for (PlayerWarpData w : value) {
                l.add(w);

                if (l.size() == 100) {
                    uploads.add(new ArrayList<>(l));
                    l.clear();
                }
            }
        }

        if (!l.isEmpty()) uploads.add(l);

        handler.setActive((Server<?>) connection, true);
        handler.setTimeDependent((Server<?>) connection, packet.isTimeDependent());

        SendPlayerWarpOptionsPacket options = new SendPlayerWarpOptionsPacket(handler.getInactiveTime());
        Core.getPlugin().dataHandler().send(options, (Server<?>) connection, Direction.DOWN);

        for (List<PlayerWarpData> upload : uploads) {
            SendPlayerWarpsPacket p = new SendPlayerWarpsPacket(upload);
            Core.getPlugin().dataHandler().send(p, (Server<?>) connection, Direction.DOWN);
        }

        uploads.clear();
    }
}
