package de.codingair.warpsystem.core.proxy.base.handlers;

import de.codingair.codingapi.tools.io.JSON.BungeeJSON;
import de.codingair.codingapi.tools.io.utils.DataMask;
import de.codingair.packetmanagement.utils.Direction;
import de.codingair.warpsystem.core.features.cooldown.Cooldown;
import de.codingair.warpsystem.core.features.cooldown.ICooldownManager;
import de.codingair.warpsystem.core.proxy.Core;
import de.codingair.warpsystem.core.proxy.utils.Player;
import de.codingair.warpsystem.core.proxy.utils.Server;
import de.codingair.warpsystem.core.transfer.handlers.CooldownDataPacketHandler;
import de.codingair.warpsystem.core.transfer.handlers.CooldownPacketHandler;
import de.codingair.warpsystem.core.transfer.packets.spigot.CooldownDataPacket;
import de.codingair.warpsystem.core.transfer.packets.spigot.CooldownPacket;

import java.util.*;

public abstract class CooldownHandler implements ICooldownManager {
    public static final String configFile = "Cooldown";
    //expired cooldown will be removed on access (get, save)
    protected final HashMap<UUID, List<Cooldown>> cache = new HashMap<>();

    public CooldownHandler() {
        Core.getPlugin().dataHandler().registerHandler(CooldownDataPacket.class, new CooldownDataPacketHandler(this));
        Core.getPlugin().dataHandler().registerHandler(CooldownPacket.class, new CooldownPacketHandler(this));
    }

    public void load(DataMask config) {
        long time = config.getLong("Date", -1L);
        if (time == -1) return; //date does not exist -> stop here

        for (String key : config.keySet(false)) {
            try {
                UUID id = UUID.fromString(key);

                List<?> data = config.getList(key);
                if (data != null) {
                    for (Object s : data) {
                        if (s instanceof Map) {
                            try {
                                Cooldown cooldown = new Cooldown(id);
                                BungeeJSON json = new BungeeJSON((Map<?, ?>) s);
                                cooldown.read(json, time);
                                addCooldown(cooldown);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            } catch (IllegalArgumentException ignored) {
                //might be the date tag
            }
        }
    }

    public void save(DataMask config) {
        config.clear();

        long time = System.currentTimeMillis();

        cache.entrySet().removeIf(entry -> {
            List<BungeeJSON> configData = new ArrayList<>();
            List<Cooldown> data = entry.getValue();

            data.removeIf(cooldown -> {
                if (cooldown.getRemainingTime() == 0) return true;

                BungeeJSON json = new BungeeJSON();
                cooldown.write(json, time);
                configData.add(json);
                return false;
            });

            if (!configData.isEmpty()) config.put(entry.getKey().toString(), configData);
            return data.isEmpty();
        });

        if (!cache.isEmpty()) config.put("Date", time);
    }

    public void addCooldown(Cooldown cooldown) {
        if (cooldown.getRemainingTime() != 0) cache.computeIfAbsent(cooldown.getPlayer(), k -> new ArrayList<>()).add(cooldown);
    }

    protected void sendData(Server server) {
        cache.forEach(((uuid, data) -> Core.getPlugin().dataHandler().send(new CooldownDataPacket(data.toArray(new Cooldown[0])), server, Direction.DOWN)));
    }

    protected void sendDataFor(Player player, Server server) {
        if (Core.getServerManager().isOnline(server)) {
            List<Cooldown> data = cache.get(player.getUniqueId());
            if (data != null) {
                Core.getPlugin().dataHandler().send(new CooldownDataPacket(data.toArray(new Cooldown[0])), server, Direction.DOWN);
            }
        }
    }
}
