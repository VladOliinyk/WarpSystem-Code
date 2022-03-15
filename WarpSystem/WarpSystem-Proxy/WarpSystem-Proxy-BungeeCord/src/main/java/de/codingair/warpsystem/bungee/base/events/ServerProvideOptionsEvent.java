package de.codingair.warpsystem.bungee.base.events;

import de.codingair.warpsystem.bungee.utils.BungeeServer;
import de.codingair.warpsystem.core.proxy.base.events.IServerProvideOptionsEvent;
import de.codingair.warpsystem.core.transfer.utils.serializeable.ServerOptions;
import net.md_5.bungee.api.plugin.Event;

public class ServerProvideOptionsEvent extends Event implements IServerProvideOptionsEvent<BungeeServer> {
    private final BungeeServer info;
    private final ServerOptions options;

    public ServerProvideOptionsEvent(BungeeServer info, ServerOptions options) {
        this.info = info;
        this.options = options;
    }

    public BungeeServer getServer() {
        return info;
    }

    public ServerOptions getOptions() {
        return options;
    }
}
