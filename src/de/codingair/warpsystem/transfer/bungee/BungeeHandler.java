package de.codingair.warpsystem.transfer.bungee;

import de.codingair.codingapi.transfer.bungee.BungeeDataHandler;
import de.codingair.warpsystem.transfer.packets.utils.PacketType;
import net.md_5.bungee.api.plugin.Plugin;

public class BungeeHandler extends BungeeDataHandler {
    public BungeeHandler(Plugin plugin) {
        super(plugin);
    }

    @Override
    public void registering() {
        for(PacketType value : PacketType.values()) {
            registerPacket(value.getPacket());
        }
    }
}
