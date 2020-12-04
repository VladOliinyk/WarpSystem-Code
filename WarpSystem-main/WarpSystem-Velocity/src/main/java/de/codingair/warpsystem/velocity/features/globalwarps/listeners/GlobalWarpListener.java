package de.codingair.warpsystem.velocity.features.globalwarps.listeners;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import de.codingair.codingapi.tools.Callback;
import de.codingair.warpsystem.base.transfer.packets.general.BooleanPacket;
import de.codingair.warpsystem.base.transfer.packets.general.IntegerPacket;
import de.codingair.warpsystem.base.transfer.packets.general.PrepareCoordinationTeleportPacket;
import de.codingair.warpsystem.base.transfer.packets.spigot.DeleteGlobalWarpPacket;
import de.codingair.warpsystem.base.transfer.packets.spigot.GlobalWarpTeleportPacket;
import de.codingair.warpsystem.base.transfer.packets.spigot.PublishGlobalWarpPacket;
import de.codingair.warpsystem.base.transfer.packets.spigot.utils.ServerPing;
import de.codingair.warpsystem.base.transfer.packets.utils.Packet;
import de.codingair.warpsystem.base.transfer.packets.utils.PacketType;
import de.codingair.warpsystem.base.transfer.serializeable.SGlobalWarp;
import de.codingair.warpsystem.base.transfer.utils.PacketListener;
import de.codingair.warpsystem.velocity.base.WarpSystem;
import de.codingair.warpsystem.velocity.base.events.ServerInitializeEvent;
import de.codingair.warpsystem.velocity.base.managers.ServerManager;
import de.codingair.warpsystem.velocity.features.FeatureType;
import de.codingair.warpsystem.velocity.features.globalwarps.managers.GlobalWarpManager;

import java.util.Optional;

public class GlobalWarpListener extends PacketListener {

    @Subscribe
    public void onConnect(ServerInitializeEvent e) {
        GlobalWarpManager manager = WarpSystem.getInstance().getDataManager().getManager(FeatureType.GLOBAL_WARPS);
        manager.synchronize(e.getServer());
    }

    @Override
    public void onReceive(Packet packet, String extra) {
        GlobalWarpManager manager = WarpSystem.getInstance().getDataManager().getManager(FeatureType.GLOBAL_WARPS);
        Optional<RegisteredServer> server = WarpSystem.proxy().getServer(extra);
        if(!server.isPresent()) return;

        RegisteredServer sender = server.get();

        switch(PacketType.getByObject(packet)) {
            case PublishGlobalWarpPacket:
                ((PublishGlobalWarpPacket) packet).warp.setServer(extra);

                BooleanPacket answerBooleanPacket = new BooleanPacket();
                boolean overwrite = ((PublishGlobalWarpPacket) packet).isOverwrite();

                if(overwrite) {
                    SGlobalWarp warp = manager.get(((PublishGlobalWarpPacket) packet).warp.getName());
                    if(warp != null) {
                        //Changed
                        answerBooleanPacket.setValue(true);
                        warp.setLoc(((PublishGlobalWarpPacket) packet).warp.getLoc());
                        warp.setServer(((PublishGlobalWarpPacket) packet).warp.getServer());
                        manager.synchronize(((PublishGlobalWarpPacket) packet).warp);
                    } else {
                        //Name already exists
                        answerBooleanPacket.setValue(false);
                    }
                } else {
                    if(manager.add(((PublishGlobalWarpPacket) packet).warp)) {
                        //Added
                        answerBooleanPacket.setValue(true);
                        manager.synchronize(((PublishGlobalWarpPacket) packet).warp);
                    } else {
                        //Name already exists
                        answerBooleanPacket.setValue(false);
                    }
                }

                ((PublishGlobalWarpPacket) packet).applyAsAnswer(answerBooleanPacket);
                WarpSystem.getInstance().getDataHandler().send(answerBooleanPacket, sender);
                break;

            case DeleteGlobalWarpPacket:
                SGlobalWarp warp = manager.get(((DeleteGlobalWarpPacket) packet).getWarp());
                answerBooleanPacket = new BooleanPacket();
                ((DeleteGlobalWarpPacket) packet).applyAsAnswer(answerBooleanPacket);

                if(warp == null) {
                    answerBooleanPacket.setValue(false);
                    WarpSystem.getInstance().getDataHandler().send(answerBooleanPacket, sender);
                } else {
                    manager.remove(warp.getName());
                    answerBooleanPacket.setValue(true);
                    WarpSystem.getInstance().getDataHandler().send(answerBooleanPacket, sender);

                    manager.synchronize(warp);
                }
                break;

            case GlobalWarpTeleportPacket:
                GlobalWarpTeleportPacket teleportPacket = (GlobalWarpTeleportPacket) packet;
                String player = teleportPacket.getPlayer();
                String teleport = teleportPacket.getId();
                warp = manager.get(teleport);
                String teleportDisplayName = teleportPacket.getDisplayName();
                if(teleportDisplayName == null) teleportDisplayName = warp.getName();

                Optional<RegisteredServer> oS = WarpSystem.proxy().getServer(warp.getServer());
                Optional<Player> oP = WarpSystem.proxy().getPlayer(player);

                IntegerPacket answerIntegerPacket = new IntegerPacket();
                teleportPacket.applyAsAnswer(answerIntegerPacket);

                if(warp == null) {
                    //unknown warp
                    answerIntegerPacket.setValue(1);
                    WarpSystem.getInstance().getDataHandler().send(answerIntegerPacket, sender);
                } else if(!oS.isPresent() || !oP.isPresent()) {
                    //player is not online OR server ist not online
                    answerIntegerPacket.setValue(2);
                    WarpSystem.getInstance().getDataHandler().send(answerIntegerPacket, sender);
                } else {
                    answerIntegerPacket.setValue(0);

                    Player p = oP.get();
                    RegisteredServer otherServer = oS.get();
                    Optional<ServerConnection> currentServer = p.getCurrentServer();

                    PrepareCoordinationTeleportPacket out = new PrepareCoordinationTeleportPacket(p.getUsername(), null, warp.getLoc().getWorld(), teleportDisplayName, teleportPacket.getMessage() == null ? PrepareCoordinationTeleportPacket.NO_MESSAGE : teleportPacket.getMessage(), warp.getLoc().getX(), warp.getLoc().getY(), warp.getLoc().getZ(),
                            teleportPacket.isKeepRotation() ? -420 : warp.getLoc().getYaw(), teleportPacket.isKeepRotation() ? -420 : warp.getLoc().getPitch(), teleportPacket.getCosts(), teleportPacket.isIgnoreLimit(), null);

                    if(currentServer.isPresent() && currentServer.get().getServer().equals(otherServer)) {
                        WarpSystem.getInstance().getDataHandler().send(answerIntegerPacket, sender);
                        WarpSystem.getInstance().getDataHandler().send(out, otherServer);
                    } else {
                        if(WarpSystem.getInstance().getServerManager().isOnline(otherServer)) {
                            ServerPing ping = WarpSystem.getInstance().getServerManager().getLastPing(otherServer);

                            if(ping == null) {
                                answerIntegerPacket.setValue(GlobalWarpTeleportPacket.Result.ERROR.getId());
                                WarpSystem.getInstance().getDataHandler().send(answerIntegerPacket, sender);
                            } else {
                                if(teleportPacket.isIgnoreLimit() || otherServer.getPlayersConnected().size() < ping.getMaxPlayers()) {
                                    WarpSystem.getInstance().getDataHandler().send(answerIntegerPacket, sender);
                                    ServerManager.sendPlayerTo(otherServer, p, new Callback<RegisteredServer>() {
                                        @Override
                                        public void accept(RegisteredServer object) {
                                            WarpSystem.getInstance().getDataHandler().send(out, otherServer);
                                        }
                                    });
                                } else {
                                    answerIntegerPacket.setValue(GlobalWarpTeleportPacket.Result.SERVER_IS_FULL.getId());
                                    WarpSystem.getInstance().getDataHandler().send(answerIntegerPacket, sender);
                                }
                            }
                        } else {
                            answerIntegerPacket.setValue(2);
                            WarpSystem.getInstance().getDataHandler().send(answerIntegerPacket, sender);
                        }
                    }
                }
                break;

            case RequestGlobalWarpNamesPacket:
                manager.synchronize(sender);
                break;
        }
    }

    @Override
    public boolean onSend(Packet packet) {
        return false;
    }
}
