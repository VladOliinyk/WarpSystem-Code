package de.codingair.warpsystem.velocity.api.files;

import com.google.common.base.Preconditions;
import de.codingair.warpsystem.velocity.api.VelocityPlugin;

import java.util.HashMap;

public class FileManager {
    private final VelocityPlugin plugin;
    private final HashMap<String, ConfigFile> configs = new HashMap<>();

    public FileManager(VelocityPlugin plugin) {
        this.plugin = plugin;
    }

    private String key(String name) {
        Preconditions.checkNotNull(name);
        return name.toLowerCase().trim();
    }

    public ConfigFile getFile(String name) {
        return getFile(name, "/", "/");
    }

    public ConfigFile getFile(String name, String path) {
        return getFile(name, path, "/");
    }

    public ConfigFile getFile(String name, String path, String srcPath) {
        ConfigFile file = configs.get(key(name));
        if(file != null) return file;

        file = new ConfigFile(name, path, srcPath, plugin);
        this.configs.put(key(name), file);
        return file;
    }
}