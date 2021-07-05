package de.codingair.warpsystem.core.proxy.features;

import de.codingair.codingapi.tools.io.utils.DataMask;
import de.codingair.packetmanagement.utils.Direction;
import de.codingair.warpsystem.core.proxy.Core;
import de.codingair.warpsystem.core.proxy.utils.Server;
import de.codingair.warpsystem.core.transfer.packets.proxy.SendGlobalWarpNamesPacket;
import de.codingair.warpsystem.core.transfer.packets.proxy.UpdateGlobalWarpPacket;
import de.codingair.warpsystem.core.transfer.utils.serializeable.SGlobalWarp;
import de.codingair.warpsystem.core.transfer.utils.serializeable.SLocation;
import de.codingair.warpsystem.core.utils.Manager;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public abstract class GlobalWarpHandler implements Manager {
    private final Set<SGlobalWarp> globalWarps = new HashSet<>();

    protected boolean load(boolean loader, DataMask mask) {
        if (!loader) Core.getPlugin().log("  > Loading locations of GlobalWarps");

        this.globalWarps.clear();

        for (String data : mask.keySet(false)) {
            SGlobalWarp warp = new SGlobalWarp();

            warp.setName(data);
            warp.setServer(mask.getString(data + ".Server"));
            warp.setLoc(new SLocation(
                    mask.getString(data + ".Location.World"),
                    mask.getDouble(data + ".Location.X"),
                    mask.getDouble(data + ".Location.Y"),
                    mask.getDouble(data + ".Location.Z"),
                    mask.getFloat(data + ".Location.Yaw"),
                    mask.getFloat(data + ".Location.Pitch")
            ));

            this.globalWarps.add(warp);
        }

        return true;
    }

    @Override
    public void save(boolean saver) {
        if (!saver) Core.getPlugin().log("  > Saving locations of GlobalWarps");

        for (SGlobalWarp globalWarp : this.globalWarps) {
            save(globalWarp);
        }
    }

    @Override
    public void destroy() {
        this.globalWarps.clear();
    }

    public abstract void save(SGlobalWarp warp);

    protected void save(SGlobalWarp warp, DataMask mask) {
        mask.put(warp.getName() + ".Server", warp.getServer());
        mask.put(warp.getName() + ".Location.World", warp.getLoc().getWorld());
        mask.put(warp.getName() + ".Location.X", warp.getLoc().getX());
        mask.put(warp.getName() + ".Location.Y", warp.getLoc().getY());
        mask.put(warp.getName() + ".Location.Z", warp.getLoc().getZ());
        mask.put(warp.getName() + ".Location.Yaw", warp.getLoc().getYaw());
        mask.put(warp.getName() + ".Location.Pitch", warp.getLoc().getPitch());
    }

    public void synchronize(SGlobalWarp warp, @NotNull UpdateGlobalWarpPacket.Action action) {
        int id = action.getId();
        Core.getServerManager().getOnlineServer().forEach(server -> Core.getPlugin().dataHandler().send(new UpdateGlobalWarpPacket(id, warp.getName(), warp.getServer()), server, Direction.DOWN));
    }

    public void synchronize(Server<?> server) {
        if (this.globalWarps.isEmpty()) return;

        boolean start = true;
        for (SGlobalWarp warp : this.globalWarps) {
            Core.getPlugin().dataHandler().send(new SendGlobalWarpNamesPacket(warp.getName(), warp.getServer(), start), server, Direction.DOWN);
            start = false;
        }
    }

    public abstract void delete(SGlobalWarp warp);

    protected void delete(SGlobalWarp warp, DataMask mask) {
        mask.remove(warp.getName());
    }

    public SGlobalWarp get(String name) {
        for (SGlobalWarp warp : this.globalWarps) {
            if (warp.getName().equalsIgnoreCase(name)) return warp;
        }

        return null;
    }

    public boolean add(SGlobalWarp warp) {
        if (get(warp.getName()) != null) return false;
        this.globalWarps.add(warp);
        save(warp);
        return true;
    }

    public SGlobalWarp remove(String name) {
        SGlobalWarp warp = get(name);
        if (warp == null) return null;
        this.globalWarps.remove(warp);
        delete(warp);
        return warp;
    }
}
