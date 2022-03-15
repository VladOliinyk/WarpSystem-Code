package de.codingair.warpsystem.bungee.base.managers;

import de.codingair.warpsystem.bungee.features.FeatureType;
import de.codingair.warpsystem.core.utils.Manager;

import java.util.ArrayList;
import java.util.List;

public class DataManager {
    private final List<Manager> managers = new ArrayList<>();

    public DataManager() {
        for (FeatureType.Priority value : FeatureType.Priority.values()) {
            for (FeatureType ft : FeatureType.values(value)) {
                try {
                    this.managers.add(ft.getManagerClass().newInstance());
                } catch (InstantiationException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void preLoad() {
        for (Manager manager : this.managers) {
            manager.preLoad();
        }
    }

    public boolean load(boolean hidePrints) {
        boolean success = true;
        for (Manager manager : this.managers) {
            if (!manager.load(hidePrints)) success = false;
        }
        return success;
    }

    public void save(boolean saver) {
        for (Manager manager : this.managers) {
            manager.save(saver);
        }
    }

    public boolean reload() {
        save(true);
        return load(true);
    }

    public <T extends Manager> T getManager(FeatureType type) {
        for (Manager manager : this.managers) {
            if (manager.getClass().equals(type.getManagerClass())) return (T) manager;
        }

        return null;
    }

    public <T extends Manager> T getManager(Class<T> type) {
        for (Manager manager : this.managers) {
            if (checkSuperClass(manager.getClass(), type)) return (T) manager;
        }

        return null;
    }

    private boolean checkSuperClass(Class<?> a, Class<?> b) {
        if (a.equals(b)) return true;

        if (a.getSuperclass() == null) return false;
        else return checkSuperClass(a.getSuperclass(), b);
    }

    public List<Manager> getManagers() {
        return managers;
    }
}
