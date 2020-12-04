package de.codingair.warpsystem.velocity.features.spawn.listeners;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import de.codingair.codingapi.tools.Callback;
import de.codingair.warpsystem.base.transfer.packets.general.SendGlobalSpawnOptionsPacket;
import de.codingair.warpsystem.base.transfer.packets.general.TeleportSpawnPacket;
import de.codingair.warpsystem.base.transfer.packets.utils.Packet;
import de.codingair.warpsystem.base.transfer.packets.utils.PacketType;
import de.codingair.warpsystem.base.transfer.utils.PacketListener;
import de.codingair.warpsystem.velocity.base.WarpSystem;
import de.codingair.warpsystem.velocity.base.events.ServerProvideOptionsEvent;
import de.codingair.warpsystem.velocity.base.language.Lang;
import de.codingair.warpsystem.velocity.base.managers.ServerManager;
import de.codingair.warpsystem.velocity.features.spawn.managers.SpawnManager;
import net.kyori.text.TextComponent;

import java.util.Optional;

public class ServerListener extends PacketListener {

    @Subscribe
    public void onInit(ServerProvideOptionsEvent e) {
        if(!e.getOptions().sameVersion()) return;
        WarpSystem.getInstance().getDataHandler().send(SpawnManager.getInstance().getInfoPacket(), e.getServer());
    }

    @Override
    public void onReceive(Packet packet, String extra) {
        Optional<RegisteredServer> opt = WarpSystem.proxy().getServer(extra);
        if(!opt.isPresent()) return;
        RegisteredServer origin = opt.get();

        if(packet.getType() == PacketType.SendGlobalSpawnOptionsPacket) {
            SendGlobalSpawnOptionsPacket p = (SendGlobalSpawnOptionsPacket) packet;
            SpawnManager.getInstance().update(origin, p.getSpawn(), p.getRespawn());
        } else if(packet.getType() == PacketType.TeleportSpawnPacket) {
            TeleportSpawnPacket p = (TeleportSpawnPacket) packet;

            Optional<Player> player = WarpSystem.proxy().getPlayer(p.getPlayer());
            Optional<RegisteredServer> server = WarpSystem.proxy().getServer(p.isRespawn() ? SpawnManager.getInstance().getRespawn() : SpawnManager.getInstance().getSpawn());

            if(player.isPresent() && server.isPresent()) {
                if(WarpSystem.getInstance().getServerManager().isOnline(server.get())) {
                    ServerManager.sendPlayerTo(server.get(), player.get(), new Callback<RegisteredServer>() {
                        @Override
                        public void accept(RegisteredServer server) {
                            WarpSystem.getInstance().getDataHandler().send(p, server);
                        }
                    });
                } else player.get().sendMessage(TextComponent.of(Lang.getPrefix() + Lang.get("Server_Is_Not_Online")));
            }
        }
    }

    @Override
    public boolean onSend(Packet packet) {
        return false;
    }
}
