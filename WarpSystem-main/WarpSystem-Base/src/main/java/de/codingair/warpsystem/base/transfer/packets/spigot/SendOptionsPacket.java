package de.codingair.warpsystem.base.transfer.packets.spigot;

import de.codingair.warpsystem.base.transfer.serializeable.ServerOptions;
import de.codingair.warpsystem.base.transfer.packets.utils.Packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class SendOptionsPacket implements Packet {
    private ServerOptions options;

    public SendOptionsPacket() {
    }

    public SendOptionsPacket(ServerOptions options) {
        this.options = options;
    }

    @Override
    public void write(DataOutputStream out) throws IOException {
        options.write(out);
    }

    @Override
    public void read(DataInputStream in) throws IOException {
        options = new ServerOptions();
        options.read(in);
    }

    public ServerOptions getOptions() {
        return options;
    }
}
