package de.codingair.warpsystem.core.proxy.base.handlers;

import de.codingair.codingapi.tools.io.lib.JSONArray;
import de.codingair.codingapi.tools.io.utils.DataMask;
import de.codingair.packetmanagement.utils.Direction;
import de.codingair.warpsystem.core.proxy.Core;
import de.codingair.warpsystem.core.proxy.utils.Server;
import de.codingair.warpsystem.core.transfer.packets.proxy.SendServerWorldNamesPacket;
import de.codingair.warpsystem.core.transfer.packets.spigot.SendWorldNamesPacket;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class WorldHandler {
    protected static final String configFile = "Worlds";
    protected final ConcurrentHashMap<String, Set<String>> worlds = new ConcurrentHashMap<>();

    public void load(DataMask mask) {
        for (String s : mask.keySet(false)) {
            JSONArray array = mask.getList(s);

            Set<String> worlds = new HashSet<>(array.size());
            for (Object o : array) {
                if(o instanceof String) worlds.add((String) o);
            }

            this.worlds.put(s, worlds);
        }
    }

    public void save(DataMask mask) {
        mask.clear();

        for (Map.Entry<String, Set<String>> e : worlds.entrySet()) {
            JSONArray array = new JSONArray();
            array.addAll(e.getValue());
            mask.put(e.getKey(), array);
        }
    }

    public void onUpdate(SendWorldNamesPacket packet, Server<?> connection) {
        worlds.put(connection.getName(), packet.getWorlds());
        Core.getPlugin().dataHandler().send(new SendServerWorldNamesPacket(this.worlds), connection, Direction.DOWN);
    }
}
