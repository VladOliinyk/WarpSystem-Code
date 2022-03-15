package de.codingair.warpsystem.spigot.base.utils.teleport.destinations;

import de.codingair.codingapi.tools.Callback;
import de.codingair.codingapi.tools.Location;
import de.codingair.codingapi.tools.io.utils.DataMask;
import de.codingair.codingapi.tools.io.utils.Serializable;
import de.codingair.codingapi.utils.ImprovedDouble;
import de.codingair.warpsystem.api.destinations.utils.*;
import de.codingair.warpsystem.spigot.api.placeholders.PAPI;
import de.codingair.warpsystem.spigot.base.utils.teleport.Origin;
import de.codingair.warpsystem.spigot.base.utils.teleport.destinations.adapters.CloneableAdapter;
import de.codingair.warpsystem.spigot.base.utils.teleport.destinations.adapters.GlobalWarpAdapter;
import de.codingair.warpsystem.spigot.base.utils.teleport.destinations.adapters.LocationAdapter;
import de.codingair.warpsystem.spigot.base.utils.teleport.destinations.adapters.ServerAdapter;
import de.codingair.warpsystem.spigot.features.globalwarps.managers.GlobalWarpManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;

import java.util.concurrent.CompletableFuture;

public class Destination implements IDestination {
    private final Options customOptions = new Options();
    private String id;
    private DestinationType type;
    private IDestinationAdapter adapter;
    private double offsetX, offsetY, offsetZ;

    public Destination() {
        id = null;
        type = DestinationType.UNKNOWN;
        adapter = null;
    }

    public Destination(String id, DestinationType type) {
        this.id = id;
        this.type = type;
        this.adapter = type.getInstance().dest(this);
    }

    public Destination(String id, DestinationAdapter adapter) {
        this(adapter);
        this.id = id;
    }

    public Destination(DestinationAdapter adapter) {
        this.id = null;
        this.type = DestinationType.getByAdapter(adapter);
        this.adapter = adapter;
        adapter.destination = this;
    }

    @Deprecated
    public Destination(String data) {
        try {
            JSONArray json = (JSONArray) new JSONParser().parse(data);

            this.type = json.get(0) == null ? null : DestinationType.valueOf((String) json.get(0));
            this.id = json.get(1) == null ? null : (String) json.get(1);
            this.adapter = type == null ? null : type.getInstance().dest(this);
            if (json.size() > 2) {
                offsetX = Double.parseDouble(json.get(2) + "");
                offsetY = Double.parseDouble(json.get(3) + "");
                offsetZ = Double.parseDouble(json.get(4) + "");
            }
        } catch (Exception ex) {
            throw new IllegalArgumentException("Wrong serialized data!", ex);
        }
    }

    public Destination apply(Destination destination) {
        if (destination == null) {
            this.id = null;
            this.adapter = null;
            this.type = null;
            this.offsetX = 0;
            this.offsetY = 0;
            this.offsetZ = 0;
            this.customOptions.destroy();
            return this;
        }

        this.id = destination.id;
        this.adapter = destination.adapter instanceof CloneableAdapter ? ((CloneableAdapter) destination.adapter).clone() : destination.adapter == null ? null : destination.type.getInstance();
        if (this.adapter != null) ((DestinationAdapter) this.adapter).destination = this;
        this.type = destination.type;
        this.offsetX = destination.offsetX;
        this.offsetY = destination.offsetY;
        this.offsetZ = destination.offsetZ;
        this.customOptions.apply(destination.customOptions);
        return this;
    }

    public String getTargetServer() {
        if (adapter instanceof GlobalWarpAdapter) {
            return GlobalWarpManager.getInstance().getGlobalWarps().get(id);
        } else if (adapter instanceof ServerAdapter) {
            return ((ServerAdapter) adapter).getServer();
        } else return null;
    }

    @Override
    public void sendMessage(Player player, String message, String displayName, double costs, de.codingair.warpsystem.api.destinations.utils.Origin origin) {
        sendMessage(player, message, displayName, costs, Origin.getByApi(origin));
    }

    @Override
    public @NotNull CompletableFuture<Boolean> teleport(@NotNull Player player, @Nullable String message, @Nullable String displayName, boolean checkPermission, double costs, @Nullable Callback<Result> callback) {
        return teleport(player, message, displayName, checkPermission, false, costs, callback);
    }

    public CompletableFuture<Boolean> teleport(@NotNull Player player, @Nullable String message, String displayName, boolean checkPermission, boolean silent, double costs, @Nullable Callback<Result> callback) {
        if (adapter == null) return CompletableFuture.completedFuture(false);
        player.setFallDistance(0F);

        message = this.customOptions.buildMessage(message);
        if (!customOptions.sendMessage()) message = null;
        else message = PAPI.convert(message, player);

        if (customOptions.getDisplayName() != null) displayName = customOptions.getDisplayName();

        if (adapter instanceof DestinationAdapter) return ((DestinationAdapter) adapter).teleport(player, id, buildRandomOffset(), displayName, checkPermission, message, silent, costs, callback);
        else return adapter.teleport(player, id, buildRandomOffset(), displayName, checkPermission, message, costs, callback);
    }

    public void sendMessage(@NotNull Player player, @Nullable String message, @Nullable String displayName, double costs, @NotNull Origin origin) {
        if (adapter == null
                || type == DestinationType.GlobalWarp
                || (customOptions.getMessage() == null ? !origin.sendTeleportMessage() : !customOptions.getMessage())
        ) return;

        if (customOptions.getCustomMessage() != null) message = ChatColor.translateAlternateColorCodes('&', customOptions.getCustomMessage());
        if (message == null) return;

        message = PAPI.convert(message, player);
        message = message
                .replace("%AMOUNT%", new ImprovedDouble(costs).toString())
                .replace("%player%", player.getName())
                .replace("%PLAYER%", player.getName());

        String finalDisplayName = customOptions.getDisplayName() == null ? displayName : customOptions.getDisplayName();
        if (finalDisplayName != null) message = message.replace("%warp%", ChatColor.translateAlternateColorCodes('&', finalDisplayName));

        player.sendMessage(message);
    }

    public void adjustLocation(@NotNull Player player, @NotNull org.bukkit.Location location) {
        location.add(buildRandomOffset());
        if (!customOptions.isRotation() || (location.getYaw() == -420 && location.getPitch() == -420)) {
            org.bukkit.Location p = player.getLocation();
            location.setYaw(p.getYaw());
            location.setPitch(p.getPitch());
        }
    }

    public Location buildLocation() {
        org.bukkit.Location location = adapter.buildLocation(id);
        if (location == null) return null;
        Location l = new Location(location);

        if (offsetX != 0 || offsetY != 0 || offsetZ != 0) {
            l.add(buildRandomOffset());
        }

        return l;
    }

    @NotNull
    public Vector buildRandomOffset() {
        double offsetX = Math.random() * 2 * this.offsetX - this.offsetX;
        double offsetY = Math.random() * this.offsetY;
        double offsetZ = Math.random() * 2 * this.offsetZ - this.offsetZ;

        return new Vector(offsetX, offsetY, offsetZ);
    }

    public double getCosts() {
        return adapter == null ? 0 : adapter.getCosts(id);
    }

    public SimulatedTeleportResult simulate(Player player, boolean checkPermission) {
        if (adapter == null) return new SimulatedTeleportResult(null, Result.NO_ADAPTER);
        return adapter.simulate(player, this.id, checkPermission);
    }

    public String getId() {
        if (this.adapter instanceof IdAdapter) {
            return ((IdAdapter) this.adapter).getId();
        } else return id;
    }

    public void setId(String id) {
        if (this.adapter instanceof LocationAdapter && ((LocationAdapter) this.adapter).getLocation() != null) {
            ((LocationAdapter) this.adapter).setLocation(null);
        }
        this.id = id;
    }

    public DestinationType getType() {
        return type;
    }

    public void setType(DestinationType type) {
        this.type = type;
    }

    public IDestinationAdapter getAdapter() {
        return adapter;
    }

    @Override
    public void setAdapter(IDestinationAdapter adapter) {
        this.adapter = adapter;
        if (this.adapter instanceof DestinationAdapter) ((DestinationAdapter) this.adapter).destination = this;
    }

    @Override
    public boolean read(DataMask d) throws Exception {
        this.type = DestinationType.getById(d.getInteger("type"));
        this.adapter = type.getInstance();
        ((DestinationAdapter) this.adapter).destination = this;

        if (adapter instanceof Serializable) {
            ((Serializable) adapter).read(d);
        } else id = d.getRaw("id");

        this.offsetX = d.getDouble("oX");
        this.offsetY = d.getDouble("oY");
        this.offsetZ = d.getDouble("oZ");
        Boolean message = d.getBoolean("message", null); //old
        d.getSerializable("options", customOptions);
        if (message != null) customOptions.setMessage(!message);
        return true;
    }

    @Override
    public void write(DataMask d) {
        d.put("type", type.getId());

        if (adapter != null && adapter instanceof Serializable) ((Serializable) adapter).write(d);
        else d.put("id", this.id);

        d.put("oX", offsetX);
        d.put("oY", offsetY);
        d.put("oZ", offsetZ);
        d.put("options", customOptions);
    }

    @Override
    public void destroy() {
        this.type = null;
        this.id = null;
        this.adapter = null;
        this.offsetX = 0;
        this.offsetY = 0;
        this.offsetZ = 0;
        customOptions.destroy();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Destination that = (Destination) o;
        if (getId() == null) return that.getId() == null && type == that.type;
        return getId().equals(that.getId())
                && type == that.type
                && offsetX == that.offsetX
                && offsetY == that.offsetY
                && offsetZ == that.offsetZ
                && customOptions.equals(that.customOptions);
    }

    public Destination clone() {
        Destination destination = new Destination();
        destination.apply(this);
        return destination;
    }

    @Override
    public String toString() {
        return "Destination{id=" + getId() + ", " + this.type + "}";
    }

    public double getOffsetX() {
        return offsetX;
    }

    public void setOffsetX(double offsetX) {
        this.offsetX = offsetX;
    }

    public double getOffsetY() {
        return offsetY;
    }

    public void setOffsetY(double offsetY) {
        this.offsetY = offsetY;
    }

    public double getOffsetZ() {
        return offsetZ;
    }

    public void setOffsetZ(double offsetZ) {
        this.offsetZ = offsetZ;
    }

    @Override
    public boolean usesBukkitTeleportation() {
        return adapter != null && adapter.usesBukkitTeleportation();
    }

    public IDestinationOptions getCustomOptions() {
        return customOptions;
    }
}
