package de.codingair.warpsystem.spigot.base.utils.teleport.process;

import de.codingair.codingapi.tools.Callback;
import de.codingair.warpsystem.api.destinations.utils.Result;
import de.codingair.warpsystem.spigot.base.utils.teleport.Origin;
import de.codingair.warpsystem.spigot.base.utils.teleport.TeleportOptions;
import de.codingair.warpsystem.spigot.base.utils.teleport.destinations.Destination;
import de.codingair.warpsystem.spigot.base.utils.teleport.destinations.adapters.EmptyAdapter;
import org.bukkit.entity.Player;

public class TeleportDummy extends Teleport {
    public TeleportDummy(Player player, Origin origin, Callback<Result> callback) {
        super(player, new TeleportOptions(new Destination(new EmptyAdapter()), "DUMMY", origin).addCallback(callback));
    }
}
