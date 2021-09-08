package de.codingair.warpsystem.spigot.versionfactory.handlers;

import de.codingair.codingapi.API;
import de.codingair.codingapi.tools.io.ConfigMask;
import de.codingair.codingapi.tools.io.JSON.JSON;
import de.codingair.codingapi.tools.io.lib.JSONArray;
import de.codingair.codingapi.tools.items.ItemBuilder;
import de.codingair.codingapi.tools.items.XMaterial;
import de.codingair.warpsystem.core.transfer.packets.general.SendPlayerWarpsPacket;
import de.codingair.warpsystem.core.transfer.packets.spigot.MoveLocalPlayerWarpsPacket;
import de.codingair.warpsystem.core.transfer.packets.spigot.RegisterServerForPlayerWarpsPacket;
import de.codingair.warpsystem.core.transfer.packets.spigot.utils.PlayerWarpData;
import de.codingair.warpsystem.spigot.api.StringFormatter;
import de.codingair.warpsystem.spigot.base.WarpSystem;
import de.codingair.warpsystem.spigot.base.utils.Permissions;
import de.codingair.warpsystem.spigot.features.playerwarps.commands.CPlayerWarp;
import de.codingair.warpsystem.spigot.features.playerwarps.commands.CPlayerWarpReference;
import de.codingair.warpsystem.spigot.features.playerwarps.commands.CPlayerWarps;
import de.codingair.warpsystem.spigot.features.playerwarps.guis.list.FilterType;
import de.codingair.warpsystem.spigot.features.playerwarps.managers.PlayerWarpManager;
import de.codingair.warpsystem.spigot.features.playerwarps.utils.Category;
import de.codingair.warpsystem.spigot.features.playerwarps.utils.PlayerWarp;
import de.codingair.warpsystem.spigot.features.playerwarps.utils.tempwarps.TempWarpAdapter;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PlayerWarpHandler extends PlayerWarpManager {
    @Override
    public boolean hasPermission(Player player) {
        if (player.isOp()) return true;

        int warps = de.codingair.warpsystem.spigot.features.playerwarps.managers.PlayerWarpManager.getManager().getOwnWarps(player).size();
        int maxAmount = getMaxAmount(player);

        return maxAmount == -1 || warps < maxAmount;
    }

    @Override
    public int getMaxAmount(Player player) {
        if (player.isOp()) return -1;

        if (Permissions.PERMISSION_USE_PLAYER_WARPS != null) {
            int amount = 0;
            for (PermissionAttachmentInfo effectivePermission : player.getEffectivePermissions()) {
                if (!effectivePermission.getValue()) continue;
                String perm = effectivePermission.getPermission();

                if (perm.equals("*") || perm.equalsIgnoreCase("warpsystem.*")) return -1;
                if (perm.toLowerCase().startsWith("warpsystem.playerwarps.")) {
                    String s = perm.substring(23);
                    if (s.equals("*") || s.equalsIgnoreCase("n")) return -1;

                    try {
                        int i = Integer.parseInt(s);
                        if (i > amount) amount = i;
                    } catch (Throwable ignored) {
                    }
                }
            }
            return amount;
        } else return maxAmount;
    }

    @Override
    public boolean load(boolean loader) {
        this.warps.clear();
        this.warpCategories.clear();
        this.nameBlacklist.clear();
        this.worldBlacklist.clear();

        this.playerWarpsData = WarpSystem.getInstance().getFileManager().loadFile("PlayerWarps", "/Memory/");
        this.config = WarpSystem.getInstance().getFileManager().loadFile("PlayerWarpConfig", "/");
        FileConfiguration config = this.config.getConfig();

        int size = 0;

        this.bungeeCord = config.getBoolean("PlayerWarps.General.BungeeCord", true);
        this.economy = config.getBoolean("PlayerWarps.General.Economy", true);
        WarpSystem.log("  > Loading PlayerWarps [Bungee: " + bungeeCord + "; TimeDependent: " + economy + "]");

        // Timings
        this.minTime = StringFormatter.convertFromTimeFormat(config.getString("PlayerWarps.Time.Min_Time", null), 300000);
        this.maxTime = StringFormatter.convertFromTimeFormat(config.getString("PlayerWarps.Time.Max_Time", null), 2592000000L);

        List<String> reminds = config.getStringList("Inactive.Reminds");
        this.inactiveReminds = new ArrayList<>();

        for (String data : reminds) {
            long time = StringFormatter.convertFromTimeFormat(data);
            if (time > 0) inactiveReminds.add(time);
        }

        this.inactiveTime = StringFormatter.convertFromTimeFormat(config.getString("PlayerWarps.Inactive.Time_After_Expiration", null), 2592000000L);

        //Costs - Generally
        this.maxAmount = config.getInt("PlayerWarps.General.Max_Warp_Amount", 5);
        this.protectedRegions = config.getBoolean("PlayerWarps.General.Support.ProtectedRegions", true);
        this.nameBlacklist.addAll(config.getStringList("PlayerWarps.General.Name_Blacklist"));
        this.worldBlacklist.addAll(config.getStringList("PlayerWarps.General.World_Blacklist"));
        this.createCosts = config.getDouble("PlayerWarps.Costs.Create", 200);
        this.editCosts = config.getDouble("PlayerWarps.Costs.Edit", 200);
        this.naturalNumbers = config.getBoolean("PlayerWarps.Costs.Round_costs_to_natural_numbers", false);
        this.internalRefundFactor = config.getBoolean("PlayerWarps.Costs.Internal_Refund_Factor", false);
        this.forcePlayerHead = config.getBoolean("PlayerWarps.General.Force_Player_Head", false);
        this.customTeleportCosts = config.getBoolean("PlayerWarps.General.Custom_teleport_costs", true);
        this.timeStandardValue = StringFormatter.convertFromTimeFormat(config.getString("PlayerWarps.Time.Standard_Value", "1h"));
        this.forceCreateGUI = config.getBoolean("PlayerWarps.General.Force_Create_GUI", false);
        this.allowPublicWarps = config.getBoolean("PlayerWarps.General.Allow_Public_Warps", true);
        this.allowTrustedMembers = config.getBoolean("PlayerWarps.General.Allow_Trusted_Members", true);
        this.allowTeleportMessage = config.getBoolean("PlayerWarps.General.Allow_Teleport_Messages", true);
        this.allowDescription = config.getBoolean("PlayerWarps.General.Allow_Description", true);
        this.time = economy && config.getBoolean("PlayerWarps.General.Time_Bound", true);
        this.defaultPage = FilterType.checkName(config.getString("PlayerWarps.General.Default_GUI_Page", "OWN_WARPS"));
        config.set("PlayerWarps.General.Default_GUI_Page", this.defaultPage.name()); //replace mistakes
        this.config.saveConfig();

        //Costs - Editing
        this.nameChangeCosts = config.getDouble("PlayerWarps.Costs.Editing.Name", 400);
        this.positionChangeCosts = config.getDouble("PlayerWarps.Costs.Editing.Target_Position", 200);
        this.itemChangeCosts = config.getDouble("PlayerWarps.Costs.Editing.Personal_Item", 100);

        //Costs - Fields
        this.personalItemCosts = config.getDouble("PlayerWarps.Costs.Personal_Item", 200);
        this.messageCosts = config.getDouble("PlayerWarps.Costs.Text.Teleport_Message", 2);
        this.descriptionCosts = config.getDouble("PlayerWarps.Costs.Text.Warp_Description", 2);

        this.publicCosts = config.getDouble("PlayerWarps.Costs.PublicWarp", 100);
        this.activeTimeCosts = config.getDouble("PlayerWarps.Costs.Active_Time", 0.5);

        //Teleport costs
        this.teleportCosts = config.getDouble("PlayerWarps.Costs.Teleport_Fee", 25);
        this.maxTeleportCosts = config.getDouble("PlayerWarps.Teleport_Fee.Max", 500);

        //Teleport message
        this.messageMinLength = config.getInt("PlayerWarps.Teleport_Message.Length.Min", 5);
        this.messageMaxLength = config.getInt("PlayerWarps.Teleport_Message.Length.Max", 50);

        //Description
        this.descriptionLineMinLength = config.getInt("PlayerWarps.Warp_Description.Line_Length.Min", 5);
        this.descriptionLineMaxLength = config.getInt("PlayerWarps.Warp_Description.Line_Length.Max", 25);
        this.descriptionMaxLines = config.getInt("PlayerWarps.Warp_Description.Max_Lines", 3);

        //Name
        this.nameMinLength = config.getInt("PlayerWarps.Name_Length.Min", 3);
        this.nameMaxLength = config.getInt("PlayerWarps.Name_Length.Max", 20);

        //generally
        this.firstPublic = config.getBoolean("PlayerWarps.General.Public_as_create_state", false);
        this.trustedMemberCosts = config.getDouble("PlayerWarps.Costs.Trusted_Member", 50);

        //refund
        this.personalItemRefund = config.getDouble("PlayerWarps.Refunds.Personal_Item", 0.5);
        this.descriptionRefund = config.getDouble("PlayerWarps.Refunds.Warp_Description", 0.5);
        this.messageRefund = config.getDouble("PlayerWarps.Refunds.Teleport_Message", 0.5);
        this.publicRefund = config.getDouble("PlayerWarps.Refunds.PublicWarp", 0.5);
        this.teleportCostsRefund = config.getDouble("PlayerWarps.Refunds.Teleport_Fee", 0.5);
        this.activeTimeRefund = config.getDouble("PlayerWarps.Refunds.Active_Time", 1);
        this.trustedMemberRefund = config.getDouble("PlayerWarps.Refunds.Trusted_Member", 0.5);

        //Classes
        this.classes = config.getBoolean("PlayerWarps.General.Categories.Enabled", true);
        this.classesMin = config.getInt("PlayerWarps.General.Categories.Min", 1);
        this.classesMax = config.getInt("PlayerWarps.General.Categories.Max", 2);

        List<?> l = config.getList("PlayerWarps.General.Categories.Classes");
        if (l != null)
            for (Object o : l) {
                if (o instanceof Map) {
                    JSON json = new JSON((Map<?, ?>) o);
                    Category c = new Category();
                    try {
                        c.read(json);
                        this.warpCategories.add(c);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

        //loading PlayerWarps
        List<?> data = playerWarpsData.getConfig().getList("PlayerWarps");
        if (data != null)
            for (Object o : data) {
                JSON json = new JSON((Map<?, ?>) o);
                PlayerWarp p = new PlayerWarp();

                try {
                    p.read(json);
                    add(p);
                    size++;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        List<PlayerWarp> imported = TempWarpAdapter.convertTempWarps(true);
        for (PlayerWarp playerWarp : imported) {
            add(playerWarp);
        }

        new CPlayerWarp(config.getStringList("PlayerWarps.General.PlayerWarp_Command_Aliases")).register();
        new CPlayerWarps(config.getStringList("PlayerWarps.General.PlayerWarps_Command_Aliases")).register();

        List<String> aliases = config.getStringList("PlayerWarps.General.Command_References");
        if (!aliases.isEmpty()) new CPlayerWarpReference(aliases.remove(0), aliases.toArray(new String[0])).register();

        WarpSystem.log("    ...got " + warpCategories.size() + " Class(es)");
        if (!imported.isEmpty()) WarpSystem.log("    ...got " + imported.size() + " imported TempWarp(s)");
        imported.clear();

        if (!bungeeCord) WarpSystem.log("    ...got " + size + " PlayerWarp(s)");
        if (economy && time) API.addTicker(this);

        WarpSystem.getInstance().getProxyFeatureList().add(this);
        Bukkit.getPluginManager().registerEvents(this.listener, WarpSystem.getInstance());

        return true;
    }

    @Override
    public void save(boolean saver) {
        if (!saver) WarpSystem.log("  > Saving PlayerWarps...");
        playerWarpsData.clearConfig();

        JSONArray a = null;
        if (!bungeeCord || !WarpSystem.getInstance().isProxyConnected()) {
            a = new JSONArray();

            for (List<PlayerWarp> data : this.warps.values()) {
                for (PlayerWarp w : data) {
                    JSON json = new JSON();
                    w.write(json);
                    a.add(json);
                }
            }
            playerWarpsData.getConfig().set("PlayerWarps", a);
            playerWarpsData.saveConfig();
        } else if (!saver) WarpSystem.log("    ...skipping PlayerWarp(s) > Saved on BungeeCord");

        if (warpCategories.isEmpty()) {
            this.warpCategories.add(new Category(new ItemBuilder(XMaterial.EMERALD), "&a&lShop", 1, new ArrayList<String>() {{
                add("&7This class marks a warp");
                add("&7as a &aShop&7!");
            }}));

            this.warpCategories.add(new Category(new ItemBuilder(XMaterial.OAK_DOOR), "&c&lHome", 2, new ArrayList<String>() {{
                add("&7This class marks a warp");
                add("&7as a &cHome&7!");
            }}));

            this.warpCategories.add(new Category(new ItemBuilder(XMaterial.FARMLAND), "&9&lFarm", 3, new ArrayList<String>() {{
                add("&7This class marks a warp");
                add("&7as a &9Farm&7!");
            }}));

            this.warpCategories.add(new Category(new ItemBuilder(XMaterial.IRON_SWORD), "&e&lPvP-Zone", 4, new ArrayList<String>() {{
                add("&7This class marks a warp");
                add("&7as a &ePvP-Zone&7!");
            }}));

            this.warpCategories.add(new Category(new ItemBuilder(XMaterial.BOW), "&b&lHunting-Area", 5, new ArrayList<String>() {{
                add("&7This class marks a warp");
                add("&7as a &bHunting-Area&7!");
            }}));

            this.warpCategories.add(new Category(new ItemBuilder(XMaterial.ENDER_EYE), "&3&lMiscellaneous", 6, new ArrayList<String>() {{
                add("&7This class marks a warp");
                add("&7as a &3miscellaneous &7warp!");
            }}));

            JSONArray array = new JSONArray();
            for (Category c : this.warpCategories) {
                JSON json = new JSON();
                c.write(json);
                array.add(json);
            }

            config.loadConfig();
            ConfigMask writer = new ConfigMask(config);
            List<?> l = writer.getList("PlayerWarps.General.Categories.Classes");

            if (l.isEmpty()) {
                //still empty?
                writer.put("PlayerWarps.General.Categories.Classes", array);
                config.saveConfig();
            }
        }

        if (!saver && a != null) WarpSystem.log("    ...saved " + a.size() + " PlayerWarp(s)");
    }

    @Override
    public void onConnect(Player connection) {
        if (bungeeCord) {
            if (!getWarps().isEmpty()) {
                List<List<PlayerWarpData>> uploads = new ArrayList<>();

                List<PlayerWarpData> l = new ArrayList<>();
                for (List<PlayerWarp> value : getWarps().values()) {
                    for (PlayerWarp w : value) {
                        l.add(w.getData());

                        if (l.size() == 100) {
                            uploads.add(new ArrayList<>(l));
                            l.clear();
                        }
                    }
                }

                if (!l.isEmpty()) uploads.add(l);

                for (List<PlayerWarpData> upload : uploads) {
                    SendPlayerWarpsPacket p = new SendPlayerWarpsPacket(upload);
                    p.setClearable(true);
                    WarpSystem.getDataHandler().send(p, connection);
                }

                uploads.clear();
            }

            WarpSystem.getDataHandler().send(new RegisterServerForPlayerWarpsPacket(isEconomy()), connection);
        } else WarpSystem.getDataHandler().send(new MoveLocalPlayerWarpsPacket(), connection);
    }

    @Override
    public void onDisconnect() {
        if (bungeeCord) {
            for (List<PlayerWarp> value : this.warps.values()) {
                value.clear();
            }
            this.warps.clear();
        }
    }
}
