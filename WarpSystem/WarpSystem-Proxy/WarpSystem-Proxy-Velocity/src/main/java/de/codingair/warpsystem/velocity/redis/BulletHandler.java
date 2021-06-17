package de.codingair.warpsystem.velocity.redis;

import de.codingair.warpsystem.core.proxy.redis.RedisHandler;
import de.codingair.warpsystem.velocity.base.WarpSystem;
import net.nitrado.pubsub.PubSub;
import net.nitrado.pubsub.PubSubConnection;
import net.nitrado.pubsub.PubSubObject;

import java.io.*;

public class BulletHandler extends RedisHandler {
    private final PubSub pubSub;

    public BulletHandler() {
        super(getProxyId(), "warpsystem");
        this.pubSub = PubSubConnection.get(super.channel);
    }

    private static String getProxyId() {
        return WarpSystem.proxy().getBoundAddress().getHostName() + ":" + WarpSystem.proxy().getBoundAddress().getPort();
    }

    @Override
    public void send(byte[] data) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(stream);

        try {
            out.writeUTF(source);
            out.writeUTF(new String(data));
            pubSub.publish(channel, new PacketPayload(source, stream.toString()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void registerChannel() {
        pubSub.subscribe("packets", PacketPayload.class, packetPayload -> {
            if (packetPayload.source.equals(source)) {
                return; // Ignore data from own proxy
            }

            DataInputStream in = new DataInputStream(new ByteArrayInputStream(packetPayload.data.getBytes()));

            try {
                String source = in.readUTF();

                if (source.equals(this.source)) return;

                byte[] data = in.readUTF().getBytes();
                sink.receive(data, source);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
    }

    @Override
    public void unregisterChannel() {
        try {
            pubSub.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class PacketPayload implements PubSubObject {
        private final String source;
        private final String data;

        public PacketPayload(String source, String data) {
            this.source = source;
            this.data = data;
        }
    }
}
