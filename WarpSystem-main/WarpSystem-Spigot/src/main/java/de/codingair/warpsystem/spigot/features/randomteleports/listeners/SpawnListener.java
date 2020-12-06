package de.codingair.warpsystem.spigot.features.randomteleports.listeners;

import de.codingair.warpsystem.base.transfer.packets.utils.Packet;
import de.codingair.warpsystem.base.transfer.utils.PacketListener;
import org.bukkit.event.Listener;

public class SpawnListener extends PacketListener implements Listener {
    public SpawnListener() {
    }

    @Override
    public void onReceive(Packet packet, String extra) {

    }

    @Override
    public boolean onSend(Packet packet) {
        return false;
    }
}
