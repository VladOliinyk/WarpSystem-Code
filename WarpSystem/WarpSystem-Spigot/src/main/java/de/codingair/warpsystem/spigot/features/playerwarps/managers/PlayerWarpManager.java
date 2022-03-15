package de.codingair.warpsystem.spigot.features.playerwarps.managers;

import de.codingair.codingapi.API;
import de.codingair.codingapi.files.ConfigFile;
import de.codingair.codingapi.tools.Callback;
import de.codingair.codingapi.utils.ChatColor;
import de.codingair.codingapi.utils.Ticker;
import de.codingair.warpsystem.core.transfer.packets.general.DeletePlayerWarpPacket;
import de.codingair.warpsystem.core.transfer.packets.general.SendPlayerWarpUpdatePacket;
import de.codingair.warpsystem.core.transfer.packets.general.SendPlayerWarpsPacket;
import de.codingair.warpsystem.core.transfer.packets.spigot.utils.PlayerWarpData;
import de.codingair.warpsystem.core.transfer.packets.spigot.utils.PlayerWarpUpdate;
import de.codingair.warpsystem.core.utils.Manager;
import de.codingair.warpsystem.spigot.api.StringFormatter;
import de.codingair.warpsystem.spigot.api.events.FakeBlockBreakEvent;
import de.codingair.warpsystem.spigot.base.WarpSystem;
import de.codingair.warpsystem.spigot.base.setupassistant.annotations.AvailableForSetupAssistant;
import de.codingair.warpsystem.spigot.base.setupassistant.annotations.Function;
import de.codingair.warpsystem.spigot.base.utils.Lang;
import de.codingair.warpsystem.spigot.base.utils.ProxyFeature;
import de.codingair.warpsystem.spigot.base.utils.featureobjects.actions.Action;
import de.codingair.warpsystem.spigot.base.utils.featureobjects.actions.types.WarpAction;
import de.codingair.warpsystem.spigot.base.utils.teleport.destinations.adapters.GlobalLocationAdapter;
import de.codingair.warpsystem.spigot.bstats.Collectible;
import de.codingair.warpsystem.spigot.features.FeatureType;
import de.codingair.warpsystem.spigot.features.playerwarps.guis.list.FilterType;
import de.codingair.warpsystem.spigot.features.playerwarps.guis.list.PWList;
import de.codingair.warpsystem.spigot.features.playerwarps.listeners.PlayerWarpListener;
import de.codingair.warpsystem.spigot.features.playerwarps.utils.Category;
import de.codingair.warpsystem.spigot.features.playerwarps.utils.PlayerWarp;
import de.codingair.warpsystem.spigot.features.playerwarps.utils.forwardcompatibility.PlayerWarpTagConverter_v4_2_2;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SingleLineChart;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@AvailableForSetupAssistant (type = "PlayerWarps", config = "PlayerWarpConfig")
@Function (name = "Enabled", defaultValue = "true", config = "Config", configPath = "WarpSystem.Functions.PlayerWarps", clazz = Boolean.class)
@Function (name = "Teleport message", defaultValue = "true", config = "Config", configPath = "WarpSystem.Send.Teleport_Message.PlayerWarps", clazz = Boolean.class)
@Function (name = "Max warp amount", defaultValue = "5", configPath = "PlayerWarps.General.Max_Warp_Amount", description = "§7If permissions are §cdisabled", clazz = Integer.class)
@Function (name = "Protected regions", defaultValue = "true", configPath = "PlayerWarps.General.Support.ProtectedRegions", clazz = Boolean.class)
@Function (name = "BungeeCord", defaultValue = "true", configPath = "PlayerWarps.General.BungeeCord", clazz = Boolean.class)
@Function (name = "Economy", description = "Disables 'Time bound' when disabled.", defaultValue = "false", configPath = "PlayerWarps.General.Economy", clazz = Boolean.class)
@Function (name = "Time bound", description = "!! Already created player warps remain time bounded, please  clear PlayerWarp data after toggling this option !!", defaultValue = "true", configPath = "PlayerWarps.General.Time_Bound", clazz = Boolean.class, since = "v4.2.9")
@Function (name = "Force player head", defaultValue = "false", configPath = "PlayerWarps.General.Force_Player_Head", clazz = Boolean.class)
@Function (name = "Force create GUI", defaultValue = "false", configPath = "PlayerWarps.General.Force_Create_GUI", clazz = Boolean.class)
@Function (name = "Public as create state", defaultValue = "false", configPath = "PlayerWarps.General.Public_as_create_state", clazz = Boolean.class)
@Function (name = "Allow public warps", defaultValue = "true", configPath = "PlayerWarps.General.Allow_Public_Warps", clazz = Boolean.class)
@Function (name = "Allow trusted members", defaultValue = "true", configPath = "PlayerWarps.General.Allow_Trusted_Members", clazz = Boolean.class)
@Function (name = "Categories", defaultValue = "true", configPath = "PlayerWarps.General.Categories.Enabled", clazz = Boolean.class)
@Function (name = "Standard time value", defaultValue = "1h", configPath = "PlayerWarps.Time.Standard_Value", clazz = String.class)
@Function (name = "Min. time value", defaultValue = "0d, 0h, 5m", configPath = "PlayerWarps.Time.Min_Time", clazz = String.class)
@Function (name = "Max. time value", defaultValue = "30d, 0h, 0m", configPath = "PlayerWarps.Time.Max_Time", clazz = String.class)
@Function (name = "Default list page", since = "v5.0.0", defaultValue = "OWN_WARPS", description = "Values: OWN_WARPS, ALL_WARPS, ALL_PLAYERS, CLASSES §8(§cCASE-SENSITIVE!§8)", configPath = "PlayerWarps.General.Default_GUI_Page", clazz = String.class)
public abstract class PlayerWarpManager implements Manager, Ticker, ProxyFeature, Collectible {
    protected final HashMap<UUID, List<PlayerWarp>> warps = new HashMap<>();
    protected final HashMap<String, UUID> names = new HashMap<>();
    protected final List<Category> warpCategories = new ArrayList<>();
    protected final Set<String> nameBlacklist = new HashSet<>();
    protected final Set<String> worldBlacklist = new HashSet<>();
    protected int lastCountedPlayerWarpSize = 0;
    protected ConfigFile playerWarpsData = null;
    protected ConfigFile config = null;
    protected boolean bungeeCord;
    protected PlayerWarpListener listener;
    protected int maxAmount = 0;
    protected long minTime;
    protected long maxTime;
    protected double maxTeleportCosts;
    protected double teleportCosts;
    protected double nameChangeCosts;
    protected List<Long> inactiveReminds;
    protected boolean firstPublic; //true = the PW will be public when you open up the create gui
    protected double publicCosts;
    protected double messageCosts;
    protected long inactiveTime;
    protected double personalItemCosts;
    protected double descriptionCosts;
    protected double positionChangeCosts;
    protected double activeTimeCosts;
    protected double itemChangeCosts;
    protected int messageMinLength;
    protected int messageMaxLength;
    protected int descriptionLineMinLength;
    protected int descriptionLineMaxLength;
    protected int nameMinLength;
    protected int nameMaxLength;
    protected int descriptionMaxLines;
    protected double trustedMemberCosts;
    protected double personalItemRefund;
    protected double descriptionRefund;
    protected double messageRefund;
    protected double publicRefund;
    protected double teleportCostsRefund;
    protected double activeTimeRefund;
    protected double trustedMemberRefund;
    protected double createCosts;
    protected double editCosts;
    protected boolean naturalNumbers;
    protected boolean internalRefundFactor;
    protected boolean economy;
    protected boolean forcePlayerHead;
    protected boolean customTeleportCosts;
    protected boolean protectedRegions;
    protected int classesMin;
    protected int classesMax;
    protected boolean classes;
    protected long timeStandardValue;
    protected boolean forceCreateGUI;
    protected boolean allowPublicWarps;
    protected boolean allowTrustedMembers;
    protected boolean allowTeleportMessage;
    protected boolean allowDescription;
    protected boolean time;
    protected FilterType defaultPage;

    public PlayerWarpManager() {
        listener = new PlayerWarpListener();
    }

    public static PlayerWarpManager getManager() {
        return WarpSystem.getInstance().getDataManager().getManager(FeatureType.PLAYER_WARS);
    }

    public static boolean isWorldBlocked(String world) {
        return getManager().worldBlacklist.contains(world.toLowerCase());
    }

    public static boolean isProtected(Player player) {
        if (isWorldBlocked(player.getWorld().getName())) return true;
        if (WarpSystem.opt().forbiddenRegion(player.getLocation())) return true;

        if (!getManager().isProtectedRegions()) return false;

        return FakeBlockBreakEvent.tryFake(player);
    }

    public abstract boolean hasPermission(Player player);

    public abstract int getMaxAmount(Player player);

    @Override
    public void collectOptionStatistics(Map<String, Integer> entry) {
        if (classes) entry.put("Classes", 1);
        if (economy) entry.put("Economy", 1);

        if (bungeeCord) {
            if (WarpSystem.getInstance().isProxyConnected()) entry.put("BungeeCord", 1);
            else if (Bukkit.getOnlinePlayers().isEmpty()) entry.put("BungeeCord (empty server)", 1);
        }

        entry.put("Warps", 1);
    }

    @Override
    public void addCustomCarts(Metrics metrics) {
        metrics.addCustomChart(new SingleLineChart("playerwarp_usage", () -> {
            if (!bungeeCord || WarpSystem.getInstance().isProxyConnected()) {
                lastCountedPlayerWarpSize = 0;

                interactWithWarps(new Callback<PlayerWarp>() {
                    @Override
                    public void accept(PlayerWarp warp) {
                        WarpAction action = warp.getAction(Action.WARP);
                        if (action != null) {
                            GlobalLocationAdapter adapter = (GlobalLocationAdapter) action.getValue().getAdapter();

                            if (adapter != null) {
                                String s = adapter.getServer();

                                if (s == null || s.equals(WarpSystem.getInstance().getCurrentServer())) {
                                    lastCountedPlayerWarpSize++;
                                }
                            }
                        }
                    }
                });
            }

            return lastCountedPlayerWarpSize;
        }));
    }

    @Override
    public void preLoad() {
        new PlayerWarpTagConverter_v4_2_2();
    }

    public void sync(PlayerWarp old, PlayerWarp warp, Player connection) {
        if (!bungeeCord || !WarpSystem.getInstance().isProxyConnected()) return;

        if (warp.isSource()) {
            warp.setSource(false);
            SendPlayerWarpsPacket packet = new SendPlayerWarpsPacket(new ArrayList<PlayerWarpData>() {{
                add(warp.getData());
            }});
            packet.setClearable(true);
            WarpSystem.getDataHandler().send(packet, connection);
        } else sync(old.getData(), warp.getData(), connection);
    }

    public void sync(PlayerWarpData old, PlayerWarpData warp, Player connection) {
        if (!bungeeCord || !WarpSystem.getInstance().isProxyConnected()) return;
        PlayerWarpUpdate update = warp.diff(old);

        if (update.isEmpty()) return;

        WarpSystem.getDataHandler().send(new SendPlayerWarpUpdatePacket(update), connection);
        old.destroy();
        warp.destroy();
    }

    @Override
    public void onTick() {
    }

    @Override
    public void onSecond() {
        List<List<PlayerWarp>> mapCopy = new ArrayList<>(warps.values());
        for (List<PlayerWarp> value : mapCopy) {
            List<PlayerWarp> copy = new ArrayList<>(value);

            for (PlayerWarp warp : copy) {
                if (!warp.isTimeDependent() || warp.isBeingEdited()) continue;
                if (warp.isExpired()) {
                    if (-(warp.getExpireDate() - System.currentTimeMillis()) <= 1000) {
                        Player p = warp.getOwner().getPlayer();
                        if (p != null)
                            p.sendMessage(Lang.getPrefix() + Lang.get("Warp_expiring").replace("%NAME%", warp.getName()).replace("%TIME_LEFT%", StringFormatter.convertInTimeFormat(inactiveTime, 0, "", "")));
                        else
                            warp.setNotify(true);
                    }

                    for (Long remind : this.inactiveReminds) {
                        if (remind == inactiveTime) continue;

                        long time = -1000L * (inactiveTime - remind);
                        if (warp.getLeftTime() >= time - 1050L && warp.getLeftTime() < time) {
                            Player p = warp.getOwner().getPlayer();
                            if (p != null)
                                p.sendMessage(Lang.getPrefix() + Lang.get("Warp_Deletion_In").replace("%NAME%", warp.getName()).replace("%TIME_LEFT%", StringFormatter.convertInTimeFormat(remind, 0, "", "")));
                        }
                    }

                    Date inactive = new Date(warp.getExpireDate() + this.inactiveTime);

                    if (inactive.before(new Date())) {
                        //Delete
                        delete(warp, false, null);
                        warp.destroy();
                        Player player = warp.getOwner().getPlayer();
                        if (player != null) player.sendMessage(Lang.getPrefix() + Lang.get("Warp_was_deleted").replace("%NAME%", warp.getName()));
                    }
                }
            }

            copy.clear();
        }
        mapCopy.clear();
    }

    @Override
    public void destroy() {
        this.warps.clear();
        this.names.clear();
        this.warpCategories.clear();
    }

    public void checkPlayerWarpOwnerNames(Player player) {
        List<PlayerWarp> warps = new ArrayList<>(getOwnWarps(player));

        for (PlayerWarp warp : warps) {
            warp.getOwner().setName(player.getName());
        }

        warps.clear();
    }

    public List<PlayerWarp> filter(List<Category> classes, Player toTeleport) {
        List<PlayerWarp> warps = getWarps(toTeleport, true);

        for (int i = 0; i < warps.size(); i++) {
            PlayerWarp pw = warps.get(i);
            if (pw.isExpired()) continue;

            List<Category> categories = pw.getClasses();
            boolean match = false;

            for (Category c : classes) {
                for (Category cat : categories) {
                    if (c.equals(cat)) {
                        match = true;
                        break;
                    }
                }
            }

            if (!match) {
                warps.remove(i);
                i--;
            }
        }

        return warps;
    }

    public void updateWarp(PlayerWarp warp) {
        PlayerWarp w = getWarp(warp.getOwner().getId(), warp.getName());

        if (w == null) add(warp);
        else w.apply(warp);
    }

    public List<PlayerWarp> getPublicWarps() {
        List<PlayerWarp> warps = new ArrayList<>();
        for (List<PlayerWarp> value : this.warps.values()) {
            for (PlayerWarp warp : value) {
                if (warp.isPublic()) warps.add(warp);
            }
        }

        return warps;
    }

    public HashMap<UUID, List<PlayerWarp>> getWarps() {
        return warps;
    }

    public void interactWithWarps(Callback<PlayerWarp> interact) {
        List<List<PlayerWarp>> values = new ArrayList<>(warps.values());
        for (List<PlayerWarp> value : values) {
            List<PlayerWarp> warps = new ArrayList<>(value);

            for (PlayerWarp warp : warps) {
                interact.accept(warp);
            }

            warps.clear();
        }
        values.clear();
    }

    public Set<UUID> getUUIDs() {
        return this.warps.keySet();
    }

    public List<PlayerWarp> getOwnWarps(Player player) {
        return getOwnWarps(WarpSystem.getInstance().getPlayerDataManager().get(player));
    }

    public List<PlayerWarp> getOwnWarps(UUID id) {
        List<PlayerWarp> l = warps.get(id);
        return l == null ? new ArrayList<>() : l;
    }

    public String checkSymbols(String name, String highlighter, String reset) {
        StringBuilder finalName = new StringBuilder();
        String modifiedName = name;
        String lowerName = modifiedName.toLowerCase();

        for (String s : this.nameBlacklist) {
            s = s.toLowerCase();

            int first, last = 0, matches = 0;
            while ((first = lowerName.indexOf(s, last)) > -1) {
                last = first + 1;

                StringBuilder builder = new StringBuilder();
                int modFirst = first + matches * (highlighter.length() + reset.length());
                for (int i = 0; i < modifiedName.toCharArray().length; i++) {
                    if (i == modFirst) builder.append(highlighter);
                    builder.append(modifiedName.charAt(i));
                    if (i == modFirst + s.length() - 1) builder.append(reset);
                }

                modifiedName = builder.toString();
                matches++;
            }
        }

        Pattern p = Pattern.compile("[\\p{L}0-9\\p{Blank}_\\-'§]*");

        for (char c : modifiedName.toCharArray()) {
            Matcher m = p.matcher(c + "");
            if (!m.matches()) {
                finalName.append(highlighter).append(c).append(reset);
            } else finalName.append(c);
        }

        return finalName.toString().equals(name) ? null : finalName.toString();
    }

    /**
     * @param id         Owner of warps
     * @param toTeleport Player, who wants to see the warps
     * @return A list with usable PlayerWarps of id
     */
    public List<PlayerWarp> getUsableWarpsOf(UUID id, Player toTeleport) {
        List<PlayerWarp> warps = new ArrayList<>();

        for (PlayerWarp warp : getOwnWarps(id)) {
            if (warp.isExpired()) continue;
            if (warp.isOwner(toTeleport) || (allowPublicWarps && warp.isPublic()) || (allowTrustedMembers && warp.isTrusted(toTeleport))) warps.add(warp);
        }

        return warps;
    }

    public int getTrustedWarpAmountOf(UUID id, Player trustedPlayer) {
        int i = 0;

        for (PlayerWarp warp : getOwnWarps(id)) {
            if (warp.isOwner(trustedPlayer) || (allowPublicWarps && warp.isPublic()) || (allowTrustedMembers && warp.isTrusted(trustedPlayer))) i++;
        }

        return i;
    }

    public List<PlayerWarp> getWarps(Player player, boolean trusted) {
        if (!trusted) return getOwnWarps(player);
        List<PlayerWarp> warps = new ArrayList<>();

        for (List<PlayerWarp> ws : this.warps.values()) {
            for (PlayerWarp warp : ws) {
                if (warp.isOwner(player) || (allowPublicWarps && warp.isPublic()) || (allowTrustedMembers && warp.isTrusted(player))) warps.add(warp);
            }
        }

        return warps;
    }

    public List<PlayerWarp> getForeignAvailableWarps(Player player) {
        List<PlayerWarp> warps = new ArrayList<>();

        for (List<PlayerWarp> ws : this.warps.values()) {
            for (PlayerWarp warp : ws) {
                if (warp.isExpired()) continue;
                if (warp.isOwner(player) || (allowPublicWarps && warp.isPublic()) || (allowTrustedMembers && warp.isTrusted(player))) warps.add(warp);
            }
        }

        return warps;
    }

    public PlayerWarp getWarp(Player player, String name) {
        return getWarp(player, name, null);
    }

    //<player>.<name>
    //<name> « private warps haben vorrang
    public PlayerWarp getWarp(Player player, String name, PlayerWarp except) {
        name = name.replace(" ", "_");

        String[] a = name.split("\\.", -1);
        if (a.length > 2) {
            return null;
        }

        String prefer = a.length == 2 ? a[0] : null;
        name = a[a.length - 1];

        PlayerWarp searched = null;
        PlayerWarp pWarp = null; //private warp

        if (prefer == null) {
            List<PlayerWarp> warps = this.warps.get(WarpSystem.getInstance().getPlayerDataManager().get(player));

            if (warps != null) {
                warps = new ArrayList<>(warps);

                for (PlayerWarp warp : warps) {
                    if (warp.equals(except)) continue;
                    if (warp.equalsName(name)) {
                        searched = warp;
                        break;
                    }
                }

                warps.clear();
                if (searched != null) return searched;
            }

            List<List<PlayerWarp>> lists = new ArrayList<>(this.warps.values());
            for (List<PlayerWarp> list : lists) {
                warps = new ArrayList<>(list);

                for (PlayerWarp warp : warps) {
                    if (warp.equals(except)) continue;
                    if (warp.equalsName(name)) {
                        if (warp.canTeleport(player)) {
                            searched = warp;
                            break;
                        } else pWarp = warp;
                    }
                }

                warps.clear();
            }
            lists.clear();
        } else {
            UUID id = names.get(prefer);
            if (id != null) {
                List<PlayerWarp> warps = this.warps.get(id);

                if (warps != null) {
                    warps = new ArrayList<>(warps);

                    for (PlayerWarp warp : warps) {
                        if (warp.equals(except)) continue;
                        if (warp.equalsName(name)) {
                            searched = warp;
                            break;
                        }
                    }

                    warps.clear();
                }
            }
        }

        return searched == null ? pWarp : searched;
    }

    public PlayerWarp getWarp(UUID id, String name) {
        for (PlayerWarp w : getOwnWarps(id)) {
            if (w.equalsName(name)) return w;
        }

        return null;
    }

    public boolean exists(Player player, String name) {
        return exists(player, name, null);
    }

    public boolean existsOwn(Player player, String name) {
        return existsOwn(player, name, null);
    }

    public void add(PlayerWarp warp) {
        List<PlayerWarp> warps = getOwnWarps(warp.getOwner().getId());
        if (getWarp(warp.getOwner().getId(), warp.getName()) != null) return;

        warp.setName(getCopiedName(warps, warp.getName()));
        warps.add(warp);

        if (warp.getStarted() == 0) {
            warp.setStarted(System.currentTimeMillis());
            warp.born();
        }

        names.putIfAbsent(warp.getOwner().getName(), warp.getOwner().getId());
        this.warps.putIfAbsent(warp.getOwner().getId(), warps);
    }

    private String getCopiedName(List<PlayerWarp> list, String name) {
        int num = 0;
        boolean found;

        name = name.replace(" ", "_");

        do {
            found = false;
            if (num == 0) num++;
            else {
                name = name.replaceAll("_\\([0-9]{1,5}?\\)\\z", "");
                name += "_(" + num++ + ")";
            }

            for (PlayerWarp d : list) {
                String nameWithoutColor = net.md_5.bungee.api.ChatColor.stripColor(net.md_5.bungee.api.ChatColor.translateAlternateColorCodes('&', name));
                String dName = net.md_5.bungee.api.ChatColor.stripColor(net.md_5.bungee.api.ChatColor.translateAlternateColorCodes('&', d.getName())).replace(" ", "_");
                if (dName.equalsIgnoreCase(nameWithoutColor)) {
                    found = true;
                    break;
                }
            }
        } while (found);

        return name;
    }

    public double delete(PlayerWarp warp, boolean informBungee, @Nullable Player connection) {
        if (warp == null) return 0;
        List<PlayerWarp> warps = getOwnWarps(warp.getOwner().getId());
        double refund = warps.remove(warp) ? calculateRefund(warp) : -1;

        if (warps.isEmpty()) {
            this.warps.remove(warp.getOwner().getId());
            this.names.remove(warp.getOwner().getName());
        }

        if (informBungee && checkBungeeCord()) {
            DeletePlayerWarpPacket packet = new DeletePlayerWarpPacket(warp.getName(), warp.getOwner().getId());
            WarpSystem.getDataHandler().send(packet, connection);
        }

        return refund;
    }

    public void updateGUIs() {
        for (PWList gui : API.getRemovables(PWList.class)) {
            gui.updateList();
        }
    }

    public double calculateRefund(PlayerWarp warp) {
        double refund = 0;
        if (warp == null) return -1;
        if (!isEconomy()) return 0;

        //personal item
        if (!warp.isStandardItem()) refund += PlayerWarpManager.getManager().getItemCosts() * PlayerWarpManager.getManager().getPersonalItemRefund() * warp.getRefundFactor();

        //description
        int length = 0;

        if (warp.getItem().getLore() != null)
            for (String s : warp.getItem().getLore()) {
                length += s.replaceFirst("§f", "").length();
            }

        if (length > 0) refund += length * PlayerWarpManager.getManager().getDescriptionCosts() * PlayerWarpManager.getManager().getDescriptionRefund() * warp.getRefundFactor();

        //teleport message
        length = warp.getTeleportMessage() == null ? 0 : warp.getTeleportMessage().length();

        if (length > 0) refund += length * PlayerWarpManager.getManager().getMessageCosts() * PlayerWarpManager.getManager().getMessageRefund() * warp.getRefundFactor();

        //public state
        if (warp.isPublic()) refund += PlayerWarpManager.getManager().getPublicCosts() * PlayerWarpManager.getManager().getPublicRefund() * warp.getRefundFactor();

        //teleport costs
        double tpCosts = warp.getTeleportCosts();
        if (tpCosts > 0) refund += tpCosts * PlayerWarpManager.getManager().getTeleportCosts() * PlayerWarpManager.getManager().getTeleportCostsRefund() * warp.getRefundFactor();

        //active time
        refund += (warp.getLeftTime() / 60000D) * PlayerWarpManager.getManager().getActiveTimeCosts() * PlayerWarpManager.getManager().getActiveTimeRefund() * warp.getRefundFactor();

        //trusted members
        length = warp.getTrusted().size();
        if (length > 0) refund += length * PlayerWarpManager.getManager().getTrustedMemberCosts() * PlayerWarpManager.getManager().getTrustedMemberRefund() * warp.getRefundFactor();

        if (isNaturalNumbers()) return Math.ceil(refund);
        return refund;
    }

    public boolean exists(Player player, String name, PlayerWarp except) {
        return getWarp(player, name, except) != null;
    }

    public boolean existsOwn(Player player, String name, PlayerWarp except) {
        String[] a = name.split("\\.", -1);
        return exists(player, player.getName() + "." + a[a.length - 1], except);
    }

    public long getMinTime() {
        return minTime;
    }

    public long getMaxTime() {
        return maxTime;
    }

    public double getMaxTeleportCosts() {
        return maxTeleportCosts;
    }

    public double getTeleportCosts() {
        return teleportCosts;
    }

    public double getNameChangeCosts() {
        return nameChangeCosts;
    }

    public boolean isFirstPublic() {
        return firstPublic;
    }

    public double getPublicCosts() {
        return publicCosts;
    }

    public double getMessageCosts() {
        return messageCosts;
    }

    public double getItemCosts() {
        return personalItemCosts;
    }

    public double getDescriptionCosts() {
        return descriptionCosts;
    }

    public double getPositionChangeCosts() {
        return positionChangeCosts;
    }

    public double getActiveTimeCosts() {
        return activeTimeCosts;
    }

    public double getItemChangeCosts() {
        return itemChangeCosts;
    }

    public int getMessageMinLength() {
        return messageMinLength;
    }

    public int getMessageMaxLength() {
        return messageMaxLength;
    }

    public int getDescriptionLineMinLength() {
        return descriptionLineMinLength;
    }

    public int getDescriptionLineMaxLength() {
        return descriptionLineMaxLength;
    }

    public int getDescriptionMaxLines() {
        return descriptionMaxLines;
    }

    public double getTrustedMemberCosts() {
        return trustedMemberCosts;
    }

    public double getPersonalItemRefund() {
        return personalItemRefund;
    }

    public double getDescriptionRefund() {
        return descriptionRefund;
    }

    public double getMessageRefund() {
        return messageRefund;
    }

    public double getPublicRefund() {
        return publicRefund;
    }

    public double getTeleportCostsRefund() {
        return teleportCostsRefund;
    }

    public double getActiveTimeRefund() {
        return activeTimeRefund;
    }

    public double getTrustedMemberRefund() {
        return trustedMemberRefund;
    }

    public double getCreateCosts() {
        return createCosts;
    }

    public double getEditCosts() {
        return editCosts;
    }

    public boolean isNaturalNumbers() {
        return naturalNumbers;
    }

    public boolean isInternalRefundFactor() {
        return internalRefundFactor;
    }

    public boolean isEconomy() {
        return economy;
    }

    public boolean isForcePlayerHead() {
        return forcePlayerHead;
    }

    public List<Category> getWarpClasses() {
        return warpCategories;
    }

    public Category getWarpClass(String name) {
        for (Category c : this.warpCategories) {
            if (ChatColor.stripColor(c.getName()).equals(ChatColor.stripColor(name))) return c;
        }

        return null;
    }

    public Category getWarpClass(int id) {
        for (Category c : this.warpCategories) {
            if (c.getId() == id) return c;
        }

        return null;
    }

    public boolean isCustomTeleportCosts() {
        return customTeleportCosts;
    }

    public int getClassesMin() {
        return classesMin;
    }

    public int getClassesMax() {
        return classesMax;
    }

    public boolean isClasses() {
        return classes;
    }

    public boolean isBungeeCord() {
        return bungeeCord;
    }

    public boolean checkBungeeCord() {
        return isBungeeCord() && WarpSystem.getInstance().isProxyConnected();
    }

    public long getInactiveTime() {
        return inactiveTime;
    }

    public void setInactiveTime(long inactiveTime) {
        this.inactiveTime = inactiveTime;
    }

    public boolean isProtectedRegions() {
        return protectedRegions;
    }

    public Set<String> getNameBlacklist() {
        return nameBlacklist;
    }

    public int getNameMinLength() {
        return nameMinLength;
    }

    public int getNameMaxLength() {
        return nameMaxLength;
    }

    public long getTimeStandardValue() {
        return isEconomy() ? timeStandardValue : 0;
    }

    public boolean isForceCreateGUI() {
        return forceCreateGUI;
    }

    public boolean isAllowPublicWarps() {
        return allowPublicWarps;
    }

    public boolean isAllowTrustedMembers() {
        return allowTrustedMembers;
    }

    public Set<String> getWorldBlacklist() {
        return worldBlacklist;
    }

    public boolean isAllowTeleportMessage() {
        return allowTeleportMessage;
    }

    public boolean isAllowDescription() {
        return allowDescription;
    }

    public boolean isTime() {
        return time;
    }

    public FilterType getDefaultPage() {
        return defaultPage;
    }
}
