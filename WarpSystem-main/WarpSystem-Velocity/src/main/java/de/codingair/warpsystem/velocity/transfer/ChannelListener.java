package de.codingair.warpsystem.velocity.transfer;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ServerConnection;
import de.codingair.warpsystem.base.transfer.packets.utils.Packet;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class ChannelListener {
    protected VelocityDataHandler velocityDataHandler;

    public ChannelListener(VelocityDataHandler velocityDataHandler) {
        this.velocityDataHandler = velocityDataHandler;
    }

    @Subscribe
    public void onPluginMessage(PluginMessageEvent e) {
        if(e.getIdentifier().getId().equals(velocityDataHandler.getChannelProxy())) {
            e.setResult(PluginMessageEvent.ForwardResult.handled());

            ServerConnection s = (ServerConnection) e.getSource();
            velocityDataHandler.onReceive(e.getData(), s.getServer());
        }
    }

}
