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

public class JnRPage extends Page {
    public JnRPage(GUI gui, Page basic) {
        super(gui, basic);
        setTitle("Teleporter - §3§lJump & Runs");
    }

    public void buildItems() {
        addButton(2, 1, new JnRButton("Lagerhaus", true, Material.CHEST, new Destination("Lagerhaus", DestinationType.SimpleWarp)));
        addButton(4, 1, new JnRButton("Höhle", true, Material.WITHER_SKELETON_SKULL, new Destination("Höhle", DestinationType.SimpleWarp)));
        addButton(6, 1, new JnRButton("Katzensprung", true, Material.FEATHER, new Destination("Katzensprung", DestinationType.SimpleWarp)));
        addButton(2, 3, new JnRButton("Splash-Dash", true, Material.TROPICAL_FISH_BUCKET, new Destination("Splash-Dash", DestinationType.SimpleWarp)));
        addButton(3, 3, new JnRButton("Schnuffwerke", true, Material.STONECUTTER, new Destination("Schnuffwerke", DestinationType.SimpleWarp)));
        addButton(5, 3, new JnRButton("Strandhaus", true, Material.CHISELED_SANDSTONE, new Destination("Strandhaus", DestinationType.SimpleWarp)));
        addButton(6, 3, new JnRButton("Labyrinth", false, Material.JUNGLE_LEAVES, new Destination("Labyrinth", DestinationType.SimpleWarp)));
    }

    private static class JnRButton extends Button {
        private final String name;
        private final Material material;
        private final String skull;
        private final TeleportOptions options;
        private final boolean lottery;

        public JnRButton(String name, boolean lottery, Material material, Destination destination) {
            this.name = name;
            this.lottery = lottery;
            this.material = material;
            this.skull = null;

            this.options = new TeleportOptions(destination, "");
            this.options.setMessage("§x§F§F§D§7§4§4Nitrado §8» §7Du wurdest zum Jump 'n' Run §e" + name + "§7 teleportiert.");
        }

        public ItemStack buildItem() {
            ItemBuilder builder;
            if (this.material != null) {
                builder = new ItemBuilder(this.material).setName("§e" + this.name);
            } else {
                builder = new ItemBuilder(this.skull).setName("§e" + this.name);
            }

            if (lottery) builder.addLore("§8(Gewinnspiel-Relevant)");

            return builder.setHideStandardLore(true)
                    .addLore("", "§7» Teleportieren")
                    .getItem();
        }

        public boolean canClick(ClickType type) {
            return true;
        }

        public void onClick(GUI gui, InventoryClickEvent e) {
            TeleportManager.getInstance().teleport(gui.getPlayer(), this.options);
        }
    }
}
