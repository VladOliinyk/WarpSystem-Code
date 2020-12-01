package de.codingair.warpsystem.transfer.utils;

import de.codingair.codingapi.server.reflections.IReflection;
import de.codingair.warpsystem.transfer.packets.utils.Packet;

public abstract class PacketListener implements de.codingair.codingapi.transfer.utils.PacketListener {
    public void onReceive(de.codingair.codingapi.transfer.packets.utils.Packet packet, Object server) {
        onReceive((Packet) packet, getName(server));
    }

    public boolean onSend(de.codingair.codingapi.transfer.packets.utils.Packet packet) {
        return onSend((Packet) packet);
    }

    public abstract void onReceive(Packet packet, String extra);

    public abstract boolean onSend(Packet packet);

    public String getName(Object o) {
        if(o == null) return "BungeeCord";

        try {
            Class<?> c = Class.forName("net.md_5.bungee.api.config.ServerInfo");
            IReflection.MethodAccessor getName = IReflection.getMethod(c, "getName", String.class, new Class[0]);
            return (String) getName.invoke(o);
        } catch(ClassNotFoundException e) {
            e.printStackTrace();
            return "BungeeCord";
        }
    }
}
