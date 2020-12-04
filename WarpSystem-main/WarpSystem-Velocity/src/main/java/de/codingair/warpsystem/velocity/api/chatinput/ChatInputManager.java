package de.codingair.warpsystem.velocity.api.chatinput;

import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.proxy.Player;
import de.codingair.warpsystem.base.transfer.packets.spigot.ChatInputGUITogglePacket;
import de.codingair.warpsystem.base.transfer.packets.utils.Packet;
import de.codingair.warpsystem.base.transfer.packets.utils.PacketType;
import de.codingair.warpsystem.base.transfer.utils.PacketListener;
import de.codingair.warpsystem.velocity.base.WarpSystem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ChatInputManager extends PacketListener {
    private final List<String> using = new ArrayList<>();
    private final HashMap<String, String> cache = new HashMap<>();

    public ChatInputManager() {
        WarpSystem.getInstance().getDataHandler().register(this);
        WarpSystem.proxy().getEventManager().register(WarpSystem.getInstance(), this);
    }

    @Subscribe(order = PostOrder.FIRST)
    public void onChatSave(PlayerChatEvent e) {
        Player player = e.getPlayer();
        String name = player.getUsername();
        if(this.using.contains(name)) {
            String msg = e.getMessage();
            if(msg.startsWith("/")) msg = "$c." + msg;

            cache.put(name, e.getMessage().substring(0, Math.min(msg.length(), 256)));
            e.setResult(PlayerChatEvent.ChatResult.denied());
            e.setResult(PlayerChatEvent.ChatResult.message(""));
        }
    }

    @Subscribe(order = PostOrder.LAST)
    public void onChatCache(PlayerChatEvent e) {
        String name = e.getPlayer().getUsername();

        if(this.using.remove(name)) {
            String message = cache.remove(name);
            e.setResult(PlayerChatEvent.ChatResult.allowed());
            e.setResult(PlayerChatEvent.ChatResult.message(message));
        }
    }

    @Subscribe
    public void onQuit(DisconnectEvent e) {
        this.using.remove(e.getPlayer().getUsername());
        this.cache.remove(e.getPlayer().getUsername());
    }

    @Subscribe
    public void onChange(ServerConnectedEvent e) {
        this.using.remove(e.getPlayer().getUsername());
        this.cache.remove(e.getPlayer().getUsername());
    }

    @Override
    public void onReceive(Packet packet, String extra) {
        if(packet.getType() == PacketType.ChatInputGUITogglePacket) {
            ChatInputGUITogglePacket p = (ChatInputGUITogglePacket) packet;

            if(p.isUsing()) {
                if(!this.using.contains(p.getName())) this.using.add(p.getName());
            } else this.using.remove(p.getName());
        }
    }

    @Override
    public boolean onSend(Packet packet) {
        return false;
    }
}
