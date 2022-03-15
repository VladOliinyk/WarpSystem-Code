package de.codingair.warpsystem.spigot.base.utils.teleport.destinations;

import de.codingair.warpsystem.spigot.base.utils.teleport.destinations.adapters.*;
import de.codingair.warpsystem.spigot.features.portals.utils.PortalDestinationAdapter;
import org.jetbrains.annotations.NotNull;

public enum DestinationType {
    UNKNOWN(-1, null),
    WarpIcon(0, SimpleWarpAdapter.class),
    SimpleWarp(1, SimpleWarpAdapter.class),
    GlobalWarpIcon(2, GlobalWarpAdapter.class),
    GlobalWarp(3, true, GlobalWarpAdapter.class),
    Server(5, true, ServerAdapter.class),
    Location(6, LocationAdapter.class),
    GlobalLocation(7, true, GlobalLocationAdapter.class),
    Portal(8, PortalDestinationAdapter.class),
    Velocity(9, VelocityAdapter.class),
    ;

    private final int id;
    private final boolean bungee;
    private final Class<? extends DestinationAdapter> adapter;

    DestinationType(int id, boolean bungee, Class<? extends DestinationAdapter> adapter) {
        this.id = id;
        this.bungee = bungee;
        this.adapter = adapter;
    }

    DestinationType(int id, Class<? extends DestinationAdapter> adapter) {
        this(id, false, adapter);
    }

    public static DestinationType getByAdapter(DestinationAdapter adapter) {
        if (adapter == null) return UNKNOWN;

        for (DestinationType value : values()) {
            if (adapter.getClass().equals(value.adapter)) return value;
        }

        return UNKNOWN;
    }

    public static DestinationType getById(int id) {
        for (DestinationType value : values()) {
            if (value.getId() == id) return value;
        }

        return UNKNOWN;
    }

    public int getId() {
        return id;
    }

    @NotNull
    public Class<? extends DestinationAdapter> getAdapter() {
        return adapter;
    }

    @NotNull
    public DestinationAdapter getInstance() {
        try {
            return getAdapter().newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

    public boolean isBungee() {
        return bungee;
    }
}
