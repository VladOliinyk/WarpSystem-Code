package de.codingair.warpsystem.spigot.base.utils.featureobjects.actions.types;

import de.codingair.codingapi.tools.io.utils.DataMask;
import de.codingair.warpsystem.api.destinations.utils.Usable;
import de.codingair.warpsystem.spigot.base.utils.featureobjects.actions.Action;
import de.codingair.warpsystem.spigot.base.utils.featureobjects.actions.ActionObject;
import de.codingair.warpsystem.spigot.base.utils.teleport.destinations.Destination;
import org.bukkit.entity.Player;

public class WarpAction extends ActionObject<Destination> {
    public WarpAction(Destination destination) {
        super(Action.WARP, destination);
    }

    public WarpAction() {
        this(null);
    }

    @Override
    public void read(String s) {
        if (s != null) {
            setValue(new Destination(s));
        }
    }

    @Override
    public boolean read(DataMask d) {
        setValue(d.getSerializable("destination", new Destination()));
        return true;
    }

    @Override
    public void write(DataMask d) {
        d.put("destination", getValue());
    }

    @Override
    public boolean perform(Player player) {
        return true;
    }

    @Override
    public boolean usable() {
        return getValue() != null && (getValue().getId() != null
                || (getValue().getAdapter() instanceof Usable && ((Usable) getValue().getAdapter()).usable())
        );
    }

    @Override
    public WarpAction clone() {
        return new WarpAction(getValue() == null ? null : getValue().clone());
    }
}
