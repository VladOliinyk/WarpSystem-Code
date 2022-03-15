package de.codingair.warpsystem.velocity.utils;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.messages.ChannelIdentifier;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import de.codingair.packetmanagement.utils.Direction;
import de.codingair.warpsystem.core.proxy.transfer.CoreDataHandler;
import de.codingair.warpsystem.core.proxy.transfer.handlers.SendOptionsPacketHandler;
import de.codingair.warpsystem.core.proxy.transfer.handlers.TeleportSpawnPacketHandler;
import de.codingair.warpsystem.core.proxy.utils.Player;
import de.codingair.warpsystem.core.proxy.utils.Server;
import de.codingair.warpsystem.core.transfer.packets.general.TeleportSpawnPacket;
import de.codingair.warpsystem.core.transfer.packets.spigot.SendOptionsPacket;
import de.codingair.warpsystem.core.transfer.utils.serializeable.ServerOptions;
import de.codingair.warpsystem.velocity.base.WarpSystem;
import de.codingair.warpsystem.velocity.base.events.ServerProvideOptionsEvent;
import de.codingair.warpsystem.velocity.base.utils.Lang;
import net.kyori.adventure.text.Component;

public class VelocityHandler extends CoreDataHandler<ChannelIdentifier> {
    private final ChannelIdentifier identifierProxy;
    private final ChannelIdentifier identifierBackend;

    public VelocityHandler(WarpSystem plugin) {
        super(plugin);

        String namespace = plugin.getName().toLowerCase();
        identifierProxy = MinecraftChannelIdentifier.create(namespace, "proxy");
        identifierBackend = MinecraftChannelIdentifier.create(namespace, "backend");
    }

    @Override
    public void registering() {
        super.registering();

        registerHandler(SendOptionsPacket.class, new SendOptionsPacketHandler() {
            @Override
            public void callEvent(Server<?> connection, ServerOptions options) {
                WarpSystem.proxy().getEventManager().fire(new ServerProvideOptionsEvent((VelocityServer) connection, options));
            }
        });

        registerHandler(TeleportSpawnPacket.class, new TeleportSpawnPacketHandler() {
            @Override
            public void sendServerIsNotOnline(Player player) {
                ((VelocityPlayer) player).getPlayer().sendMessage(Component.text(Lang.getPrefix() + Lang.get("Server_Is_Not_Online")));
            }
        });
    }

    @Override
    public ChannelIdentifier getBackendChannel() {
        return identifierBackend;
    }

    @Override
    public ChannelIdentifier getProxyChannel() {
        return identifierProxy;
    }

    public void onEnable() {
        super.onEnable();
        WarpSystem.proxy().getEventManager().register(WarpSystem.getInstance(), this);
        WarpSystem.proxy().getChannelRegistrar().register(identifierProxy, identifierBackend);
    }

    public void onDisable() {
        super.onDisable();
        WarpSystem.proxy().getEventManager().unregisterListener(WarpSystem.getInstance(), this);
        WarpSystem.proxy().getChannelRegistrar().unregister(identifierProxy, identifierBackend);
    }

    @Subscribe
    public void onPluginMessage(PluginMessageEvent e) {
        if (e.getIdentifier().equals(getProxyChannel())) {
            receive(e.getData(), new VelocityServer(((ServerConnection) e.getSource()).getServer()), Direction.DOWN);

            //cancel here to avoid sending these packets to clients
            e.setResult(PluginMessageEvent.ForwardResult.handled());
        } else if (e.getIdentifier().equals(getBackendChannel())) {
            //disallow hack clients from sending own packets through this channel
            e.setResult(PluginMessageEvent.ForwardResult.handled());
        }
    }
}
