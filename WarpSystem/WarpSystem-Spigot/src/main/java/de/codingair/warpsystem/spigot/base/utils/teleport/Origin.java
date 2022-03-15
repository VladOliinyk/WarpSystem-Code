package de.codingair.warpsystem.spigot.base.utils.teleport;

import de.codingair.warpsystem.spigot.base.WarpSystem;
import de.codingair.warpsystem.spigot.base.utils.featureobjects.FeatureObject;
import de.codingair.warpsystem.spigot.features.shortcuts.utils.Shortcut;
import de.codingair.warpsystem.spigot.features.warps.nextlevel.utils.Icon;
import org.jetbrains.annotations.NotNull;

public enum Origin {
    WarpIcon(Icon.class, "WarpGUI", de.codingair.warpsystem.api.destinations.utils.Origin.WarpIcon),
    GlobalWarp(null, "GlobalWarps", de.codingair.warpsystem.api.destinations.utils.Origin.GlobalWarp),
    SimpleWarp(null, "SimpleWarps", de.codingair.warpsystem.api.destinations.utils.Origin.SimpleWarp),
    WarpSign(de.codingair.warpsystem.spigot.features.signs.utils.WarpSign.class, "WarpSigns", de.codingair.warpsystem.api.destinations.utils.Origin.WarpSign),
    ShortCut(Shortcut.class, "Shortcuts", de.codingair.warpsystem.api.destinations.utils.Origin.ShortCut),
    CommandBlock(null, "CommandBlocks", de.codingair.warpsystem.api.destinations.utils.Origin.CommandBlock),
    TeleportCommand(de.codingair.warpsystem.api.destinations.utils.Origin.TeleportCommand),
    Custom(de.codingair.warpsystem.api.destinations.utils.Origin.Custom),
    TeleportRequest(de.codingair.warpsystem.api.destinations.utils.Origin.TeleportRequest),
    PlayerWarp(de.codingair.warpsystem.spigot.features.playerwarps.utils.PlayerWarp.class, "PlayerWarps", de.codingair.warpsystem.api.destinations.utils.Origin.PlayerWarp),
    Portal(de.codingair.warpsystem.spigot.features.portals.utils.Portal.class, "Portals", de.codingair.warpsystem.api.destinations.utils.Origin.Portal),
    Spawn(de.codingair.warpsystem.spigot.features.spawn.utils.Spawn.class, "Spawn", de.codingair.warpsystem.api.destinations.utils.Origin.Spawn),
    RandomTP(null, "RandomTp", de.codingair.warpsystem.api.destinations.utils.Origin.RandomTP),
    TeleportInterception(de.codingair.warpsystem.api.destinations.utils.Origin.TeleportInterception),
    UNKNOWN(de.codingair.warpsystem.api.destinations.utils.Origin.UNKNOWN);

    private final Class<? extends FeatureObject> featureClass;
    private final String configName;
    private final de.codingair.warpsystem.api.destinations.utils.Origin apiOrigin;

    Origin(@NotNull de.codingair.warpsystem.api.destinations.utils.Origin apiOrigin) {
        this(null, null, apiOrigin);
    }

    Origin(Class<? extends FeatureObject> featureClass, String configName, @NotNull de.codingair.warpsystem.api.destinations.utils.Origin apiOrigin) {
        this.featureClass = featureClass;
        this.configName = configName;
        this.apiOrigin = apiOrigin;
    }

    @NotNull
    public static Origin getByApi(@NotNull de.codingair.warpsystem.api.destinations.utils.Origin origin) {
        for (Origin value : values()) {
            if (value.apiOrigin == origin) return value;
        }

        throw new IllegalStateException();
    }

    public static Origin getByClass(FeatureObject object) {
        for (Origin value : values()) {
            if(value.featureClass == null) continue;
            if (value.featureClass.isInstance(object)) return value;
        }

        return UNKNOWN;
    }

    public String getConfigName() {
        return configName;
    }

    public boolean sendTeleportMessage() {
        if (configName == null) return true;
        return WarpSystem.getInstance().getFileManager().getFile("Config").getConfig().getBoolean("WarpSystem.Send.Teleport_Message." + getConfigName(), true);
    }

    public long getCooldown() {
        return WarpSystem.opt().getCooldown(this);
    }

    public de.codingair.warpsystem.api.destinations.utils.Origin getApiOrigin() {
        return apiOrigin;
    }
}
