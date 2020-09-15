package de.codingair.warpsystem.transfer.bungee;

import de.codingair.codingapi.tools.Callback;
import de.codingair.codingapi.transfer.bungee.BungeeDataHandler;
import de.codingair.codingapi.transfer.packets.utils.Packet;
import de.codingair.codingapi.transfer.utils.PacketListener;
import de.codingair.warpsystem.transfer.packets.utils.AnswerPacket;
import de.codingair.warpsystem.transfer.packets.utils.AssignedPacket;
import de.codingair.warpsystem.transfer.packets.utils.PacketType;
import de.codingair.warpsystem.transfer.packets.utils.RequestPacket;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.plugin.Plugin;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.UUID;

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

    @Override
    public void send(Packet packet, ServerInfo server) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(stream);

        int id = getId(packet.getClass());
        if(id == -1) throw new IllegalStateException(packet.getClass() + " is not registered!");

        if(packet instanceof RequestPacket && ((RequestPacket<?>) packet).getCallback() != null) {
            if(callbacks.get(((RequestPacket<?>) packet).getUniqueId()) != null) ((RequestPacket<?>) packet).checkUUID(this.callbacks.keySet());
            callbacks.put(((RequestPacket<?>) packet).getUniqueId(), ((RequestPacket<?>) packet).getCallback());
        }

        try {
            out.writeShort(id);
            packet.write(out);
        } catch(IOException e) {
            e.printStackTrace();
        }

        for(PacketListener listener : listeners) {
            if(listener.onSend(packet)) return;
        }

        server.sendData(getChannel, stream.toByteArray());
    }

    @Override
    public void onReceive(Packet packet, ServerInfo server) {
        if(packet instanceof AnswerPacket) {
            UUID uniqueId = ((AssignedPacket) packet).getUniqueId();
            Callback callback;
            if((callback = this.callbacks.remove(uniqueId)) == null) return;
            callback.accept(((AnswerPacket) packet).getValue());
        }

        for(PacketListener listener : listeners) {
            listener.onReceive(packet, server);
        }
    }
}
