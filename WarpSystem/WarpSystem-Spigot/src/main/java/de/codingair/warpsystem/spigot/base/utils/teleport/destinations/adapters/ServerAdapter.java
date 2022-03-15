package de.codingair.warpsystem.spigot.base.utils.teleport.destinations.adapters;

import de.codingair.codingapi.tools.Callback;
import de.codingair.codingapi.tools.Location;
import de.codingair.codingapi.tools.io.utils.DataMask;
import de.codingair.warpsystem.api.destinations.IServerAdapter;
import de.codingair.warpsystem.api.destinations.utils.Result;
import de.codingair.warpsystem.api.destinations.utils.SimulatedTeleportResult;
import de.codingair.warpsystem.core.transfer.packets.general.PrepareCoordinationTeleportPacket;
import de.codingair.warpsystem.core.transfer.packets.spigot.PrepareServerSwitchPacket;
import de.codingair.warpsystem.spigot.base.WarpSystem;
import de.codingair.warpsystem.spigot.base.utils.Lang;
import de.codingair.warpsystem.spigot.base.utils.Permissions;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class ServerAdapter extends CloneableAdapter implements IServerAdapter {
    private boolean keepPosition = false;
    private String server = null;

    public ServerAdapter() {
    }

    private ServerAdapter(ServerAdapter serverAdapter) {
        this.keepPosition = serverAdapter.keepPosition;
        this.server = serverAdapter.server;
    }

    @Override
    public CompletableFuture<Boolean> teleport(@NotNull Player player, @Nullable String id, @NotNull Vector randomOffset, String displayName, boolean checkPermission, String message, boolean silent, double costs, Callback<Result> callback) {
        if (!WarpSystem.getInstance().isProxyConnected()) {
            if (callback != null) callback.accept(Result.NO_CONNECTED_PROXY);
            return CompletableFuture.completedFuture(false);
        }

        if (WarpSystem.getInstance().getCurrentServer().equalsIgnoreCase(server)) {
            player.sendMessage(Lang.getPrefix() + Lang.get("Player_Is_Already_On_Target_Server"));
            return CompletableFuture.completedFuture(false);
        }

        if (keepPosition) {
            if(message == null) message = PrepareCoordinationTeleportPacket.NO_MESSAGE;

            org.bukkit.Location location = player.getLocation();
            PrepareCoordinationTeleportPacket packet = new PrepareCoordinationTeleportPacket(player.getName(), server, player.getWorld().getName(), displayName, message,
                    location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch(),
                    costs, player.hasPermission(Permissions.PERMISSION_ByPass_Teleport_Max_Players));

            GlobalLocationAdapter.coordinationTeleportPacket(player, callback, packet);
        } else {
            WarpSystem.getDataHandler().send(new PrepareServerSwitchPacket(player.getName(), server, message, player.hasPermission(Permissions.PERMISSION_ByPass_Teleport_Max_Players)), player).thenAccept(packet -> {
                int result = packet.a();
                if (callback != null) {
                    if (result == 0) callback.accept(Result.SUCCESS);
                    else if (result == 1) callback.accept(Result.SERVER_NOT_AVAILABLE);
                    else if (result == 2) callback.accept(Result.ALREADY_ON_TARGET_SERVER);
                    else if (result == 3) callback.accept(Result.SERVER_NOT_AVAILABLE);
                    else if (result == 4) callback.accept(Result.ERROR);
                    else if (result == 5) callback.accept(Result.TARGET_SERVER_IS_FULL);
                }

                if (result == 2) player.sendMessage(Lang.getPrefix() + Lang.get("Player_Is_Already_On_Target_Server"));
            });
        }
        return CompletableFuture.completedFuture(false);
    }

    @Override
    public SimulatedTeleportResult simulate(@NotNull Player player, @NotNull String id, boolean checkPermission) {
        if (!WarpSystem.getInstance().isProxyConnected())
            return new SimulatedTeleportResult(null, Result.NO_CONNECTED_PROXY);

        if (WarpSystem.getInstance().getCurrentServer().equalsIgnoreCase(id))
            return new SimulatedTeleportResult(Lang.getPrefix() + Lang.get("Player_Is_Already_On_Target_Server"), Result.ALREADY_ON_TARGET_SERVER);
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
    public boolean read(DataMask d) throws Exception {
        this.keepPosition = d.getBoolean("keepPosition");
        this.server = d.getString("id");
        return true;
    }

    @Override
    public void write(DataMask d) {
        d.put("keepPosition", this.keepPosition);
        d.put("id", this.server);
    }

    public boolean isKeepPosition() {
        return keepPosition;
    }

    public void setKeepPosition(boolean keepPosition) {
        this.keepPosition = keepPosition;
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    @Override
    public ServerAdapter clone() {
        return new ServerAdapter(this);
    }

    @Override
    public boolean usable() {
        return server != null;
    }

    @Override
    public String getId() {
        return server;
    }

    @Override
    public boolean usesBukkitTeleportation() {
        return false;
    }
}
