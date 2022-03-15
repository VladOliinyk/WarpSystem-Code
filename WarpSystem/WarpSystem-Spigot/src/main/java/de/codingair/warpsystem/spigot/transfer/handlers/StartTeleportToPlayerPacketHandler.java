package de.codingair.warpsystem.spigot.transfer.handlers;

import de.codingair.codingapi.tools.Callback;
import de.codingair.codingapi.utils.ChatColor;
import de.codingair.packetmanagement.handlers.ResponsiblePacketHandler;
import de.codingair.packetmanagement.packets.impl.IntegerPacket;
import de.codingair.packetmanagement.utils.Direction;
import de.codingair.packetmanagement.utils.Proxy;
import de.codingair.warpsystem.api.destinations.utils.Result;
import de.codingair.warpsystem.core.transfer.packets.general.StartTeleportToPlayerPacket;
import de.codingair.warpsystem.core.transfer.packets.spigot.PrepareTeleportPlayerToPlayerPacket;
import de.codingair.warpsystem.spigot.base.WarpSystem;
import de.codingair.warpsystem.spigot.base.utils.Lang;
import de.codingair.warpsystem.spigot.base.utils.teleport.Origin;
import de.codingair.warpsystem.spigot.base.utils.teleport.TeleportOptions;
import de.codingair.warpsystem.spigot.base.utils.teleport.destinations.Destination;
import de.codingair.warpsystem.spigot.base.utils.teleport.destinations.adapters.EmptyAdapter;
import de.codingair.warpsystem.spigot.features.teleportcommand.TeleportCommandManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class StartTeleportToPlayerPacketHandler implements ResponsiblePacketHandler<StartTeleportToPlayerPacket, IntegerPacket> {
    @Override
    public @NotNull CompletableFuture<IntegerPacket> response(@NotNull StartTeleportToPlayerPacket packet, @NotNull Proxy proxy, @Nullable Object connection, @NotNull Direction direction) {
        Player player = Bukkit.getPlayerExact(packet.getPlayer());

        if (player == null) {
            return CompletableFuture.completedFuture(new IntegerPacket(1));
        }

        TeleportOptions options = new TeleportOptions(new Destination(new EmptyAdapter()), packet.getToDisplayName(), Origin.TeleportRequest);
        options.setWaitForTeleport(true);
        options.setMessage(null);
        options.setPayMessage(null);
        options.setCosts(TeleportCommandManager.getInstance().getTpaCosts());
        options.addCallback(new Callback<Result>() {
            @Override
            public void accept(Result result) {
                //move
                if (result == Result.SUCCESS) {
                    WarpSystem.getDataHandler().send(new PrepareTeleportPlayerToPlayerPacket(player.getName(), packet.getTo()).setCosts(TeleportCommandManager.getInstance().getTpaCosts()), player).thenAccept(integerPacket -> {
                        if (integerPacket.a() != 0) {
                            player.sendMessage(Lang.getPrefix() + Lang.get("TeleportRequest_not_valid").replace("%PLAYER%", ChatColor.stripColor(packet.getToDisplayName())));
                        }
                    });
                }
            }
        });

        WarpSystem.getInstance().getTeleportManager().teleport(player, options);

        return CompletableFuture.completedFuture(new IntegerPacket(0));
    }
}
