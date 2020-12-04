package de.codingair.warpsystem.base.transfer.utils;

import de.codingair.codingapi.server.reflections.IReflection;
import de.codingair.warpsystem.base.transfer.packets.utils.Packet;

public abstract class PacketListener implements de.codingair.codingapi.transfer.utils.PacketListener {
    private static NameConverter CONVERTER = null;

    public void onReceive(de.codingair.codingapi.transfer.packets.utils.Packet packet, Object server) {
        onReceive((Packet) packet, getName(server));
    }

    public boolean onSend(de.codingair.codingapi.transfer.packets.utils.Packet packet) {
        return onSend((Packet) packet);
    }

    public abstract void onReceive(Packet packet, String extra);

    public abstract boolean onSend(Packet packet);

    public String getName(Object o) {
        return convert(o);
    }

    public static String convert(Object o) {
        if(o == null) return "Proxy";
        if(CONVERTER == null) {
            try {
                Class<?> c = Class.forName("net.md_5.bungee.api.config.ServerInfo");
                IReflection.MethodAccessor getName = IReflection.getMethod(c, "getName", String.class, new Class[0]);
                CONVERTER = o1 -> (String) getName.invoke(o1);
            } catch(ClassNotFoundException e) {
                try {
                    Class<?> sc = Class.forName("com.velocitypowered.api.proxy.ServerConnection");
                    Class<?> si = Class.forName("com.velocitypowered.api.proxy.server.ServerInfo");
                    IReflection.MethodAccessor getServerInfo = IReflection.getMethod(sc, "getServerInfo", si, new Class[0]);
                    IReflection.MethodAccessor getName = IReflection.getMethod(si, "getName", String.class, new Class[0]);
                    CONVERTER = o12 -> (String) getName.invoke(getServerInfo.invoke(o12));
                } catch(ClassNotFoundException e1) {
                    throw new IllegalStateException("Unsupported proxy!");
                }
            }
        }

        return CONVERTER.getName(o);
    }

    private interface NameConverter {
        String getName(Object o);
    }
}
