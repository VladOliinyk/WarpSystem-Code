package de.codingair.warpsystem.spigot.versionfactory.featureobjects.dimensional;

import de.codingair.codingapi.server.sounds.Sound;
import de.codingair.codingapi.server.sounds.SoundData;
import de.codingair.codingapi.tools.io.utils.DataMask;
import de.codingair.warpsystem.spigot.base.WarpSystem;
import de.codingair.warpsystem.spigot.base.utils.featureobjects.FeatureObject;
import de.codingair.warpsystem.spigot.base.utils.teleport.TeleportOptions;
import de.codingair.warpsystem.spigot.base.utils.teleport.destinations.Destination;
import de.codingair.warpsystem.spigot.features.portals.dimensions.DimensionType;
import de.codingair.warpsystem.spigot.features.portals.dimensions.IDimensionalPortal;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPortalEvent;

import java.util.Objects;

public class DimensionalPortal extends FeatureObject implements IDimensionalPortal, Listener {
    private DimensionType type;

    public DimensionalPortal() {
    }

    public DimensionalPortal(DimensionType type) {
        this.type = type;
    }

    public DimensionalPortal(DimensionalPortal portal) {
        this.apply(portal);
    }

    @Override
    public void enable() {
        Bukkit.getPluginManager().registerEvents(this, WarpSystem.getInstance());
    }

    @Override
    public boolean read(DataMask d) throws Exception {
        boolean success = super.read(d);
        this.type = DimensionType.get(d.getString("type", DimensionType.NETHER_PORTAL.name()));
        return success && type != null;
    }

    @Override
    public FeatureObject perform(Player player, TeleportOptions options) {
        options.setDisplayName(null);
        options.setTeleportSound(new SoundData(Sound.BLOCK_PORTAL_TRAVEL, 0.5F, 1F));
        options.setCancelSound(null);
        options.setMessage(null);
        options.setAfterEffects(false, false);
        options.setTeleportAnimation(false);
        options.setWaitForTeleport(false);
        options.setPlayTick(false);
        options.setPrintDelay(false);
        options.setSkip(true);

        return super.perform(player, options);
    }

    @Override
    public void write(DataMask d) {
        d.put("type", this.type.name());
        super.write(d);
    }

    @Override
    public void destroy() {
        super.destroy();
        this.type = null;
        HandlerList.unregisterAll(this);
    }

    @Override
    public void apply(FeatureObject object) {
        super.apply(object);

        if (object instanceof DimensionalPortal) {
            DimensionalPortal p = (DimensionalPortal) object;
            this.type = p.type;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        DimensionalPortal portal = (DimensionalPortal) o;
        return type == portal.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type);
    }

    @Override
    public DimensionType getType() {
        return type;
    }

    @EventHandler (ignoreCancelled = true)
    public void onPortal(PlayerPortalEvent e) {
        if (e.getCause() == type.getCause()) {
            Destination destination = getDestination();
            Location l = destination.buildLocation();

            if (l == null || !e.getPlayer().getWorld().equals(l.getWorld())) {
                perform(e.getPlayer());
                e.setCancelled(true);
            }
        }
    }

    @Override
    public <T extends FeatureObject> T createDestinationIfAbsent() {
        T t = super.createDestinationIfAbsent();
        getDestination().getCustomOptions().setParticles(false);
        return t;
    }
}
