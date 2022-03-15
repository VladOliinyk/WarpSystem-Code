package de.codingair.warpsystem.spigot.base.utils.teleport.destinations;

import de.codingair.warpsystem.api.destinations.*;
import de.codingair.warpsystem.api.destinations.utils.IDestination;
import de.codingair.warpsystem.spigot.base.utils.teleport.destinations.adapters.*;
import org.bukkit.Location;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DestinationBuilder implements IDestinationBuilder {
    @Override
    public @NotNull IEmptyAdapter empty() {
        return new EmptyAdapter();
    }

    @Override
    public @NotNull IGlobalLocationAdapter globalLocation(@Nullable String server, @NotNull Location location) {
        return new GlobalLocationAdapter(server, new de.codingair.codingapi.tools.Location(location));
    }

    @Override
    public @NotNull IGlobalWarpAdapter globalWarp() {
        return new GlobalWarpAdapter();
    }

    @Override
    public @NotNull ILocationAdapter location(@NotNull Location location) {
        return new LocationAdapter(location);
    }

    @Override
    public @NotNull IServerAdapter server() {
        return new ServerAdapter();
    }

    @Override
    public @NotNull ISimpleWarpAdapter simpleWarp() {
        return new SimpleWarpAdapter();
    }

    @Override
    public @NotNull IVelocityAdapter velocity(@NotNull Vector vector, @Nullable Double multiplier) {
        return new VelocityAdapter(vector, multiplier);
    }

    @Override
    public @NotNull IDestination emptyDestination() {
        return new Destination(new EmptyAdapter());
    }

    @Override
    public @NotNull IDestination globalLocationDestination(@Nullable String server, @NotNull Location location) {
        return new Destination(new GlobalLocationAdapter(server, new de.codingair.codingapi.tools.Location(location)));
    }

    @Override
    public @NotNull IDestination globalWarpDestination(@NotNull String id) {
        return new Destination(id, new GlobalWarpAdapter());
    }

    @Override
    public @NotNull IDestination locationDestination(@NotNull Location location) {
        return new Destination(new LocationAdapter(location));
    }

    @Override
    public @NotNull IDestination serverDestination(@NotNull String server) {
        return new Destination(server, new ServerAdapter());
    }

    @Override
    public @NotNull IDestination simpleWarpDestination(@NotNull String id) {
        return new Destination(id, new SimpleWarpAdapter());
    }

    @Override
    public @NotNull IDestination velocityDestination(@NotNull Vector vector, @Nullable Double multiplier) {
        return new Destination(new VelocityAdapter(vector, multiplier));
    }
}
