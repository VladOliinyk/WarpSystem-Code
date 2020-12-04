package de.codingair.warpsystem.base.transfer.packets.bungee;

import de.codingair.warpsystem.base.transfer.packets.utils.Packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class SendJarPacket implements Packet {
    public static final int SIZE = 32000; //max payload 32767
    public byte opt;
    public byte[] data;

    public SendJarPacket() {
    }

    public SendJarPacket(int opt, byte[] data) {
        this.opt = (byte) opt; //0=start, 1=continue, 2=end
        this.data = data;
    }

    @Override
    public void write(DataOutputStream out) throws IOException {
        out.writeByte(opt);
        if(opt != 1) out.writeShort(data.length);
        out.write(data);
    }

    @Override
    public void read(DataInputStream in) throws IOException {
        opt = in.readByte();
        int length = SIZE;
        if(opt != 1) length = in.readUnsignedShort();

        data = new byte[length];
        in.read(data);
    }
}
