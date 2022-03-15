package de.codingair.warpsystem.spigot.features.globalwarps.managers;

import de.codingair.codingapi.tools.Callback;
import de.codingair.packetmanagement.packets.impl.BooleanPacket;
import de.codingair.warpsystem.core.transfer.packets.spigot.DeleteGlobalWarpPacket;
import de.codingair.warpsystem.core.transfer.packets.spigot.GlobalWarpTeleportPacket;
import de.codingair.warpsystem.core.transfer.packets.spigot.PublishGlobalWarpPacket;
import de.codingair.warpsystem.core.transfer.packets.spigot.RequestGlobalWarpNamesPacket;
import de.codingair.warpsystem.core.transfer.utils.serializeable.SGlobalWarp;
import de.codingair.warpsystem.core.transfer.utils.serializeable.SLocation;
import de.codingair.warpsystem.core.utils.Manager;
import de.codingair.warpsystem.spigot.base.WarpSystem;
import de.codingair.warpsystem.spigot.base.managers.TeleportManager;
import de.codingair.warpsystem.spigot.base.utils.Permissions;
import de.codingair.warpsystem.spigot.base.utils.ProxyFeature;
import de.codingair.warpsystem.spigot.base.utils.teleport.process.Teleport;
import de.codingair.warpsystem.spigot.features.FeatureType;
import de.codingair.warpsystem.spigot.features.globalwarps.commands.CGlobalWarp;
import de.codingair.warpsystem.spigot.features.globalwarps.commands.CGlobalWarps;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

public class GlobalWarpManager implements Manager, ProxyFeature {
    //              Name,   Server
    private final HashMap<String, String> globalWarps = new HashMap<>();

    public static GlobalWarpManager getInstance() {
        return WarpSystem.getInstance().getDataManager().getManager(FeatureType.GLOBAL_WARPS);
    }

    public CompletableFuture<BooleanPacket> create(Player player, String warpName, Location loc) {
        return WarpSystem.getDataHandler().send(new PublishGlobalWarpPacket(new SGlobalWarp(warpName, new SLocation(loc.getWorld().getName(), loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch()))), player);
    }

    public CompletableFuture<BooleanPacket> updatePosition(Player player, String warpName, Location loc) {
        return WarpSystem.getDataHandler().send(new PublishGlobalWarpPacket(new SGlobalWarp(warpName, new SLocation(loc.getWorld().getName(), loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch())), true), player);
    }

    public CompletableFuture<BooleanPacket> delete(Player player, @NotNull String warpName) {
        return WarpSystem.getDataHandler().send(new DeleteGlobalWarpPacket(warpName), player);
    }

    public String getCaseCorrectlyName(String name) {
        for (String warp : this.globalWarps.keySet()) {
            if (warp.equalsIgnoreCase(name)) return warp;
        }

        return name;
    }

    public boolean exists(String name) {
        for (String warp : this.globalWarps.keySet()) {
            if (warp.equalsIgnoreCase(name)) return true;
        }

        return false;
    }

    public void teleport(Player player, String id, Vector randomOffset, String displayName, String message, double costs, @NotNull Callback<GlobalWarpTeleportPacket.Result> callback) {
        if (id == null) {
            callback.accept(GlobalWarpTeleportPacket.Result.WARP_NOT_EXISTS);
            return;
        }

        id = getCaseCorrectlyName(id);
        if (!this.globalWarps.containsKey(id)) {
            callback.accept(GlobalWarpTeleportPacket.Result.WARP_NOT_EXISTS);
            return;
        }

        double x = 0, y = 0, z = 0;
        if (randomOffset != null) {
            x = randomOffset.getX();
            y = randomOffset.getY();
            z = randomOffset.getZ();
        }

        Teleport t = TeleportManager.getInstance().getTeleport(player);
        boolean keepRotation = false;
        if (t != null) keepRotation = !t.getDestination().getCustomOptions().isRotation();

        WarpSystem.getDataHandler().send(new GlobalWarpTeleportPacket(player.getName(), id, x, y, z, displayName, message, costs, keepRotation, player.hasPermission(Permissions.PERMISSION_ByPass_Teleport_Max_Players)), player).thenAccept(packet -> callback.accept(GlobalWarpTeleportPacket.Result.getById(packet.a())));
    }

    @Override
    public boolean load(boolean loader) {
        this.globalWarps.clear();

        WarpSystem.getInstance().getProxyFeatureList().add(this);

        new CGlobalWarp().register();
        new CGlobalWarps().register();
        return true;
    }

    @Override
    public void save(boolean saver) {
    }

    @Override
    public void destroy() {
        this.globalWarps.clear();
    }

    @Override
    public void onInitiate(Player connection) {
        WarpSystem.getDataHandler().send(new RequestGlobalWarpNamesPacket(), connection);
    }

    @Override
    public void onConnect(Player connection) {
    }

    @Override
    public void onDisconnect() {
    }

    public HashMap<String, String> getGlobalWarps() {
        return globalWarps;
    }
}
