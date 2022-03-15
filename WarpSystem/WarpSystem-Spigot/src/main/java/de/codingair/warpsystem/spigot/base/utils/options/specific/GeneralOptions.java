package de.codingair.warpsystem.spigot.base.utils.options.specific;

import de.codingair.warpsystem.core.transfer.packets.spigot.utils.ServerPing;
import de.codingair.warpsystem.spigot.api.StringFormatter;
import de.codingair.warpsystem.spigot.api.worldguard.WorldGuardHelper;
import de.codingair.warpsystem.spigot.base.WarpSystem;
import de.codingair.warpsystem.spigot.base.utils.options.Option;
import de.codingair.warpsystem.spigot.base.utils.options.Options;
import de.codingair.warpsystem.spigot.base.utils.teleport.Origin;
import de.codingair.warpsystem.spigot.base.utils.teleport.process.TeleportDelay;
import org.bukkit.ChatColor;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntPredicate;
import java.util.stream.Stream;

public class GeneralOptions extends Options {
    private Option<String> lang = new Option<>("WarpSystem.Language", "ENG");
    private Option<Integer> teleportDelay = new Option<>("WarpSystem.Teleport.Delay");
    private Option<Boolean> allowMove = new Option<>("WarpSystem.Teleport.Allow_Move");
    private Option<Boolean> afterEffects = new Option<>("WarpSystem.Teleport.Animation_After_Teleport.Enabled");
    private Option<Boolean> publicAnimations = new Option<>("WarpSystem.Teleport.Public_Animations");
    private Option<String> cmdSugColor = new Option<>("WarpSystem.Command_Suggestions.Color", "&7");
    private Option<String> cmdArgColor = new Option<>("WarpSystem.Command_Suggestions.Argument", "&e");
    private Option<String> cooldownTpa = new Option<>("WarpSystem.Cooldown.Tpa", "5m");
    private Option<String> cooldownBack = new Option<>("WarpSystem.Cooldown.Back", "0s");
    private Option<String> cooldownRandomTP = new Option<>("WarpSystem.Cooldown.RandomTP", "5m");
    private Option<String> delayDisplay = new Option<>("WarpSystem.Teleport.Delay_Display", "ACTION_BAR");
    private Option<Boolean> teleportInterceptions = new Option<>("WarpSystem.Beta.Functions.Teleport_Interceptions", false);
    private Option<Integer> fetchUpdates = new Option<>("WarpSystem.Proxy.Fetch_Updated_Jars", 1);
    private Option<String> placeholderOnline = new Option<>("WarpSystem.Proxy.Placeholder.Online", ".sc.Online");
    private Option<String> placeholderOffline = new Option<>("WarpSystem.Proxy.Placeholder.Offline", ".sc.Offline");
    private Option<String> placeholderCountInfo = new Option<>("WarpSystem.Proxy.Placeholder.Count_Info", ".cc..p.&8/.cc..mp.");
    private Option<String> placeholderColorsOnline = new Option<>("WarpSystem.Proxy.Placeholder.Colors.Online", "&a");
    private Option<String> placeholderColorsOffline = new Option<>("WarpSystem.Proxy.Placeholder.Colors.Offline", "&c");
    private Option<String> placeholderColorsFull = new Option<>("WarpSystem.Proxy.Placeholder.Colors.Full", "&c");
    private Option<String> placeholderColorsNotFull = new Option<>("WarpSystem.Proxy.Placeholder.Colors.Not_Full", "&a");
    private Option<List<String>> forbiddenRegions = new Option<>("WarpSystem.Teleport.Forbidden_Regions", new ArrayList<>());
    private Option<Boolean> safeTp = new Option<>("WarpSystem.Teleport.Safe_TP", false);

    public GeneralOptions() {
        super("Config");
    }

    public GeneralOptions(GeneralOptions options) {
        super(options.getFile());
        apply(options);
    }

    @Override
    public void write() {
        set(lang);
        set(teleportDelay);
        set(allowMove);
        set(afterEffects);
        set(publicAnimations);
        set(cmdSugColor);
        set(cmdArgColor);
        set(cooldownTpa);
        set(cooldownBack);
        set(cooldownRandomTP);
        set(delayDisplay);
        set(teleportInterceptions);
        set(fetchUpdates);
        set(placeholderOnline);
        set(placeholderOffline);
        set(placeholderCountInfo);
        set(placeholderColorsOnline);
        set(placeholderColorsOffline);
        set(placeholderColorsFull);
        set(placeholderColorsNotFull);
        set(forbiddenRegions);
        set(safeTp);
        save();
    }

    @Override
    public void read() {
        get(lang);
        get(teleportDelay);
        get(allowMove);
        get(afterEffects);
        get(publicAnimations);
        get(cmdSugColor);
        get(cmdArgColor);
        get(cooldownTpa);
        get(cooldownBack);
        get(cooldownRandomTP);
        get(delayDisplay);
        get(teleportInterceptions);
        get(fetchUpdates);
        get(placeholderOnline);
        get(placeholderOffline);
        get(placeholderCountInfo);
        get(placeholderColorsOnline);
        get(placeholderColorsOffline);
        get(placeholderColorsFull);
        get(placeholderColorsNotFull);
        get(forbiddenRegions);
        get(safeTp);

        if (fetchUpdates.getValue() < 0 || fetchUpdates.getValue() > 2) fetchUpdates.setValue(1);
        if (System.getProperty("os.name").toLowerCase().contains("win") || System.getProperty("os.name").toLowerCase().contains("mac"))
            fetchUpdates.setValue(0);   //file-system does not allow to delete active files.

        IntPredicate test = new IntPredicate() {
            private boolean color = false;

            @Override
            public boolean test(int value) {
                char c = (char) value;

                if (color) color = false;
                else if (c == '&') color = true;
                else return false;

                return true;
            }
        };

        StringBuilder sb = new StringBuilder();
        for (int c : cmdSugColor.getValue().trim().chars().filter(test).toArray()) {
            sb.append((char) c);
        }
        cmdSugColor.setValue(sb.toString());

        sb = new StringBuilder();
        for (int c : cmdArgColor.getValue().trim().chars().filter(test).toArray()) {
            sb.append((char) c);
        }
        cmdArgColor.setValue(sb.toString());
    }

    @Override
    public void apply(Options options) {
        if (options instanceof GeneralOptions) {
            GeneralOptions o = (GeneralOptions) options;

            this.lang = o.lang.clone();
            this.teleportDelay = o.teleportDelay.clone();
            this.allowMove = o.allowMove.clone();
            this.afterEffects = o.afterEffects.clone();
            this.publicAnimations = o.publicAnimations.clone();
            this.cmdSugColor = o.cmdSugColor.clone();
            this.cmdArgColor = o.cmdArgColor.clone();
            this.cooldownTpa = o.cooldownTpa.clone();
            this.cooldownBack = o.cooldownBack.clone();
            this.cooldownRandomTP = o.cooldownRandomTP.clone();
            this.delayDisplay = o.delayDisplay.clone();
            this.teleportInterceptions = o.teleportInterceptions.clone();
            this.fetchUpdates = o.fetchUpdates.clone();
            this.placeholderOnline = o.placeholderOnline.clone();
            this.placeholderOffline = o.placeholderOffline.clone();
            this.placeholderCountInfo = o.placeholderCountInfo.clone();
            this.placeholderColorsOnline = o.placeholderColorsOnline.clone();
            this.placeholderColorsOffline = o.placeholderColorsOffline.clone();
            this.placeholderColorsFull = o.placeholderColorsFull.clone();
            this.placeholderColorsNotFull = o.placeholderColorsNotFull.clone();
            this.forbiddenRegions = o.forbiddenRegions.clone();
            this.safeTp = o.safeTp.clone();
        }
    }

    @Override
    public Options clone() {
        return new GeneralOptions(this);
    }

    public String getLang() {
        return lang.getValue();
    }

    public void setLang(String lang) {
        this.lang.setValue(lang);
    }

    public int getTeleportDelay() {
        return teleportDelay.getValue();
    }

    public boolean isAllowMove() {
        return allowMove.getValue();
    }

    public boolean isAfterEffects() {
        return afterEffects.getValue();
    }

    public boolean isPublicAnimations() {
        return publicAnimations.getValue();
    }

    public String cmdSug() {
        return ChatColor.translateAlternateColorCodes('&', cmdSugColor.getValue());
    }

    public String cmdArg() {
        return ChatColor.translateAlternateColorCodes('&', cmdArgColor.getValue());
    }

    public TeleportDelay.Display getDelayDisplay() {
        try {
            return TeleportDelay.Display.valueOf(delayDisplay.getValue());
        } catch (Exception ex) {
            return TeleportDelay.Display.ACTION_BAR;
        }
    }

    public long getCooldownTpa() {
        return StringFormatter.convertFromTimeFormat(cooldownTpa.getValue(), 300000);
    }

    public long getCooldownBack() {
        return StringFormatter.convertFromTimeFormat(cooldownBack.getValue(), 0);
    }

    public long getCooldownRandomTP() {
        return StringFormatter.convertFromTimeFormat(cooldownRandomTP.getValue(), 300000);
    }

    public long getCooldown(Origin origin) {
        if (origin == Origin.TeleportRequest) return getCooldownTpa();
        else if (origin == Origin.TeleportCommand) return getCooldownBack();
        else if (origin == Origin.RandomTP) return getCooldownRandomTP();
        return 0;
    }

    public boolean isTeleportInterceptions() {
        return teleportInterceptions.getValue();
    }

    public int getFetchUpdateOption() {
        return fetchUpdates.getValue();
    }

    public String getStatus(ServerPing ping) {
        if (ping != null && ping.getStatus()) {
            return getPlaceholderOnline(ping);
        } else {
            return getPlaceholderOffline();
        }
    }

    private String getPlaceholderOnline(ServerPing ping) {
        String s = prepareServerColorString(ping, placeholderOnline.getValue());

        return s == null ? null : de.codingair.codingapi.utils.ChatColor.translateAll('&', s);
    }

    public String getPlaceholderOffline() {
        String s = prepareServerColorString(null, placeholderOffline.getValue());

        return s == null ? null : de.codingair.codingapi.utils.ChatColor.translateAll('&', s);
    }

    public String getPlaceholderCountInfo(ServerPing ping) {
        String s = placeholderCountInfo.getValue();
        if (s == null) return null;

        if (ping != null) {
            s = s.replace(".p.", ping.getPlayers() + "")
                    .replace(".mp.", ping.getMaxPlayers() + "")
                    .replace(".s.", WarpSystem.opt().getStatus(ping))
                    .replace(".m.", ping.getMotd() == null ? "" : ping.getMotd())
            ;
        } else {
            s = s.replace(".p.", "0")
                    .replace(".mp.", "0")
                    .replace(".s.", getPlaceholderOffline())
                    .replace(".m.", "")
            ;
        }

        return de.codingair.codingapi.utils.ChatColor.translateAll('&', prepareServerColorString(ping, s));
    }

    private String prepareServerColorString(ServerPing ping, String s) {
        if (ping != null) {
            s = s.replace(".sc.", (ping.getStatus() ? placeholderColorsOnline.getValue() : placeholderColorsOffline.getValue()))
                    .replace(".cc.", (ping.getPlayers() < ping.getMaxPlayers() ? placeholderColorsNotFull.getValue() : placeholderColorsFull.getValue()));
        } else {
            s = s.replace(".sc.", placeholderColorsOffline.getValue())
                    .replace(".cc.", placeholderColorsFull.getValue());
        }

        return de.codingair.codingapi.utils.ChatColor.translateAll('&', s);
    }

    public String prepareServerString(ServerPing ping, String s) {
        if (ping != null) {
            s = s.replace(".p.", ping.getPlayers() + "")
                    .replace(".mp.", ping.getMaxPlayers() + "")
                    .replace(".s.", WarpSystem.opt().getStatus(ping))
                    .replace(".m.", ping.getMotd() == null ? "" : de.codingair.codingapi.utils.ChatColor.translateAll('&', ping.getMotd()))
                    .replace(".ci.", WarpSystem.opt().getPlaceholderCountInfo(ping));
        } else {
            s = s.replace(".p.", "0")
                    .replace(".mp.", "0")
                    .replace(".s.", getPlaceholderOffline())
                    .replace(".m.", "")
                    .replace(".ci.", WarpSystem.opt().getPlaceholderCountInfo(null));
        }

        return de.codingair.codingapi.utils.ChatColor.translateAll('&', prepareServerColorString(ping, s));
    }

    public boolean forbiddenRegion(Location location) {
        Stream<String> regions = WorldGuardHelper.getRegion(location);

        if (regions == null) return false;
        return regions.anyMatch(s -> forbiddenRegions.getValue().contains(s));
    }

    public boolean isSafeTp() {
        return safeTp.getValue();
    }
}
