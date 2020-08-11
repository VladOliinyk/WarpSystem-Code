package de.codingair.warpsystem.transfer.spigot;

import de.codingair.codingapi.transfer.spigot.SpigotDataHandler;
import de.codingair.warpsystem.transfer.packets.utils.PacketType;
import org.bukkit.plugin.java.JavaPlugin;

public class SpigotHandler extends SpigotDataHandler {
    public SpigotHandler(JavaPlugin plugin) {
        super(plugin);
    }

    @Override
    public void registering() {
        for(PacketType value : PacketType.values()) {
            registerPacket(value.getPacket());
        }
    }
}
