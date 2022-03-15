package de.codingair.warpsystem.core.proxy.transfer.handlers;

import de.codingair.packetmanagement.handlers.PacketHandler;
import de.codingair.packetmanagement.utils.Direction;
import de.codingair.packetmanagement.utils.Proxy;
import de.codingair.warpsystem.core.proxy.Core;
import de.codingair.warpsystem.core.proxy.features.PlayerWarpHandler;
import de.codingair.warpsystem.core.proxy.utils.Server;
import de.codingair.warpsystem.core.transfer.packets.general.SendPlayerWarpsPacket;
import de.codingair.warpsystem.core.transfer.packets.spigot.MoveLocalPlayerWarpsPacket;
import de.codingair.warpsystem.core.transfer.packets.spigot.utils.PlayerWarpData;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class MoveLocalPlayerWarpsPacketHandler implements PacketHandler<MoveLocalPlayerWarpsPacket> {
    @Override
    public void process(@NotNull MoveLocalPlayerWarpsPacket packet, @NotNull Proxy proxy, Object connection, @NotNull Direction direction) {
        PlayerWarpHandler handler = Core.getPlugin().getHandler(PlayerWarpHandler.class);
        List<List<PlayerWarpData>> uploads = new ArrayList<>();
        List<PlayerWarpData> l = new ArrayList<>();

        for (List<PlayerWarpData> value : handler.getWarps().values()) {
            for (PlayerWarpData w : value) {
                if (w.getServer().equalsIgnoreCase(((Server<?>) connection).getName())) l.add(w);
                if (l.size() == 100) {
                    uploads.add(new ArrayList<>(l));
                    l.clear();
                }
            }
        }

        if (!l.isEmpty()) uploads.add(l);

        handler.setActive((Server<?>) connection, false);

        for (List<PlayerWarpData> upload : uploads) {
            for (PlayerWarpData d : upload) {
                handler.delete(d, true);
            }

            SendPlayerWarpsPacket p = new SendPlayerWarpsPacket(upload);
            Core.getPlugin().dataHandler().send(p, (Server<?>) connection, Direction.DOWN);
        }

        uploads.clear();
    }
}
