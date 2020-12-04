package de.codingair.warpsystem.velocity.base.managers;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.proxy.Player;
import de.codingair.warpsystem.base.transfer.packets.bungee.PacketVanishInfo;
import de.codingair.warpsystem.base.transfer.packets.utils.Packet;
import de.codingair.warpsystem.base.transfer.packets.utils.PacketType;
import de.codingair.warpsystem.base.transfer.utils.PacketListener;
import de.codingair.warpsystem.velocity.base.WarpSystem;
import de.codingair.warpsystem.velocity.base.events.ServerInitializeEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class VanishManager extends PacketListener {
    private final List<String> vanished = new ArrayList<>();

    @Override
    public void onReceive(Packet packet, String extra) {
        if(packet.getType() == PacketType.PacketVanishInfo) {
            PacketVanishInfo p = (PacketVanishInfo) packet;
            if(p.isVanished()) {
                if(!vanished.contains(p.getPlayer())) vanished.add(p.getPlayer().toLowerCase());
            } else vanished.remove(p.getPlayer());
        }
    }

    @Override
    public boolean onSend(Packet packet) {
        return false;
    }

    @Subscribe
    public void onInit(ServerInitializeEvent e) {
        List<String> l = new ArrayList<>(vanished);
        for(String s : l) {
            Optional<Player> p = WarpSystem.proxy().getPlayer(s);
            if(!p.isPresent() || !p.get().getCurrentServer().isPresent()) vanished.remove(s);
            else if(p.get().getCurrentServer().get().getServer().equals(e.getServer())) vanished.remove(s);
        }
        l.clear();
    }

    @Subscribe
    public void onQuit(DisconnectEvent e) {
        vanished.remove(e.getPlayer().getUsername());
    }

    @Subscribe
    public void onQuit(ServerConnectedEvent e) {
        vanished.remove(e.getPlayer().getUsername());
    }

    public List<String> getVanished() {
        return vanished;
    }

    public boolean isVanished(String player) {
        return vanished.contains(player.toLowerCase());
    }
}
