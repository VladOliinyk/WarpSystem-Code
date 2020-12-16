package de.codingair.warpsystem.spigot.transfer.spigot;

import de.codingair.codingapi.tools.Callback;
import de.codingair.codingapi.transfer.core.PacketListener;
import de.codingair.codingapi.transfer.packets.utils.Packet;
import de.codingair.codingapi.transfer.spigot.SpigotDataHandler;
import de.codingair.warpsystem.base.transfer.packets.utils.AnswerPacket;
import de.codingair.warpsystem.base.transfer.packets.utils.AssignedPacket;
import de.codingair.warpsystem.base.transfer.packets.utils.PacketType;
import de.codingair.warpsystem.base.transfer.packets.utils.RequestPacket;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.*;

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

    @Override
    public void send(Player player, Packet packet, int timeOut) {
        if(!Bukkit.getOnlinePlayers().isEmpty()) {
            if(player == null) {
                Optional<? extends Player> opt = Bukkit.getOnlinePlayers().stream().findAny();
                if(!opt.isPresent()) return;
                player = opt.get();
            }

            ByteArrayOutputStream b = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(b);

            int id = getId(packet.getClass());
            if(id == -1) throw new IllegalStateException(packet.getClass() + " is not registered!");

            if(packet instanceof RequestPacket && ((RequestPacket<?>) packet).getCallback() != null) {
                if(callbacks.get(((RequestPacket<?>) packet).getUniqueId()) != null) ((RequestPacket<?>) packet).checkUUID(this.callbacks.keySet());
                callbacks.put(((RequestPacket<?>) packet).getUniqueId(), ((RequestPacket<?>) packet).getCallback());

                if(timeOut > 0) this.timeOut.add(((RequestPacket<?>) packet).getUniqueId(), timeOut);
            }

            try {
                out.writeShort(id);
                packet.write(out);
            } catch(IOException e) {
                e.printStackTrace();
            }

            List<PacketListener> listeners = new ArrayList<>(this.listeners);
            for(PacketListener listener : listeners) {
                if(listener.onSend(packet)) return;
            }
            listeners.clear();

            player.sendPluginMessage(this.plugin, channelProxy, b.toByteArray());
        }
    }

    @Override
    public void onReceive(Packet packet, Player player) {
        if(packet instanceof AnswerPacket) {
            UUID uniqueId = ((AssignedPacket) packet).getUniqueId();
            Callback callback;
            if((callback = this.callbacks.remove(uniqueId)) == null) return;
            callback.accept(((AnswerPacket) packet).getValue());

            this.timeOut.remove(uniqueId);
        }

        Set<PacketListener<Player>> listeners = new HashSet<>(this.listeners);
        for(PacketListener listener : listeners) {
            listener.onReceive(packet, null);
        }
        listeners.clear();
    }
}
