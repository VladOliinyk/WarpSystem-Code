package de.codingair.warpsystem.transfer.utils;

import de.codingair.codingapi.server.reflections.IReflection;
import de.codingair.warpsystem.transfer.packets.utils.Packet;

public interface PacketListener extends de.codingair.codingapi.transfer.utils.PacketListener {
    default void onReceive(de.codingair.codingapi.transfer.packets.utils.Packet packet, Object server) {
        onReceive((Packet) packet, getName(server));
    }

    default boolean onSend(de.codingair.codingapi.transfer.packets.utils.Packet packet) {
        return onSend((Packet) packet);
    }

    void onReceive(Packet packet, String extra);

    boolean onSend(Packet packet);

    default String getName(Object o) {
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
