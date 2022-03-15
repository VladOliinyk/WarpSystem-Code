package de.codingair.warpsystem.spigot.base.utils.featureobjects;

import com.google.common.base.CharMatcher;
import de.codingair.codingapi.server.sounds.SoundData;
import de.codingair.codingapi.tools.Call;
import de.codingair.codingapi.tools.Callback;
import de.codingair.codingapi.tools.io.JSON.JSON;
import de.codingair.codingapi.tools.io.JSON.JSONParser;
import de.codingair.codingapi.tools.io.lib.JSONArray;
import de.codingair.codingapi.tools.io.lib.ParseException;
import de.codingair.codingapi.tools.io.utils.DataMask;
import de.codingair.codingapi.tools.io.utils.Serializable;
import de.codingair.codingapi.utils.ImprovedDouble;
import de.codingair.codingapi.utils.Value;
import de.codingair.warpsystem.api.destinations.utils.Result;
import de.codingair.warpsystem.core.transfer.packets.spigot.utils.ServerPing;
import de.codingair.warpsystem.spigot.api.placeholders.PAPI;
import de.codingair.warpsystem.spigot.base.WarpSystem;
import de.codingair.warpsystem.spigot.base.guis.editor.pages.SoundPage;
import de.codingair.warpsystem.spigot.base.managers.TeleportManager;
import de.codingair.warpsystem.spigot.base.utils.Lang;
import de.codingair.warpsystem.spigot.base.utils.featureobjects.actions.Action;
import de.codingair.warpsystem.spigot.base.utils.featureobjects.actions.ActionObject;
import de.codingair.warpsystem.spigot.base.utils.featureobjects.actions.ActionObjectReadException;
import de.codingair.warpsystem.spigot.base.utils.featureobjects.actions.types.CostsAction;
import de.codingair.warpsystem.spigot.base.utils.featureobjects.actions.types.SoundAction;
import de.codingair.warpsystem.spigot.base.utils.featureobjects.actions.types.WarpAction;
import de.codingair.warpsystem.spigot.base.utils.money.Bank;
import de.codingair.warpsystem.spigot.base.utils.teleport.Origin;
import de.codingair.warpsystem.spigot.base.utils.teleport.TeleportOptions;
import de.codingair.warpsystem.spigot.base.utils.teleport.destinations.Destination;
import de.codingair.warpsystem.spigot.base.utils.teleport.process.ConfirmPayment;
import de.codingair.warpsystem.spigot.base.utils.teleport.process.Teleport;
import de.codingair.warpsystem.spigot.base.utils.teleport.process.TeleportDummy;
import de.codingair.warpsystem.spigot.base.utils.teleport.process.WaitForTeleport;
import de.codingair.warpsystem.spigot.features.warps.nextlevel.exceptions.IconReadException;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public abstract class FeatureObject implements Serializable {
    protected int performed = 0;
    protected String permission = null;
    protected long cooldown = 0;
    protected boolean disabled = false;
    protected boolean skip = false;
    protected Origin origin = null;
    private List<ActionObject<?>> actions;

    protected FeatureObject() {
        this.actions = new ArrayList<>();
    }

    protected FeatureObject(String permission, boolean disabled, List<ActionObject<?>> actions) {
        this.permission = permission;
        this.disabled = disabled;
        this.actions = actions == null ? new ArrayList<>() : actions;
    }

    protected FeatureObject(String permission, boolean disabled, ActionObject<?>... actions) {
        this.permission = permission;
        this.disabled = disabled;
        this.actions = new ArrayList<>(Arrays.asList(actions));
    }

    protected FeatureObject(FeatureObject featureObject) {
        this.actions = featureObject.getCopyOfActions();
        this.permission = featureObject.permission;
        this.cooldown = featureObject.cooldown;
        this.disabled = featureObject.disabled;
        this.skip = featureObject.skip;
        this.performed = featureObject.performed;
        this.origin = featureObject.origin;
    }

    public FeatureObject perform(Player player) {
        WarpAction warp = getAction(WarpAction.class);
        if (warp != null) return perform(player, warp.getValue().getId(), warp.getValue(), SoundPage.createStandard(), skip, true);
        else return perform(player, null, null, SoundPage.createStandard(), skip, true);
    }

    public FeatureObject perform(Player player, String destName, Destination dest, SoundData sound, boolean skip, boolean afterEffects) {
        TeleportOptions options = new TeleportOptions(dest, destName);
        options.setTeleportSound(sound);
        options.setSkip(skip);
        options.setCanMove(skip);
        options.setAfterEffects(afterEffects, false);

        return perform(player, options);
    }

    public Origin getOrigin() {
        if (origin == null) origin = Origin.getByClass(this);
        return origin;
    }

    public void prepareTeleportOptions(String player, TeleportOptions options) {
        if (options.getOriginalDestination() == null) options.setDestination(hasAction(Action.WARP) ? getAction(WarpAction.class).getValue() : null);
        if (options.getDisplayName() == null) options.setDisplayName(hasAction(Action.WARP) ? getAction(WarpAction.class).getValue().getId() : null);
        if (hasAction(Action.SOUND)) options.setTeleportSound(getAction(SoundAction.class).getValue());

        if (options.getSkip() == null) options.setSkip(isSkip());

        options.setOrigin(getOrigin());
        if (getAction(CostsAction.class) != null) options.setCosts(getAction(CostsAction.class).getValue());

        if (hasAction(Action.WARP)) {
            options.setPermission(this.permission == null ? TeleportManager.NO_PERMISSION : permission);
            if (!getOrigin().sendTeleportMessage()) options.setMessage(null);

            options.addCallback(new Callback<Result>() {
                @Override
                public void accept(Result result) {
                    if (result == Result.SUCCESS) {
                        Player p = Bukkit.getPlayer(player);
                        if (p == null) return;

                        for (ActionObject<?> action : actions) {
                            if (action.getType() == Action.WARP || action.getType() == Action.COSTS || action.getType() == Action.SOUND || !action.usable()) continue;
                            action.perform(p);
                        }
                    }
                }
            });
        }
    }

    protected Call confirmPayment(Player player, double costs, Callback<Result> callback) {
        return ConfirmPayment.confirm(player, costs, callback);
    }

    public FeatureObject perform(Player player, TeleportOptions options) {
        if (this.actions == null) return this;

        if (WarpSystem.cooldown().checkPlayer(player, buildHashCode())) {
            options.fireCallbacks(Result.REMAINING_COOLDOWN);
            return this;
        }

        prepareTeleportOptions(player.getName(), options);
        double costs = options.getCosts(player);

        options.addCallback(new Callback<Result>() {
            @Override
            public void accept(Result result) {
                if (result == Result.SUCCESS) {
                    performed++;

                    //check cooldown
                    if (FeatureObject.this.cooldown > 0) WarpSystem.cooldown().register(player, FeatureObject.this.cooldown, buildHashCode());
                }
            }
        });

        if (hasAction(Action.WARP)) {
            WarpSystem.getInstance().getTeleportManager().teleport(player, options);
            return this;
        }

        Value<BukkitRunnable> waiting = new Value<>(null);
        Value<Call> payment = new Value<>(null);

        if (TeleportManager.getInstance().isTeleporting(player)) {
            Teleport teleport = TeleportManager.getInstance().getTeleport(player);
            long diff = System.currentTimeMillis() - teleport.getStartTime();
            if (diff > 50)
                player.sendMessage(Lang.getPrefix() + Lang.get("Player_Is_Already_Teleporting"));
            return this;
        }

        //register dummy to cache this perform instance
        TeleportManager.getInstance().registerTeleport(player, new TeleportDummy(player, getOrigin(), new Callback<Result>() {
            @Override
            public void accept(Result result) {
                if (waiting.getValue() != null) waiting.getValue().cancel();
                else if (payment.getValue() != null) payment.getValue().proceed();
            }
        }));

        //invalidating
        options.addCallback(new Callback<Result>() {
            @Override
            public void accept(Result result) {
                TeleportManager.getInstance().invalidate(player);
            }
        });

        //actions
        options.addCallback(new Callback<Result>() {
            @Override
            public void accept(Result result) {
                if (result == Result.SUCCESS) {
                    for (ActionObject<?> action : actions) {
                        if (action.getType() == Action.WARP || action.getType() == Action.COSTS) continue;
                        action.perform(player);
                    }
                }
            }
        });

        if (costs > 0) {
            if (!Bank.isReady() || Bank.adapter().getMoney(player) < costs) {
                player.sendMessage(Lang.getPrefix() + Lang.get("Not_Enough_Money").replace("%AMOUNT%", options.getFinalCosts(player).toString()));
                return this;
            }

            waiting.setValue(WaitForTeleport.wait(player, new Callback<Result>() {
                @Override
                public void accept(Result result) {
                    if (result != Result.SUCCESS) {
                        options.fireCallbacks(result);
                        return;
                    }

                    waiting.setValue(null);

                    payment.setValue(confirmPayment(player, costs, new Callback<Result>() {
                        @Override
                        public void accept(Result result) {
                            payment.setValue(null);

                            if (result == Result.SUCCESS) {
                                player.sendMessage(Lang.getPrefix() + Lang.get("Money_Paid_Use").replace("%AMOUNT%", new ImprovedDouble(getAction(CostsAction.class).getValue()).toString()));
                            } else if (result == Result.NOT_ENOUGH_MONEY) {
                                player.sendMessage(Lang.getPrefix() + Lang.get("Not_Enough_Money").replace("%AMOUNT%", options.getFinalCosts(player).toString()));
                            } else if (result == Result.DENIED_PAYMENT) {
                                if (options.getPaymentDeniedMessage(player) != null) player.sendMessage(options.getPaymentDeniedMessage(player));
                            }

                            options.fireCallbacks(result);
                        }
                    }));
                }
            }));
        } else options.fireCallbacks(Result.SUCCESS);
        return this;
    }

    @Override
    public boolean read(DataMask d) throws Exception {
        destroy();

        this.disabled = d.getBoolean("disabled");
        this.permission = d.getString("permission");
        this.cooldown = d.getLong("cooldown");
        if (this.permission != null) this.permission = ChatColor.stripColor(CharMatcher.whitespace().trimFrom(this.permission));

        this.skip = d.getBoolean("skip");
        this.performed = d.getInteger("performed");

        if (this.actions == null) this.actions = new ArrayList<>();

        if (d.get("actions") != null) {
            JSONArray actionList = d.getList("actions");

            for (Object o : actionList) {
                JSON j;

                if (o instanceof String) {
                    String data = (String) o;
                    try {
                        j = (JSON) new JSONParser().parse(data);
                    } catch (ParseException e) {
                        throw new IconReadException("Could not parse action object.", e);
                    }
                } else j = new JSON((Map<?, ?>) o);

                int id = j.getInteger("id");

                Object validData = j.getRaw("value");

                Action a = Action.getById(id);
                if (a != null) {
                    ActionObject<?> ao;
                    try {
                        ao = a.getClazz().newInstance();
                    } catch (InstantiationException | IllegalAccessException e) {
                        throw new IconReadException("Could not initialize action object instance.", e);
                    }

                    if (validData instanceof String) {
                        try {
                            ao.read((String) validData);
                        } catch (Exception e) {
                            throw new ActionObjectReadException("Could not read ActionObject properly.", e);
                        }
                    } else {
                        j.read(ao, "value");
                    }

                    this.actions.add(ao);
                }
            }
        }

        return true;
    }

    @Override
    public void write(DataMask d) {
        d.put("disabled", this.disabled);
        d.put("permission", this.permission);
        d.put("cooldown", this.cooldown);
        d.put("skip", this.skip);
        d.put("performed", this.performed);

        JSONArray actionList = new JSONArray();
        if (this.actions != null) {
            for (ActionObject<?> action : this.actions) {
                JSON jo = new JSON();
                jo.put("id", action.getType().getId());
                jo.put("value", action);
                actionList.add(jo);
            }
        }

        d.put("actions", actionList);
    }

    @Override
    public void destroy() {
        this.disabled = false;
        this.permission = null;
        this.cooldown = 0;

        if (this.actions != null) {
            this.actions.forEach(ActionObject::destroy);
            this.actions.clear();
        }
    }

    public void apply(FeatureObject object) {
        this.destroy();

        this.skip = object.skip;
        this.disabled = object.disabled;
        this.permission = object.permission;
        this.performed = object.performed;
        this.cooldown = object.cooldown;
        this.actions = object.getCopyOfActions();
        checkActionList();
    }

    public void checkActionList() {
        List<ActionObject<?>> l = new ArrayList<>(this.actions);

        for (ActionObject<?> object : l) {
            if (!object.usable()) {
                this.actions.remove(object);
            }
        }

        l.clear();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FeatureObject object = (FeatureObject) o;
        return disabled == object.disabled &&
                Objects.equals(permission, object.permission) &&
                actions.equals(object.actions) &&
                cooldown == object.cooldown;
    }

    private int buildHashCode() {
        return Objects.hash(getOrigin().ordinal(), hashCode());
    }

    @Override
    public int hashCode() {
        throw new IllegalStateException("Outdated feature object");
    }

    public String prepareLine(String s) {
        return prepareLine(s, null);
    }

    public String prepareLine(String s, Player player) {
        if (s == null) return null;

        s = de.codingair.codingapi.utils.ChatColor.translateAll('&', s);

        if (getDestination() != null) {
            String server = getDestination().getTargetServer();
            if (server != null) {
                ServerPing ping = WarpSystem.getInstance().getServerManager().getProperties(server);
                s = WarpSystem.opt().prepareServerString(ping, s);
            }
        }

        if (player != null) {
            s = PAPI.convert(s, player);
        }

        return s;
    }

    public Destination getDestination() {
        return hasAction(Action.WARP) ? ((WarpAction) getAction(Action.WARP)).getValue() : null;
    }

    public <T extends FeatureObject> T setDestination(Destination destination) {
        if (destination == null) removeAction(Action.WARP);
        else addAction(new WarpAction(destination));
        return (T) this;
    }

    public <T extends FeatureObject> T createDestinationIfAbsent() {
        if (getDestination() == null) setDestination(new Destination());
        return (T) this;
    }

    public <T extends FeatureObject> T createTeleportSoundIfAbsent() {
        if (!hasAction(Action.SOUND)) addAction(new SoundAction(SoundPage.createStandard()));
        return (T) this;
    }

    public <T extends ActionObject<?>> T getAction(Action action) {
        for (ActionObject<?> ao : this.actions) {
            if (ao.getType() == action) return (T) ao;
        }

        return null;
    }

    public <T extends ActionObject<?>> T getAction(Class<T> clazz) {
        for (ActionObject<?> ao : this.actions) {
            if (ao.getClass() == clazz) return (T) ao;
        }

        return null;
    }

    public boolean hasAction(Action action) {
        return getAction(action) != null;
    }

    public void removeAction(Action action) {
        ActionObject<?> ao = getAction(action);
        if (ao == null) return;
        this.actions.remove(ao);
    }

    public FeatureObject addAction(ActionObject<?> action) {
        return addAction(action, true);
    }

    public FeatureObject addAction(ActionObject<?> action, boolean overwrite) {
        ActionObject<?> ao = getAction(action.getType());
        if (ao != null) {
            if (overwrite) this.actions.remove(ao);
            else return this;
        }

        this.actions.add(action);
        return this;
    }

    public List<ActionObject<?>> getActions() {
        return actions;
    }

    public List<ActionObject<?>> getCopyOfActions() {
        List<ActionObject<?>> l = new ArrayList<>();
        if (actions == null) return l;

        for (ActionObject<?> a : actions) {
            l.add(a.clone());
        }

        return l;
    }

    public String getPermission() {
        return permission;
    }

    public FeatureObject setPermission(String permission) {
        this.permission = permission;
        return this;
    }

    public boolean hasPermission() {
        return this.permission != null;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public FeatureObject setDisabled(boolean disabled) {
        this.disabled = disabled;
        return this;
    }

    public boolean isSkip() {
        return skip;
    }

    public void setSkip(boolean skip) {
        this.skip = skip;
    }

    public int getPerformed() {
        return performed;
    }

    public long getCooldown() {
        return cooldown;
    }

    public void setCooldown(long cooldown) {
        this.cooldown = cooldown;
    }
}
