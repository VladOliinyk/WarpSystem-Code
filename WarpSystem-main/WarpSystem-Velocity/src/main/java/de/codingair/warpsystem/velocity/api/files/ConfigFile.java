package de.codingair.warpsystem.velocity.api.files;

import com.google.common.base.Charsets;
import de.codingair.codingapi.server.reflections.IReflection;
import de.codingair.warpsystem.velocity.api.VelocityPlugin;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.loader.HeaderMode;
import ninja.leaping.configurate.yaml.YAMLConfigurationLoader;
import org.yaml.snakeyaml.DumperOptions;

import java.io.*;
import java.net.URL;
import java.util.Map;

/**
 * Removing of this disclaimer is forbidden.
 *
 * @author codingair
 * @verions: 1.0.0
 **/

public class ConfigFile {
    private YAMLConfigurationLoader loader;
    private ConfigurationNode config;
    private final String name;
    private final String path;
    private final String srcPath;
    private final VelocityPlugin plugin;

    public ConfigFile(String name, String path, String srcPath, VelocityPlugin plugin) {
        this.name = name;
        this.path = path;
        this.srcPath = srcPath == null ? "" : srcPath;
        this.plugin = plugin;

        try {
            this.load();
            this.save();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    public ConfigFile(String name, String path, VelocityPlugin plugin) {
        this(name, path, "", plugin);
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public VelocityPlugin getPlugin() {
        return plugin;
    }

    private File getDataFolder() {
        return plugin.getDataFolder();
    }

    public void load() throws IOException {
        if(getDataFolder() == null || !getDataFolder().exists()) getDataFolder().mkdir();

        File file = new File(getDataFolder() + this.path, this.name + ".yml");

        InputStream inStream = plugin.getResourceAsStream(srcPath + this.name + ".yml");

        if(!file.exists()) {
            file.createNewFile();

            if(inStream != null) {
                OutputStream out = new FileOutputStream(file);
                copy(inStream, out);
                out.close();
                inStream.close();
            }
        }

        URL in = plugin.getResource(srcPath + this.name + ".yml");
        ConfigurationNode defaults = null;
        if(in != null) {
            this.loader = YAMLConfigurationLoader.builder()
                    .setURL(in)
                    .setFlowStyle(DumperOptions.FlowStyle.BLOCK)
                    .setIndent(2)
                    .setHeaderMode(HeaderMode.PRESERVE)
                    .build();
            defaults = loader.load();
        }

        this.config = defaults == null ? YAMLConfigurationLoader.builder().build().createEmptyNode() : defaults;

        if(file != null) {
            this.loader = YAMLConfigurationLoader.builder()
                    .setFile(file)
                    .setFlowStyle(DumperOptions.FlowStyle.BLOCK)
                    .setIndent(2)
                    .setHeaderMode(HeaderMode.PRESERVE)
                    .build();
            ConfigurationNode config = loader.load();
            this.config.mergeValuesFrom(config);
        }
    }

    private long copy(InputStream from, OutputStream to) throws IOException {
        if(from == null) return -1;
        if(to == null) throw new NullPointerException();

        byte[] buf = new byte[4096];
        long total = 0L;

        while(true) {
            int r = from.read(buf);
            if(r == -1) {
                return total;
            }

            to.write(buf, 0, r);
            total += r;
        }
    }

    public void save() {
        try {
            this.loader.save(this.config);
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    public ConfigurationNode getConfig() {
        if(config == null) {
            try {
                load();
            } catch(IOException e) {
                e.printStackTrace();
            }
        }

        return config;
    }

    public Configuration getSimpleConfig() {
        return new Configuration(getConfig());
    }

    public void clear() {
        getConfig().setValue(null);
    }
}
