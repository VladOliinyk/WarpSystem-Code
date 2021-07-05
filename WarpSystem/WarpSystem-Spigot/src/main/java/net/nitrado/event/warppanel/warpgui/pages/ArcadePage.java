package net.nitrado.event.warppanel.warpgui.pages;

import de.codingair.codingapi.player.gui.inventory.v2.GUI;
import de.codingair.codingapi.player.gui.inventory.v2.Page;
import de.codingair.codingapi.player.gui.inventory.v2.buttons.Button;
import de.codingair.codingapi.tools.items.ItemBuilder;
import de.codingair.warpsystem.spigot.base.managers.TeleportManager;
import de.codingair.warpsystem.spigot.base.utils.teleport.TeleportOptions;
import de.codingair.warpsystem.spigot.base.utils.teleport.destinations.Destination;
import de.codingair.warpsystem.spigot.base.utils.teleport.destinations.DestinationType;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class ArcadePage extends Page {
    private final TeleportOptions bb;
    private final TeleportOptions ffa;

    public ArcadePage(GUI gui, Page basic) {
        super(gui, basic);
        setTitle("Teleporter - §c§lMinigames");

        this.bb = new TeleportOptions(new Destination("bb", DestinationType.SimpleWarp), "");
        this.ffa = new TeleportOptions(new Destination("ffa", DestinationType.SimpleWarp), "");

        this.bb.setMessage("§x§F§F§D§7§4§4Nitrado §8» §7Du wurdest zum Warp §eBuildBattle§7 teleportiert.");
        this.ffa.setMessage("§x§F§F§D§7§4§4Nitrado §8» §7Du wurdest zum Warp §eFree for All§7 teleportiert.");
    }

    public void buildItems() {
        addButton(3, 2, new Button() {
            public ItemStack buildItem() {
                return (new ItemBuilder(Material.DIAMOND_PICKAXE)).setName("§eBuildBattle").setHideStandardLore(true).addLore("", "§7» Teleportieren").getItem();
            }


            public boolean canClick(ClickType type) {
                return true;
            }


            public void onClick(GUI gui, InventoryClickEvent e) {
                TeleportManager.getInstance().teleport(gui.getPlayer(), ArcadePage.this.bb);
            }
        });

        addButton(5, 2, new Button() {
            public ItemStack buildItem() {
                return (new ItemBuilder(Material.DIAMOND_SWORD)).setName("§eFree for All").setHideStandardLore(true).addLore("", "§7» Teleportieren").getItem();
            }


            public boolean canClick(ClickType type) {
                return true;
            }


            public void onClick(GUI gui, InventoryClickEvent e) {
                TeleportManager.getInstance().teleport(gui.getPlayer(), ArcadePage.this.ffa);
            }
        });
    }
}
