package de.codingair.warpsystem.spigot.base.managers;

import de.codingair.warpsystem.spigot.base.WarpSystem;
import de.codingair.warpsystem.spigot.base.utils.BungeeFeature;
import de.codingair.warpsystem.transfer.packets.bungee.PacketVanishInfo;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class VanishManager implements BungeeFeature {
    private BukkitRunnable runnable;
    private final Set<Player> vanished = new HashSet<>();

    private BukkitRunnable build() {
        return new BukkitRunnable() {
            @Override
            public void run() {
                Collection<? extends Player> c = Bukkit.getOnlinePlayers();
                if(c.size() <= 1) return;

                for(Player p : c) {
                    boolean isVanished = false;

                    for(Player other : Bukkit.getOnlinePlayers()) {
                        if(!other.canSee(p)) {
                            //vanished
                            isVanished = true;

                            if(!vanished.contains(p)) {
                                //toggled
                                vanished.add(p);
                                WarpSystem.getInstance().getDataHandler().send(new PacketVanishInfo(p.getName(), true));
                            }
                            break;
                        }
                    }

                    if(!isVanished && vanished.remove(p)) {
                        //toggled
                        WarpSystem.getInstance().getDataHandler().send(new PacketVanishInfo(p.getName(), false));
                    }
                }
            }
        };
    }

    @Override
    public void onConnect() {
        this.runnable = build();
        this.runnable.runTaskTimer(WarpSystem.getInstance(), 0L, 200L);
    }

    @Override
    public void onDisconnect() {
        if(runnable != null) {
            runnable.cancel();
            this.runnable = null;
        }
    }

    public boolean isVanished(Player player) {
        return vanished.contains(player);
    }
}
