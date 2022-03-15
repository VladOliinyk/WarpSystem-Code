package de.codingair.warpsystem.spigot.versionfactory.handlers;

import de.codingair.codingapi.server.sounds.Sound;
import de.codingair.codingapi.server.sounds.SoundData;
import de.codingair.codingapi.tools.Callback;
import de.codingair.warpsystem.api.destinations.utils.Result;
import de.codingair.warpsystem.core.transfer.packets.general.TeleportBackPacket;
import de.codingair.warpsystem.core.transfer.utils.PlayerData;
import de.codingair.warpsystem.spigot.base.WarpSystem;
import de.codingair.warpsystem.spigot.base.managers.TeleportManager;
import de.codingair.warpsystem.spigot.base.utils.teleport.Origin;
import de.codingair.warpsystem.spigot.base.utils.teleport.TeleportOptions;
import de.codingair.warpsystem.spigot.base.utils.teleport.destinations.Destination;
import de.codingair.warpsystem.spigot.base.utils.teleport.destinations.adapters.EmptyAdapter;
import de.codingair.warpsystem.spigot.features.teleportcommand.utils.PlayerLocationData;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.concurrent.CompletableFuture;

public class TeleportCommandManager extends de.codingair.warpsystem.spigot.features.teleportcommand.TeleportCommandManager {

    @Override
    public Location invalidateBackPosition(Player player) {
        return this.backPosition.remove(player.getName());
    }

    @Override
    public Location getQuitPosition(String player) {
        Location l = this.quitPosition.get(player);

        if (l != null) return l;

        PlayerLocationData data = this.dying.getIfPresent(player);

        if (data != null) return data.getQuit();
        else return null;
    }

    @Override
    public CompletableFuture<TeleportBackPacket.Result> teleportToLastBackLocation(String player, boolean proxy, boolean force, boolean skip) {
        Location l;
        Player p = Bukkit.getPlayer(player);

        if (proxy) {
            l = this.quitPosition.remove(player);
            if (l == null) l = getQuitPosition(player);
            if (l == null) return CompletableFuture.completedFuture(TeleportBackPacket.Result.NO_LAST_POSITION);

            if (!force && WarpSystem.opt().forbiddenRegion(l)) {
                return proxyBackPacket(p, player, true);
            }
        } else {
            if (p == null) throw new NullPointerException("Cannot handle back action for " + player + "!");
            l = this.backPosition.remove(player);
            if (l == null) return proxyBack(p, skip);
        }

        return CompletableFuture.completedFuture(teleportBack(player, l, force, skip, proxy));
    }

    private CompletableFuture<TeleportBackPacket.Result> proxyBack(Player player, boolean skip) {
        PlayerData data = WarpSystem.getInstance().getPlayerDataManager().getCache(player);
        if (data.getOldServer() != null) {
            //switch server
            CompletableFuture<TeleportBackPacket.Result> future = new CompletableFuture<>();
            TeleportOptions options = new TeleportOptions(new Destination(new EmptyAdapter()), "", Origin.TeleportCommand);
            options.setMessage(null);
            options.setAfterEffects(false, true);
            options.setTeleportSound(new SoundData(Sound.AMBIENT_CAVE, 0F, 1F));
            if (skip) options.setSkip(true);

            options.addCallback(new Callback<Result>() {
                @Override
                public void accept(Result result) {
                    if (result == Result.SUCCESS) {
                        proxyBackPacket(player, player.getName(), false).whenComplete((res, t) -> {
                            if (t != null) future.completeExceptionally(t);
                            else future.complete(res);
                        });
                    }
                }
            });

            TeleportManager.getInstance().teleport(player, options);
            return future;
        } else return CompletableFuture.completedFuture(TeleportBackPacket.Result.NO_LAST_POSITION);
    }

    private CompletableFuture<TeleportBackPacket.Result> proxyBackPacket(Player possibleGate, String player, boolean force) {
        CompletableFuture<TeleportBackPacket.Result> future = new CompletableFuture<>();

        WarpSystem.getDataHandler().send(new TeleportBackPacket(player, false, true, force, false), possibleGate).whenComplete((success, t) -> {
            if (t != null) future.completeExceptionally(t);

            TeleportBackPacket.Result res = TeleportBackPacket.Result.fromId(success.getByte());
            future.complete(res);
        });

        return future;
    }
}
