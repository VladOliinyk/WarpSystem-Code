package de.codingair.warpsystem.velocity.features.globalwarps.managers;

import com.velocitypowered.api.proxy.server.RegisteredServer;
import de.codingair.warpsystem.base.transfer.packets.bungee.SendGlobalWarpNamesPacket;
import de.codingair.warpsystem.base.transfer.packets.bungee.UpdateGlobalWarpPacket;
import de.codingair.warpsystem.base.transfer.serializeable.SGlobalWarp;
import de.codingair.warpsystem.base.transfer.serializeable.SLocation;
import de.codingair.warpsystem.base.utils.Manager;
import de.codingair.warpsystem.velocity.api.files.ConfigFile;
import de.codingair.warpsystem.velocity.api.files.Configuration;
import de.codingair.warpsystem.velocity.base.WarpSystem;
import de.codingair.warpsystem.velocity.features.globalwarps.listeners.GlobalWarpListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GlobalWarpManager implements Manager {
    private final HashMap<String, SGlobalWarp> globalWarps = new HashMap<>();

    public boolean load(boolean loader) {
        WarpSystem.getInstance().getFileManager().loadFile("GlobalWarps", "/");
        ConfigFile file = WarpSystem.getInstance().getFileManager().getFile("GlobalWarps");
        Configuration config = file.getSimpleConfig();

        GlobalWarpListener listener;
        WarpSystem.proxy().getEventManager().register(WarpSystem.getInstance(), listener = new GlobalWarpListener());

        if(!loader) WarpSystem.log("  > Loading locations of GlobalWarps");

        this.globalWarps.clear();
        for(String data : config.keys(false)) {
            SGlobalWarp warp = new SGlobalWarp();

            warp.setName(data);
            warp.setServer(config.getString(data + ".Server"));
            warp.setLoc(new SLocation(
                    config.getString(data + ".Location.World"),
                    config.getDouble(data + ".Location.X"),
                    config.getDouble(data + ".Location.Y"),
                    config.getDouble(data + ".Location.Z"),
                    config.getFloat(data + ".Location.Yaw"),
                    config.getFloat(data + ".Location.Pitch")
            ));

            this.globalWarps.put(warp.getName().toLowerCase(), warp);
        }

        WarpSystem.getInstance().getDataHandler().register(listener);

        return true;
    }

    @Override
    public void save(boolean saver) {
        if(!saver) WarpSystem.log("  > Saving locations of GlobalWarps");

        for(SGlobalWarp globalWarp : this.globalWarps.values()) {
            save(globalWarp);
        }
    }

    @Override
    public void destroy() {
        this.globalWarps.clear();
    }

    private void save(SGlobalWarp warp) {
        ConfigFile file = WarpSystem.getInstance().getFileManager().getFile("GlobalWarps");
        Configuration config = file.getSimpleConfig();

        config.set(warp.getName() + ".Server", warp.getServer());
        config.set(warp.getName() + ".Location.World", warp.getLoc().getWorld());
        config.set(warp.getName() + ".Location.X", warp.getLoc().getX());
        config.set(warp.getName() + ".Location.Y", warp.getLoc().getY());
        config.set(warp.getName() + ".Location.Z", warp.getLoc().getZ());
        config.set(warp.getName() + ".Location.Yaw", warp.getLoc().getYaw());
        config.set(warp.getName() + ".Location.Pitch", warp.getLoc().getPitch());

        file.save();
    }

    public void synchronize(SGlobalWarp warp) {
        for(RegisteredServer server : WarpSystem.getInstance().getServerManager().getOnlineServer()) {
            if(server.getPlayersConnected().isEmpty()) continue;

            int id;
            if(get(warp.getName()) == null) id = UpdateGlobalWarpPacket.Action.DELETE.getId();
            else if(get(warp.getName()).equals(warp)) id = UpdateGlobalWarpPacket.Action.ADD.getId();
            else id = UpdateGlobalWarpPacket.Action.UPDATE_POSITION.getId();

            WarpSystem.getInstance().getDataHandler().send(new UpdateGlobalWarpPacket(id, warp.getName(), warp.getServer()), server);
        }
    }

    public void synchronize(RegisteredServer server) {
        if(this.globalWarps.isEmpty()) {
            return;
        }

        List<HashMap<String, String>> list = new ArrayList<>();
        HashMap<String, String> current = new HashMap<>();
        int currentBytes = 0;

        for(SGlobalWarp warp : this.globalWarps.values()) {
            currentBytes += warp.getName().length() + warp.getServer().length();

            if(currentBytes > 32700) {
                list.add(current);
                current = new HashMap<>();
            }

            currentBytes = warp.getName().length() + warp.getServer().length();
            current.put(warp.getName(), warp.getServer());
        }

        if(current.size() > 0) list.add(current);

        boolean start = true;
        for(HashMap<String, String> l : list) {
            WarpSystem.getInstance().getDataHandler().send(new SendGlobalWarpNamesPacket(l, start), server);
            start = false;
        }

        list.clear();
    }

    private void delete(SGlobalWarp warp) {
        ConfigFile file = WarpSystem.getInstance().getFileManager().getFile("GlobalWarps");
        Configuration config = file.getSimpleConfig();

        config.set(warp.getName(), null);

        file.save();
    }

    public HashMap<String, SGlobalWarp> getGlobalWarps() {
        return globalWarps;
    }

    public SGlobalWarp get(String name) {
        if(name == null) return null;
        return this.globalWarps.get(name.toLowerCase());
    }

    public boolean add(SGlobalWarp warp) {
        if(get(warp.getName()) != null) return false;
        this.globalWarps.put(warp.getName().toLowerCase(), warp);
        save(warp);
        return true;
    }

    public SGlobalWarp remove(String name) {
        SGlobalWarp warp = get(name);
        if(warp == null) return null;
        this.globalWarps.remove(warp);
        delete(warp);
        return warp;
    }
}
