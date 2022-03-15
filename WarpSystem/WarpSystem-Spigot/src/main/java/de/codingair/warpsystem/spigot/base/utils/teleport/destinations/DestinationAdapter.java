package de.codingair.warpsystem.spigot.base.utils.teleport.destinations;

import de.codingair.codingapi.tools.Callback;
import de.codingair.warpsystem.api.destinations.utils.IDestinationAdapter;
import de.codingair.warpsystem.api.destinations.utils.Result;
import de.codingair.warpsystem.spigot.base.utils.teleport.TeleportUtils;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public abstract class DestinationAdapter implements IDestinationAdapter {
    Destination destination;

    @Override
    public final @NotNull CompletableFuture<Boolean> teleport(@NotNull Player player, @Nullable String id, @Nullable Vector randomOffset, @Nullable String displayName, boolean checkPermission, @Nullable String message, double costs, @Nullable Callback<Result> callback) {
        return teleport(player, id, randomOffset == null ? new Vector(0, 0, 0) : randomOffset, displayName, checkPermission, message, false, costs, callback);
    }

    public abstract CompletableFuture<Boolean> teleport(@NotNull Player player, @Nullable String id, @NotNull Vector randomOffset, @Nullable String displayName, boolean checkPermission, @Nullable String message, boolean silent, double costs, @Nullable Callback<Result> callback);

    public DestinationAdapter dest(Destination d) {
        destination = d;
        return this;
    }

    public CompletableFuture<org.bukkit.Location> prepare(Player player, org.bukkit.Location location) {
        if (location == null) return CompletableFuture.completedFuture(null);
        if (destination != null) {
            destination.adjustLocation(player, location);
            if (destination.getCustomOptions().isSafeTP()) return TeleportUtils.prepareLocation(location, player);
        }

        return CompletableFuture.completedFuture(location);
    }
}
