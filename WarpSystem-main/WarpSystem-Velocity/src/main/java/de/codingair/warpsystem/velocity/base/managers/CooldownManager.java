package de.codingair.warpsystem.velocity.base.managers;

import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import de.codingair.codingapi.tools.io.JSON.BungeeJSON;
import de.codingair.warpsystem.base.transfer.packets.spigot.CooldownDataPacket;
import de.codingair.warpsystem.base.transfer.packets.spigot.CooldownPacket;
import de.codingair.warpsystem.base.transfer.packets.spigot.utils.Cooldown;
import de.codingair.warpsystem.base.transfer.packets.utils.Packet;
import de.codingair.warpsystem.base.transfer.packets.utils.PacketType;
import de.codingair.warpsystem.base.transfer.utils.PacketListener;
import de.codingair.warpsystem.velocity.api.files.ConfigFile;
import de.codingair.warpsystem.velocity.api.files.Configuration;
import de.codingair.warpsystem.velocity.base.WarpSystem;
import de.codingair.warpsystem.velocity.base.events.ServerProvideOptionsEvent;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class CooldownManager extends PacketListener {
    //expired cooldown will be removed on access (get, save)
    private final HashMap<UUID, List<Cooldown>> cache = new HashMap<>();
    private ConfigFile file;

    public void load() {
        file = WarpSystem.getInstance().getFileManager().loadFile("Cooldown", "/");
        Configuration config = file.getSimpleConfig();

        long time = config.getLong("Date", -1L);
        if(time == -1) return; //date does not exist -> stop here

        for(String key : config.keys(true)) {
            try {
                UUID id = UUID.fromString(key);

                List<?> data = config.getList(key);
                if(data != null) {
                    for(Object s : data) {
                        if(s instanceof Map) {
                            try {
                                Cooldown cooldown = new Cooldown(id);
                                BungeeJSON json = new BungeeJSON((Map<?, ?>) s);
                                cooldown.read(json, time);
                                add(cooldown);
                            } catch(Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            } catch(IllegalArgumentException ignored) {
                //might be the date tag
            }
        }
    }

    public void save() {
        file.clear();

        Configuration config = file.getSimpleConfig();
        long time = System.currentTimeMillis();

        cache.entrySet().removeIf(entry -> {
            List<BungeeJSON> configData = new ArrayList<>();
            List<Cooldown> data = entry.getValue();

            data.removeIf(cooldown -> {
                if(cooldown.getRemainingTime() == 0) return true;

                BungeeJSON json = new BungeeJSON();
                cooldown.write(json, time);
                configData.add(json);
                return false;
            });

            if(!configData.isEmpty()) config.set(entry.getKey().toString(), configData);
            return data.isEmpty();
        });

        if(!cache.isEmpty()) config.set("Date", time);

        file.save();
    }

    private void add(Cooldown cooldown) {
        if(cooldown.getRemainingTime() != 0) cache.computeIfAbsent(cooldown.getPlayer(), k -> new ArrayList<>()).add(cooldown);
    }

    @Subscribe
    public void onInit(ServerProvideOptionsEvent e) {
        if(!e.getOptions().sameVersion()) return;
        cache.forEach(((uuid, data) -> WarpSystem.getInstance().getDataHandler().send(new CooldownDataPacket(data.toArray(new Cooldown[0])), e.getServer())));
    }

    @Subscribe(order = PostOrder.LATE)
    public void onJoin(ServerConnectedEvent e) {
        if(WarpSystem.getInstance().getServerManager().isOnline(e.getServer())) {
            List<Cooldown> data = cache.get(e.getPlayer().getUniqueId());
            if(data != null) {
                WarpSystem.scheduler(() -> WarpSystem.getInstance().getDataHandler().send(new CooldownDataPacket(data.toArray(new Cooldown[0])), e.getServer())).delay(50, TimeUnit.MILLISECONDS).schedule();
            }
        }
    }

    @Override
    public void onReceive(Packet packet, String extra) {
        if(packet.getType() == PacketType.CooldownPacket) {
            CooldownPacket p = (CooldownPacket) packet;
            add(p.getCooldown());
        } else if(packet.getType() == PacketType.CooldownDataPacket) {
            CooldownDataPacket p = (CooldownDataPacket) packet;
            for(Cooldown cooldown : p.getCooldown()) {
                add(cooldown);
            }
        }
    }

    @Override
    public boolean onSend(Packet packet) {
        return false;
    }
}
