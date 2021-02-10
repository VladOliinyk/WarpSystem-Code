package de.codingair.warpsystem.spigot.base.utils.featureobjects;

import de.codingair.codingapi.server.sounds.SoundData;
import de.codingair.codingapi.tools.io.utils.DataMask;
import de.codingair.codingapi.tools.io.utils.Serializable;
import de.codingair.warpsystem.spigot.base.utils.featureobjects.actions.Action;
import de.codingair.warpsystem.spigot.base.utils.featureobjects.actions.ActionObject;
import de.codingair.warpsystem.spigot.base.utils.teleport.Origin;
import de.codingair.warpsystem.spigot.base.utils.teleport.TeleportOptions;
import de.codingair.warpsystem.spigot.base.utils.teleport.destinations.Destination;
import org.bukkit.entity.Player;

import java.util.List;

public interface IFeatureObject extends Serializable {
    FeatureObject perform(Player player);

    FeatureObject perform(Player player, String destName, Destination dest, SoundData sound, boolean skip, boolean afterEffects);

    Origin getOrigin();

    void prepareTeleportOptions(String player, TeleportOptions options);

    FeatureObject perform(Player player, TeleportOptions options);

    @Override
    boolean read(DataMask d) throws Exception;

    @Override
    void write(DataMask d);

    @Override
    void destroy();

    void apply(FeatureObject object);

    @Override
    boolean equals(Object o);

    @Override
    int hashCode();

    String prepareLine(String s);

    String prepareLine(String s, Player player);

    Destination getDestination();

    <T extends FeatureObject> T setDestination(Destination destination);

    <T extends FeatureObject> T createDestinationIfAbsent();

    <T extends FeatureObject> T createTeleportSoundIfAbsent();

    <T extends ActionObject<?>> T getAction(Action action);

    <T extends ActionObject<?>> T getAction(Class<T> clazz);

    boolean hasAction(Action action);

    void removeAction(Action action);

    FeatureObject addAction(ActionObject<?> action);

    FeatureObject addAction(ActionObject<?> action, boolean overwrite);

    List<ActionObject<?>> getActions();

    List<ActionObject<?>> getCopyOfActions();

    String getPermission();

    FeatureObject setPermission(String permission);

    boolean hasPermission();

    boolean isDisabled();

    FeatureObject setDisabled(boolean disabled);

    boolean isSkip();

    void setSkip(boolean skip);

    int getPerformed();

    long getCooldown();

    void setCooldown(long cooldown);
}
