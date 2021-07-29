package de.codingair.warpsystem.bungee.base;

import de.codingair.codingapi.bungeecord.BungeeAPI;
import de.codingair.codingapi.bungeecord.files.FileManager;
import de.codingair.codingapi.tools.time.TimeFetcher;
import de.codingair.codingapi.tools.time.Timer;
import de.codingair.warpsystem.bungee.base.commands.CWarpSystem;
import de.codingair.warpsystem.bungee.base.listeners.MainListener;
import de.codingair.warpsystem.bungee.base.listeners.SetupAssistantListener;
import de.codingair.warpsystem.bungee.base.managers.*;
import de.codingair.warpsystem.bungee.redis.RedisBungeeHandler;
import de.codingair.warpsystem.bungee.utils.BungeeHandler;
import de.codingair.warpsystem.bungee.utils.BungeePlayer;
import de.codingair.warpsystem.bungee.utils.BungeeScheduleTask;
import de.codingair.warpsystem.bungee.utils.BungeeServer;
import de.codingair.warpsystem.core.proxy.Core;
import de.codingair.warpsystem.core.proxy.base.LangHandler;
import de.codingair.warpsystem.core.proxy.base.handlers.JarManager;
import de.codingair.warpsystem.core.proxy.base.handlers.PlayerDataHandler;
import de.codingair.warpsystem.core.proxy.base.handlers.WorldHandler;
import de.codingair.warpsystem.core.proxy.redis.RedisCore;
import de.codingair.warpsystem.core.proxy.redis.trevor.TrevorHandler;
import de.codingair.warpsystem.core.proxy.utils.Player;
import de.codingair.warpsystem.core.proxy.utils.ProxyPlugin;
import de.codingair.warpsystem.core.proxy.utils.ScheduleTask;
import de.codingair.warpsystem.core.proxy.utils.Server;
import de.codingair.warpsystem.core.utils.Manager;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public class WarpSystem extends Plugin implements ProxyPlugin {
    private static WarpSystem instance;
    private final BungeeHandler dataHandler = new BungeeHandler(this);
    private final FileManager fileManager = new FileManager(this);
    private final JarManager jarManager = new JarManager();
    private final Timer timer = new Timer();
    private final WorldManager worldManager = new WorldManager();
    private DataManager dataManager;
    private CooldownManager cooldownManager;
    private PlayerDataManager playerDataManager;

    public static void logMessage(String message) {
        System.out.println(message);
    }

    public static BungeeHandler getDataHandler() {
        return getInstance().dataHandler;
    }

    public static ProxyServer proxy() {
        return instance.getProxy();
    }

    public static WarpSystem getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;
        timer.start();
        Core.setPlugin(this);
        Core.setServerManager(new ServerManager());

        BungeeAPI.getInstance().onEnable(this);

        logMessage(" ");
        logMessage("________________________________________________________");
        logMessage(" ");
        logMessage("                   WarpSystem [" + getDescription().getVersion() + "]");
        logMessage(" ");
        logMessage("Status:");
        logMessage(" ");

        this.fileManager.loadFile("Config", "/", "proxy/");

        //initialize playerDataManager before enabling redis; we might get packets between registering redis
        playerDataManager = new PlayerDataManager();

        checkRedis();

        dataManager = new DataManager();
        dataManager.preLoad();

        try {
            LangHandler.initPreDefinedLanguages(this);
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.worldManager.load();

        //listener
        getProxy().getPluginManager().registerListener(this, new MainListener());
        getProxy().getPluginManager().registerListener(this, cooldownManager = new CooldownManager());
        getProxy().getPluginManager().registerListener(this, playerDataManager);

        cooldownManager.load();

        getProxy().getPluginManager().registerListener(this, new SetupAssistantListener());

        Core.getServerManager().run();
        new ChatInputManager();

        getProxy().getPluginManager().registerCommand(this, new CWarpSystem());

        logMessage("Initialize SpigotConnector");
        logMessage(" ");
        this.dataHandler.onEnable();

        logMessage("Loading features");
        boolean createBackup = false;
        if (!this.dataManager.load(false)) createBackup = true;

        if (createBackup) {
            logMessage("Loading with errors > Create backup...");
            createBackup();
            logMessage("Backup successfully created");
        }

        this.startAutoSaver();

        logMessage(" ");
        logMessage("Done (" + timer.result() + ")");
        logMessage(" ");
        logMessage("________________________________________________________");
        logMessage(" ");
    }

    @Override
    public void onDisable() {
        this.dataHandler.flush();
        this.dataHandler.onDisable();
        save(false);
        destroy();
        BungeeAPI.getInstance().onDisable(this);
    }

    private void checkRedis() {
        String name = "-";

        boolean enabled = fileManager.getFile("Config").getConfig().getBoolean("WarpSystem.Redis", true);
        if (!enabled) {
            name = "Disabled";
        } else if (getProxy().getPluginManager().getPlugin("Trevor") != null) {
            RedisCore.core().setHandler(new TrevorHandler());
            name = "Trevor";
        } else if (getProxy().getPluginManager().getPlugin("RedisBungee") != null) {
            RedisBungeeHandler handler = new RedisBungeeHandler();
            RedisCore.core().setHandler(handler);
            getProxy().getPluginManager().registerListener(this, handler);
            name = "RedisBungee";
        }

        logMessage("Redis hook: " + name);
    }

    private void startAutoSaver() {
        WarpSystem.logMessage("Starting AutoSaver");
        getProxy().getScheduler().schedule(this, () -> save(true), 10, 10, TimeUnit.MINUTES);
    }

    private void destroy() {
        this.dataManager.getManagers().forEach(Manager::destroy);
    }

    private void save(boolean saver) {
        try {
            if (!saver) {
                timer.start();

                logMessage(" ");
                logMessage("________________________________________________________");
                logMessage(" ");
                logMessage("                   WarpSystem [" + getDescription().getVersion() + "]");
                logMessage(" ");
                logMessage("Status:");
                logMessage(" ");
            }

            if (!saver) logMessage("Saving features");
            this.cooldownManager.save();
            this.worldManager.save();
            this.dataManager.save(saver);

            if (!saver) {
                logMessage(" ");
                logMessage("Done (" + timer.result() + ")");
                logMessage(" ");
                logMessage("________________________________________________________");
                logMessage(" ");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void createBackup() {
        getDataFolder().mkdir();

        File backupFolder = new File(getDataFolder().getPath() + "/Backups/", TimeFetcher.getYear() + "_" + (TimeFetcher.getMonthNum() + 1) + "_" + TimeFetcher.getDay() + " " + TimeFetcher.getHour() + "_" + TimeFetcher.getMinute() + "_" + TimeFetcher.getSecond());
        backupFolder.mkdirs();

        for (File file : getDataFolder().listFiles()) {
            if (file.getName().equals("Backups") || file.getName().equals("ErrorReport.txt")) continue;
            File dest = new File(backupFolder, file.getName());

            try {
                if (file.isDirectory()) {
                    copyFolder(file, dest);
                    continue;
                }

                copyFileUsingFileChannels(file, dest);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void copyFolder(File source, File dest) throws IOException {
        dest.mkdirs();
        for (File file : source.listFiles()) {
            File copy = new File(dest, file.getName());

            if (file.isDirectory()) {
                copyFolder(file, copy);
                continue;
            }

            copyFileUsingFileChannels(file, copy);
        }
    }

    private void copyFileUsingFileChannels(File source, File dest) throws IOException {
        try (FileChannel inputChannel = new FileInputStream(source).getChannel(); FileChannel outputChannel = new FileOutputStream(dest).getChannel()) {
            outputChannel.transferFrom(inputChannel, 0, inputChannel.size());
        }
    }

    @SuppressWarnings ("unchecked")
    public @NotNull BungeeHandler dataHandler() {
        return getInstance().dataHandler;
    }

    public ServerManager getServerManager() {
        return (ServerManager) Core.getServerManager();
    }

    public FileManager getFileManager() {
        return fileManager;
    }

    public DataManager getDataManager() {
        return dataManager;
    }

    public JarManager getJarManager() {
        return jarManager;
    }

    public PlayerDataManager getPlayerListener() {
        return playerDataManager;
    }

    @Override
    public @Nullable Player getPlayer(String name) {
        ProxiedPlayer p = getProxy().getPlayer(name);
        if (p != null) return new BungeePlayer(p);
        else return null;
    }

    @Override
    public @NotNull Stream<Player> getOnlinePlayers() {
        return getProxy().getPlayers().stream().map(BungeePlayer::new);
    }

    @Override
    public @NotNull Stream<Server<?>> getRegisteredServers() {
        return getProxy().getServers().values().stream().map(BungeeServer::new);
    }

    @Override
    public @NotNull ScheduleTask schedule(Runnable runnable, long delay, long interval, TimeUnit unit) {
        return new BungeeScheduleTask(getProxy().getScheduler().schedule(this, runnable, delay, interval, unit));
    }

    @Override
    public void runAsync(Runnable runnable) {
        getProxy().getScheduler().runAsync(this, runnable);
    }

    @Override
    public @NotNull String getVersion() {
        return getDescription().getVersion();
    }

    @Override
    public Server<?> getServer(String server) {
        ServerInfo info = getProxy().getServerInfo(server);
        if (info != null) return new BungeeServer(info);
        else return null;
    }

    @Override
    public void log(String message) {
        logMessage(message);
    }

    @Override
    public <A extends Manager> A getHandler(Class<A> c) {
        return dataManager.getManager(c);
    }

    @Override
    public PlayerDataHandler getPlayerData() {
        return this.playerDataManager;
    }

    @Override
    public WorldHandler getWorldManager() {
        return this.worldManager;
    }

    @Override
    public CompletableFuture<Boolean> performCommand(@NotNull Player player, @NotNull String command) {
        return CompletableFuture.completedFuture(getProxy().getPluginManager().dispatchCommand(((BungeePlayer) player).getPlayer(), command));
    }
}
