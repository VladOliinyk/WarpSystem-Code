package net.nitrado.event.warppanel.warpgui.pages;

import de.codingair.codingapi.player.gui.inventory.v2.GUI;
import de.codingair.codingapi.player.gui.inventory.v2.Page;
import de.codingair.codingapi.player.gui.inventory.v2.buttons.Button;
import de.codingair.codingapi.tools.items.ItemBuilder;
import de.codingair.warpsystem.core.transfer.packets.spigot.utils.ServerPing;
import de.codingair.warpsystem.spigot.base.WarpSystem;
import net.md_5.bungee.api.ChatColor;
import net.nitrado.event.warppanel.warpgui.WarpPanel;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class ServerPage extends Page {
    private static final String[] SKULLS = new String[] {
            "7f24b7135789fe799df34594d6805f5112bee6232605ba6de215186ad94",
            "e2b35bda5ebdf135f4e71ce49726fbec5739f0adedf01c519e2aea7f51951ea2",
            "d58670551f7ff13afab239318a10bad2327ee742e157837ea1785ef443d3a576",
            "b0155d41e6865773f2ad2e9fe4cf82f2f880e54afe71a13bbe2390d4aa81f4f4",
            "80ee816121d114e1399ab9b37c460ae36dddf251fb1a9b7257f1acce3ff7852d",
            "d511a5ee4d17682a25f7e8a5da6ff7cd9ad9c4844c258a6de23e7f84f27f9b4",
            "1c15170fbe643f98c2baebf4137be8da7aed965f295aabacb5f888c6b1655b",
            "ebcd63ba973f616e8a1bfa427818e6f713df19614be1c50a546d4c67aeeaf",
            "4378b582d19ccc55b023eb82eda271bac4744fa2006cf5e190246e2b4d5d",
            "7ff264aa612359241ffdab906f8dec3c77ac6762e0695683be2b4dd5d5160982"};

    private static final String[] NAMES = new String[] {
            "Limette",
            "Apfel",
            "Melone",
            "Kiwi",
            "Mango",
            "Weintraube",
            "Ananas",
            "Erdbeere",
            "Zitrone",
            "Pfirsich"
    };

    private static final String[] COLORS = new String[] {
            "#5d930c",
            "#d24528",
            "#829e53",
            "#a28b54",
            "#be4715",
            "#b0586a",
            "#db8019",
            "#e44756",
            "#e8b814",
            "#e8604f"
    };

    private static final String QUESTION = "b4d7cc4dca986a53f1d6b52aaf376dc6acc73b8b287f42dc8fef5808bb5d76";

    public ServerPage(GUI gui, Page basic) {
        super(gui, basic);
        setTitle("Teleporter - " + WarpPanel.COLOR_NITRADO + "§lEvent Server");
    }

    public void buildItems() {
        int id = 0;
        int i;
        for (i = 0; i < 3; i++) {
            addButton(3 + i, 1, new PanelButton(ChatColor.of(COLORS[id]) + NAMES[id], SKULLS[id++], id, "nitem" + ((id < 10) ? "0" : "") + id, this.gui.getPlayer()));
        }
        for (i = 0; i < 5; i++) {
            if (i == 2) continue;
            addButton(2 + i, 2, new PanelButton(ChatColor.of(COLORS[id]) + NAMES[id], SKULLS[id++], id, "nitem" + ((id < 10) ? "0" : "") + id, this.gui.getPlayer()));
        }
        for (i = 0; i < 3; i++) {
            addButton(3 + i, 3, new PanelButton(ChatColor.of(COLORS[id]) + NAMES[id], SKULLS[id++], id, "nitem" + ((id < 10) ? "0" : "") + id, this.gui.getPlayer()));
        }

        String server = WarpSystem.getInstance().getCurrentServer();
        if (server.toLowerCase().contains("nitem")) {
            int nitemId = Integer.parseInt(server.replaceAll("\\D", ""));
            server = ChatColor.of(COLORS[nitemId]) + NAMES[nitemId];
        } else {
            String name = server.split("[0-9]", -1)[0];
            server = name + " " + server.replace(name, "");
            server = server.substring(0, 1).toUpperCase() + server.substring(1).toLowerCase();
        }

        final String finalServer = server;

        final ServerPing ping = WarpSystem.getInstance().getServerManager().getProperties(WarpSystem.getInstance().getCurrentServer());
        addButton(8, 2, new Button() {
            public ItemStack buildItem() {
                return (new ItemBuilder(QUESTION)).setName("§7Du bist hier: §e" + finalServer).addLore("§7Status: §aOnline").addLore("§7Spieler: " + ((ping == null) ? "§7?" : ((PanelButton.isFull(ping) ? "§c" : "§a") + ping.getPlayers())) + "§8/§7" + WarpPanel.MAX_PLAYER_COUNT_PER_SERVER).getItem();
            }


            public boolean canClick(ClickType type) {
                return false;
            }

            public void onClick(GUI gui, InventoryClickEvent e) {
            }
        });
    }
}
