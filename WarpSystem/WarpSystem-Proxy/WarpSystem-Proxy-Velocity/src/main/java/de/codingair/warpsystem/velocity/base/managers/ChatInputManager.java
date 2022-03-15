package de.codingair.warpsystem.velocity.base.managers;

import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.proxy.Player;
import de.codingair.warpsystem.core.proxy.Core;
import de.codingair.warpsystem.core.proxy.chatinput.ChatInputHandler;
import de.codingair.warpsystem.core.transfer.packets.spigot.ChatInputGUITogglePacket;
import de.codingair.warpsystem.velocity.base.WarpSystem;

public class ChatInputManager extends ChatInputHandler {
    public ChatInputManager() {
        Core.getPlugin().dataHandler().registerHandler(ChatInputGUITogglePacket.class, (packet, proxy, connection, direction) -> {
            if (packet.isUsing()) {
                if (!using.contains(packet.getName())) using.add(packet.getName());
            } else using.remove(packet.getName());
        });
        WarpSystem.proxy().getEventManager().register(WarpSystem.getInstance(), this);
    }

    @Subscribe (order = PostOrder.FIRST)
    public void onChatSave(PlayerChatEvent e) {
        Player player = e.getPlayer();
        String name = player.getUsername();
        if (this.using.contains(name)) {
            String msg = e.getMessage();
            if (msg.startsWith("/")) msg = "$c." + msg;

            cache.put(name, e.getMessage().substring(0, Math.min(msg.length(), 256)));
            e.setResult(PlayerChatEvent.ChatResult.message(""));
        }
    }

    @Subscribe (order = PostOrder.LAST)
    public void onChatCache(PlayerChatEvent e) {
        String name = e.getPlayer().getUsername();

        if (this.using.remove(name)) {
            String message = cache.remove(name);
            e.setResult(PlayerChatEvent.ChatResult.message(message));
        }
    }

    @Subscribe
    public void onQuit(DisconnectEvent e) {
        remove(e.getPlayer().getUsername());
    }

    @Subscribe
    public void onChange(ServerConnectedEvent e) {
        remove(e.getPlayer().getUsername());
    }
}