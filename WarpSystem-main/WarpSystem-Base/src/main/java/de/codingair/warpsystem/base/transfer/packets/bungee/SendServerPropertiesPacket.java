package de.codingair.warpsystem.base.transfer.packets.bungee;

import de.codingair.warpsystem.base.transfer.packets.spigot.utils.ServerPing;
import de.codingair.warpsystem.base.transfer.packets.utils.Packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SendServerPropertiesPacket implements Packet {
    private HashMap<String, ServerPing> properties;

    public SendServerPropertiesPacket() {
    }

    public SendServerPropertiesPacket(HashMap<String, ServerPing> properties) {
        this.properties = properties;
    }

    @Override
    public void write(DataOutputStream out) throws IOException {
        out.writeShort((short) properties.size());

        for(Map.Entry<String, ServerPing> e : properties.entrySet()) {
            out.writeUTF(e.getKey());
            e.getValue().write(out);
        }
    }

    @Override
    public void read(DataInputStream in) throws IOException {
        int size = in.readUnsignedShort();
        this.properties = new HashMap<>();

        for(int i = 0; i < size; i++) {
            ServerPing properties = new ServerPing();

            String server = in.readUTF();
            properties.read(in);
            this.properties.put(server, properties);
        }
    }

    public HashMap<String, ServerPing> getProperties() {
        return properties;
    }
}
