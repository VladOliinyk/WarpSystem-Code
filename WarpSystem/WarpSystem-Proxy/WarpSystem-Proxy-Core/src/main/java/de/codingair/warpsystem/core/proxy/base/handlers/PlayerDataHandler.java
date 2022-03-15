package de.codingair.warpsystem.core.proxy.base.handlers;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Iterables;
import de.codingair.packetmanagement.utils.Direction;
import de.codingair.warpsystem.core.proxy.Core;
import de.codingair.warpsystem.core.proxy.utils.Player;
import de.codingair.warpsystem.core.proxy.utils.Server;
import de.codingair.warpsystem.core.transfer.packets.general.UpdatePlayerDataPacket;
import de.codingair.warpsystem.core.transfer.packets.proxy.PlayerJoinPacket;
import de.codingair.warpsystem.core.transfer.packets.proxy.PlayerQuitPacket;
import de.codingair.warpsystem.core.transfer.packets.proxy.ProvidePlayerDataPacket;
import de.codingair.warpsystem.core.transfer.utils.PlayerData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class PlayerDataHandler {
    private final Cache<String, Server<?>> lastSwitch = CacheBuilder.newBuilder().expireAfterAccess(10, TimeUnit.MINUTES).build();
    private final Cache<String, String> abbreviations = CacheBuilder.newBuilder().expireAfterAccess(1, TimeUnit.MINUTES).build();
    private final ConcurrentHashMap<String, PlayerData> cached = new ConcurrentHashMap<>();

    protected PlayerDataHandler() {
        Core.getPlugin().dataHandler().registerHandler(UpdatePlayerDataPacket.class, (packet, proxy, connection, direction) -> {
            PlayerData cached = this.cached.get(packet.getName().toLowerCase());
            if (cached == null) return;

            packet.update(cached);
        });
    }

    @Nullable
    public PlayerData getCache(String name) {
        if (name == null) return null;

        PlayerData data = getCacheExact(name);
        if (data != null) return data;

        String lowerName = name.toLowerCase(Locale.ENGLISH);
        String abbreviation = abbreviations.getIfPresent(lowerName);
        if (abbreviation != null) return getCacheExact(abbreviation);

        int delta = 2147483647;
        for (String player : cached.keySet()) {
            if (player.startsWith(lowerName)) {
                int curDelta = Math.abs(player.length() - lowerName.length());
                if (curDelta < delta) {
                    abbreviation = player;
                    delta = curDelta;
                }

                if (curDelta == 0) break;
            }
        }

        if (abbreviation != null) abbreviations.put(lowerName, abbreviation);
        return getCacheExact(abbreviation);
    }

    public PlayerData getCacheExact(String name) {
        if (name == null) return null;
        return cached.get(name.toLowerCase());
    }

    protected void onServerProvideOptions(Server<?> s) {
        buildPlayerDataPackets(p -> Core.getPlugin().dataHandler().send(p, s, Direction.DOWN));
    }

    protected void connectPlayer(Player player, Server<?> server) {
        String name = player.getName().toLowerCase();
        
        Server<?> lastSwitch = this.lastSwitch.getIfPresent(name);
        this.lastSwitch.invalidate(name);
        String lastServer = lastSwitch == null ? null : lastSwitch.getName();

        if (this.cached.putIfAbsent(name, new PlayerData(player.getName(), player.getUniqueId(), server.getName(), lastServer, true)) == null) {
            PlayerJoinPacket packet = new PlayerJoinPacket(player.getName(), server.getName(), player.getUniqueId());

            ServerHandler.wait(server, packet);
            Core.getServerManager().getOnlineServer(server).forEach(s -> Core.getPlugin().dataHandler().send(packet, s, Direction.DOWN));
            Core.getPlugin().dataHandler().send(new PlayerJoinPacket(player.getName(), server.getName(), player.getUniqueId()), null, Direction.UP);
        }
    }

    protected void disconnectPlayer(Player player) {
        if (this.cached.remove(player.getName().toLowerCase()) != null) {
            Core.getServerManager().getOnlineServer().filter(s -> s.getOnlineCount() > 0).forEach(s -> Core.getPlugin().dataHandler().send(new PlayerQuitPacket(player.getName()), s, Direction.DOWN));
            Core.getPlugin().dataHandler().send(new PlayerQuitPacket(player.getName()), null, Direction.UP);
        }
    }

    protected void onSwitch(@NotNull Player player, @NotNull Server<?> from, @NotNull Server<?> to) {
        String name = player.getName().toLowerCase();
        PlayerData cached = this.cached.get(name);
        if (cached == null) return;

        cached.setFirstServer(false);

        lastSwitch.put(name, from);

        UpdatePlayerDataPacket packet = new UpdatePlayerDataPacket(player.getName());

        if (cached.isVanished()) {
            cached.setVanished(false);
            packet.setVanished(false);
        }

        if (!cached.getServer().equals(to.getName())) {
            cached.setServer(to.getName());
            cached.setOldServer(from.getName());
            packet.setServer(to.getName(), from.getName());
        }

        ServerHandler.wait(to, packet);
        Core.getServerManager().getOnlineServer(to).forEach(s -> Core.getPlugin().dataHandler().send(packet, s, Direction.DOWN));
        Core.getPlugin().dataHandler().send(packet, null, Direction.UP);
    }

    public void buildPlayerDataPackets(Consumer<ProvidePlayerDataPacket> consumer) {
        for (Collection<PlayerData> names : Iterables.partition(cached.values(), 64)) {
            consumer.accept(new ProvidePlayerDataPacket(names));
        }
    }

    public void onUpdate(UpdatePlayerDataPacket packet, Server<?> info) {
        PlayerData data = this.cached.get(packet.getName().toLowerCase());
        if (data == null) return;

        if (!packet.update(data)) return;
        Core.getServerManager().getOnlineServer(info).forEach(s -> Core.getPlugin().dataHandler().send(packet, s, Direction.DOWN));
        Core.getPlugin().dataHandler().send(packet, null, Direction.UP);
    }

    //redis
    public void onUpdate(UpdatePlayerDataPacket packet) {
        PlayerData data = this.cached.get(packet.getName().toLowerCase());
        if (data == null) return;

        if (!packet.update(data)) return;
        Core.getServerManager().getOnlineServer().forEach(s -> Core.getPlugin().dataHandler().send(packet, s, Direction.DOWN));
    }

    //redis
    public void apply(@NotNull ProvidePlayerDataPacket packet) {
        packet.getData().forEach(entry -> cached.put(entry.getName().toLowerCase(), entry));

        Core.getServerManager().getOnlineServer().forEach(s -> Core.getPlugin().dataHandler().send(packet, s, Direction.DOWN));
    }

    //redis
    public void connectPlayer(PlayerJoinPacket packet) {
        this.cached.putIfAbsent(packet.getPlayer().toLowerCase(), new PlayerData(packet.getPlayer(), packet.getId(), packet.getServer(), true));
        Core.getServerManager().getOnlineServer().forEach(s -> Core.getPlugin().dataHandler().send(packet, s, Direction.DOWN));
    }

    //redis
    public void disconnectPlayer(PlayerQuitPacket packet) {
        this.cached.remove(packet.getPlayer().toLowerCase());
        Core.getServerManager().getOnlineServer().forEach(s -> Core.getPlugin().dataHandler().send(packet, s, Direction.DOWN));
    }
}
