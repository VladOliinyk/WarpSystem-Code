package de.codingair.warpsystem.core.proxy.redis.trevor;

import de.codingair.warpsystem.core.proxy.redis.RedisCore;
import tech.tagline.trevor.api.network.event.EventProcessor;
import tech.tagline.trevor.api.network.event.NetworkIntercomEvent;
import tech.tagline.trevor.api.network.payload.NetworkPayload;

public class PacketPayload extends NetworkPayload<String> {
    private final byte[] data;

    /**
     * Gson constructor
     */
    private PacketPayload() {
        data = new byte[0];
    }

    protected PacketPayload(String source, byte[] data) {
        super(source);
        this.data = data;
    }

    @Override
    public EventProcessor.EventAction<NetworkIntercomEvent> process(EventProcessor processor) {
        RedisCore.core().getHandler().receive(data, source());

        //dummy
        return new EventProcessor.EventAction<>(null, networkIntercomEvent -> null);
    }

    public byte[] data() {
        return data;
    }

    public static PacketPayload of(String source, byte[] data) {
        return new PacketPayload(source, data);
    }
}
