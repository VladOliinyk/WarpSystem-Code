package de.codingair.warpsystem.spigot.api.bungee;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class HoverEventBuilder {
    public static HoverEvent build(HoverEvent.Action action, String text) {
        try {
            return new HoverEvent(action, new net.md_5.bungee.api.chat.hover.content.Text(text));
        } catch(Throwable t) {
            return new HoverEvent(action, new BaseComponent[]{new TextComponent(text)});
        }
    }
}
