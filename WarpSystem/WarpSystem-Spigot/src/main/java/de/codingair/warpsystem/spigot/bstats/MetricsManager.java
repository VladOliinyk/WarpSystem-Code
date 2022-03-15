package de.codingair.warpsystem.spigot.bstats;

import de.codingair.warpsystem.core.utils.Manager;
import de.codingair.warpsystem.spigot.base.WarpSystem;
import de.codingair.warpsystem.spigot.features.FeatureType;
import de.codingair.warpsystem.spigot.versionfactory.VFac;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.AdvancedPie;
import org.bstats.charts.SimplePie;
import org.bukkit.Bukkit;

import java.util.HashMap;
import java.util.Map;

public class MetricsManager implements Manager {

    @Override
    public boolean load(boolean loader) {
        Metrics metrics = new Metrics(WarpSystem.getInstance(), 3968);

        metrics.addCustomChart(new AdvancedPie("features", () -> {
            Map<String, Integer> map = new HashMap<>();

            for (FeatureType type : FeatureType.values()) {
                if (type.getPriority() == FeatureType.Priority.ALWAYS_ON
                        || type.getPriority() == FeatureType.Priority.DISABLED
                        || type == FeatureType.METRICS
                        || type == FeatureType.SIMPLE_WARPS
                        || type == FeatureType.GLOBAL_WARPS
                ) continue;

                map.put(type.getName(), type.isActive() ? 1 : 0);
            }

            return map;
        }));

        for (FeatureType type : FeatureType.values()) {
            Manager m = WarpSystem.getInstance().getDataManager().getManager(type);
            if (m == null) continue;

            if (m instanceof Collectible) {
                ((Collectible) m).addCustomCarts(metrics);

                metrics.addCustomChart(new AdvancedPie(type.getName().toLowerCase(), () -> {
                    Map<String, Integer> entry = new HashMap<>();
                    ((Collectible) m).collectOptionStatistics(entry);
                    return entry;
                }));
            }
        }

        metrics.addCustomChart(new SimplePie("type", () -> VFac.isAvailable("Indicator") ? "Free" : "Premium"));
        metrics.addCustomChart(new SimplePie("bungeecord", () -> {
            if (Bukkit.getServer().getOnlinePlayers().isEmpty()) return "Is empty";
            return WarpSystem.getInstance().isProxyConnected() ? "Yes" : "No";
        }));
        return true;
    }

    @Override
    public void save(boolean saver) {

    }

    @Override
    public void destroy() {

    }
}
