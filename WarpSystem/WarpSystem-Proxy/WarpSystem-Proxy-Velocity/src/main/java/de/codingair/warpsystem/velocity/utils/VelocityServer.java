package de.codingair.warpsystem.velocity.utils;

import com.velocitypowered.api.proxy.messages.ChannelIdentifier;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import de.codingair.warpsystem.core.proxy.utils.Player;
import de.codingair.warpsystem.core.proxy.utils.Server;
import de.codingair.warpsystem.core.transfer.packets.spigot.utils.ServerPing;
import net.kyori.adventure.text.TextComponent;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public class VelocityServer implements Server<ChannelIdentifier> {
    private final RegisteredServer server;

    public VelocityServer(@NotNull RegisteredServer server) {
        this.server = server;
    }

    @Override
    public String getName() {
        return server.getServerInfo().getName();
    }

    @Override
    public Stream<Player> getOnlinePlayers() {
        return server.getPlayersConnected().stream().map(VelocityPlayer::new);
    }

    @Override
    public int getOnlineCount() {
        return this.server.getPlayersConnected().size();
    }

    @Override
    public CompletableFuture<ServerPing> ping() {
        CompletableFuture<ServerPing> future = new CompletableFuture<>();
        server.ping().whenComplete((serverPing, t) -> {
            if (t != null) future.completeExceptionally(t);
            else {
                Optional<com.velocitypowered.api.proxy.server.ServerPing.Players> players = serverPing.getPlayers();

                int online = 0;
                int max = 0;
                if (players.isPresent()) {
                    online = players.get().getOnline();
                    max = players.get().getMax();
                }

                String motd = null;
                if (serverPing.getDescriptionComponent() instanceof TextComponent) {
                    motd = ((TextComponent) serverPing.getDescriptionComponent()).content();
                }

                future.complete(new ServerPing(true, online, max, motd));
            }
        });
        return future;
    }

    public RegisteredServer getServer() {
        return server;
    }

    @Override
    public void sendData(ChannelIdentifier channel, byte[] data) {
        try {
            this.server.sendPluginMessage(channel, data);
        } catch (IllegalStateException ex) {
            if(ex.getMessage().equals("Not connected to server!")) return;
            ex.printStackTrace();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VelocityServer that = (VelocityServer) o;
        return server.equals(that.server);
    }

    @Override
    public int hashCode() {
        return Objects.hash(server);
    }

    @Override
    public String toString() {
        return "VelocityServer{" +
                "server=" + server +
                '}';
    }
}
