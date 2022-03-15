package de.codingair.warpsystem.velocity.base.events;

import de.codingair.warpsystem.core.proxy.base.events.IServerProvideOptionsEvent;
import de.codingair.warpsystem.core.transfer.utils.serializeable.ServerOptions;
import de.codingair.warpsystem.velocity.utils.VelocityServer;

public class ServerProvideOptionsEvent implements IServerProvideOptionsEvent {
    private final VelocityServer info;
    private final ServerOptions options;

    public ServerProvideOptionsEvent(VelocityServer info, ServerOptions options) {
        this.info = info;
        this.options = options;
    }

    public VelocityServer getServer() {
        return info;
    }

    public ServerOptions getOptions() {
        return options;
    }
}
