package de.codingair.warpsystem.velocity.features.randomtp;

import com.velocitypowered.api.proxy.server.RegisteredServer;
import de.codingair.warpsystem.base.transfer.packets.spigot.QueueRTPUsagePacket;
import de.codingair.warpsystem.base.utils.Manager;
import de.codingair.warpsystem.velocity.api.files.ConfigFile;
import de.codingair.warpsystem.velocity.api.files.Configuration;
import de.codingair.warpsystem.velocity.base.WarpSystem;
import de.codingair.warpsystem.velocity.features.FeatureType;

import java.util.*;

public class RandomTPManager implements Manager {
    private final HashMap<String, List<String>> worlds = new HashMap<>();
    private final HashMap<String, List<UUID>> queuedEntries = new HashMap<>();
    private ConfigFile file;
    private Configuration config;

    public static RandomTPManager getInstance() {
        return WarpSystem.getInstance().getDataManager().getManager(FeatureType.RANDOM_TP);
    }

    @Override
    public boolean load(boolean loader) {
        if(!loader) WarpSystem.log("  > Loading RandomTPManager");
        destroy();
        WarpSystem.getInstance().getFileManager().loadFile("RTP_Queue", "/");
        ConfigFile queue = WarpSystem.getInstance().getFileManager().getFile("RTP_Queue");

        int queueSize = 0;
        Configuration config = queue.getSimpleConfig();
        for(String server : config.keys(false)) {
            List<String> l = (List<String>) config.getList(server);
            List<UUID> data = new ArrayList<>();
            for(String s : l) {
                data.add(UUID.fromString(s));
                queueSize++;
            }
            queuedEntries.put(server, data);
        }

        if(!loader) WarpSystem.log("    ...got " + queueSize + " queued random tp(s)");

        WarpSystem.getInstance().getFileManager().loadFile("Worlds", "/");
        file = WarpSystem.getInstance().getFileManager().getFile("Worlds");
        this.config = file.getSimpleConfig();

        int size = 0;
        for(String key : config.keys(false)) {
            List<String> data = (List<String>) config.getList(key);
            size += data.size();
            addWorldData(key, data);
        }
        if(!loader) WarpSystem.log("    ...got " + size + " registered random tp world(s)");

        RandomTPListener l = new RandomTPListener();
        WarpSystem.getInstance().getDataHandler().register(l);
        WarpSystem.proxy().getEventManager().register(WarpSystem.getInstance(), l);
        return true;
    }

    @Override
    public void save(boolean saver) {
        if(!saver) WarpSystem.log("  > Saving RandomTPManager");
        saveQueue(saver);

        file.clear();
        int size = 0;
        for(String s : worlds.keySet()) {
            List<String> worlds = this.worlds.get(s);
            config.set(s, worlds);
            size += worlds.size();
        }
        file.save();
        if(!saver) WarpSystem.log("    ...saved " + size + " registered random tp world(s)");
    }

    private void saveQueue(boolean saver) {
        ConfigFile queue = WarpSystem.getInstance().getFileManager().getFile("RTP_Queue");
        queue.clear();
        Configuration config = queue.getSimpleConfig();

        int size = 0;
        for(String server : queuedEntries.keySet()) {
            List<UUID> value = queuedEntries.get(server);
            if(value != null && !value.isEmpty()) {
                List<String> data = new ArrayList<>();
                for(UUID uuid : value) {
                    data.add(uuid.toString());
                }
                config.set(server, data);
            }

            size += value.size();
        }
        queue.save();
        if(!saver) WarpSystem.log("    ...saved " + size + " queued random tp(s)");
    }

    @Override
    public void destroy() {
        this.queuedEntries.values().forEach(List::clear);
        this.queuedEntries.clear();
        this.worlds.values().forEach(List::clear);
        this.worlds.clear();
    }

    public void addQueueEntry(UUID uuid, String server) {
        List<UUID> l = queuedEntries.computeIfAbsent(server, s -> new ArrayList<>());
        l.add(uuid);
    }

    public void updateQueue(RegisteredServer info) {
        List<UUID> l = queuedEntries.remove(info.getServerInfo().getName());
        if(l != null && !l.isEmpty()) {
            WarpSystem.getInstance().getDataHandler().send(new QueueRTPUsagePacket(l), info);
        }
    }

    public void addWorldData(String server, List<String> worlds) {
        if(worlds == null || worlds.isEmpty()) this.worlds.remove(server);
        else this.worlds.put(server, worlds);
    }

    public List<String> getWorlds(String server) {
        return this.worlds.getOrDefault(server, new ArrayList<>());
    }

    public boolean hasRegisteredServers() {
        return !this.worlds.isEmpty();
    }

    public List<String> getServer() {
        List<String> servers = new ArrayList<>();
        for(String s : this.worlds.keySet()) {
            Optional<RegisteredServer> info = WarpSystem.proxy().getServer(s);
            if(info.isPresent() && WarpSystem.getInstance().getServerManager().isOnline(info.get())) servers.add(s);
        }
        return servers;
    }
}
