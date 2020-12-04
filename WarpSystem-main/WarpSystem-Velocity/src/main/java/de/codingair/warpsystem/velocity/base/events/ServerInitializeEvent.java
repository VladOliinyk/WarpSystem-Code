package de.codingair.warpsystem.velocity.base.events;

import com.velocitypowered.api.proxy.server.RegisteredServer;

public class ServerInitializeEvent {
    private final RegisteredServer server;

    public ServerInitializeEvent(RegisteredServer server) {
        this.server = server;
    }

    public RegisteredServer getServer() {
        return server;
    }
}
