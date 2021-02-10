package de.codingair.warpsystem.spigot.features.portals.dimensions;

import de.codingair.warpsystem.spigot.base.utils.featureobjects.IFeatureObject;

public interface IDimensionalPortal extends IFeatureObject {
    void enable();

    DimensionType getType();
}
