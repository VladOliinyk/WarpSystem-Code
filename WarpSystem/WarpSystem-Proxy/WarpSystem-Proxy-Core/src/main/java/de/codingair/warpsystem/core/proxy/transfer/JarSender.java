package de.codingair.warpsystem.core.proxy.transfer;

import de.codingair.codingapi.tools.Call;
import de.codingair.codingapi.utils.Value;
import de.codingair.packetmanagement.utils.Direction;
import de.codingair.warpsystem.core.proxy.Core;
import de.codingair.warpsystem.core.proxy.utils.ScheduleTask;
import de.codingair.warpsystem.core.proxy.utils.Server;
import de.codingair.warpsystem.core.transfer.packets.proxy.SendJarPacket;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class JarSender implements Runnable {
    private final Server server;
    private final Call call;
    public File file;

    public JarSender(Server server, File file, Call call) {
        this.server = server;
        this.file = file;
        this.call = call == null ? () -> {
        } : call;
    }

    @Override
    public void run() {
        try {
            List<SendJarPacket> packets = prepare();
            Value<ScheduleTask> task = new Value<>(null);

            task.setValue(Core.getPlugin().schedule(new Runnable() {
                int index = 0;

                @Override
                public void run() {
                    Core.getPlugin().dataHandler().send(packets.get(index++), server, Direction.DOWN);
                    if (index == packets.size()) {
                        task.getValue().cancel();
                        call.proceed();
                    }
                }
            }, 0, 50, TimeUnit.MILLISECONDS));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<SendJarPacket> prepare() throws IOException {
        byte[] data = Files.readAllBytes(file.toPath());
        if (data.length == 0) throw new IllegalStateException("File is empty!");

        List<byte[]> slices = slice(SendJarPacket.SIZE, data);
        return transform(slices);
    }

    private List<byte[]> slice(int size, byte[] data) {
        List<byte[]> slices = new ArrayList<>();

        for (int i = 0; i < data.length; i += size) {
            int remain = Math.min(data.length - i, size);
            byte[] copy = new byte[remain];
            System.arraycopy(data, i, copy, 0, remain);
            slices.add(copy);
        }

        return slices;
    }

    private List<SendJarPacket> transform(List<byte[]> slices) {
        List<SendJarPacket> packets = new ArrayList<>();

        packets.add(new SendJarPacket(0, file.getName().getBytes()));

        for (byte[] slice : slices) {
            packets.add(new SendJarPacket(1, slice));
        }

        packets.get(packets.size() - 1).opt = 2;            //end

        slices.clear();
        return packets;
    }
}
