package de.codingair.warpsystem.bungee.features.randomtp;

import de.codingair.packetmanagement.utils.Direction;
import de.codingair.warpsystem.bungee.base.WarpSystem;
import de.codingair.warpsystem.bungee.base.events.ServerProvideOptionsEvent;
import de.codingair.warpsystem.core.transfer.packets.spigot.RandomTPWorldsPacket;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class RandomTPListener implements Listener {

    @EventHandler
    public void onInitialize(ServerProvideOptionsEvent e) {
        if (!e.getOptions().sameVersion()) return;
        RandomTPManager.getInstance().updateQueue(e.getServer());

        WarpSystem.getDataHandler().send(new RandomTPWorldsPacket(RandomTPManager.getInstance().getWorlds()), e.getServer(), Direction.DOWN);
    }
}
