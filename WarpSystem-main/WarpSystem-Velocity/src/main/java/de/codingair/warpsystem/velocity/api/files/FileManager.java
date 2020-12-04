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
        return configs.get(key(name));
    }

    public ConfigFile loadFile(String name) {
        return loadFile(name, "/", "/");
    }

    public ConfigFile loadFile(String name, String path) {
        return loadFile(name, path, "/");
    }

    public ConfigFile loadFile(String name, String path, String srcPath) {
        ConfigFile file = getFile(name);
        if(file != null) return file;

        file = new ConfigFile(name, path, plugin);
        this.configs.put(key(name), file);
        return file;
    }
}