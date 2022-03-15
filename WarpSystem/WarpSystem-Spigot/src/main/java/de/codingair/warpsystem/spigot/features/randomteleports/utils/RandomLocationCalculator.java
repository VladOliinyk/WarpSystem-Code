package de.codingair.warpsystem.spigot.features.randomteleports.utils;

import de.codingair.codingapi.server.Environment;
import de.codingair.codingapi.server.specification.Version;
import de.codingair.codingapi.tools.Callback;
import de.codingair.codingapi.tools.Location;
import de.codingair.codingapi.utils.Node;
import de.codingair.warpsystem.spigot.api.events.FakeBlockBreakEvent;
import de.codingair.warpsystem.spigot.base.WarpSystem;
import de.codingair.warpsystem.spigot.features.randomteleports.managers.RandomTeleportManager;
import io.papermc.lib.PaperLib;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

public abstract class RandomLocationCalculator implements Runnable {
    private final org.bukkit.Location startLocation;
    private final Player player;
    private final Player fakeCheck;
    private final Callback<RandomLocationCalculator> callback;
    private final double minRange;
    private final double maxRange;
    private final double diffRange;
    private long lastReaction = 0;
    private Location result = null;

    public RandomLocationCalculator(Player player, org.bukkit.Location location, double minRange, double maxRange, Callback<RandomLocationCalculator> callback) {
        fakeCheck = FakeBlockBreakEvent.buildFake(player);
        this.player = player;

        this.callback = callback;
        this.startLocation = location;
        this.minRange = minRange;
        this.maxRange = maxRange;
        this.diffRange = maxRange - minRange;
    }

    @Override
    public void run() {
        Location location = calculate();
        if (location != null) {
            location.setX(location.getBlockX() + 0.5);
            location.setY(location.getBlockY() + 0.5);
            location.setZ(location.getBlockZ() + 0.5);
        }

        result = location;
        callback.accept(this);
    }

    private Location calculate() {
        long start = System.currentTimeMillis();
        Location location = new Location(startLocation);

        double x = startLocation.getX();
        double z = startLocation.getZ();

        Random r = new Random();

        long maxTime = (long) ((maxRange - minRange) / 2);
        if (maxTime < 1000) maxTime = 1000;
        if (maxTime > 5000) maxTime = 5000;

        do {
            location.setY(startLocation.getY());
            lastReaction = System.currentTimeMillis();

            Node<Double, Double> offset = getRandomOffset(r);
            location.setX(x + offset.getKey());
            location.setZ(z + offset.getValue());

            getChunkAtAsync(location).join();
            if (start + maxTime < System.currentTimeMillis()) {
                return null;
            }

            if (correct(location, false)) location.setY(calculateYCoord(location));
        } while (!checkY(location) || blockedMaterial(location) || !correct(location, true));
        return location;
    }

    @NotNull
    private CompletableFuture<Chunk> getChunkAtAsync(@NotNull Location location) {
        if (Version.get().isBiggerThan(8)) return PaperLib.getChunkAtAsync(location);
        else {
            //1.8.8 cannot handle async chunk loading
            CompletableFuture<Chunk> future = new CompletableFuture<>();
            Bukkit.getScheduler().runTask(WarpSystem.getInstance(), () -> PaperLib.getChunkAtAsync(location).whenComplete((chunk, t) -> {
                if (t != null) future.completeExceptionally(t);
                else future.complete(chunk);
            }));
            return future;
        }
    }

    private Node<Double, Double> getRandomOffset(Random r) {
        double degree = r.nextDouble() * 2 * Math.PI - Math.PI;

        double x = -Math.sin(degree) * ((r.nextDouble() * diffRange) + minRange);
        double z = Math.cos(degree) * ((r.nextDouble() * diffRange) + minRange);

        return new Node<>(x, z);
    }

    private boolean blockedMaterial(Location location) {
        Location below = location.clone();
        below.setY(below.getY() - 1);

        List<Material> l = RandomTeleportManager.getInstance().getMaterialBlackList();
        return l.contains(below.getBlock().getType());
    }

    protected boolean isEnoughSpace(Location location) {
        Block b;
        return Environment.canBeEntered(location.getBlock())
                && Environment.canBeEntered(location.clone().add(0, 1, 0).getBlock())
                && (!Environment.canBeEntered(b = location.clone().subtract(0, 1, 0).getBlock()) || Environment.isWaterFluid(b));
    }

    private boolean checkY(Location location) {
        if (location.getWorld() == null) throw new IllegalArgumentException();
        return location.getY() <= getHighestY(location.getWorld()) && location.getY() > 0;
    }

    private int getHighestY(World w) {
        switch (w.getEnvironment()) {
            case NETHER:
                return RandomTeleportManager.getInstance().getNetherHeight();
            case THE_END:
                return RandomTeleportManager.getInstance().getEndHeight();
            default:
                return 72;
        }
    }

    private int calculateYCoord(Location location) {
        Location loc = location.clone();
        if (location.getWorld() == null || loc.getWorld() == null) throw new IllegalArgumentException();

        if (location.getWorld().getEnvironment() != World.Environment.NORMAL) loc.setY(getHighestY(loc.getWorld()));

        if (location.getWorld().getEnvironment() == World.Environment.NETHER) {
            int free = Environment.canBeEntered(loc.clone().add(0, 1, 0).getBlock()) ? 1 : 0;
            while (free < 2 && loc.getY() >= 0) {
                if (Environment.canBeEntered(loc.getBlock().getType())) free++;
                else free = 0;

                loc.setY(loc.getY() - 1);
            }

            while (Environment.canBeEntered(loc.getBlock().getType()) && loc.getBlockY() > 0) {
                loc.setY(loc.getY() - 1);
            }

            loc.setY(loc.getY() + 1);
        } else {
            if (!Environment.canBeEntered(loc.getBlock().getType())) {
                while (!Environment.canBeEntered(loc.getBlock().getType())) {
                    loc.setY(loc.getY() + 4);
                }

                while (Environment.canBeEntered(loc.getBlock().getType()) && !Environment.isWaterFluid(loc.getBlock())) {
                    loc.setY(loc.getY() - 1);
                }

                loc.setY(loc.getY() + 1);
            } else {
                while (Environment.canBeEntered(loc.getBlock().getType()) && !Environment.isWaterFluid(loc.getBlock()) && loc.getBlockY() > 0) {
                    loc.setY(loc.getY() - 4);
                }

                if (loc.getBlockY() > 0) {
                    while (!Environment.canBeEntered(loc.getBlock().getType())) {
                        loc.setY(loc.getY() + 1);
                    }
                }
            }
        }

        return loc.getBlockY();
    }

    public abstract boolean correct(Location location, boolean safety);

    protected CompletableFuture<Boolean> isProtected(Location location) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        if (WarpSystem.opt().forbiddenRegion(location)) future.complete(true);
        else {
            //events can only be triggered synchronously
            Bukkit.getScheduler().runTask(WarpSystem.getInstance(), () -> future.complete(FakeBlockBreakEvent.tryWithFake(this.fakeCheck, location)));
        }

        return future;
    }

    public long getLastReaction() {
        return lastReaction;
    }

    public Player getPlayer() {
        return player;
    }

    public Location getResult() {
        return result;
    }
}
