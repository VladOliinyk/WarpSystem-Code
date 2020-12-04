package de.codingair.warpsystem.velocity.features.spawn.managers;

import com.velocitypowered.api.proxy.server.RegisteredServer;
import de.codingair.warpsystem.base.transfer.packets.general.SendGlobalSpawnOptionsPacket;
import de.codingair.warpsystem.base.utils.Manager;
import de.codingair.warpsystem.velocity.api.files.ConfigFile;
import de.codingair.warpsystem.velocity.api.files.Configuration;
import de.codingair.warpsystem.velocity.base.WarpSystem;
import de.codingair.warpsystem.velocity.features.FeatureType;
import de.codingair.warpsystem.velocity.features.spawn.listeners.ServerListener;

import java.util.Objects;

public class SpawnManager implements Manager {
    private String spawn, respawn;

    public static SpawnManager getInstance() {
        return WarpSystem.getInstance().getDataManager().getManager(FeatureType.SPAWN);
    }

    @Override
    public boolean load(boolean loader) {
        ConfigFile file = WarpSystem.getInstance().getFileManager().getFile("Config");
        Configuration config = file.getSimpleConfig();

        this.spawn = config.getString("WarpSystem.GlobalSpawnOptions.Spawn", null);
        this.respawn = config.getString("WarpSystem.GlobalSpawnOptions.Respawn", null);

        ServerListener listener = new ServerListener();
        WarpSystem.proxy().getEventManager().register(WarpSystem.getInstance(), listener);
        WarpSystem.getInstance().getDataHandler().register(listener);
        return true;
    }

    @Override
    public void save(boolean saver) {
        ConfigFile file = WarpSystem.getInstance().getFileManager().getFile("Config");
        Configuration config = file.getSimpleConfig();

        config.set("WarpSystem.GlobalSpawnOptions.Spawn", this.spawn);
        config.set("WarpSystem.GlobalSpawnOptions.Respawn", this.respawn);

        file.save();
    }

    @Override
    public void destroy() {
    }

    public void update(RegisteredServer sender, String spawn, String respawn) {
        if(!Objects.equals(this.spawn, spawn) || !Objects.equals(this.respawn, respawn)) {
            this.spawn = spawn;
            this.respawn = respawn;
            synchronize(sender);
        }
    }

    public void synchronize(RegisteredServer except) {
        for(RegisteredServer serverInfo : WarpSystem.getInstance().getServerManager().getOnlineServer()) {
            if(serverInfo.equals(except)) continue;
            WarpSystem.getInstance().getDataHandler().send(getInfoPacket(), serverInfo);
        }
    }

    public SendGlobalSpawnOptionsPacket getInfoPacket() {
        return new SendGlobalSpawnOptionsPacket(this.spawn, this.respawn);
    }

    public String getSpawn() {
        return spawn;
    }

    public String getRespawn() {
        return respawn;
    }
}
