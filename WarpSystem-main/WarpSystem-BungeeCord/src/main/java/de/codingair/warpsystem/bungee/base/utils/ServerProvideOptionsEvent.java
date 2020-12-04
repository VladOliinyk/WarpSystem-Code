package de.codingair.warpsystem.bungee.base.utils;

import de.codingair.warpsystem.base.transfer.serializeable.ServerOptions;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.plugin.Event;

public class ServerProvideOptionsEvent extends Event {
    private final ServerInfo info;
    private final ServerOptions options;

    public ServerProvideOptionsEvent(ServerInfo info, ServerOptions options) {
        this.info = info;
        this.options = options;
    }

    public ServerInfo getInfo() {
        return info;
    }

    public ServerOptions getOptions() {
        return options;
    }
}
