package de.codingair.warpsystem.core.proxy.redis;

import de.codingair.packetmanagement.utils.Direction;
import de.codingair.warpsystem.core.proxy.Core;
import de.codingair.warpsystem.core.transfer.packets.proxy.ProxyAwarenessPacket;

import java.util.HashSet;
import java.util.Set;

public class RedisCore {
    public static final long TIME_OUT = 200;
    private static final RedisCore INSTANCE = new RedisCore();
    private final Set<String> proxies = new HashSet<>();
    private RedisHandler handler;

    private RedisCore() {
    }

    public RedisHandler getHandler() {
        return handler;
    }

    public static boolean ready() {
        return core().getHandler() != null;
    }

    public void setHandler(RedisHandler handler) {
        this.handler = handler;
        this.handler.setSink((data, source) -> Core.getPlugin().dataHandler().receive(data, null, Direction.UP));
        createAwareness();
    }

    private static void createAwareness() {
        Core.getPlugin().dataHandler().send(new ProxyAwarenessPacket(RedisCore.core().handler.source), null, Direction.UP);
    }

    public static void recognize(String proxy) {
        if (!core().proxies.add(proxy)) createAwareness();
    }

    public Set<String> getProxies() {
        return proxies;
    }

    public static RedisCore core() {
        return INSTANCE;
    }
}
