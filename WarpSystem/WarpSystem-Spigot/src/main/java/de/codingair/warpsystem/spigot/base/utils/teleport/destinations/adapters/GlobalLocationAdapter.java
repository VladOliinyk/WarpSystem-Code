package de.codingair.warpsystem.spigot.base.utils.teleport.destinations.adapters;

import de.codingair.codingapi.tools.Callback;
import de.codingair.codingapi.tools.Location;
import de.codingair.codingapi.tools.io.utils.DataMask;
import de.codingair.warpsystem.api.destinations.IGlobalLocationAdapter;
import de.codingair.warpsystem.api.destinations.utils.Result;
import de.codingair.warpsystem.api.destinations.utils.SimulatedTeleportResult;
import de.codingair.warpsystem.core.transfer.packets.general.PrepareCoordinationTeleportPacket;
import de.codingair.warpsystem.spigot.base.WarpSystem;
import de.codingair.warpsystem.spigot.base.utils.Lang;
import de.codingair.warpsystem.spigot.base.utils.Permissions;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class GlobalLocationAdapter extends LocationAdapter implements IGlobalLocationAdapter {
    private String server;

    public GlobalLocationAdapter() {
    }

    public GlobalLocationAdapter(String server, Location location) {
        super(location);
        this.server = server;
    }

    @Override
    public GlobalLocationAdapter clone() {
        return new GlobalLocationAdapter(server, location.clone());
    }

    @Override
    public boolean read(DataMask d) throws Exception {
        d.put("server", server);
        location = new Location();
        d.getSerializable("id", location);
        return true;
    }

    @Override
    public void write(DataMask d) {
        d.put("server", server);
        d.put("id", location);
    }

    @Override
    public CompletableFuture<Boolean> teleport(@NotNull Player player, @Nullable String id, @NotNull Vector randomOffset, String displayName, boolean checkPermission, String message, boolean silent, double costs, Callback<Result> callback) {
        if (location == null) {
            player.sendMessage(Lang.getPrefix() + Lang.get("WARP_DOES_NOT_EXISTS"));
            if (callback != null) callback.accept(Result.DESTINATION_DOES_NOT_EXIST);
            return CompletableFuture.completedFuture(false);
        }

        Location location = this.location.clone();

        if (server == null || server.equals(WarpSystem.getInstance().getCurrentServer())) {
            if (location.getWorld() == null) {
                player.sendMessage(Lang.getPrefix() + Lang.get("World_Not_Exists"));
                if (callback != null) callback.accept(Result.WORLD_DOES_NOT_EXIST);
                return CompletableFuture.completedFuture(false);
            } else {
                CompletableFuture<Boolean> future = new CompletableFuture<>();
                super.teleport(player, silent, callback, location, future);
                return future;
            }
        } else {
            PrepareCoordinationTeleportPacket packet = new PrepareCoordinationTeleportPacket(player.getName(), server, location.getWorldName(), displayName, message,
                    location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch(),
                    costs, player.hasPermission(Permissions.PERMISSION_ByPass_Teleport_Max_Players));

            coordinationTeleportPacket(player, callback, packet);
            return CompletableFuture.completedFuture(true);
        }
    }

    static void coordinationTeleportPacket(Player player, Callback<Result> callback, PrepareCoordinationTeleportPacket packet) {
        WarpSystem.getDataHandler().send(packet, player).thenAccept(result -> {
            if (callback == null) return;
            switch (result.a()) {
                case 0:
                    callback.accept(Result.SUCCESS);
                    break;
                case 1:
                    callback.accept(Result.SERVER_NOT_AVAILABLE);
                    break;
                case 2:
                    callback.accept(Result.WORLD_DOES_NOT_EXIST);
                    break;
                case 3:
                    callback.accept(Result.TARGET_SERVER_IS_FULL);
                    break;
                default:
                    callback.accept(Result.CANCELLED);
            }
        });
    }

    @Override
    public SimulatedTeleportResult simulate(@NotNull Player player, @NotNull String id, boolean checkPermission) {
        Location location = buildLocation(id);

        if (location == null) {
            return new SimulatedTeleportResult(Lang.getPrefix() + Lang.get("WARP_DOES_NOT_EXISTS"), Result.DESTINATION_DOES_NOT_EXIST);
        }

        if (server == null || server.equals(WarpSystem.getInstance().getCurrentServer())) {
            if (location.getWorld() == null) {
                return new SimulatedTeleportResult(Lang.getPrefix() + Lang.get("World_Not_Exists"), Result.WORLD_DOES_NOT_EXIST);
            } else return new SimulatedTeleportResult(null, Result.SUCCESS);
        } else {
            return new SimulatedTeleportResult(null, Result.SUCCESS);
        }
    }

    @Override
    public double getCosts(@NotNull String id) {
        return 0;
    }

    @Override
    public Location buildLocation(@NotNull String id) {
        return this.location == null ? Location.getByJSONString(id) : this.location;
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    @Override
    public boolean usesBukkitTeleportation() {
        return server == null || server.equals(WarpSystem.getInstance().getCurrentServer());
    }
}
