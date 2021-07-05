package de.codingair.warpsystem.core.transfer.packets.proxy;

import de.codingair.packetmanagement.packets.Packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class SendGlobalWarpNamesPacket implements Packet {
    private String warp, server;
    private boolean reset;

    public SendGlobalWarpNamesPacket() {
    }

    public SendGlobalWarpNamesPacket(String warp, String server, boolean reset) {
        this.warp = warp;
        this.server = server;
        this.reset = reset;
    }

    @Override
    public void write(DataOutputStream out) throws IOException {
        out.writeUTF(this.warp);
        out.writeUTF(this.server);
        out.writeBoolean(this.reset);
    }

    @Override
    public void read(DataInputStream in) throws IOException {
        this.warp = in.readUTF();
        this.server = in.readUTF();
        this.reset = in.readBoolean();
    }

    public String getWarp() {
        return warp;
    }

    public String getServer() {
        return server;
    }

    public boolean isReset() {
        return reset;
    }
}
