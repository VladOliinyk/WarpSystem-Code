package de.codingair.warpsystem.velocity.api;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.velocitypowered.api.proxy.Player;
import de.codingair.warpsystem.velocity.base.WarpSystem;

import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class Players {
    private static final Cache<String, Player> CACHE = CacheBuilder.newBuilder().expireAfterAccess(30, TimeUnit.SECONDS).build();

    public static Player getPlayer(String name) {
        Optional<Player> found = WarpSystem.proxy().getPlayer(name);
        if(found.isPresent()) return found.get();
        String lowerName = name.toLowerCase(Locale.ENGLISH);

        Player player = CACHE.getIfPresent(lowerName);
        if(player != null) return player;

        int delta = 2147483647;
        for(Player p : WarpSystem.proxy().getAllPlayers()) {
            if(p.getUsername().toLowerCase(Locale.ENGLISH).startsWith(lowerName)) {
                int curDelta = Math.abs(p.getUsername().length() - lowerName.length());
                if(curDelta < delta) {
                    player = p;
                    delta = curDelta;
                }

                if(curDelta == 0) {
                    break;
                }
            }
        }

        if(player != null) CACHE.put(lowerName, player);
        return player;
    }
}
