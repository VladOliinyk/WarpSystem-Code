package de.codingair.warpsystem.spigot.api.blocks.utils;

import de.codingair.warpsystem.spigot.api.blocks.listeners.RuleListener;
import org.bukkit.Location;

public abstract class StaticBlock extends Block {
    private final Position pos;

    public StaticBlock(Location location) {
        super(location);
        this.pos = new Position(location);
        RuleListener.BLOCKS.put(this.pos, this);
    }

    @Override
    public void destroy() {
        super.destroy();
        RuleListener.BLOCKS.remove(this.pos);
    }
}
