package de.codingair.warpsystem.spigot.features.portals.utils;

import de.codingair.codingapi.tools.Callback;
import de.codingair.warpsystem.api.destinations.utils.Result;
import de.codingair.warpsystem.api.destinations.utils.SimulatedTeleportResult;
import de.codingair.warpsystem.spigot.base.utils.Lang;
import de.codingair.warpsystem.spigot.base.utils.teleport.destinations.DestinationAdapter;
import de.codingair.warpsystem.spigot.base.utils.teleport.destinations.adapters.LocationAdapter;
import de.codingair.warpsystem.spigot.features.portals.managers.PortalManager;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class PortalDestinationAdapter extends DestinationAdapter {
    @Override
    public CompletableFuture<Boolean> teleport(@NotNull Player player, @Nullable String id, @NotNull Vector randomOffset, String displayName, boolean checkPermission, String message, boolean silent, double costs, Callback<Result> callback) {
        Location location = buildLocation(id);

        if (location == null) {
            player.sendMessage(Lang.getPrefix() + Lang.get("WARP_DOES_NOT_EXISTS"));
            if (callback != null) callback.accept(Result.DESTINATION_DOES_NOT_EXIST);
            return CompletableFuture.completedFuture(false);
        }

        if (location.getWorld() == null) {
            player.sendMessage(Lang.getPrefix() + Lang.get("World_Not_Exists"));
            if (callback != null) callback.accept(Result.WORLD_DOES_NOT_EXIST);
            return CompletableFuture.completedFuture(false);
        } else {
            CompletableFuture<Boolean> future = new CompletableFuture<>();
            prepare(player, location.clone()).whenComplete(LocationAdapter.handleLocation(player, silent, callback, future));
            return future;
        }
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
    public de.codingair.codingapi.tools.Location buildLocation(String id) {
        Portal p = PortalManager.getInstance().getPortal(id);
        if (p == null) return null;

        return p.getSpawn();
    }

    @Override
    public boolean usesBukkitTeleportation() {
        return true;
    }
}
