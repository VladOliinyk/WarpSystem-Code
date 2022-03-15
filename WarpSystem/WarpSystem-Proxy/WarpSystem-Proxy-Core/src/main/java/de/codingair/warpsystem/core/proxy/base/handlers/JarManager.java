package de.codingair.warpsystem.core.proxy.base.handlers;

import de.codingair.codingapi.tools.Call;
import de.codingair.warpsystem.core.proxy.Core;
import de.codingair.warpsystem.core.proxy.transfer.JarSender;
import de.codingair.warpsystem.core.proxy.utils.Server;
import de.codingair.warpsystem.core.transfer.utils.serializeable.ServerOptions;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class JarManager {
    public static boolean tryOS() {
        try {
            Files.newByteChannel(getOrigin().toPath());
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private static File getOrigin() {
        return getCurrentJar(Core.getPlugin().getDataFolder().getParentFile());
    }

    private static File getCurrentJar(File parent) {
        for (File file : parent.listFiles()) {
            if (file.isDirectory()) continue;
            if (file.getName().toLowerCase().contains("warpsystem")) {
                return file;
            }
        }

        return null;
    }

    public boolean fetchPossible(Server info) {
        ServerOptions options = Core.getServerManager().getOptions(info);
        return options != null && !options.getVersion().equals(Core.getPlugin().getVersion());
    }

    public void sendJar(Server info, Call call) throws IllegalStateException {
        File file = getOrigin();
        Core.getServerManager().getOptions(info).setFetched(true);
        Core.getPlugin().runAsync(new JarSender(info, file, call));
    }
}
