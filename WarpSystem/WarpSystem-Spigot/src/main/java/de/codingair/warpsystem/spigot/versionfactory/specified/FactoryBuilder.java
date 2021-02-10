package de.codingair.warpsystem.spigot.versionfactory.specified;

import de.codingair.warpsystem.spigot.base.utils.featureobjects.IFeatureObject;
import de.codingair.warpsystem.spigot.versionfactory.VFac;
import de.codingair.warpsystem.spigot.versionfactory.VKey;

public class FactoryBuilder<F extends IFeatureObject> {
    private final VKey key;

    public FactoryBuilder(VKey key) {
        this.key = key;
    }

    protected F newInstance(Object... args) {
        return VFac.build(this.key, args);
    }
}
