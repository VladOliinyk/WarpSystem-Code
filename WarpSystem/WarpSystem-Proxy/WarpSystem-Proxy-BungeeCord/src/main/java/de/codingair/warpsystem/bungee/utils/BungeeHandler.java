package de.codingair.warpsystem.bungee.utils;

import de.codingair.codingapi.bungeecord.BungeeAPI;
import de.codingair.packetmanagement.packets.Packet;
import de.codingair.packetmanagement.packets.RequestPacket;
import de.codingair.packetmanagement.packets.ResponsePacket;
import de.codingair.packetmanagement.utils.Direction;
import de.codingair.warpsystem.bungee.base.Lang;
import de.codingair.warpsystem.bungee.base.WarpSystem;
import de.codingair.warpsystem.bungee.base.events.ServerProvideOptionsEvent;
import de.codingair.warpsystem.core.proxy.transfer.CoreDataHandler;
import de.codingair.warpsystem.core.proxy.transfer.handlers.SendOptionsPacketHandler;
import de.codingair.warpsystem.core.proxy.transfer.handlers.TeleportSpawnPacketHandler;
import de.codingair.warpsystem.core.proxy.utils.Player;
import de.codingair.warpsystem.core.proxy.utils.Server;
import de.codingair.warpsystem.core.transfer.packets.general.TeleportSpawnPacket;
import de.codingair.warpsystem.core.transfer.packets.spigot.SendOptionsPacket;
import de.codingair.warpsystem.core.transfer.utils.serializeable.ServerOptions;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class BungeeHandler extends CoreDataHandler<String> implements Listener {
    public BungeeHandler(WarpSystem plugin) {
        super(plugin);
    }

    @Override
    public void registering() {
        super.registering();

        registerHandler(SendOptionsPacket.class, new SendOptionsPacketHandler() {
            @Override
            public void callEvent(Server<?> connection, ServerOptions options) {
                WarpSystem.getInstance().getProxy().getPluginManager().callEvent(new ServerProvideOptionsEvent((BungeeServer) connection, options));
            }
        });

        registerHandler(TeleportSpawnPacket.class, new TeleportSpawnPacketHandler() {
            @Override
            public void sendServerIsNotOnline(Player player) {
                ((BungeePlayer) player).getPlayer().sendMessage(new TextComponent(Lang.getPrefix() + Lang.get("Server_Is_Not_Online")));
            }
        });
    }

    @Override
    public String getBackendChannel() {
        return channelBackend;
    }

    @Override
    public String getProxyChannel() {
        return channelProxy;
    }

    public void onEnable() {
        super.onEnable();
        BungeeAPI.getProxy().getPluginManager().registerListener((WarpSystem) proxy, this);
        BungeeAPI.getProxy().registerChannel(channelProxy);
        BungeeAPI.getProxy().registerChannel(channelBackend);
    }

    public void onDisable() {
        super.onDisable();
        BungeeAPI.getProxy().getPluginManager().unregisterListener(this);
        BungeeAPI.getProxy().unregisterChannel(channelProxy);
        BungeeAPI.getProxy().unregisterChannel(channelBackend);
    }

    public void send(@NotNull Packet packet, @NotNull ServerInfo connection, @NotNull Direction direction) {
        super.send(packet, new BungeeServer(connection), direction);
    }

    public <A extends ResponsePacket> CompletableFuture<A> send(@NotNull RequestPacket<A> packet, @NotNull ServerInfo connection, @NotNull Direction direction) {
        return super.send(packet, new BungeeServer(connection), direction);
    }

    public <A extends ResponsePacket> CompletableFuture<A> send(@NotNull RequestPacket<A> packet, @NotNull ServerInfo connection, @NotNull Direction direction, long timeOut) {
        return super.send(packet, new BungeeServer(connection), direction, timeOut);
    }

    @EventHandler
    public void onPluginMessage(PluginMessageEvent e) {
        if (e.getTag().equals(getChannelProxy())) {
            receive(e.getData(), new BungeeServer(((ProxiedPlayer) e.getReceiver()).getServer().getInfo()), Direction.DOWN);

            //cancel here to avoid sending these packets to clients
            e.setCancelled(true);
        } else if (e.getTag().equals(getChannelBackend())) {
            //disallow hack clients from sending own packets through this channel
            e.setCancelled(true);
        }
    }
}
