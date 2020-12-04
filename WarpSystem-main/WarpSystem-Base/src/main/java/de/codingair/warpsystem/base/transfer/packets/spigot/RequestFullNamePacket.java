package de.codingair.warpsystem.base.transfer.packets.spigot;

import de.codingair.codingapi.tools.Callback;
import de.codingair.warpsystem.base.transfer.packets.utils.RequestPacket;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class RequestFullNamePacket extends RequestPacket<String> {
    private String name; //not the full name

    public RequestFullNamePacket() {
    }

    public RequestFullNamePacket(Callback<String> callback, String name) {
        super(callback);
        this.name = name;
    }

    @Override
    public void write(DataOutputStream out) throws IOException {
        super.write(out);
        out.writeUTF(name);
    }

    @Override
    public void read(DataInputStream in) throws IOException {
        super.read(in);
        this.name = in.readUTF();
    }

    public String getName() {
        return name;
    }
}
