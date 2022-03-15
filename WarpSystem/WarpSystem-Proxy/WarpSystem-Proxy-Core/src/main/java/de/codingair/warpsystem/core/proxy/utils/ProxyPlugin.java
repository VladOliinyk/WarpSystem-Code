package de.codingair.warpsystem.core.proxy.utils;

import de.codingair.packetmanagement.utils.Proxy;
import de.codingair.packetmanagement.variants.bytestream.StreamDataHandler;
import de.codingair.warpsystem.core.proxy.base.handlers.PlayerDataHandler;
import de.codingair.warpsystem.core.proxy.base.handlers.WorldHandler;
import de.codingair.warpsystem.core.utils.Manager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.InputStream;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.stream.Stream;

public interface ProxyPlugin extends Proxy {
    @Nullable Player getPlayer(String name);

    @NotNull Stream<Player> getOnlinePlayers();

    @NotNull Stream<Server<?>> getRegisteredServers();

    @NotNull <D extends StreamDataHandler<Server<?>>> D dataHandler();

    @NotNull ScheduleTask schedule(Runnable runnable, long delay, long interval, TimeUnit unit);

    void runAsync(Runnable runnable);

    @NotNull String getVersion();

    @NotNull File getDataFolder();

    @Nullable InputStream getResourceAsStream(String path);

    void log(String message);

    void log(Level level, String message);

    Server<?> getServer(String server);

    <A extends Manager> A getHandler(Class<A> c);

    PlayerDataHandler getPlayerData();

    WorldHandler getWorldManager();

    /**
     * @param player  The player who should executed the command line.
     * @param command The command line that should be executed.
     * @return true if the command is existing and has been executed.
     */
    CompletableFuture<Boolean> performCommand(@NotNull Player player, @NotNull String command);
}
