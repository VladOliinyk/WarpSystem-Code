package de.codingair.warpsystem.velocity.base.listeners;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import de.codingair.codingapi.tools.Callback;
import de.codingair.warpsystem.base.transfer.packets.bungee.PrepareLoginMessagePacket;
import de.codingair.warpsystem.base.transfer.packets.bungee.SendUUIDPacket;
import de.codingair.warpsystem.base.transfer.packets.general.BooleanPacket;
import de.codingair.warpsystem.base.transfer.packets.general.IntegerPacket;
import de.codingair.warpsystem.base.transfer.packets.general.PrepareCoordinationTeleportPacket;
import de.codingair.warpsystem.base.transfer.packets.general.StringPacket;
import de.codingair.warpsystem.base.transfer.packets.spigot.*;
import de.codingair.warpsystem.base.transfer.packets.spigot.utils.ServerPing;
import de.codingair.warpsystem.base.transfer.packets.utils.PacketType;
import de.codingair.warpsystem.velocity.api.Players;
import de.codingair.warpsystem.velocity.base.WarpSystem;
import de.codingair.warpsystem.base.transfer.packets.utils.Packet;
import de.codingair.warpsystem.base.transfer.utils.PacketListener;
import de.codingair.warpsystem.velocity.base.managers.ServerManager;
import net.kyori.text.TextComponent;
import net.kyori.text.format.TextColor;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class MainListener extends PacketListener {

    @Subscribe
    public void onConnect(ServerConnectedEvent e) {
        if(e.getServer().getPlayersConnected().size() == 0) {
            //Update it
            WarpSystem.proxy().getScheduler().buildTask(WarpSystem.getInstance(), () -> WarpSystem.getInstance().getServerManager().sendInitialPacket(e.getServer())).delay(50, TimeUnit.MILLISECONDS).schedule();
        }
    }

    @Override
    public void onReceive(Packet packet, String extra) {
        RegisteredServer server = WarpSystem.proxy().getServer(extra).get();

        switch(PacketType.getByObject(packet)) {
            case RequestInitialPacket: {
                WarpSystem.getInstance().getServerManager().sendInitialPacket(server);
                break;
            }

            case RequestUUIDPacket: {
                RequestUUIDPacket p = (RequestUUIDPacket) packet;
                Optional<Player> pp = WarpSystem.proxy().getPlayer(p.getName());

                SendUUIDPacket answer = pp.map(player -> new SendUUIDPacket(player.getUniqueId())).orElseGet(() -> new SendUUIDPacket(null));
                p.applyAsAnswer(answer);

                WarpSystem.getInstance().getDataHandler().send(answer, server);
                break;
            }

            case MessagePacket: {
                MessagePacket p = (MessagePacket) packet;
                Optional<Player> player = WarpSystem.proxy().getPlayer(p.getPlayer());

                if(player.isPresent()) {
                    TextComponent tc = TextComponent.of(p.getMessage());
                    tc.color(TextColor.GRAY);
                    player.get().sendMessage(tc);
                }
                break;
            }

            case RequestServerStatusPacket: {
                RequestServerStatusPacket p = (RequestServerStatusPacket) packet;
                BooleanPacket answer = new BooleanPacket();
                p.applyAsAnswer(answer);

                Optional<RegisteredServer> info = WarpSystem.proxy().getServer(p.getServer());

                if(!info.isPresent()) {
                    answer.setValue(false);
                    WarpSystem.getInstance().getDataHandler().send(answer, server);
                } else {
                    RegisteredServer ping = info.get();
                    ping.ping().whenComplete((serverPing, throwable) -> {
                        WarpSystem.getInstance().getServerManager().setStatus(ping, throwable == null);
                        answer.setValue(throwable == null);
                        WarpSystem.getInstance().getDataHandler().send(answer, server);
                    });
                }
                break;
            }

            case PrepareServerSwitchPacket: {
                PrepareServerSwitchPacket p = (PrepareServerSwitchPacket) packet;
                IntegerPacket answer = new IntegerPacket();
                p.applyAsAnswer(answer);

                Optional<Player> pp = WarpSystem.proxy().getPlayer(p.getPlayer());
                Optional<RegisteredServer> info = WarpSystem.proxy().getServer(p.getServer());

                if(!pp.isPresent() || !info.isPresent()) {
                    answer.setValue(1);
                    WarpSystem.getInstance().getDataHandler().send(answer, server);
                } else {
                    Player player = pp.get();
                    RegisteredServer target = info.get();
                    Optional<ServerConnection> current = player.getCurrentServer();

                    if(current.isPresent() && current.get().getServer().equals(target)) {
                        answer.setValue(2);
                        WarpSystem.getInstance().getDataHandler().send(answer, server);
                        return;
                    }

                    if(WarpSystem.getInstance().getServerManager().isOnline(target)) {
                        ServerPing ping = WarpSystem.getInstance().getServerManager().getLastPing(target);

                        if(ping == null) {
                            answer.setValue(4);
                            WarpSystem.getInstance().getDataHandler().send(answer, server);
                        } else {
                            if(p.isIgnoreLimit() || target.getPlayersConnected().size() < ping.getMaxPlayers()) {
                                answer.setValue(0);
                                WarpSystem.getInstance().getDataHandler().send(answer, server);
                                ServerManager.sendPlayerTo(target, player, new Callback<RegisteredServer>() {
                                    @Override
                                    public void accept(RegisteredServer object) {
                                        WarpSystem.getInstance().getDataHandler().send(new PrepareLoginMessagePacket(player.getUsername(), p.getMessage()), target);
                                    }
                                });
                            } else {
                                answer.setValue(5);
                                WarpSystem.getInstance().getDataHandler().send(answer, server);
                            }
                        }
                    } else {
                        answer.setValue(3);
                        WarpSystem.getInstance().getDataHandler().send(answer, server);
                    }
                }
                break;
            }

            case PrepareCoordinationTeleportPacket: {
                PrepareCoordinationTeleportPacket p = (PrepareCoordinationTeleportPacket) packet;
                IntegerPacket answer = new IntegerPacket();
                p.applyAsAnswer(answer);

                Optional<Player> pp = WarpSystem.proxy().getPlayer(p.getPlayer());
                Optional<RegisteredServer> info = WarpSystem.proxy().getServer(p.getServer());

                if(pp.isPresent() && info.isPresent() && WarpSystem.getInstance().getServerManager().isOnline(info.get())) {
                    Player player = pp.get();
                    RegisteredServer target = info.get();

                    if(target.getPlayersConnected().isEmpty()) {
                        //switch and teleport

                        ServerManager.sendPlayerTo(target, player, new Callback<RegisteredServer>() {
                            @Override
                            public void accept(RegisteredServer target) {
                                if(target == null) {
                                    answer.setValue(1);
                                } else {
                                    answer.setValue(0);
                                    PrepareCoordinationTeleportPacket finalCall = p.clone(null);
                                    finalCall.setServer(null);
                                    WarpSystem.getInstance().getDataHandler().send(finalCall, target);
                                }

                                WarpSystem.getInstance().getDataHandler().send(answer, server);
                            }
                        });
                    } else {
                        ServerPing ping = WarpSystem.getInstance().getServerManager().getLastPing(target);

                        if(ping == null) {
                            answer.setValue(4);
                            WarpSystem.getInstance().getDataHandler().send(answer, server);
                        } else {
                            if(p.isIgnoreLimit() || target.getPlayersConnected().size() < ping.getMaxPlayers()) {
                                //prepare and switch
                                PrepareCoordinationTeleportPacket finalCall = p.clone(new Callback<Integer>() {
                                    @Override
                                    public void accept(Integer object) {
                                        answer.setValue(object);
                                        WarpSystem.getInstance().getDataHandler().send(answer, server);
                                    }
                                });
                                finalCall.setServer(null);
                                WarpSystem.getInstance().getDataHandler().send(finalCall, target);
                                player.createConnectionRequest(target).connect();
                            } else {
                                answer.setValue(3);
                                WarpSystem.getInstance().getDataHandler().send(answer, server);
                            }
                        }
                    }
                } else {
                    answer.setValue(1);
                    WarpSystem.getInstance().getDataHandler().send(answer, server);
                }
                break;
            }

            case RequestFullNamePacket: {
                RequestFullNamePacket p = (RequestFullNamePacket) packet;

                StringPacket answer = new StringPacket();
                p.applyAsAnswer(answer);

                if(p.getName() == null) {
                    answer.setValue(null);
                    WarpSystem.getInstance().getDataHandler().send(answer, server);
                    return;
                }

                Player pp = Players.getPlayer(p.getName());

                answer.setValue(pp == null ? null : pp.getUsername());
                WarpSystem.getInstance().getDataHandler().send(answer, server);
            }
        }
    }

    @Override
    public boolean onSend(Packet packet) {
        return false;
    }
}
