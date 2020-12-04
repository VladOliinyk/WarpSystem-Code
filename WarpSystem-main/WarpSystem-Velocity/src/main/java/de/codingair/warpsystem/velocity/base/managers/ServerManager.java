package de.codingair.warpsystem.velocity.base.managers;

import com.google.common.base.Preconditions;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import de.codingair.codingapi.tools.Callback;
import de.codingair.warpsystem.velocity.base.WarpSystem;
import de.codingair.warpsystem.velocity.base.events.ServerInitializeEvent;
import de.codingair.warpsystem.velocity.base.events.ServerProvideOptionsEvent;
import de.codingair.warpsystem.base.transfer.packets.bungee.InitialPacket;
import de.codingair.warpsystem.base.transfer.packets.bungee.SendServerPropertiesPacket;
import de.codingair.warpsystem.base.transfer.packets.spigot.SendOptionsPacket;
import de.codingair.warpsystem.base.transfer.packets.spigot.utils.ServerPing;
import de.codingair.warpsystem.base.transfer.packets.utils.Packet;
import de.codingair.warpsystem.base.transfer.packets.utils.PacketType;
import de.codingair.warpsystem.base.transfer.serializeable.ServerOptions;
import de.codingair.warpsystem.base.transfer.utils.PacketListener;
import de.codingair.warpsystem.velocity.features.teleport.managers.TeleportManager;
import net.kyori.text.TextComponent;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class ServerManager extends PacketListener {
    private final HashMap<RegisteredServer, ServerOptions> options = new HashMap<>();
    private final ConcurrentHashMap<String, ServerPing> cachedPing = new ConcurrentHashMap<>();
    private final Set<RegisteredServer> onlineServer = new HashSet<>();
    private final HashMap<RegisteredServer, List<Callback<RegisteredServer>>> waiting = new HashMap<>();

    public static void sendPlayerTo(RegisteredServer server, Player player, Callback<RegisteredServer> c) {
        Preconditions.checkNotNull(server);
        Preconditions.checkNotNull(player);

        Optional<ServerConnection> opt = player.getCurrentServer();
        if(opt.isPresent() && opt.get().getServer().equals(server)) {
            c.accept(server);
        } else {
            if(server.getPlayersConnected().isEmpty()) addCallbackTo(server, c);
            else c.accept(server);
            player.createConnectionRequest(server).connect();
        }
    }

    private static void addCallbackTo(RegisteredServer info, Callback<RegisteredServer> c) {
        List<Callback<RegisteredServer>> l = WarpSystem.getInstance().getServerManager().waiting.computeIfAbsent(info, k -> new ArrayList<>());
        l.add(c);
    }

    public Set<RegisteredServer> getOnlineServer() {
        return onlineServer;
    }

    public boolean isOnline(RegisteredServer info) {
        return onlineServer.contains(info);
    }

    public void run() {
        for(RegisteredServer info : WarpSystem.proxy().getAllServers()) {
            cachedPing.put(info.getServerInfo().getName().toLowerCase(), new ServerPing(false, 0, 0, null));
        }

        WarpSystem.proxy().getScheduler().buildTask(WarpSystem.getInstance(), () -> {
            for(RegisteredServer info : WarpSystem.proxy().getAllServers()) {
                info.ping().whenComplete((serverPing, error) -> {
                    setStatus(info, error == null);

                    cachedPing.compute(info.getServerInfo().getName().toLowerCase(), (name, ping) -> {
                        if(error != null) {
                            ping.setStatus(false);
                            ping.setPlayers(0);
                            ping.setMaxPlayers(0);
                            ping.setMotd(null);
                        } else {
                            Optional<com.velocitypowered.api.proxy.server.ServerPing.Players> opt = serverPing.getPlayers();

                            if(opt.isPresent()) {
                                com.velocitypowered.api.proxy.server.ServerPing.Players players = opt.get();

                                ping.setStatus(true);
                                ping.setPlayers(players.getOnline());
                                ping.setMaxPlayers(players.getMax());
                                ping.setMotd(((TextComponent) serverPing.getDescription()).content());
                            }
                        }

                        return ping;
                    });
                });
            }
        }).repeat(5, TimeUnit.SECONDS).schedule();

        WarpSystem.proxy().getScheduler().buildTask(WarpSystem.getInstance(), () -> {
            HashMap<String, ServerPing> copy = new HashMap<>();
            for(Map.Entry<String, ServerPing> e : cachedPing.entrySet()) {
                copy.put(e.getKey(), new ServerPing(e.getValue()));
            }

            SendServerPropertiesPacket p = new SendServerPropertiesPacket(copy);
            for(RegisteredServer target : WarpSystem.proxy().getAllServers()) {
                if(!target.getPlayersConnected().isEmpty()) {
                    WarpSystem.getInstance().getDataHandler().send(p, target);
                }
            }
        }).delay(3, TimeUnit.SECONDS).repeat(5, TimeUnit.SECONDS).schedule();
    }

    public void sendInitialPacket(RegisteredServer server) {
        WarpSystem.getInstance().getDataHandler().send(new InitialPacket(WarpSystem.getInstance().getVersion(), server.getServerInfo().getName()), server);
        WarpSystem.proxy().getEventManager().fire(new ServerInitializeEvent(server));

        List<Callback<RegisteredServer>> l = WarpSystem.getInstance().getServerManager().waiting.remove(server);
        if(l != null) {
            l.forEach(c -> c.accept(server));
            l.clear();
        }
    }

    public synchronized void setStatus(RegisteredServer info, boolean online) {
        if(!online) {
            this.onlineServer.remove(info);
            TeleportManager.getInstance().removeOptions(info);
            this.options.remove(info);
        } else this.onlineServer.add(info);
    }

    public ServerOptions getOptions(ServerInfo info) {
        if(info == null) return null;
        return options.get(info);
    }

    public ServerPing getLastPing(RegisteredServer info) {
        if(info == null) return null;
        return cachedPing.get(info.getServerInfo().getName().toLowerCase());
    }

    @Override
    public void onReceive(Packet packet, String extra) {
        if(packet.getType() == PacketType.SendOptionsPacket) {
            RegisteredServer info = WarpSystem.proxy().getServer(extra).get();

            SendOptionsPacket p = (SendOptionsPacket) packet;
            options.put(info, p.getOptions());
            p.getOptions().setSameVersion(WarpSystem.getInstance().getVersion().equals(p.getOptions().getVersion()));
            WarpSystem.proxy().getEventManager().fire(new ServerProvideOptionsEvent(info, p.getOptions()));
        }
    }

    @Override
    public boolean onSend(Packet packet) {
        return false;
    }
}
