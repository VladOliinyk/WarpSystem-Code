package de.codingair.warpsystem.velocity.base;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.scheduler.Scheduler;
import de.codingair.codingapi.tools.time.TimeFetcher;
import de.codingair.codingapi.tools.time.Timer;
import de.codingair.warpsystem.velocity.api.VelocityPlugin;
import de.codingair.warpsystem.velocity.api.chatinput.ChatInputManager;
import de.codingair.warpsystem.velocity.api.files.FileManager;
import de.codingair.warpsystem.velocity.base.listeners.MainListener;
import de.codingair.warpsystem.velocity.base.managers.CooldownManager;
import de.codingair.warpsystem.velocity.base.managers.DataManager;
import de.codingair.warpsystem.velocity.base.managers.ServerManager;
import de.codingair.warpsystem.velocity.base.managers.VanishManager;
import de.codingair.warpsystem.velocity.transfer.VelocityDataHandler;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

public class WarpSystem extends VelocityPlugin {
    private static WarpSystem instance;
    private final ProxyServer proxy;
    private final Logger logger;

    private final ServerManager serverManager = new ServerManager();
    private final DataManager dataManager = new DataManager();
    private final VanishManager vanishManager = new VanishManager();
    private final FileManager fileManager = new FileManager(this);
    private final CooldownManager cooldownManager = new CooldownManager();
    private VelocityDataHandler dataHandler;

    @Inject
    public WarpSystem(ProxyServer proxy, Logger logger) {
        instance = this;
        loadPluginJson();

        this.proxy = proxy;
        this.logger = logger;
        dataManager.preLoad();
    }

    @Subscribe
    public void onEnable(ProxyInitializeEvent e) {
        Timer t = new Timer();
        t.start();

        log(" ");
        log("________________________________________________________");
        log(" ");
        log("                   WarpSystem [" + getVersion() + "]");
        log(" ");
        logger.warn("Velocity has not been fully supported yet!");
        logger.warn("Missing functions: teleport tab completions, RandomTP");
        log(" ");
        log("Status:");
        log(" ");

        log("Initialize SpigotConnector");
        this.dataHandler = new VelocityDataHandler(this, proxy, getName());
        this.dataHandler.onEnable();

        proxy.getEventManager().register(this, new MainListener());
        this.serverManager.run();
        new ChatInputManager();
        getDataFolder().mkdir();

        log("Loading features");
        boolean createBackup = false;
        if(!this.dataManager.load(false)) createBackup = true;
        this.cooldownManager.load();

        if(createBackup) {
            log("Loading with errors > Create backup...");
            createBackup();
            log("Backup successfully created");
        }

        log(" ");
        log("Done (" + t.result() + ")");
        log(" ");
        log("________________________________________________________");
        log(" ");
    }

    @Subscribe
    public void onDisable(ProxyShutdownEvent e) {
        this.dataHandler.onDisable();
        save(true);
    }

    private void save(boolean saver) {
        try {
            Timer timer = new Timer();
            if(!saver) {
                timer.start();

                log(" ");
                log("________________________________________________________");
                log(" ");
                log("                   WarpSystem [" + getVersion() + "]");
                log(" ");
                log("Status:");
                log(" ");
            }

            if(!saver) log("Saving features");
            this.dataManager.save(saver);
            this.cooldownManager.save();

            if(!saver) {
                log(" ");
                log("Done (" + timer.result() + ")");
                log(" ");
                log("________________________________________________________");
                log(" ");
            }
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }

    public void createBackup() {
        getDataFolder().mkdir();

        File backupFolder = new File(getDataFolder().getPath() + "/Backups/", TimeFetcher.getYear() + "_" + (TimeFetcher.getMonthNum() + 1) + "_" + TimeFetcher.getDay() + " " + TimeFetcher.getHour() + "_" + TimeFetcher.getMinute() + "_" + TimeFetcher.getSecond());
        backupFolder.mkdirs();

        for(File file : getDataFolder().listFiles()) {
            if(file.getName().equals("Backups") || file.getName().equals("ErrorReport.txt")) continue;
            File dest = new File(backupFolder, file.getName());

            try {
                if(file.isDirectory()) {
                    copyFolder(file, dest);
                    continue;
                }

                copyFileUsingFileChannels(file, dest);
            } catch(IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void copyFolder(File source, File dest) throws IOException {
        dest.mkdirs();
        for(File file : source.listFiles()) {
            File copy = new File(dest, file.getName());

            if(file.isDirectory()) {
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
            inputChannel.close();
            outputChannel.close();
        }
    }

    public Logger getLogger() {
        return logger;
    }

    public VelocityDataHandler getDataHandler() {
        return dataHandler;
    }

    public static void log(String message) {
        System.out.println(message);
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

    public ServerManager getServerManager() {
        return serverManager;
    }

    public static VanishManager getVanishManager() {
        return getInstance().vanishManager;
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
}
