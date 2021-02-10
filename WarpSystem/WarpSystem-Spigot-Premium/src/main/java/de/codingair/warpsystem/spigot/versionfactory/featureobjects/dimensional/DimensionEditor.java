package de.codingair.warpsystem.spigot.versionfactory.featureobjects.dimensional;

import de.codingair.codingapi.tools.items.ItemBuilder;
import de.codingair.codingapi.tools.items.XMaterial;
import de.codingair.warpsystem.spigot.base.guis.editor.Backup;
import de.codingair.warpsystem.spigot.base.guis.editor.Editor;
import de.codingair.warpsystem.spigot.base.guis.editor.pages.DestinationPage;
import de.codingair.warpsystem.spigot.base.utils.Lang;
import de.codingair.warpsystem.spigot.features.portals.managers.PortalManager;
import org.bukkit.entity.Player;

public class DimensionEditor extends Editor<DimensionalPortal> {
    public DimensionEditor(Player p, DimensionalPortal portal) {
        this(p, portal, new DimensionalPortal(portal).createDestinationIfAbsent());
    }

    private DimensionEditor(Player p, DimensionalPortal portal, DimensionalPortal clone) {
        super(p, clone, new Backup<DimensionalPortal>(portal) {
            @Override
            public void applyTo(DimensionalPortal value) {
                portal.apply(value);

                if (portal.getDestination() == null) {
                    PortalManager.getInstance().deleteDimensionalPortal(portal.getType());
                    return;
                }

                PortalManager.getInstance().registerDimensionalPortal(portal);
            }

            @Override
            public void cancel(DimensionalPortal value) {
                clone.destroy();
            }
        }, () -> new ItemBuilder(XMaterial.GRAY_STAINED_GLASS_PANE).setHideName(true).getItem(), new DestinationPage(p, title(), clone.getDestination(), clone.getOrigin()));
    }

    public static String title() {
        return Editor.TITLE_COLOR + Lang.get("Portals_Editor");
    }
}
