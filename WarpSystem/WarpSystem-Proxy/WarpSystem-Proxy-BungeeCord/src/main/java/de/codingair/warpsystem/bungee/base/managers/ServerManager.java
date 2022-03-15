package de.codingair.warpsystem.bungee.base.managers;

import de.codingair.codingapi.tools.io.BungeeConfigMask;
import de.codingair.warpsystem.bungee.base.WarpSystem;
import de.codingair.warpsystem.bungee.base.events.ServerInitializeEvent;
import de.codingair.warpsystem.bungee.utils.BungeeServer;
import de.codingair.warpsystem.core.proxy.base.handlers.ServerHandler;
import de.codingair.warpsystem.core.proxy.utils.Server;
import de.codingair.warpsystem.core.transfer.utils.serializeable.ServerOptions;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.plugin.Listener;

public class ServerManager extends ServerHandler implements Listener {
    public ServerManager() {
        super(new BungeeConfigMask(WarpSystem.getInstance().getFileManager().getFile("Config")));
    }

    @Override
    public void triggerServerInitializeEvent(Server server) {
        WarpSystem.getInstance().getProxy().getPluginManager().callEvent(new ServerInitializeEvent((BungeeServer) server));
    }

    public ServerOptions getOptions(ServerInfo info) {
        if (info == null) return null;
        return super.getOptions(new BungeeServer(info));
    }
}
