package de.codingair.warpsystem.bungee.features.globalwarps;

import de.codingair.codingapi.bungeecord.files.ConfigFile;
import de.codingair.codingapi.tools.io.BungeeConfigMask;
import de.codingair.warpsystem.bungee.base.WarpSystem;
import de.codingair.warpsystem.core.proxy.features.GlobalWarpHandler;
import de.codingair.warpsystem.core.transfer.utils.serializeable.SGlobalWarp;

public class GlobalWarpManager extends GlobalWarpHandler {

    @Override
    public boolean load(boolean loader) {
        WarpSystem.getInstance().getFileManager().loadFile("GlobalWarps", "/");
        ConfigFile file = WarpSystem.getInstance().getFileManager().getFile("GlobalWarps");

        return super.load(loader, new BungeeConfigMask(file));
    }

    @Override
    public void save(SGlobalWarp warp) {
        ConfigFile file = WarpSystem.getInstance().getFileManager().getFile("GlobalWarps");
        super.save(warp, new BungeeConfigMask(file));
        file.save();
    }

    @Override
    public void delete(SGlobalWarp warp) {
        ConfigFile file = WarpSystem.getInstance().getFileManager().getFile("GlobalWarps");
        super.delete(warp, new BungeeConfigMask(file));
        file.save();
    }
}
