package de.codingair.warpsystem.velocity.base;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.scheduler.Scheduler;
import de.codingair.codingapi.tools.time.TimeFetcher;
import de.codingair.codingapi.tools.time.Timer;
import de.codingair.warpsystem.core.proxy.Core;
import de.codingair.warpsystem.core.proxy.base.handlers.JarManager;
import de.codingair.warpsystem.core.proxy.base.handlers.PlayerDataHandler;
import de.codingair.warpsystem.core.proxy.base.handlers.WorldHandler;
import de.codingair.warpsystem.core.proxy.redis.RedisCore;
import de.codingair.warpsystem.core.proxy.redis.trevor.TrevorHandler;
import de.codingair.warpsystem.core.proxy.utils.Player;
import de.codingair.warpsystem.core.proxy.utils.ScheduleTask;
import de.codingair.warpsystem.core.proxy.utils.Server;
import de.codingair.warpsystem.core.utils.Manager;
import de.codingair.warpsystem.velocity.api.VelocityPlugin;
import de.codingair.warpsystem.velocity.api.files.FileManager;
import de.codingair.warpsystem.velocity.base.listeners.MainListener;
import de.codingair.warpsystem.velocity.base.listeners.SetupAssistantListener;
import de.codingair.warpsystem.velocity.base.managers.*;
import de.codingair.warpsystem.velocity.redis.BulletHandler;
import de.codingair.warpsystem.velocity.utils.VelocityHandler;
import de.codingair.warpsystem.velocity.utils.VelocityPlayer;
import de.codingair.warpsystem.velocity.utils.VelocityScheduleTask;
import de.codingair.warpsystem.velocity.utils.VelocityServer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public class WarpSystem extends VelocityPlugin {
    private static WarpSystem instance;
    private final ProxyServer proxy;
    private final Logger logger;

    private final FileManager fileManager = new FileManager(this);
    private final JarManager jarManager = new JarManager();
    private final WorldManager worldManager = new WorldManager();
    private final VelocityHandler dataHandler = new VelocityHandler(this);
    private DataManager dataManager;
    private CooldownManager cooldownManager;
    private PlayerDataManager playerDataManager;

    @Inject
    public WarpSystem(ProxyServer proxy, Logger logger) {
        instance = this;
        loadPluginJson();

        this.proxy = proxy;
        this.logger = logger;
    }

    public static WarpSystem getInstance() {
        return instance;
    }

    public static Logger logger() {
        return getInstance().logger;
    }

    public static ProxyServer proxy() {
        return getInstance().proxy;
    }

    public static Scheduler.TaskBuilder scheduler(Runnable runnable) {
        return proxy().getScheduler().buildTask(getInstance(), runnable);
    }

    @Subscribe
    public void onEnable(ProxyInitializeEvent e) {
        Timer t = new Timer();
        t.start();

        Core.setPlugin(this);

        log(" ");
        log("________________________________________________________");
        log(" ");
        log("                   WarpSystem [" + getVersion() + "]");
        log(" ");
        log(" ");
        log("Status:");
        log(" ");

        this.fileManager.getFile("Config", "/", "proxy/");

        //initialize playerDataManager before enabling redis; we might get packets between registering redis
        playerDataManager = new PlayerDataManager();

        checkRedis();

        Core.setServerManager(new ServerManager());
        Core.getServerManager().run();

        dataManager = new DataManager();
        dataManager.preLoad();

        log("Initialize SpigotConnector");
        this.dataHandler.onEnable();

        proxy.getEventManager().register(this, new MainListener());

        proxy.getEventManager().register(this, cooldownManager = new CooldownManager());
        proxy.getEventManager().register(this, playerDataManager);
        proxy.getEventManager().register(this, new SetupAssistantListener());

        new ChatInputManager();
        getDataFolder().mkdir();

        log("Loading features");
        boolean createBackup = !this.dataManager.load(false);
        this.worldManager.load();
        this.cooldownManager.load();

        if (createBackup) {
            log("Loading with errors > Create backup...");
            createBackup();
            log("Backup successfully created");
        }

        startAutoSaver();

        log(" ");
        log("Done (" + t.result() + ")");
        log(" ");
        log("________________________________________________________");
        log(" ");
    }

    @Subscribe
    public void onDisable(ProxyShutdownEvent e) {
        this.dataHandler.onDisable();
        save(false);
    }

    private void startAutoSaver() {
        log("Starting AutoSaver");
        proxy.getScheduler().buildTask(this, () -> save(true)).delay(10, TimeUnit.MINUTES).repeat(10, TimeUnit.MINUTES).schedule();
    }

    private void checkRedis() {
        String name = "-";

        boolean enabled = fileManager.getFile("Config").getSimpleConfig().getBoolean("WarpSystem.Redis", true);
        if (!enabled) {
            name = "Disabled";
        } else if (proxy.getPluginManager().getPlugin("bullet").isPresent()) {
            RedisCore.core().setHandler(new BulletHandler());
            name = "Bullet";
        } else if (proxy.getPluginManager().getPlugin("trevor").isPresent()) {
            RedisCore.core().setHandler(new TrevorHandler());
            name = "Trevor";
        }

        log("Redis hook: " + name);
    }

    private void save(boolean saver) {
        try {
            Timer timer = new Timer();
            if (!saver) {
                timer.start();

                log(" ");
                log("________________________________________________________");
                log(" ");
                log("                   WarpSystem [" + getVersion() + "]");
                log(" ");
                log("Status:");
                log(" ");
            }

            if (!saver) log("Saving features");
            this.dataManager.save(saver);
            this.cooldownManager.save();
            this.worldManager.save();

            if (!saver) {
                log(" ");
                log("Done (" + timer.result() + ")");
                log(" ");
                log("________________________________________________________");
                log(" ");
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
        FileChannel inputChannel = null;
        FileChannel outputChannel = null;
        try {
            inputChannel = new FileInputStream(source).getChannel();
            outputChannel = new FileOutputStream(dest).getChannel();
            outputChannel.transferFrom(inputChannel, 0, inputChannel.size());
        } finally {
            if (inputChannel != null && outputChannel != null) {
                inputChannel.close();
                outputChannel.close();
            }
        }
    }

    public VelocityHandler getDataHandler() {
        return dataHandler;
    }

    public FileManager getFileManager() {
        return fileManager;
    }

    public DataManager getDataManager() {
        return dataManager;
    }

    public CooldownManager getCooldownManager() {
        return cooldownManager;
    }

    @Override
    public @Nullable Player getPlayer(String name) {
        com.velocitypowered.api.proxy.Player player = proxy.getPlayer(name).orElse(null);
        if (player != null) return new VelocityPlayer(player);
        else return null;
    }

    @Override
    public @NotNull Stream<Player> getOnlinePlayers() {
        return proxy.getAllPlayers().stream().map(VelocityPlayer::new);
    }

    @Override
    public @NotNull Stream<Server<?>> getRegisteredServers() {
        return proxy.getAllServers().stream().map(VelocityServer::new);
    }

    @Override
    public @NotNull VelocityHandler dataHandler() {
        return dataHandler;
    }

    @Override
    public @NotNull ScheduleTask schedule(Runnable runnable, long delay, long interval, TimeUnit unit) {
        return new VelocityScheduleTask(proxy.getScheduler().buildTask(this, runnable).delay(delay, unit).repeat(interval, unit).schedule());
    }

    @Override
    public void runAsync(Runnable runnable) {
        proxy.getScheduler().buildTask(this, runnable).schedule();
    }

    @Override
    public Server<?> getServer(String server) {
        RegisteredServer s = proxy.getServer(server).orElse(null);
        if (s != null) return new VelocityServer(s);
        else return null;
    }

    @Override
    public <A extends Manager> A getHandler(Class<A> c) {
        return this.dataManager.getManager(c);
    }

    @Override
    public PlayerDataHandler getPlayerData() {
        return playerDataManager;
    }

    @Override
    public void log(String message) {
        System.out.println(message);
    }

    public JarManager getJarManager() {
        return jarManager;
    }

    @Override
    public WorldHandler getWorldManager() {
        return this.worldManager;
    }

    @Override
    public CompletableFuture<Boolean> performCommand(@NotNull Player player, @NotNull String command) {
        return proxy.getCommandManager().executeAsync(((VelocityPlayer) player).getPlayer(), command);
    }
}