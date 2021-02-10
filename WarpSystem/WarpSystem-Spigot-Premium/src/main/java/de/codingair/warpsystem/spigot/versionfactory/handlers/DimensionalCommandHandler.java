package de.codingair.warpsystem.spigot.versionfactory.handlers;

import de.codingair.warpsystem.spigot.base.utils.Lang;
import de.codingair.warpsystem.spigot.features.portals.dimensions.DimensionType;
import de.codingair.warpsystem.spigot.features.portals.dimensions.IDimensionalCommandHandler;
import de.codingair.warpsystem.spigot.features.portals.managers.PortalManager;
import de.codingair.warpsystem.spigot.versionfactory.featureobjects.dimensional.DimensionEditor;
import de.codingair.warpsystem.spigot.versionfactory.featureobjects.dimensional.DimensionalPortal;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class DimensionalCommandHandler implements IDimensionalCommandHandler {

    @Override
    public void open(@NotNull Player player, @NotNull String type) {
        DimensionType dType = DimensionType.get(type);
        if (dType == null) {
            player.sendMessage(Lang.getPrefix() + "not existing");
            return;
        }

        DimensionalPortal portal = (DimensionalPortal) PortalManager.getInstance().getDimensionalPortal(dType);
        if (portal == null) portal = new DimensionalPortal(dType);

        new DimensionEditor(player, portal).open();
    }

    @Override
    public void delete(@NotNull CommandSender sender, @NotNull String type) {

    }
}
