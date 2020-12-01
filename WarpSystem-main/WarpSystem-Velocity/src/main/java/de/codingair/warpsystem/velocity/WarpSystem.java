package de.codingair.warpsystem.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import org.slf4j.Logger;

@Plugin(id="warpsystem", name = "WarpSystem", version = "${project.version}", description = "WarpSystem", authors = {"CodingAir"})
public class WarpSystem {
    private final ProxyServer server;
    private final Logger logger;

    @Inject
    public WarpSystem(ProxyServer server, Logger logger) {
        this.server = server;
        this.logger = logger;

        this.logger.info("Hello there!");
    }
}
