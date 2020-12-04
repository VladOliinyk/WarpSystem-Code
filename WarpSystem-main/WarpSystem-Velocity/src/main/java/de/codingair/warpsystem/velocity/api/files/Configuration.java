package de.codingair.warpsystem.velocity.api.files;

import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.ConfigurationOptions;

import java.util.*;

public class Configuration {
    private final ConfigurationNode node;
    private HashSet<String> keys = null;

    public Configuration() {
        this.node = ConfigurationNode.root(ConfigurationOptions.defaults());
    }

    public Configuration(Map<?, ?> map) {
        this();
        this.node.setValue(map);
    }

    public Configuration(ConfigurationNode node) {
        this.node = node;
    }

    public Set<String> keys(boolean depth) {
        if(this.keys == null) {
            this.keys = new HashSet<>();
            addKeys(depth, "", node);
        }

        return this.keys;
    }

    private void addKeys(boolean depth, String prefix, ConfigurationNode node) {
        if(node.getChildrenMap().isEmpty()) {
            if(node.getKey() != null) this.keys.add(prefix + node.getKey());
        } else {
            if(depth || node.getKey() == null) {
                for(Map.Entry<Object, ? extends ConfigurationNode> e : node.getChildrenMap().entrySet()) {
                    addKeys(depth, node.getKey() == null ? prefix : prefix + node.getKey() + ".", e.getValue());
                }
            } else this.keys.add(prefix + node.getKey());
        }
    }

    public Object get(String path) {
        ConfigurationNode node = getNode(path);
        return node == null ? null : node.getValue();
    }

    public ConfigurationNode getNode(String key) {
        String[] args = key.split("\\.", -1);
        return this.node.getNode((Object[]) args);
    }

    public void set(String key, Object value) {
        ConfigurationNode node = getNode(key);
        if(node == null) return;
        node.setValue(value);
    }

    public ConfigurationNode node() {
        return node;
    }

    public Boolean getBoolean(String key, Boolean def) {
        Object o = get(key);
        return o instanceof Boolean ? (Boolean) o : def;
    }

    public Integer getInt(String key, Integer def) {
        Object o = get(key);
        return o instanceof Integer ? (Integer) o : def;
    }

    public List<?> getList(String key, List<?> def) {
        Object o = get(key);
        return o instanceof List ? (List<?>) o : def;
    }

    public List<?> getList(String key) {
        return getList(key, null);
    }

    public Long getLong(String key, Long def) {
        Object o = get(key);
        return o instanceof Long ? (Long) o : def;
    }

    public Double getDouble(String key, Double def) {
        Object o = get(key);
        return o instanceof Double ? (Double) o : def;
    }

    public double getDouble(String key) {
        return getDouble(key, 0D);
    }

    public String getString(String key, String def) {
        Object o = get(key);
        return o instanceof String ? (String) o : def;
    }

    public String getString(String key) {
        return getString(key, null);
    }

    public Float getFloat(String key, Float def) {
        Object o = get(key);
        return o instanceof Float ? (Float) o : def;
    }

    public float getFloat(String key) {
        return getFloat(key, 0F);
    }
}
