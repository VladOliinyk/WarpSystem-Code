package de.codingair.warpsystem.bungee.features;

import de.codingair.warpsystem.bungee.features.globalwarps.GlobalWarpManager;
import de.codingair.warpsystem.bungee.features.playerwarps.PlayerWarpManager;
import de.codingair.warpsystem.bungee.features.randomtp.RandomTPManager;
import de.codingair.warpsystem.bungee.features.spawn.SpawnManager;
import de.codingair.warpsystem.bungee.features.teleport.TeleportManager;
import de.codingair.warpsystem.core.utils.Manager;

import java.util.ArrayList;
import java.util.List;

public enum FeatureType {
    GLOBAL_WARPS(GlobalWarpManager.class, Priority.LOW),
    TELEPORT(TeleportManager.class, Priority.LOW),
    PLAYER_WARPS(PlayerWarpManager.class, Priority.LOW),
    SPAWN(SpawnManager.class, Priority.LOW),
    RANDOM_TP(RandomTPManager.class, Priority.LOW),
    ;

    private final Class<? extends Manager> managerClass;
    private final Priority priority;

    FeatureType(Class<? extends Manager> managerClass, Priority priority) {
        this.managerClass = managerClass;
        this.priority = priority;
    }

    public static FeatureType[] values(Priority priority) {
        List<FeatureType> featureTypes = new ArrayList<>();

        for (FeatureType value : values()) {
            if (value.getPriority().equals(priority)) featureTypes.add(value);
        }

        return featureTypes.toArray(new FeatureType[0]);
    }

    public Class<? extends Manager> getManagerClass() {
        return managerClass;
    }

    public Priority getPriority() {
        return priority;
    }

    public enum Priority {
        HIGHEST,
        HIGH,
        LOW,
        LOWEST
    }
}
