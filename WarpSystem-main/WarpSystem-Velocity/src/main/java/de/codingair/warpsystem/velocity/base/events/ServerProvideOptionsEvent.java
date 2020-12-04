package de.codingair.warpsystem.velocity.base.events;

import com.velocitypowered.api.proxy.server.RegisteredServer;
import de.codingair.warpsystem.base.transfer.serializeable.ServerOptions;

public class ServerProvideOptionsEvent {
    private final RegisteredServer server;
    private final ServerOptions options;

    public ServerProvideOptionsEvent(RegisteredServer server, ServerOptions options) {
        this.server = server;
        this.options = options;
    }

    public RegisteredServer getServer() {
        return server;
    }

    public ServerOptions getOptions() {
        return options;
    }
}
