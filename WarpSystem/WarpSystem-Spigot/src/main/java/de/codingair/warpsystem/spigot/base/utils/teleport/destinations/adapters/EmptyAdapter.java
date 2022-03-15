package de.codingair.warpsystem.spigot.base.utils.teleport.destinations.adapters;

import de.codingair.codingapi.tools.Callback;
import de.codingair.codingapi.tools.Location;
import de.codingair.warpsystem.api.destinations.IEmptyAdapter;
import de.codingair.warpsystem.api.destinations.utils.Result;
import de.codingair.warpsystem.api.destinations.utils.SimulatedTeleportResult;
import de.codingair.warpsystem.spigot.api.events.PlayerTeleportAcceptEvent;
import de.codingair.warpsystem.spigot.base.WarpSystem;
import de.codingair.warpsystem.spigot.base.utils.teleport.destinations.DestinationAdapter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class EmptyAdapter extends DestinationAdapter implements IEmptyAdapter {

    @Override
    public CompletableFuture<Boolean> teleport(@NotNull Player player, @Nullable String id, @NotNull Vector randomOffset, String displayName, boolean checkPermission, String message, boolean silent, double costs, Callback<Result> callback) {
        if (callback != null) callback.accept(Result.SUCCESS);
        Bukkit.getScheduler().runTaskLater(WarpSystem.getInstance(), () -> Bukkit.getPluginManager().callEvent(new PlayerTeleportAcceptEvent(player)), 1L);
        return CompletableFuture.completedFuture(true);
    }

    @Override
    public SimulatedTeleportResult simulate(@NotNull Player player, @NotNull String id, boolean checkPermission) {
        return new SimulatedTeleportResult(null, Result.SUCCESS);
    }

    @Override
    public double getCosts(@NotNull String id) {
        return 0;
    }

    @Override
    public Location buildLocation(@NotNull String id) {
        return null;
    }

    @Override
    public boolean usesBukkitTeleportation() {
        return true;
    }
}
