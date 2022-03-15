package de.codingair.warpsystem.spigot.base.utils.teleport.destinations.adapters;

import de.codingair.codingapi.tools.Callback;
import de.codingair.codingapi.tools.Location;
import de.codingair.codingapi.tools.io.utils.DataMask;
import de.codingair.warpsystem.api.destinations.ILocationAdapter;
import de.codingair.warpsystem.api.destinations.utils.Result;
import de.codingair.warpsystem.api.destinations.utils.SimulatedTeleportResult;
import de.codingair.warpsystem.spigot.base.WarpSystem;
import de.codingair.warpsystem.spigot.base.listeners.TeleportListener;
import de.codingair.warpsystem.spigot.base.utils.Lang;
import io.papermc.lib.PaperLib;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

public class LocationAdapter extends CloneableAdapter implements ILocationAdapter {
    protected Location location;

    public LocationAdapter() {
    }

    public LocationAdapter(@NotNull Location location) {
        this.location = location;
    }

    public LocationAdapter(@NotNull org.bukkit.Location location) {
        this.location = new Location(location);
    }

    @Override
    public LocationAdapter clone() {
        return new LocationAdapter(location.clone());
    }

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

            teleport(player, silent, callback, location, future);

            return future;
        }
    }

    protected void teleport(Player player, boolean silent, Callback<Result> callback, Location location, CompletableFuture<Boolean> future) {
        prepare(player, location.clone()).whenComplete(handleLocation(player, silent, callback, future));
    }

    @NotNull
    public static BiConsumer<org.bukkit.Location, Throwable> handleLocation(Player player, boolean silent, Callback<Result> callback, CompletableFuture<Boolean> future) {
        return (l, t) -> {

            if (t != null) t.printStackTrace();
            if (l == null) {
                future.complete(false);
            } else {
                if (silent) TeleportListener.TELEPORTS.put(player, l);

                Bukkit.getScheduler().runTask(WarpSystem.getInstance(),  () -> PaperLib.teleportAsync(player, l, PlayerTeleportEvent.TeleportCause.PLUGIN).whenComplete((b, t2) -> {
                    if (t2 != null) {
                        t2.printStackTrace();
                        if (callback != null) callback.accept(Result.ERROR);
                        future.complete(false);
                    } else if (b) {
                        if (callback != null) callback.accept(Result.SUCCESS);
                        future.complete(true);
                    } else {
                        if (callback != null) callback.accept(Result.ERROR);
                        future.complete(false);
                    }
                }));
            }
        };
    }

    @Override
    public SimulatedTeleportResult simulate(@NotNull Player player, @NotNull String id, boolean checkPermission) {
        Location location = buildLocation(id);

        if (location == null) {
            return new SimulatedTeleportResult(Lang.getPrefix() + Lang.get("WARP_DOES_NOT_EXISTS"), Result.DESTINATION_DOES_NOT_EXIST);
        }

        if (location.getWorld() == null) {
            return new SimulatedTeleportResult(Lang.getPrefix() + Lang.get("World_Not_Exists"), Result.WORLD_DOES_NOT_EXIST);
        } else return new SimulatedTeleportResult(null, Result.SUCCESS);
    }

    @Override
    public double getCosts(@NotNull String id) {
        return 0;
    }

    @Override
    public Location buildLocation(@NotNull String id) {
        return this.location == null ? Location.getByJSONString(id) : this.location;
    }

    public @Nullable Location getLocation() {
        return location;
    }

    public void setLocation(@Nullable Location location) {
        this.location = location;
    }

    @Override
    public boolean read(DataMask d) throws Exception {
        this.location = d.getSerializable("id", new Location());
        return true;
    }

    @Override
    public void write(DataMask d) {
        d.put("id", this.location);
    }

    @Override
    public boolean usable() {
        return location != null;
    }

    @Override
    public String getId() {
        return location.toJSONString(2);
    }

    @Override
    public boolean usesBukkitTeleportation() {
        return true;
    }
}
