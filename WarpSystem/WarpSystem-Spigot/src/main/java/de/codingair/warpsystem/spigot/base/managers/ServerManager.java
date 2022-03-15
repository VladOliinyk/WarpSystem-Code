package de.codingair.warpsystem.spigot.base.managers;

import de.codingair.warpsystem.core.transfer.packets.proxy.SendServerPropertiesPacket;
import de.codingair.warpsystem.core.transfer.packets.proxy.SendServerWorldNamesPacket;
import de.codingair.warpsystem.core.transfer.packets.spigot.SendWorldNamesPacket;
import de.codingair.warpsystem.core.transfer.packets.spigot.utils.ServerPing;
import de.codingair.warpsystem.spigot.base.WarpSystem;
import de.codingair.warpsystem.spigot.base.utils.ProxyFeature;
import de.codingair.warpsystem.spigot.features.FeatureType;
import de.codingair.warpsystem.spigot.features.signs.managers.SignManager;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class ServerManager implements ProxyFeature {
    private final HashMap<String, ServerPing> properties = new HashMap<>();
    private final HashMap<String, Set<String>> worlds = new HashMap<>();

    public ServerManager() {
        WarpSystem.getDataHandler().registerHandler(SendServerPropertiesPacket.class, (packet, proxy, connection, direction) -> {
            properties.putAll(packet.getProperties());
            onUpdate();
        });

        WarpSystem.getDataHandler().registerHandler(SendServerWorldNamesPacket.class, (packet, proxy, connection, direction) -> {
            worlds.putAll(packet.getWorlds());
        });

        WarpSystem.getInstance().getProxyFeatureList().add(this);
    }

    public @Nullable ServerPing getProperties(String server) {
        return properties.get(server.toLowerCase());
    }

    public @NotNull Set<String> getWorlds(String server) {
        return worlds.getOrDefault(server.toLowerCase(), new HashSet<>());
    }

    public HashMap<String, Set<String>> getWorlds() {
        return worlds;
    }

    public void onUpdate() {
        Bukkit.getScheduler().runTask(WarpSystem.getInstance(), () -> {
            if (FeatureType.SIGNS.isActive()) SignManager.getInstance().updateAll();
        });
    }

    public void sendWorlds(Player connection) {
        Set<String> worlds = new HashSet<>(Bukkit.getWorlds().size(), 1F);
        for (World w : Bukkit.getWorlds()) {
            worlds.add(w.getName());
        }

        WarpSystem.getDataHandler().send(new SendWorldNamesPacket(worlds), connection);
    }

    @Override
    public void onConnect(Player connection) {
    }

    @Override
    public void onDisconnect() {
    }

    @Override
    public void onInitiate(Player connection) {
        sendWorlds(connection);
    }
}
