package de.codingair.warpsystem.bungee.redis;

import com.imaginarycode.minecraft.redisbungee.RedisBungee;
import com.imaginarycode.minecraft.redisbungee.events.PubSubMessageEvent;
import de.codingair.warpsystem.core.proxy.redis.RedisHandler;
import de.codingair.warpsystem.core.proxy.transfer.CoreDataHandler;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.io.*;
import java.util.Base64;

public class RedisBungeeHandler extends RedisHandler implements Listener {
    public RedisBungeeHandler() {
        super(RedisBungee.getApi().getServerId(), CoreDataHandler.redisChannel);
    }

    @Override
    public void send(byte[] data) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(stream);

        try {
            out.writeUTF(source);

            //use Base64 to avoid virtual ends for the packet stream
            byte[] encoded = Base64.getEncoder().encode(data);
            out.writeUTF(new String(encoded));

            RedisBungee.getApi().sendChannelMessage(channel, stream.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void registerChannel() {
        RedisBungee.getApi().registerPubSubChannels(channel);
    }

    @Override
    public void unregisterChannel() {
        RedisBungee.getApi().unregisterPubSubChannels(channel);
    }

    @EventHandler
    public void onPubSub(PubSubMessageEvent e) {
        if (e.getChannel().equals(channel)) {
            DataInputStream in = new DataInputStream(new ByteArrayInputStream(e.getMessage().getBytes()));

            try {
                String source = in.readUTF();
                if (source.equals(this.source)) return;

                String encoded = in.readUTF();
                byte[] data = Base64.getDecoder().decode(encoded.getBytes());

                sink.receive(data, source);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}
