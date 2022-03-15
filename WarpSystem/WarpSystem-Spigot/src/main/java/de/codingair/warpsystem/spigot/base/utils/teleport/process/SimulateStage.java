package de.codingair.warpsystem.spigot.base.utils.teleport.process;

import de.codingair.warpsystem.api.destinations.utils.Result;
import de.codingair.warpsystem.api.destinations.utils.SimulatedTeleportResult;
import de.codingair.warpsystem.spigot.base.managers.TeleportManager;
import de.codingair.warpsystem.spigot.base.utils.Lang;
import de.codingair.warpsystem.spigot.base.utils.money.Bank;

public class SimulateStage extends TeleportStage {
    protected SimulateStage(Teleport teleport) {
        super(teleport);
    }

    @Override
    public void start() {
        if (options.getOriginalDestination() == null) throw new IllegalArgumentException("Destination cannot be null!");
        if (options.getPermission() != null && !options.getPermission().equals(TeleportManager.NO_PERMISSION) && !player.hasPermission(options.getPermission())) {
            player.sendMessage(Lang.getPrefix() + Lang.get("Player_Cannot_Use_Warp"));
            cancel(Result.NO_PERMISSION);
            return;
        }

        SimulatedTeleportResult sim = this.options.getOriginalDestination().simulate(player, options.getPermission() == null);
        if (sim.getError() != null) {
            player.sendMessage(sim.getError());
            cancel(sim.getResult());
            return;
        }

        if (sim.getResult() == Result.NO_ADAPTER) {
            cancel(sim.getResult());
            return;
        }

        double costs = options.getCosts(player);
        if (costs > 0 && (!Bank.isReady() || Bank.adapter().getMoney(player) < costs)) {
            cancel(Result.NOT_ENOUGH_MONEY);
            return;
        }

        end();
    }

    @Override
    public void destroy() {

    }
}
