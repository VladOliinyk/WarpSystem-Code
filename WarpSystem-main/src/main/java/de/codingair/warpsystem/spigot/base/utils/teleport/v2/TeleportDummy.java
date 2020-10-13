package de.codingair.warpsystem.spigot.base.utils.teleport.v2;

import de.codingair.codingapi.tools.Callback;
import de.codingair.warpsystem.spigot.base.utils.teleport.Origin;
import de.codingair.warpsystem.spigot.base.utils.teleport.Result;
import de.codingair.warpsystem.spigot.base.utils.teleport.TeleportOptions;
import de.codingair.warpsystem.spigot.base.utils.teleport.destinations.Destination;
import de.codingair.warpsystem.spigot.base.utils.teleport.destinations.adapters.EmptyAdapter;
import org.bukkit.entity.Player;

public class TeleportDummy extends Teleport {
    public TeleportDummy(Player player, Origin origin, Callback<Result> callback) {
        super(player, new TeleportOptions(new Destination(new EmptyAdapter()), "DUMMY").addCallback(callback).setOrigin(origin));
    }
}
