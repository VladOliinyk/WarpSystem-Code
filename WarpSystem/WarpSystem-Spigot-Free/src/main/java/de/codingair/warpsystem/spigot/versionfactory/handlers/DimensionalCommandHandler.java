package de.codingair.warpsystem.spigot.versionfactory.handlers;

import de.codingair.warpsystem.spigot.base.utils.Lang;
import de.codingair.warpsystem.spigot.features.portals.dimensions.IDimensionalCommandHandler;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class DimensionalCommandHandler implements IDimensionalCommandHandler {

    @Override
    public void open(@NotNull Player player, @NotNull String type) {
        Lang.PREMIUM_CHAT(player);
    }

    @Override
    public void delete(@NotNull CommandSender sender, @NotNull String type) {
        Lang.PREMIUM_CHAT(sender);
    }
}
