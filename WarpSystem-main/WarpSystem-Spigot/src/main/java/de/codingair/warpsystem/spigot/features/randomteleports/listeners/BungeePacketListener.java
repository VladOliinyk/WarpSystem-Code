package de.codingair.warpsystem.spigot.features.randomteleports.listeners;

import de.codingair.warpsystem.base.transfer.packets.utils.Packet;
import de.codingair.warpsystem.base.transfer.utils.PacketListener;

public class BungeePacketListener extends PacketListener {
    @Override
    public void onReceive(Packet packet, String extra) {
    }

    @Override
    public boolean onSend(Packet packet) {
        return false;
    }
}
