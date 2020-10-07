package de.codingair.warpsystem.spigot.base.guis.editor.buttons;

import de.codingair.codingapi.player.gui.anvil.AnvilClickEvent;
import de.codingair.codingapi.player.gui.anvil.AnvilCloseEvent;
import de.codingair.codingapi.player.gui.anvil.AnvilSlot;
import de.codingair.codingapi.player.gui.inventory.gui.simple.SyncAnvilGUIButton;
import de.codingair.codingapi.tools.items.ItemBuilder;
import de.codingair.codingapi.tools.items.XMaterial;
import de.codingair.codingapi.utils.ChatColor;
import de.codingair.warpsystem.spigot.base.language.Lang;
import de.codingair.warpsystem.spigot.base.utils.featureobjects.FeatureObject;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public abstract class LoreButton extends SyncAnvilGUIButton {
    private final ItemBuilder toChange;
    private final FeatureObject featureObject;
    private String editing = null;

    public LoreButton(int x, int y, ItemBuilder toChange, FeatureObject featureObject) {
        super(x, y, ClickType.LEFT, ClickType.SHIFT_RIGHT);

        this.featureObject = featureObject;
        this.toChange = toChange;
        update(false);
    }

    public abstract void updatingLore(ItemBuilder toChange);

    @Override
    public ItemStack craftItem() {
        if(toChange == null) return new ItemStack(Material.AIR);

        List<String> loreOfItem = toChange.getLore();
        List<String> lore = new ArrayList<>();
        if(loreOfItem == null) lore = null;
        else {
            for(String s : loreOfItem) {
                lore.add("§7- '§f" + prepareLine(s) + "§7'");
            }
        }

        List<String> lore2 = new ArrayList<>();
        if(lore != null && !lore.isEmpty()) lore2.add("§3" + Lang.get("Rightclick") + ": §c" + Lang.get("Remove") + " §8(§e" + Lang.get("Edit") + "§8)");

        return new ItemBuilder(XMaterial.PAPER)
                .setName("§6§n" + Lang.get("Description"))
                .setLore("§3" + Lang.get("Current") + ": " + (lore == null || lore.isEmpty() ? "§c" + Lang.get("Not_Set") : ""))
                .addLore(lore)
                .addLore("", "§3" + Lang.get("Leftclick") + ": §a" + Lang.get("Add_Line"))
                .addLore(lore2)
                .getItem();
    }

    @Override
    public boolean canClick(ClickType click) {
        if(click == ClickType.SHIFT_RIGHT && toChange.getLore().isEmpty()) return false;
        return super.canClick(click);
    }

    protected String prepareLine(String s) {
        if(featureObject == null) return ChatColor.translateAll('&', s);
        else return featureObject.prepareLine(s);
    }

    @Override
    public ItemStack craftAnvilItem(ClickType trigger) {
        if(trigger == ClickType.SHIFT_RIGHT) {
            return new ItemBuilder(Material.PAPER).setName(editing = toChange.getLore().remove(toChange.getLore().size() - 1).replace("§", "&")).getItem();
        } else return new ItemBuilder(Material.PAPER).setName(Lang.get("Line") + "...").getItem();
    }

    @Override
    public void onOtherClick(InventoryClickEvent e) {
        if(e.getClick() == ClickType.RIGHT) {
            if(toChange.getLore() != null && !toChange.getLore().isEmpty()) toChange.getLore().remove(toChange.getLore().size() - 1);
            updatingLore(toChange);
            update();
        }
    }

    @Override
    public void onClick(AnvilClickEvent e) {
        if(!e.getSlot().equals(AnvilSlot.OUTPUT)) return;

        String input = e.getInput();

        if(input == null) {
            e.getPlayer().sendMessage(Lang.getPrefix() + Lang.get("Enter_Lore"));
            return;
        }

        editing = null;
        e.setClose(true);

        toChange.addLore(ChatColor.WHITE + ChatColor.translateAll('&', input));
        updatingLore(toChange);
        update();
    }

    @Override
    public void onClose(AnvilCloseEvent e) {
        System.out.println("onClose: " + editing);
        if(editing != null) {
            toChange.addLore(editing);
            updatingLore(toChange);
            update();
            editing = null;
        }
    }
}
