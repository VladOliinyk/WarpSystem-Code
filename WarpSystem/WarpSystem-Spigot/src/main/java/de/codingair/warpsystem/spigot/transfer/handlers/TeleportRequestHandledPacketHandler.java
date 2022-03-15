package de.codingair.warpsystem.spigot.transfer.handlers;

import de.codingair.packetmanagement.handlers.PacketHandler;
import de.codingair.packetmanagement.utils.Direction;
import de.codingair.packetmanagement.utils.Proxy;
import de.codingair.warpsystem.core.transfer.packets.spigot.TeleportRequestHandledPacket;
import de.codingair.warpsystem.spigot.features.teleportcommand.Invitation;
import de.codingair.warpsystem.spigot.features.teleportcommand.TeleportCommandManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TeleportRequestHandledPacketHandler implements PacketHandler<TeleportRequestHandledPacket> {
    @Override
    public void process(@NotNull TeleportRequestHandledPacket packet, @NotNull Proxy proxy, @Nullable Object connection, @NotNull Direction direction) {
        Invitation invitation = TeleportCommandManager.getInstance().getInvitation(packet.getSender(), packet.getRecipient());
        if (invitation != null) {
            invitation.handle(packet.getRecipient(), packet.isAccepted());
        }
    }
}
