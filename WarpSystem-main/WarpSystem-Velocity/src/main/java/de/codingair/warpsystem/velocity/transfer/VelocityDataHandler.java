package de.codingair.warpsystem.velocity.transfer;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import de.codingair.codingapi.tools.Callback;
import de.codingair.codingapi.transfer.core.DataHandler;
import de.codingair.codingapi.transfer.core.PacketListener;
import de.codingair.warpsystem.base.transfer.packets.utils.*;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.UUID;

public class VelocityDataHandler extends DataHandler<RegisteredServer> {
    protected final ProxyServer proxy;
    protected final Object plugin;
    protected final ChannelListener listener = new ChannelListener(this);
    private MinecraftChannelIdentifier in;
    private MinecraftChannelIdentifier out;

    public VelocityDataHandler(Object plugin, ProxyServer proxy, String name) {
        super(name.toLowerCase().trim().replace(" ", "_"));
        this.plugin = plugin;
        this.proxy = proxy;
    }

    @Override
    public void registering() {
        for(PacketType value : PacketType.values()) {
            registerPacket(value.getPacket());
        }
    }

    public void onEnable() {
        String[] a = channelProxy.split(":");
        proxy.getChannelRegistrar().register(in = MinecraftChannelIdentifier.create(a[0], a[1]));
        a = channelBackend.split(":");
        proxy.getChannelRegistrar().register(out = MinecraftChannelIdentifier.create(a[0], a[1]));

        proxy.getEventManager().register(plugin, this.listener);
    }

    public void onDisable() {
        if(in != null) proxy.getChannelRegistrar().unregister(in);
        if(out != null) proxy.getChannelRegistrar().unregister(out);
        this.listeners.clear();
        proxy.getEventManager().unregisterListener(plugin, this.listener);
    }

    @Override
    public void send(byte[] bytes, RegisteredServer server) {
        server.sendPluginMessage(this.out, bytes);
    }

    public void send(Packet packet, RegisteredServer server) {
        if(server.getPlayersConnected().isEmpty()) throw new IllegalStateException("Cannot send packet to an empty server!");

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

        server.sendPluginMessage(this.out, stream.toByteArray());
    }

    public void onReceive(Packet packet, ServerConnection server) {
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

    public MinecraftChannelIdentifier getIn() {
        return in;
    }

    public MinecraftChannelIdentifier getOut() {
        return out;
    }
}
