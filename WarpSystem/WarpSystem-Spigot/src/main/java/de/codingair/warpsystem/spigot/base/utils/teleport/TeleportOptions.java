package de.codingair.warpsystem.spigot.base.utils.teleport;

import de.codingair.codingapi.server.sounds.Sound;
import de.codingair.codingapi.server.sounds.SoundData;
import de.codingair.codingapi.tools.Callback;
import de.codingair.codingapi.utils.ImprovedDouble;
import de.codingair.warpsystem.api.Options;
import de.codingair.warpsystem.api.Result;
import de.codingair.warpsystem.spigot.base.WarpSystem;
import de.codingair.warpsystem.spigot.base.guis.editor.pages.SoundPage;
import de.codingair.warpsystem.spigot.base.utils.Lang;
import de.codingair.warpsystem.spigot.base.utils.Permissions;
import de.codingair.warpsystem.spigot.base.utils.money.Bank;
import de.codingair.warpsystem.spigot.base.utils.teleport.destinations.Destination;
import de.codingair.warpsystem.spigot.base.utils.teleport.destinations.adapters.LocationAdapter;
import de.codingair.warpsystem.spigot.features.animations.AnimationManager;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Set;

public class TeleportOptions {
    private final Set<Callback<Result>> callback = new HashSet<>();
    private Origin origin;
    private Destination destination;

    private String displayName;
    private String permission;
    private double costs;
    private int delay;

    private Boolean skip;
    private boolean canMove;
    private boolean waitForTeleport; //Waiting for walking teleports
    private boolean confirmPayment = true;
    private boolean silent;

    private String payMessage;
    private String paymentDeniedMessage;
    private String message;
    private String serverNotOnline;

    private SoundData teleportSound;
    private SoundData cancelSound;

    private Boolean afterEffects;
    private boolean publicAnimations;
    private boolean teleportAnimation = true;

    public TeleportOptions() {
        this((Destination) null, null);
    }

    public TeleportOptions(Location location, String displayName) {
        this(location, displayName, Origin.Custom);
    }

    public TeleportOptions(Location location, String displayName, Origin origin) {
        this(new Destination(new LocationAdapter(location)), displayName, origin);
    }

    public TeleportOptions(Destination destination, String displayName) {
        this(destination, displayName, Origin.Custom);
    }

    public TeleportOptions(Destination destination, String displayName, Origin origin) {
        this.origin = origin;
        this.destination = destination;
        setDisplayName(displayName);
        this.permission = null;
        this.costs = 0;
        this.delay = WarpSystem.opt().getTeleportDelay();
        this.canMove = WarpSystem.opt().isAllowMove();
        this.waitForTeleport = false;
        this.payMessage = Lang.getPrefix() + Lang.get("Money_Paid");
        this.paymentDeniedMessage = Lang.getPrefix() + Lang.get("Payment_denied");
        this.message = Lang.getPrefix() + Lang.get("Teleported_To");
        this.serverNotOnline = Lang.getPrefix() + Lang.get("Server_Is_Not_Online");
        this.silent = false;
        this.teleportSound = null;
        this.cancelSound = new SoundData(Sound.ENTITY_ITEM_BREAK, 0.7F, 1F);
        this.afterEffects = destination.getCustomOptions().getParticles();
        this.publicAnimations = WarpSystem.opt().isPublicAnimations();
    }

    public TeleportOptions(Options o) {
        this.destination = new Destination(o.getDestination(), o.randomOffset(new Vector()));
        this.origin = Origin.Custom;
        this.displayName = o.displayName();
        this.permission = o.permission(null);
        this.costs = o.costs(0);
        this.delay = o.delay(WarpSystem.opt().getTeleportDelay());
        this.canMove = o.canMove(WarpSystem.opt().isAllowMove());
        this.waitForTeleport = o.waitForTeleport(false);
        this.payMessage = o.payMessage(Lang.getPrefix() + Lang.get("Money_Paid"));
        this.paymentDeniedMessage = o.paymentDeniedMessage(Lang.getPrefix() + Lang.get("Payment_denied"));
        this.message = o.message(Lang.getPrefix() + Lang.get("Teleported_To"));
        this.serverNotOnline = o.serverNotOnline(Lang.getPrefix() + Lang.get("Server_Is_Not_Online"));
        this.silent = false;
        this.teleportSound = o.teleportSound(null);
        this.cancelSound = o.cancelSound(new SoundData(Sound.ENTITY_ITEM_BREAK, 0.7F, 1F));
        this.afterEffects = o.afterEffects(destination.getCustomOptions().isParticles());
        this.publicAnimations = o.publicAnimations(WarpSystem.opt().isPublicAnimations());
    }

    public Location buildLocation() {
        return destination == null ? null : destination.buildLocation();
    }

    public void destroy() {
        callback.clear();
    }

    public Origin getOrigin() {
        return origin;
    }

    public TeleportOptions setOrigin(Origin origin) {
        this.origin = origin;
        return this;
    }

    public Destination getDestination() {
        return destination;
    }

    public void setDestination(Destination destination) {
        this.destination = destination;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    public double getCosts(Player player) {
        if (player.hasPermission(Permissions.PERMISSION_ByPass_Teleport_Costs)) return 0;
        return costs;
    }

    public void setCosts(double costs) {
        this.costs = costs;
    }

    public Number getFinalCosts(Player player) {
        return new ImprovedDouble(costs > 0 && Bank.adapter() != null && !player.hasPermission(Permissions.PERMISSION_ByPass_Teleport_Costs) ? costs : 0).get();
    }

    public boolean isSkip() {
        if (skip == null) return false;
        return skip;
    }

    public Boolean getSkip() {
        return skip;
    }

    public void setSkip(boolean skip) {
        this.skip = skip;
    }

    public boolean isCanMove() {
        return canMove;
    }

    public void setCanMove(boolean canMove) {
        this.canMove = canMove;
    }

    public boolean isWaitForTeleport() {
        return waitForTeleport;
    }

    public void setWaitForTeleport(boolean waitForTeleport) {
        this.waitForTeleport = waitForTeleport;
    }

    public String getMessage() {
        String displayName = this.displayName;
        if (destination.getCustomOptions().getDisplayName() != null) displayName = destination.getCustomOptions().getColoredDisplayName();

        return message == null ? null : displayName == null ? message : message.replace("%warp%", displayName);
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isSilent() {
        return silent;
    }

    public void setSilent(boolean silent) {
        this.silent = silent;
    }

    public SoundData getTeleportSound() {
        if (teleportSound == null) teleportSound = AnimationManager.getInstance().getActive().getTeleportSound();
        if (this.teleportSound == null) this.teleportSound = SoundPage.createStandard();
        return teleportSound;
    }

    public void setTeleportSound(SoundData teleportSound) {
        this.teleportSound = teleportSound;
    }

    public boolean isAfterEffects() {
        return afterEffects != null ? afterEffects : WarpSystem.opt().isAfterEffects();
    }

    public void setAfterEffects(boolean afterEffects, boolean force) {
        if (this.afterEffects != null && !force) return;
        this.afterEffects = afterEffects;
    }

    public void fireCallbacks(Result result) {
        for (Callback<Result> teleportResultCallback : this.callback) {
            teleportResultCallback.accept(result);
        }

        this.callback.clear();
    }

    public boolean expired() {
        return this.callback.isEmpty();
    }

    public TeleportOptions addCallback(Callback<Result> callback) {
        if (callback == null) return this;
        this.callback.add(callback);
        return this;
    }

    public String getPayMessage() {
        return payMessage == null ? null : payMessage.replace("%warp%", displayName);
    }

    public void setPayMessage(String payMessage) {
        this.payMessage = payMessage;
    }

    public String getFinalMessage(Player player) {
        return getFinalCosts(player).doubleValue() > 0 ? getPayMessage() : getMessage();
    }

    public boolean isConfirmPayment() {
        return confirmPayment;
    }

    public void setConfirmPayment(boolean confirmPayment) {
        this.confirmPayment = confirmPayment;
    }

    public String getPaymentDeniedMessage(Player player) {
        if (this.paymentDeniedMessage == null) return null;
        return this.paymentDeniedMessage.replace("%AMOUNT%", getFinalCosts(player) + "");
    }

    public void setPaymentDeniedMessage(String paymentDeniedMessage) {
        this.paymentDeniedMessage = paymentDeniedMessage;
    }

    public boolean isTeleportAnimation() {
        return teleportAnimation;
    }

    public void setTeleportAnimation(boolean teleportAnimation) {
        this.teleportAnimation = teleportAnimation;
    }

    public String getServerNotOnline() {
        return serverNotOnline;
    }

    public void setServerNotOnline(String serverNotOnline) {
        this.serverNotOnline = serverNotOnline;
    }

    public boolean isPublicAnimations() {
        return publicAnimations;
    }

    public void setPublicAnimations(boolean publicAnimations) {
        this.publicAnimations = publicAnimations;
    }

    public SoundData getCancelSound() {
        return cancelSound;
    }

    public void setCancelSound(SoundData cancelSound) {
        this.cancelSound = cancelSound;
    }

    public int getDelay(Player player) {
        if (player.hasPermission(Permissions.PERMISSION_ByPass_Teleport_Delay) || (skip != null && skip)) return 0;
        if (destination != null) return destination.getCustomOptions().getDelay(delay);
        return delay;
    }

    public void setDelay(int delay) {
        this.delay = delay;
    }
}
