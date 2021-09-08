package net.nitrado.event.warppanel.warpgui.pages;

import de.codingair.codingapi.player.gui.inventory.v2.GUI;
import de.codingair.codingapi.player.gui.inventory.v2.buttons.Button;
import de.codingair.codingapi.tools.Callback;
import de.codingair.codingapi.tools.items.ItemBuilder;
import de.codingair.warpsystem.api.Result;
import de.codingair.warpsystem.core.transfer.packets.spigot.utils.ServerPing;
import de.codingair.warpsystem.spigot.base.WarpSystem;
import de.codingair.warpsystem.spigot.base.utils.teleport.destinations.Destination;
import de.codingair.warpsystem.spigot.base.utils.teleport.destinations.adapters.ServerAdapter;
import net.nitrado.event.warppanel.warpgui.WarpPanel;
import net.nitrado.misc.MiscPlugin;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;


public class PanelButton extends Button {
    private final String name;
    private final String skull;
    private final Material material;
    private final int id;
    private final Player player;
    private final ServerPing ping;
    private final Destination destination;
    private final boolean joined;
    private final boolean disabled;

    public PanelButton(String name, String skull, int id, String server, Player player, boolean disabled) {
        this.name = name;
        this.skull = skull;
        this.disabled = disabled;
        this.material = null;
        this.id = id;
        this.player = player;

        this.ping = disabled ? null : WarpSystem.getInstance().getServerManager().getProperties(server);

        ServerAdapter adapter = new ServerAdapter();
        adapter.setServer(server);

        this.destination = new Destination(adapter);

        this.joined = server.equalsIgnoreCase(WarpSystem.getInstance().getCurrentServer());
    }

    public static boolean isFull(ServerPing ping) {
        if (ping == null) return true;
        return (ping.getPlayers() >= WarpPanel.MAX_PLAYER_COUNT_PER_SERVER);
    }

    public ItemStack buildItem() {
        ItemBuilder item;
        if (this.material != null) {
            item = new ItemBuilder(this.material);
        } else {
            item = new ItemBuilder(this.skull);
        }

        item.setName("§e" + this.name);

        if (this.ping == null || !this.ping.getStatus()) {
            item.addLore("§7Status: §cOffline");
        } else {
            item.addLore("§7Status: §aOnline");
            item.addLore("§7Spieler: " + (isFull(this.ping) ? "§c" : "§a") + this.ping.getPlayers() + "§8/§7" + WarpPanel.MAX_PLAYER_COUNT_PER_SERVER);

            if (this.joined) {
                item.addLore("", "§7» Bereits beigetreten");
            } else {
                item.addLore("", "§7» Server wechseln");
            }
        }


        return item.getItem();
    }

    public boolean canClick(ClickType type) {
        return !disabled && (type == ClickType.LEFT && !this.joined && this.ping != null && (!isFull(this.ping) || this.player.hasPermission("WarpPanel.Full")));
    }

    public void onClick(GUI gui, InventoryClickEvent e) {
        if (disabled) return;
        final Player p = gui.getPlayer();

        MiscPlugin plugin = MiscPlugin.getPlugin(MiscPlugin.class);
        plugin.getDatabaseManager().setLastLocation(p, new net.nitrado.misc.utils.Callback<>() {
            @Override
            public void call(Boolean aBoolean) {
                destination.teleport(p, "§x§F§F§D§7§4§4Nitrado §8» §7Du wurdest zu §e" + name + "§7 teleportiert.", "", false, false, 0.0D, new Callback<>() {
                    public void accept(Result result) {
                        switch (result) {
                            case ALREADY_ON_TARGET_SERVER:
                                p.sendMessage("§x§F§F§D§7§4§4Nitrado §8» §7Du bist §cbereits auf diesem Server§7.");
                                break;

                            case TARGET_SERVER_IS_FULL:
                                p.sendMessage("§x§F§F§D§7§4§4Nitrado §8» §7Dieser Server ist §cvoll§7.");
                                break;

                            case SERVER_NOT_AVAILABLE:
                                p.sendMessage("§x§F§F§D§7§4§4Nitrado §8» §7Dieser Server ist §coffline§7.");
                                break;

                            case ERROR:
                                p.sendMessage("§x§F§F§D§7§4§4WarpPanel §8» §7Es ist ein §cFehler §7aufgetreten. §8(Warp#2)");
                                break;
                        }
                    }
                });
            }

            @Override
            public void exception(Exception e) {
                e.printStackTrace();
                p.sendMessage(MiscPlugin.PREFIX + "Es ist ein §cFehler §7aufgetreten. §8(Warp#1)");
            }
        });
    }
}
