package de.codingair.warpsystem.bungee.features.playerwarps.listeners;

import de.codingair.warpsystem.bungee.base.WarpSystem;
import de.codingair.warpsystem.bungee.features.playerwarps.managers.PlayerWarpManager;
import de.codingair.warpsystem.base.transfer.packets.bungee.SendPlayerWarpOptionsPacket;
import de.codingair.warpsystem.base.transfer.packets.general.DeletePlayerWarpPacket;
import de.codingair.warpsystem.base.transfer.packets.general.SendPlayerWarpUpdatePacket;
import de.codingair.warpsystem.base.transfer.packets.general.SendPlayerWarpsPacket;
import de.codingair.warpsystem.base.transfer.packets.spigot.PlayerWarpTeleportProcessPacket;
import de.codingair.warpsystem.base.transfer.packets.spigot.RegisterServerForPlayerWarpsPacket;
import de.codingair.warpsystem.base.transfer.packets.utils.Packet;
import de.codingair.warpsystem.base.transfer.packets.utils.PacketType;
import de.codingair.warpsystem.base.transfer.serializeable.Serializable;
import de.codingair.warpsystem.base.transfer.utils.PacketListener;
import de.codingair.warpsystem.base.transfer.packets.spigot.utils.PlayerWarpData;
import de.codingair.warpsystem.base.transfer.packets.spigot.utils.PlayerWarpUpdate;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.ArrayList;
import java.util.List;

public class PlayerWarpListener extends PacketListener implements Listener {

    @Override
    public void onReceive(Packet packet, String extra) {

    }

    @Override
    public boolean onSend(Packet packet) {
        return false;
    }
}
