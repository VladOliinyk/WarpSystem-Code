package de.codingair.warpsystem.base.transfer.packets.general;

import de.codingair.warpsystem.base.transfer.packets.utils.AnswerPacket;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class StringPacket extends AnswerPacket<String> {

    public StringPacket() {
    }

    public StringPacket(String s) {
        super(s);
    }

    @Override
    public void write(DataOutputStream out) throws IOException {
        out.writeBoolean(getValue() != null);
        if(getValue() != null) out.writeUTF(getValue());
        super.write(out);
    }

    @Override
    public void read(DataInputStream in) throws IOException {
        if(in.readBoolean()) setValue(in.readUTF());
        else setValue(null);
        super.read(in);
    }
}
