package de.codingair.warpsystem.bungee.base.managers;

import de.codingair.warpsystem.bungee.base.WarpSystem;
import de.codingair.warpsystem.core.proxy.Core;
import de.codingair.warpsystem.core.proxy.chatinput.ChatInputHandler;
import de.codingair.warpsystem.core.transfer.packets.spigot.ChatInputGUITogglePacket;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.event.ServerDisconnectEvent;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class ChatInputManager extends ChatInputHandler implements Listener {
    public ChatInputManager() {
        Core.getPlugin().dataHandler().registerHandler(ChatInputGUITogglePacket.class, (packet, proxy, connection, direction) -> {
            if (packet.isUsing()) {
                if (!using.contains(packet.getName())) using.add(packet.getName());
            } else using.remove(packet.getName());
        });
        WarpSystem.proxy().getPluginManager().registerListener(WarpSystem.getInstance(), this);
    }

    @EventHandler (priority = -100)
    public void onChatSave(ChatEvent e) {
        if (!(e.getSender() instanceof ProxiedPlayer)) return;
        String name = ((ProxiedPlayer) e.getSender()).getName();
        if (this.using.contains(name)) {
            if (e.isCommand()) e.setMessage("$c." + e.getMessage());

            cache.put(name, e.getMessage().substring(0, Math.min(e.getMessage().length(), 256)));
            e.setCancelled(true);
            e.setMessage("");
        }
    }

    @EventHandler (priority = 100)
    public void onChatCache(ChatEvent e) {
        if (!(e.getSender() instanceof ProxiedPlayer)) return;
        String name = ((ProxiedPlayer) e.getSender()).getName();

        if (this.using.remove(name)) {
            String message = cache.remove(name);
            e.setCancelled(false);
            e.setMessage(message);
        }
    }

    @EventHandler
    public void onQuit(ServerDisconnectEvent e) {
        remove(e.getPlayer().getName());
    }

    @EventHandler
    public void onQuit(ServerSwitchEvent e) {
        remove(e.getPlayer().getName());
    }
}
