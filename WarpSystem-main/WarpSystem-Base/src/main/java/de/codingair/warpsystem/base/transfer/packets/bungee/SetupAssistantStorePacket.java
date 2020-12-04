package de.codingair.warpsystem.base.transfer.packets.bungee;

import de.codingair.warpsystem.base.transfer.packets.utils.Packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class SetupAssistantStorePacket implements Packet {
    private String message;

    public SetupAssistantStorePacket() {
    }

    public SetupAssistantStorePacket(String message) {
        this.message = message;
    }

    @Override
    public void write(DataOutputStream out) throws IOException {
        out.writeUTF(this.message);
    }

    @Override
    public void read(DataInputStream in) throws IOException {
        this.message = in.readUTF();
    }

    public String getMessage() {
        return message;
    }
}
