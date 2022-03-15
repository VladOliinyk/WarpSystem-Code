package de.codingair.warpsystem.velocity.base.managers;

import de.codingair.warpsystem.core.proxy.base.handlers.ServerHandler;
import de.codingair.warpsystem.core.proxy.utils.Server;
import de.codingair.warpsystem.velocity.api.files.VelocityConfigMask;
import de.codingair.warpsystem.velocity.base.WarpSystem;

public class ServerManager extends ServerHandler {
    public ServerManager() {
        super(new VelocityConfigMask(WarpSystem.getInstance().getFileManager().getFile("Config")));
    }

    @Override
    public void triggerServerInitializeEvent(Server server) {

    }
}
