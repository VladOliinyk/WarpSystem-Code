package de.codingair.warpsystem.velocity.api.files;

import de.codingair.codingapi.tools.io.JSON.JSONParser;
import de.codingair.codingapi.tools.io.lib.JSONArray;
import de.codingair.codingapi.tools.io.lib.ParseException;
import de.codingair.codingapi.tools.io.utils.DataWriter;
import de.codingair.codingapi.tools.io.utils.Serializable;
import ninja.leaping.configurate.ConfigurationNode;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class VelocityConfigWriter implements DataWriter {
    private final Configuration root;
    private String prefix;
    private final ConfigFile file;

    public VelocityConfigWriter() {
        this(new Configuration());
    }

    public VelocityConfigWriter(Map<?, ?> map) {
        this(new Configuration(map));
    }

    public VelocityConfigWriter(Configuration root) {
        this(root, null);
    }

    public VelocityConfigWriter(Configuration root, String prefix) {
        this.root = root;
        this.prefix = prefix == null ? "" : prefix;
        this.file = null;
    }

    public VelocityConfigWriter(ConfigFile file) {
        this(file, null);
    }

    public VelocityConfigWriter(ConfigFile file, String prefix) {
        this.file = file;
        this.prefix = prefix == null ? "" : prefix;
        this.root = new Configuration(file.getConfig());
    }

    private Configuration c() {
        return root;
    }

    private String k(String key) {
        return (prefix.isEmpty() ? "" : prefix + ".") + key;
    }

    public ConfigFile getFile() {
        return this.file;
    }

    public String getPrefix() {
        return prefix;
    }

    @Override
    public Set<String> keySet(boolean depth) {
        return c().keys(depth);
    }

    public void write(Serializable s, String key) {
        String oldPrefix = prefix;
        prefix = k(key);
        s.write(this);
        prefix = oldPrefix;
    }

    public void read(Serializable s, String key) throws Exception {
        String oldPrefix = prefix;
        prefix = k(key);
        s.read(this);
        prefix = oldPrefix;
    }

    @Override
    public Object finalCommit(String key, Object value) {
        Object prev;

        if(value instanceof Serializable) {
            write((Serializable) value, key);
            prev = null;
        } else {
            prev = c().get(k(key));
            c().set(k(key), value);
        }

        return prev;
    }

    @Override
    public Object remove(String key) {
        Object prev = c().get(k(key));
        c().set(k(key), null);
        return prev;
    }

    @Override
    public <T extends Serializable> T getSerializable(String key, Serializable serializable) {
        try {
            read(serializable, key);
        } catch(Exception e) {
            e.printStackTrace();
        }

        return (T) serializable;
    }

    @Override
    public Boolean getBoolean(String key, Boolean def) {
        return c().getBoolean(k(key), def);
    }

    @Override
    public Integer getInteger(String key, Integer def) {
        return c().getInt(k(key), def);
    }

    @Override
    public JSONArray getList(String key) {
        JSONArray array = new JSONArray();
        List l = c().getList(k(key));

        if(l == null) return array;

        array.addAll(l);
        return array;
    }

    @Override
    public Long getLong(String key, Long def) {
        return c().getLong(k(key), def);
    }

    @Override
    public Double getDouble(String key, Double def) {
        return c().getDouble(k(key), def);
    }

    @Override
    public Float getFloat(String key, Float def) {
        Double d = getDouble(key);
        return d == null ? def : (Float) d.floatValue();
    }

    @Override
    public <T> T get(String key, T def, boolean raw) {
        Object o = c().get(k(key));

        if(!raw) {
            if(o instanceof Long) {
                long l = (long) o;
                if(l <= Integer.MAX_VALUE && l >= Integer.MIN_VALUE) return (T) (Object) Math.toIntExact(l);
            }

            if(o instanceof String) {
                try {
                    Object result = new JSONParser().parse((String) o);
                    if(result != null) o = result;
                } catch(ParseException ignored) {
                }
            }

            if(o instanceof ConfigurationNode) return (T) new Configuration((ConfigurationNode) o);
        }

        return o == null ? def : (T) o;
    }

    public ConfigurationNode getRoot() {
        return root.node();
    }
}
