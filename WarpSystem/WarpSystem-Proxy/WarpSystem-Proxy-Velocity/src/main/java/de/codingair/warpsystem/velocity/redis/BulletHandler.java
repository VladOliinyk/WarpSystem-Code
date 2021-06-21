package de.codingair.warpsystem.velocity.redis;

import de.codingair.warpsystem.core.proxy.redis.RedisHandler;
import net.nitrado.pubsub.PubSub;
import net.nitrado.pubsub.PubSubConnection;
import net.nitrado.pubsub.PubSubObject;

import java.io.IOException;
import java.util.UUID;

public class BulletHandler extends RedisHandler {
    private static final UUID id = UUID.randomUUID();

    private final PubSub pubSub;

    public BulletHandler() {
        super(getProxyId(), "warpsystem");
        this.pubSub = PubSubConnection.get(super.channel);
    }

    private static String getProxyId() {
        return id.toString();
    }

    @Override
    public void send(byte[] data) {
        pubSub.publish(channel, new PacketPayload(source, data));
    }

    @Override
    public void registerChannel() {
        pubSub.subscribe(channel, PacketPayload.class, packetPayload -> {
            String source = packetPayload.source;

            if (source.equals(this.source)) {
                return; // Ignore data from own proxy
            }

            sink.receive(packetPayload.data, source);
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
        private final byte[] data;

        public PacketPayload(String source, byte[] data) {
            this.source = source;
            this.data = data;
        }
    }
}
