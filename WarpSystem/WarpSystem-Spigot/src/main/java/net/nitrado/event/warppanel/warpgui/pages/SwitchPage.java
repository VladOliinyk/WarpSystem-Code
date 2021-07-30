package net.nitrado.event.warppanel.warpgui.pages;

import de.codingair.codingapi.player.gui.inventory.v2.GUI;
import de.codingair.codingapi.player.gui.inventory.v2.Page;
import de.codingair.codingapi.player.gui.inventory.v2.buttons.Button;
import de.codingair.codingapi.player.gui.inventory.v2.buttons.Item;
import de.codingair.codingapi.player.gui.inventory.v2.exceptions.PageAlreadyOpenedException;
import de.codingair.codingapi.tools.items.ItemBuilder;
import de.codingair.warpsystem.spigot.base.managers.TeleportManager;
import de.codingair.warpsystem.spigot.base.utils.teleport.TeleportOptions;
import de.codingair.warpsystem.spigot.features.spawn.managers.SpawnManager;
import de.codingair.warpsystem.spigot.features.spawn.utils.Spawn;
import net.nitrado.event.warppanel.warpgui.WarpPanel;
import net.nitrado.quests.QuestSystem;
import net.nitrado.quests.system.playerdata.Profile;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class SwitchPage extends Page {
    public SwitchPage(GUI gui) {
        super(gui);
    }

    public void buildItems() {
        ItemBuilder builder = (new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE)).setHideName(true);
        ItemStack item = builder.getItem();

        addLine(0, 0, 0, 4, new Item(item));
        addLine(8, 0, 8, 4, new Item(item));

        addButton(4, 3, new Button() {
            public ItemStack buildItem() {
                ItemBuilder builder = new ItemBuilder();

                builder.setType(Material.NETHER_STAR);
                builder.setName("§a§lSpawn");
                builder.addLore("", "§7» Teleportieren");

                return builder.getItem();
            }

            public boolean canClick(ClickType type) {
                return type == ClickType.LEFT;
            }

            public void onClick(GUI gui, InventoryClickEvent e) {
                Spawn spawn = SpawnManager.getInstance().getSpawn();
                spawn.perform(gui.getPlayer());
            }
        });

        addButton(2, 1, new Button() {
            public ItemStack buildItem() {
                ItemBuilder builder = new ItemBuilder();

                builder.setType(Material.LADDER);
                builder.setName("§3§lJump & Runs");
                builder.addLore("", "§7» Kategorie öffnen");

                return builder.getItem();
            }

            public boolean canClick(ClickType type) {
                return (type == ClickType.LEFT);
            }

            public void onClick(GUI gui, InventoryClickEvent e) {
                try {
                    gui.switchTo(JnRPage.class);
                } catch (PageAlreadyOpenedException pageAlreadyOpenedException) {
                }
            }
        });

        addButton(4, 1, new Button() {
            public ItemStack buildItem() {
                ItemBuilder builder = new ItemBuilder();

                builder.setType(Material.GOLD_NUGGET);
                builder.setCustomModel(2166435);
                builder.setName(WarpPanel.COLOR_NITRADO + "§lEvent Server");
                builder.addLore("", "§7» Kategorie öffnen");

                return builder.getItem();
            }

            public boolean canClick(ClickType type) {
                return (type == ClickType.LEFT);
            }

            public void onClick(GUI gui, InventoryClickEvent e) {
                try {
                    gui.switchTo(ServerPage.class);
                } catch (PageAlreadyOpenedException pageAlreadyOpenedException) {
                }
            }
        });

        addButton(6, 1, new Button() {
            public ItemStack buildItem() {
                ItemBuilder builder = new ItemBuilder();

                Profile profile = QuestSystem.getProfile(gui.getPlayer());
                if (profile != null) {
                    Location l = profile.getContinueLocation();

                    builder.setType(Material.WRITABLE_BOOK);
                    builder.setName("§c§lStory");

                    if (l != null) builder.addLore("", "§7» Zum Einstiegspunkt teleportieren");
                    else builder.addLore("", "§7» Kein §7Einstiegspunkt vorhanden");
                } else builder.addLore("", "§7» Kein §7Einstiegspunkt vorhanden");

                builder.setHideStandardLore(true);

                return builder.getItem();
            }

            public boolean canClick(ClickType type) {
                Profile profile = QuestSystem.getProfile(gui.getPlayer());
                if (profile == null) return false;

                Location l = profile.getContinueLocation();

                return type == ClickType.LEFT && l != null;
            }

            public void onClick(GUI gui, InventoryClickEvent e) {
                Profile profile = QuestSystem.getProfile(gui.getPlayer());
                if (profile == null) return;

                Location l = profile.getContinueLocation();
                if (l == null) return;

                TeleportOptions options = new TeleportOptions(l, "Story Einstiegspunkt");
                options.setSkip(true);
                TeleportManager.getInstance().teleport(gui.getPlayer(), options);
            }
        });
    }
}
