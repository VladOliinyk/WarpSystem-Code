package de.codingair.warpsystem.velocity.features.teleport.managers;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import de.codingair.warpsystem.base.utils.Manager;
import de.codingair.warpsystem.velocity.base.WarpSystem;
import de.codingair.warpsystem.velocity.features.FeatureType;
import de.codingair.warpsystem.velocity.features.teleport.listeners.TabCompleterListener;
import de.codingair.warpsystem.velocity.features.teleport.listeners.TeleportCommandListener;
import de.codingair.warpsystem.velocity.features.teleport.listeners.TeleportPacketListener;
import de.codingair.warpsystem.velocity.features.teleport.utils.TeleportCommandOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TeleportManager implements Manager {
    private final HashMap<RegisteredServer, TeleportCommandOptions> commandOptions = new HashMap<>();
    private final List<String> denyForceTpRequests = new ArrayList<>();
    private final List<String> denyForceTps = new ArrayList<>();

    public static TeleportManager getInstance() {
        return WarpSystem.getInstance().getDataManager().getManager(FeatureType.TELEPORT);
    }

    @Override
    public boolean load(boolean loader) {
        if(!loader) WarpSystem.log("  > Initializing TeleportManager");

        WarpSystem.proxy().getEventManager().register(WarpSystem.getInstance(), new TabCompleterListener());
        WarpSystem.proxy().getEventManager().register(WarpSystem.getInstance(), new TeleportCommandListener());

        WarpSystem.getInstance().getDataHandler().register(new TeleportPacketListener());
        return true;
    }

    @Override
    public void save(boolean saver) {
    }

    @Override
    public void destroy() {
    }

    public void removeOptions(RegisteredServer server) {
        this.commandOptions.remove(server);
    }

    public void registerOptions(RegisteredServer server, int options) {
        removeOptions(server);
        this.commandOptions.put(server, new TeleportCommandOptions(options));
    }

    public TeleportCommandOptions getOptions(RegisteredServer server) {
        return this.commandOptions.get(server);
    }

    public boolean isAccessible(RegisteredServer server) {
        TeleportCommandOptions options = getOptions(server);
        return options != null;
    }

    public boolean deniesForceTps(Player player) {
        return this.denyForceTps.contains(player.getUsername());
    }

    public void setDenyForceTps(Player player, boolean deny) {
        if(deny) {
            if(!this.denyForceTps.contains(player.getUsername())) this.denyForceTps.add(player.getUsername());
        } else this.denyForceTps.remove(player.getUsername());
    }

    public boolean deniesForceTpRequests(Player player) {
        return this.denyForceTpRequests.contains(player.getUsername());
    }

    public void setDenyForceTpRequests(Player player, boolean deny) {
        if(deny) {
            if(!this.denyForceTpRequests.contains(player.getUsername())) this.denyForceTpRequests.add(player.getUsername());
        } else this.denyForceTpRequests.remove(player.getUsername());
    }
}
