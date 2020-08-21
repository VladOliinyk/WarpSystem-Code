package de.codingair.warpsystem.transfer.jar;

import de.codingair.warpsystem.spigot.base.WarpSystem;
import de.codingair.warpsystem.transfer.packets.utils.Packet;
import de.codingair.warpsystem.transfer.packets.utils.PacketType;
import de.codingair.warpsystem.transfer.utils.PacketListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class JarReceiver implements PacketListener {
    private final List<byte[]> data = new ArrayList<>();
    private String name;

    private void receive(SendJarPacket packet) {
        if(packet.opt == 0) {
            if(!data.isEmpty()) throw new IllegalStateException("Too many opt=0 packets!");
            name = new String(packet.data);
            return;
        }

        data.add(packet.data);

        if(packet.opt == 2) {
            try {
                proceed();
            } catch(IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void proceed() throws IOException {
        deleteOld();
        write();
        data.clear();
        WarpSystem.getInstance().reload(true);
    }

    private void deleteOld() {
        File f = getCurrentJar();
        f.delete();
    }

    private File getCurrentJar() {
        for(File file : WarpSystem.getInstance().getDataFolder().getParentFile().listFiles()) {
            if(file.isDirectory()) continue;
            if(file.getName().toLowerCase().contains("warpsystem")) {
                return file;
            }
        }

        return null;
    }

    private void write() throws IOException {
        File file = new File(WarpSystem.getInstance().getDataFolder().getParentFile(), name);
        file.createNewFile();

        byte[] entireData = new byte[(data.size() - 1) * JarSender.SIZE + data.get(data.size() - 1).length];
        int i = 0;
        for(byte[] b : data) {
            System.arraycopy(b, 0, entireData, i, b.length);
            i += b.length;
        }

        FileOutputStream out = new FileOutputStream(file);
        out.write(entireData);
        out.close();
    }

    @Override
    public void onReceive(Packet packet, String extra) {
        if(packet.getType() == PacketType.SendJarPacket) {
            receive((SendJarPacket) packet);
        }
    }

    @Override
    public boolean onSend(Packet packet) {
        return false;
    }
}
