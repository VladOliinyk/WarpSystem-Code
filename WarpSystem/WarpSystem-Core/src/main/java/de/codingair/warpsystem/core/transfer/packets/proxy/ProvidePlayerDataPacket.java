package de.codingair.warpsystem.core.transfer.packets.proxy;

import de.codingair.packetmanagement.packets.Packet;
import de.codingair.warpsystem.core.transfer.utils.PlayerData;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

public class ProvidePlayerDataPacket implements Packet {
    private Collection<PlayerData> data;

    public ProvidePlayerDataPacket() {
    }

    public ProvidePlayerDataPacket(Collection<PlayerData> data) {
        if (data.size() > 64) throw new IllegalArgumentException("Too many names: " + data.size());
        this.data = data;
    }

    @Override
    public void write(DataOutputStream out) throws IOException {
        out.writeByte(this.data.size());

        for (PlayerData player : this.data) {
            player.write(out);
        }
    }

    @Override
    public void read(DataInputStream in) throws IOException {
        int size = in.readUnsignedByte();
        this.data = new ArrayList<>(size);

        for (int i = 0; i < size; i++) {
            PlayerData data = new PlayerData();
            data.read(in);
            this.data.add(data);
        }
    }

    public Collection<PlayerData> getData() {
        return data;
    }
}
