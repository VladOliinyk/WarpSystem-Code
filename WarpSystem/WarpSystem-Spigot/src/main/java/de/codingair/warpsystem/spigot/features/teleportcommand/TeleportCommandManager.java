package de.codingair.warpsystem.spigot.features.teleportcommand;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import de.codingair.codingapi.files.ConfigFile;
import de.codingair.codingapi.player.chat.ChatButtonManager;
import de.codingair.codingapi.tools.Callback;
import de.codingair.warpsystem.api.destinations.utils.Result;
import de.codingair.warpsystem.core.transfer.packets.general.TeleportBackPacket;
import de.codingair.warpsystem.core.transfer.packets.general.TeleportCommandOptionsPacket;
import de.codingair.warpsystem.core.transfer.packets.spigot.ToggleForceTeleportsPacket;
import de.codingair.warpsystem.core.transfer.utils.TeleportCommandOptions;
import de.codingair.warpsystem.core.utils.Manager;
import de.codingair.warpsystem.spigot.base.WarpSystem;
import de.codingair.warpsystem.spigot.base.setupassistant.annotations.AvailableForSetupAssistant;
import de.codingair.warpsystem.spigot.base.setupassistant.annotations.Function;
import de.codingair.warpsystem.spigot.base.utils.Lang;
import de.codingair.warpsystem.spigot.base.utils.ProxyFeature;
import de.codingair.warpsystem.spigot.base.utils.teleport.Origin;
import de.codingair.warpsystem.spigot.base.utils.teleport.TeleportOptions;
import de.codingair.warpsystem.spigot.base.utils.teleport.destinations.Destination;
import de.codingair.warpsystem.spigot.base.utils.teleport.destinations.adapters.LocationAdapter;
import de.codingair.warpsystem.spigot.bstats.Collectible;
import de.codingair.warpsystem.spigot.features.FeatureType;
import de.codingair.warpsystem.spigot.features.teleportcommand.commands.*;
import de.codingair.warpsystem.spigot.features.teleportcommand.listeners.BackListener;
import de.codingair.warpsystem.spigot.features.teleportcommand.listeners.TeleportListener;
import de.codingair.warpsystem.spigot.features.teleportcommand.utils.PlayerLocationData;
import de.codingair.warpsystem.spigot.versionfactory.VFac;
import de.codingair.warpsystem.spigot.versionfactory.VKey;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@AvailableForSetupAssistant (type = "TeleportCommands", config = "Config")
@Function (name = "Enabled", defaultValue = "true", configPath = "WarpSystem.Functions.TeleportCommand", clazz = Boolean.class)
@Function (name = "Proxy", defaultValue = "true", configPath = "WarpSystem.TeleportCommands.Proxy", clazz = Boolean.class)
@Function (name = "Teleport requests costs", defaultValue = "0", configPath = "WarpSystem.TeleportCommands.TeleportRequests.Teleport_Costs", clazz = Double.class)
@Function (name = "Teleport requests expire delay (seconds)", defaultValue = "30", configPath = "WarpSystem.TeleportCommands.TeleportRequests.ExpireDelay", clazz = Integer.class)
@Function (name = "Back", defaultValue = "true", configPath = "WarpSystem.TeleportCommands.Back", clazz = Boolean.class)
@Function (name = "Tp", defaultValue = "true", configPath = "WarpSystem.TeleportCommands.Tp", clazz = Boolean.class)
@Function (name = "TpAll", defaultValue = "true", configPath = "WarpSystem.TeleportCommands.TpAll", clazz = Boolean.class)
@Function (name = "TpToggle", defaultValue = "true", configPath = "WarpSystem.TeleportCommands.TpToggle", clazz = Boolean.class)
@Function (name = "Tpa", defaultValue = "true", configPath = "WarpSystem.TeleportCommands.Tpa", clazz = Boolean.class)
@Function (name = "TpaHere", defaultValue = "true", configPath = "WarpSystem.TeleportCommands.TpaHere", clazz = Boolean.class)
@Function (name = "TpaAll", defaultValue = "true", configPath = "WarpSystem.TeleportCommands.TpaAll", clazz = Boolean.class)
@Function (name = "TpaToggle", defaultValue = "true", configPath = "WarpSystem.TeleportCommands.TpaToggle", clazz = Boolean.class)
public abstract class TeleportCommandManager implements Manager, ProxyFeature, Collectible {
    protected final Cache<String, PlayerLocationData> dying = CacheBuilder.newBuilder().expireAfterAccess(10, TimeUnit.MINUTES).build();
    protected final HashMap<String, Location> backPosition = new HashMap<>();
    protected final HashMap<String, Location> quitPosition = new HashMap<>();
    private final HashMap<String, List<Invitation>> invites = new HashMap<>();
    private final Set<String> denyTpa = new HashSet<>();
    private final Set<String> denyForceTps = new HashSet<>();
    private final Map<String, TeleportCommandOptions> serverOptions = new HashMap<>();
    private int expireDelay = 30;
    private int tpaCosts = 0;
    private boolean proxy = false;
    private ITeleportCommandHandler handler;

    private CTeleport tp;
    private CTpHere tpHere;
    private CTpToggle tpToggle;
    private CTpa tpa;
    private CTpAccept tpAccept;
    private CTpDeny tpDeny;
    private CTpaHere tpaHere;
    private CTpaToggle tpaToggle;
    private CTpaAll tpaAll;
    private CTpAll tpAll;
    private CBack back;

    public static TeleportCommandManager getInstance() {
        return WarpSystem.getInstance().getDataManager().getManager(FeatureType.TELEPORT_COMMAND);
    }

    public static ITeleportCommandHandler handler() {
        return getInstance().handler;
    }

    @Override
    public void collectOptionStatistics(Map<String, Integer> entry) {
        if (tp != null) entry.put("Tp", 1);
        if (tpHere != null) entry.put("TpHere", 1);
        if (tpToggle != null) entry.put("TpToggle", 1);
        if (tpa != null) entry.put("Tpa", 1);
        if (tpaHere != null) entry.put("TpaHere", 1);
        if (tpaToggle != null) entry.put("TpaToggle", 1);
        if (tpaAll != null) entry.put("TpaAll", 1);
        if (tpAll != null) entry.put("TpAll", 1);
        if (back != null) entry.put("Back", 1);
    }

    @Override
    public boolean load(boolean loader) {
        WarpSystem.getInstance().getProxyFeatureList().add(this);
        Bukkit.getPluginManager().registerEvents(new TeleportListener(), WarpSystem.getInstance());
        Bukkit.getPluginManager().registerEvents(new BackListener(), WarpSystem.getInstance());

        ConfigFile file = WarpSystem.getInstance().getFileManager().getFile("Config");

        if (file.getConfig().getBoolean("WarpSystem.Functions.TeleportCommand", true)) {
            expireDelay = file.getConfig().getInt("WarpSystem.TeleportCommands.TeleportRequests.ExpireDelay", 30);
            tpaCosts = file.getConfig().getInt("WarpSystem.TeleportCommands.TeleportRequests.Teleport_Costs", 0);
            proxy = file.getConfig().getBoolean("WarpSystem.TeleportCommands.Proxy", true);

            if (file.getConfig().getBoolean("WarpSystem.TeleportCommands.Tp", true)) {
                (tp = new CTeleport()).register();
                (tpHere = new CTpHere()).register();
            }

            if (file.getConfig().getBoolean("WarpSystem.TeleportCommands.TpToggle", true)) (tpToggle = new CTpToggle()).register();
            if (file.getConfig().getBoolean("WarpSystem.TeleportCommands.Tpa", true)) {
                (tpa = new CTpa()).register();
                (tpAccept = new CTpAccept()).register();
                (tpDeny = new CTpDeny()).register();
            }
            if (file.getConfig().getBoolean("WarpSystem.TeleportCommands.TpaHere", true)) {
                (tpaHere = new CTpaHere()).register();
                if (tpAccept == null) (tpAccept = new CTpAccept()).register();
                if (tpDeny == null) (tpDeny = new CTpDeny()).register();
            }
            if (file.getConfig().getBoolean("WarpSystem.TeleportCommands.TpaToggle", true)) (tpaToggle = new CTpaToggle()).register();
            if (file.getConfig().getBoolean("WarpSystem.TeleportCommands.TpaAll", true)) (tpaAll = new CTpaAll()).register();
            if (file.getConfig().getBoolean("WarpSystem.TeleportCommands.TpAll", true)) (tpAll = new CTpAll()).register();
            if (file.getConfig().getBoolean("WarpSystem.TeleportCommands.Back", true)) (back = new CBack()).register();
        }

        ChatButtonManager.getInstance().addListener((player, id, type) -> {
            if (type != null && type.equalsIgnoreCase("TP")) {
                player.sendMessage(Lang.getPrefix() + Lang.get("TeleportRequest_not_valid_general"));
                return true;
            }

            return false;
        });

        this.handler = VFac.build(VKey.TeleportCommandHandler);
        return true;
    }

    @Override
    public void save(boolean saver) {
    }

    @Override
    public void destroy() {
        tp = null;
        tpHere = null;
        tpToggle = null;
        tpa = null;
        tpAccept = null;
        tpDeny = null;
        tpaHere = null;
        tpaToggle = null;
        tpaAll = null;
        tpAll = null;
        back = null;
    }

    @Override
    public void onConnect(Player connection) {
        TeleportCommandOptionsPacket packet = new TeleportCommandOptionsPacket(back != null, tp != null, tpAll != null, tpToggle != null, tpa != null, tpaHere != null, tpaAll != null, tpaToggle != null);

        if (proxy) WarpSystem.getDataHandler().send(packet, connection);
        else WarpSystem.getDataHandler().send(new TeleportCommandOptionsPacket(), connection); //tell our proxy that we disabled proxy wide transportations

        this.serverOptions.put(WarpSystem.getInstance().getCurrentServer().toLowerCase(), packet.getOptions()); //save options for this server
    }

    @Override
    public void onDisconnect() {
    }

    public boolean isProxy() {
        return proxy;
    }

    public void addToBackHistory(Player player, Location location, boolean quit) {
        if (location.getWorld() == null) return;

        if (quit) this.quitPosition.put(player.getName(), location);
        else this.backPosition.put(player.getName(), location);
    }

    public Location getQuitPosition(String player) {
        return null;
    }

    public CompletableFuture<TeleportBackPacket.Result> teleportToLastBackLocation(Player player, boolean proxy, boolean force, boolean skip) {
        return teleportToLastBackLocation(player.getName(), proxy, force, skip);
    }

    public CompletableFuture<TeleportBackPacket.Result> teleportToLastBackLocation(String player, boolean proxy, boolean force, boolean skip) {
        Location l = this.backPosition.remove(player);
        if (l == null) return CompletableFuture.completedFuture(TeleportBackPacket.Result.NO_LAST_POSITION);

        return CompletableFuture.completedFuture(teleportBack(player, l, false, skip, proxy));
    }

    protected TeleportBackPacket.Result teleportBack(String player, Location l, boolean force, boolean skip, boolean proxy) {
        if (!force && WarpSystem.opt().forbiddenRegion(l)) {
            Player p = Bukkit.getPlayer(player);
            if (p != null) p.sendMessage(Lang.getPrefix() + Lang.get("Target_Protected_Area"));
            return TeleportBackPacket.Result.PROTECTED_REGION;
        }

        de.codingair.warpsystem.spigot.base.listeners.TeleportListener.setSpawnPositionOrTeleport(player, buildTeleport(player, l, force, skip, proxy));
        return TeleportBackPacket.Result.SUCCESS;
    }

    public TeleportOptions buildTeleport(String player, Location l, boolean force, boolean skip, boolean proxy) {
        TeleportOptions options = new TeleportOptions(new Destination(new LocationAdapter(l)), Lang.get("Last_Position"), Origin.TeleportCommand);

        options.setSkip(skip);
        if (force) options.setMessage(Lang.getPrefix() + Lang.get("Target_Protected_Area"));

        options.addCallback(new Callback<Result>() {
            @Override
            public void accept(Result result) {
                if (result != Result.SUCCESS && !proxy) {
                    backPosition.put(player, l);
                } else if (proxy) {
                    Bukkit.getScheduler().runTaskLater(WarpSystem.getInstance(), () -> backPosition.remove(player), 5);
                }
            }
        });

        return options;
    }

    public boolean deniesTpaRequests(String player) {
        return this.denyTpa.contains(player);
    }

    public boolean toggleDenyTpaRequest(Player player) {
        if (this.denyTpa.contains(player.getName())) {
            if (WarpSystem.getInstance().isProxyConnected()) WarpSystem.getDataHandler().send(new ToggleForceTeleportsPacket(player.getName(), deniesForceTps(player), false), player);
            this.denyTpa.remove(player.getName());
            return false;
        } else {
            if (WarpSystem.getInstance().isProxyConnected()) WarpSystem.getDataHandler().send(new ToggleForceTeleportsPacket(player.getName(), deniesForceTps(player), true), player);
            this.denyTpa.add(player.getName());
            return true;
        }
    }

    public boolean deniesForceTps(Player player) {
        return this.denyForceTps.contains(player.getName());
    }

    public boolean toggleDenyForceTps(Player player) {
        if (this.denyForceTps.contains(player.getName())) {
            if (WarpSystem.getInstance().isProxyConnected()) WarpSystem.getDataHandler().send(new ToggleForceTeleportsPacket(player.getName(), false, deniesTpaRequests(player.getName())), player);
            this.denyForceTps.remove(player.getName());
            return false;
        } else {
            if (WarpSystem.getInstance().isProxyConnected()) WarpSystem.getDataHandler().send(new ToggleForceTeleportsPacket(player.getName(), true, deniesTpaRequests(player.getName())), player);
            this.denyForceTps.add(player.getName());
            return true;
        }
    }

    public void setDenyForceTps(String player, boolean deny) {
        if (deny) {
            this.denyForceTps.add(player);
        } else this.denyForceTps.remove(player);
    }

    public boolean isInvitedBy(String sender, String recipient) {
        return getInvitation(sender, recipient) != null;
    }

    public List<Invitation> getReceivedInvites(String player) {
        List<Invitation> invites = new ArrayList<>();

        List<List<Invitation>> data = new ArrayList<>(this.invites.values());
        for (List<Invitation> value : data) {
            for (Invitation i : value) {
                if (i.isRecipient(player)) invites.add(i);
            }
        }
        data.clear();

        return invites;
    }

    public void checkDestructionOf(Invitation inv) {
        if (inv.canBeDestroyed()) {
            List<Invitation> l = this.invites.get(inv.getSender());

            if (l != null) l.remove(inv);
            inv.destroy();
        }
    }

    public Invitation getInvitation(String sender, String recipient) {
        if (sender.equalsIgnoreCase(recipient)) return null;

        List<Invitation> l = this.invites.get(sender);
        if (l == null) return null;

        for (Invitation invitation : l) {
            if (invitation.isRecipient(recipient)) return invitation;
        }

        return null;
    }

    public void invite(String sender, boolean tpToSender, Callback<Long> callback, String recipient) {
        invite(sender, tpToSender, callback, recipient, !proxy);
    }

    public void invite(String sender, boolean tpToSender, Callback<Long> callback, String recipient, boolean bukkitOnly) {
        if (recipient != null) {
            List<Invitation> l = this.invites.get(sender);

            if (l == null) {
                l = new ArrayList<>();
                this.invites.put(sender, l);
            } else if (isInvitedBy(sender, recipient)) {
                if (callback != null) callback.accept(1L << 32);
                return;
            } else if (deniesTpaRequests(recipient)) {
                if (callback != null) callback.accept(-1L << 32);
                return;
            }

            Invitation inv = new Invitation(sender, tpToSender, recipient, bukkitOnly);
            l.add(inv);
            inv.send(new Callback<Long>() {
                @Override
                public void accept(Long result) {
                    callback.accept(result);
                    if (result.intValue() == 0) checkDestructionOf(inv);
                }
            });
        } else {
            List<Invitation> l = this.invites.computeIfAbsent(sender, k -> new ArrayList<>());

            Invitation old = null;
            for (Invitation i : l) {
                if (i.getRecipient() == null) {
                    old = i;
                    break;
                }
            }
            l.remove(old);

            //invite all
            Invitation inv = new Invitation(sender, bukkitOnly);
            l.add(inv);
            inv.send(new Callback<Long>() {
                @Override
                public void accept(Long result) {
                    callback.accept(result);
                    if (result.intValue() == 0) checkDestructionOf(inv);
                }
            });
        }
    }

    public void revive(Player player) {
        PlayerLocationData data = this.dying.getIfPresent(player.getName());
        this.dying.invalidate(player.getName());

        if (data != null) {
            Location l = data.getBack();
            if (l != null) this.backPosition.put(player.getName(), l);
            l = data.getQuit();
            if (l != null) this.quitPosition.put(player.getName(), l);
        }
    }

    public void clear(Player player) {
        //clear own/foreign invitations
        List<Invitation> invites = this.invites.remove(player.getName());
        if (invites != null) {
            for (Invitation invite : invites) {
                invite.destroy();
            }

            invites.clear();
        }

        invites = getReceivedInvites(player.getName());

        for (Invitation invite : invites) {
            invite.timeOut(player.getName());
        }

        invites.clear();

        //remove from auto deny list
        this.denyTpa.remove(player.getName());
        this.denyForceTps.remove(player.getName());

        PlayerLocationData data = new PlayerLocationData(this.backPosition.remove(player.getName()), this.quitPosition.remove(player.getName()));
        if (data.valid()) dying.put(player.getName(), data);
    }

    public Location invalidateBackPosition(Player player) {
        return null;
    }

    public int getExpireDelay() {
        return expireDelay;
    }

    public int getTpaCosts() {
        return this.tpaCosts;
    }

    public void registerServerOptions(String server, TeleportCommandOptions options) {
        this.serverOptions.put(server.toLowerCase(), options);
    }

    public TeleportCommandOptions getServerOptions(String server) {
        if (server == null) return null;
        if (!WarpSystem.getInstance().isProxyConnected() || !TeleportCommandManager.getInstance().isProxy()) return null;
        return this.serverOptions.get(server.toLowerCase());
    }

    public boolean isServerAccessible(String server) {
        return getServerOptions(server) != null;
    }
}