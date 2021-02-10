package de.codingair.warpsystem.spigot.features.portals.dimensions;

import org.bukkit.event.player.PlayerTeleportEvent;

public enum DimensionType {
    NETHER_PORTAL(PlayerTeleportEvent.TeleportCause.NETHER_PORTAL),
    END_PORTAL(PlayerTeleportEvent.TeleportCause.END_PORTAL),
    END_GATEWAY(PlayerTeleportEvent.TeleportCause.END_GATEWAY);

    private final PlayerTeleportEvent.TeleportCause cause;

    DimensionType(PlayerTeleportEvent.TeleportCause cause) {
        this.cause = cause;
    }

    public static DimensionType get(String type) {
        try {
            return valueOf(type);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    public PlayerTeleportEvent.TeleportCause getCause() {
        return cause;
    }
}
