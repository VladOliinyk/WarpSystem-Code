package de.codingair.warpsystem.spigot.features.portals.utils;

import de.codingair.warpsystem.spigot.features.portals.dimensions.IDimensionalPortal;
import de.codingair.warpsystem.spigot.versionfactory.VKey;
import de.codingair.warpsystem.spigot.versionfactory.specified.FactoryBuilder;

public class DimensionalPortalFactory extends FactoryBuilder<IDimensionalPortal> {
    private static final DimensionalPortalFactory F = new DimensionalPortalFactory();

    private DimensionalPortalFactory() {
        super(VKey.DimensionalPortal);
    }

    public static IDimensionalPortal build() {
        try {
            return F.newInstance();
        } catch (IllegalStateException ex) {
            return null;
        }
    }
}
