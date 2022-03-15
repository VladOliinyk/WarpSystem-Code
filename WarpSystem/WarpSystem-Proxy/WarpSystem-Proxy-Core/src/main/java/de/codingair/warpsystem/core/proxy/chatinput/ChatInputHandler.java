package de.codingair.warpsystem.core.proxy.chatinput;

import de.codingair.warpsystem.core.proxy.Core;
import de.codingair.warpsystem.core.transfer.packets.spigot.ChatInputGUITogglePacket;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public abstract class ChatInputHandler {
    protected final List<String> using = new ArrayList<>();
    protected final HashMap<String, String> cache = new HashMap<>();

    public ChatInputHandler() {
        Core.getPlugin().dataHandler().registerHandler(ChatInputGUITogglePacket.class, (packet, proxy, connection, direction) -> {
            if (packet.isUsing()) {
                if (!using.contains(packet.getName())) using.add(packet.getName());
            } else using.remove(packet.getName());
        });
    }

    protected void remove(String name) {
        this.using.remove(name);
        this.cache.remove(name);
    }
}
