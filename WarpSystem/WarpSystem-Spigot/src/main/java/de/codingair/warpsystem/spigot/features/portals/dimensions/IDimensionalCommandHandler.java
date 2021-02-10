package de.codingair.warpsystem.spigot.features.portals.dimensions;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public interface IDimensionalCommandHandler {
    void open(@NotNull Player player, @NotNull String type);

    void delete(@NotNull CommandSender sender, @NotNull String type);
}
