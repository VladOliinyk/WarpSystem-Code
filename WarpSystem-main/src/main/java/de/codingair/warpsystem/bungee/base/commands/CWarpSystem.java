package de.codingair.warpsystem.bungee.base.commands;

import de.codingair.warpsystem.bungee.base.WarpSystem;
import de.codingair.warpsystem.bungee.base.language.Lang;
import de.codingair.warpsystem.bungee.base.managers.JarManager;
import de.codingair.warpsystem.transfer.serializeable.ServerOptions;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

import java.util.ArrayList;

public class CWarpSystem extends Command implements TabExecutor {
    public CWarpSystem() {
        super("warpsystembungee", WarpSystem.PERMISSION_MODIFY_SYSTEM, "wsb");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(args.length == 1) {
            if(args[0].equalsIgnoreCase("reload")) {
                long time = System.currentTimeMillis();
                sender.sendMessage(new TextComponent(Lang.getPrefix() + "§7System is §creloading§7..."));
                WarpSystem.getInstance().getFileManager().reloadAll();
                WarpSystem.getInstance().getDataManager().reload();
                sender.sendMessage(new TextComponent(Lang.getPrefix() + "§7...§adone§7! (" + (System.currentTimeMillis() - time) + "ms)"));
                return;
            } else if(args[0].equalsIgnoreCase("fetch")) {
                if(sender instanceof ProxiedPlayer) {
                    ProxiedPlayer pp = (ProxiedPlayer) sender;

                    if(!JarManager.tryOS()) {
                        pp.sendMessage(new TextComponent(Lang.getPrefix() + "§7Your OS is §cnot supported §7for this operation."));
                        return;
                    }

                    ServerOptions options = WarpSystem.getInstance().getServerManager().getOptions(pp.getServer().getInfo());
                    if(options == null) {
                        pp.sendMessage(new TextComponent(Lang.getPrefix() + "§7This server is §cnot able §7to fetch a new version."));
                        return;
                    }

                    if(options.getUpdateFetching() == 0) {
                        pp.sendMessage(new TextComponent(Lang.getPrefix() + "§7This server is §cnot able §7to fetch a new version."));
                        return;
                    }

                    if(options.isFetched()) {
                        pp.sendMessage(new TextComponent(Lang.getPrefix() + "§7This server §calready fetched §7a new version."));
                        return;
                    }

                    if(!WarpSystem.getInstance().getJarManager().fetchPossible(pp.getServer().getInfo())) {
                        pp.sendMessage(new TextComponent(Lang.getPrefix() + "§7This server is §calready up to date§7."));
                        return;
                    }

                    WarpSystem.getInstance().getJarManager().sendJar(pp.getServer().getInfo(), () -> pp.sendMessage(new TextComponent(Lang.getPrefix() + "§aDone.")));
                    pp.sendMessage(new TextComponent(Lang.getPrefix() + "§7Sending jar..."));
                } else {
                    sender.sendMessage(new TextComponent(Lang.getPrefix() + Lang.get("Only_For_Players")));
                }
                return;
            }
        }

        sender.sendMessage(new TextComponent(Lang.getPrefix() + "§7" + Lang.get("Use") + ": /wsb §e<reload, fetch>"));
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender commandSender, String[] args) {
        if(args.length == 1) return new ArrayList<String>() {{
            add("reload");
            add("fetch");
        }};
        return new ArrayList<>();
    }
}
