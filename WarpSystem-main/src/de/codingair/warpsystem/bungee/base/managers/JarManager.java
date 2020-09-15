package de.codingair.warpsystem.bungee.base.managers;

import de.codingair.codingapi.tools.Call;
import de.codingair.warpsystem.bungee.base.WarpSystem;
import de.codingair.warpsystem.transfer.jar.JarSender;
import de.codingair.warpsystem.transfer.serializeable.ServerOptions;
import net.md_5.bungee.api.config.ServerInfo;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class JarManager {
    public boolean fetchPossible(ServerInfo info) {
        ServerOptions options = WarpSystem.getInstance().getServerManager().getOptions(info);
        return options != null && !options.getVersion().equals(WarpSystem.getInstance().getDescription().getVersion());
    }

    public void sendJar(ServerInfo info, Call call) throws IllegalStateException {
        File file = getOrigin();
        WarpSystem.getInstance().getServerManager().getOptions(info).setFetched(true);
        WarpSystem.getInstance().getProxy().getScheduler().runAsync(WarpSystem.getInstance(), new JarSender(info, file, call));
    }

    public static boolean tryOS() {
        try {
            Files.newByteChannel(getOrigin().toPath());
            return true;
        } catch(IOException e) {
            return false;
        }
    }

    private static File getOrigin() {
        return getCurrentJar(WarpSystem.getInstance().getDataFolder().getParentFile());
    }

    private static File getCurrentJar(File parent) {
        for(File file : parent.listFiles()) {
            if(file.isDirectory()) continue;
            if(file.getName().toLowerCase().contains("warpsystem")) {
                return file;
            }
        }

        return null;
    }
}
