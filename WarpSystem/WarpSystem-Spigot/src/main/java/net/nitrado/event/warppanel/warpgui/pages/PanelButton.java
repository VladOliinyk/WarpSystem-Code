package net.nitrado.event.warppanel.warpgui.pages;

import de.codingair.codingapi.player.gui.inventory.v2.GUI;
import de.codingair.codingapi.player.gui.inventory.v2.buttons.Button;
import de.codingair.codingapi.tools.Callback;
import de.codingair.codingapi.tools.items.ItemBuilder;
import de.codingair.warpsystem.api.Result;
import de.codingair.warpsystem.core.transfer.packets.spigot.utils.ServerPing;
import de.codingair.warpsystem.spigot.base.WarpSystem;
import de.codingair.warpsystem.spigot.base.utils.teleport.destinations.Destination;
import de.codingair.warpsystem.spigot.base.utils.teleport.destinations.DestinationType;
import net.nitrado.event.warppanel.warpgui.WarpPanel;
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

    public PanelButton(String name, String skull, int id, String server, Player player) {
        this.name = name;
        this.skull = skull;
        this.material = null;
        this.id = id;
        this.player = player;

        this.ping = WarpSystem.getInstance().getServerManager().getProperties(server);
        this.destination = new Destination(server, DestinationType.Server);

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
        return (type == ClickType.LEFT && !this.joined && this.ping != null && (!isFull(this.ping) || this.player.hasPermission("WarpPanel.Full")));
    }

    public void onClick(GUI gui, InventoryClickEvent e) {
        final Player p = gui.getPlayer();

        this.destination.teleport(p, "§x§F§F§D§7§4§4Nitrado §8» §7Du wurdest zu §eEvent-" + ((this.id < 10) ? ("0" + this.id) : this.id) + "§7 teleportiert.", "", false, false, 0.0D, new Callback<Result>() {
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
}
