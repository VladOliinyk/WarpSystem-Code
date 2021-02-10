package de.codingair.warpsystem.spigot.versionfactory.handlers;

import de.codingair.codingapi.player.gui.inventory.gui.itembutton.ItemButtonOption;
import de.codingair.codingapi.player.gui.inventory.gui.simple.Button;
import de.codingair.codingapi.player.gui.inventory.gui.simple.SyncButton;
import de.codingair.codingapi.tools.items.ItemBuilder;
import de.codingair.codingapi.tools.items.XMaterial;
import de.codingair.codingapi.utils.TextAlignment;
import de.codingair.warpsystem.spigot.api.placeholders.PAPI;
import de.codingair.warpsystem.spigot.api.chatinput.ChatInputEvent;
import de.codingair.warpsystem.spigot.api.chatinput.SyncChatInputGUIButton;
import de.codingair.warpsystem.spigot.base.WarpSystem;
import de.codingair.warpsystem.spigot.base.guis.editor.Editor;
import de.codingair.warpsystem.spigot.base.guis.editor.StandardButtonOption;
import de.codingair.warpsystem.spigot.base.guis.editor.pages.DestinationPage;
import de.codingair.warpsystem.spigot.base.utils.Lang;
import de.codingair.warpsystem.spigot.base.utils.teleport.destinations.DestinationType;
import de.codingair.warpsystem.spigot.features.globalwarps.guis.GGlobalWarpList;
import de.codingair.warpsystem.spigot.features.simplewarps.SimpleWarp;
import de.codingair.warpsystem.spigot.features.simplewarps.guis.GSimpleWarpList;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class DestinationPageHandler {

    public DestinationPageHandler(DestinationPage page, Player p, boolean showOptions) {
        if (showOptions) new Options(page).setup(p);
        else new Normal(page).setup(p);
    }

    private static class Options {
        private final DestinationPage page;

        public Options(DestinationPage page) {
            this.page = page;
        }

        protected void setup(Player p) {
            ItemButtonOption option = new StandardButtonOption();

            page.addButton(new SyncChatInputGUIButton(1, 2, ClickType.LEFT) {
                @Override
                public void onEnter(ChatInputEvent e) {
                    page.getDestination().getCustomOptions().setCustomMessage(e.getText());

                    if (!e.getText().isEmpty()) {
                        Boolean send = page.getDestination().getCustomOptions().getMessage();
                        boolean sending = page.getOrigin().sendTeleportMessage();
                        if (send != null && !send) {
                            if (send == sending) page.getDestination().getCustomOptions().setMessage(true);
                            else page.getDestination().getCustomOptions().setMessage(null);
                        } else if (send == null && !sending) page.getDestination().getCustomOptions().setMessage(true);
                    }
                    e.setClose(true);
                }

                @Override
                public ItemStack craftItem() {
                    Boolean send = page.getDestination().getCustomOptions().getMessage();
                    boolean sending = send != null ? send : page.getOrigin().sendTeleportMessage();
                    ItemBuilder builder = new ItemBuilder(XMaterial.ENDER_EYE).setName("§6§n" + Lang.get("Teleport_Message") + "§8 (" + (sending ? "§7" + Lang.get("Enabled") : "§c" + Lang.get("Disabled")) + "§8)");

                    String message = page.getDestination().getCustomOptions().getCustomMessage();
                    if (message != null) message = PAPI.convert(message, p).replace("%player%", p.getName()).replace("%PLAYER%", p.getName());


                    List<String> msg = TextAlignment.lineBreak(Editor.ITEM_SUB_TITLE_COLOR + Lang.get("Current") + ": " + (message == null ? "§e" + Lang.get("Default") : "§7\"§f" + de.codingair.codingapi.utils.ChatColor.translateAlternateColorCodes('&', message) + "§7\""), 100);

                    builder.addLore(msg.remove(0));
                    if (!msg.isEmpty()) builder.addLore(msg);

                    builder.addLore("", Editor.ITEM_SUB_TITLE_COLOR + Lang.get("Leftclick") + ": §a" + Lang.get("Change") + " §8(§7" + Lang.get("Toggle") + "§8)");
                    if (message != null) builder.addLore(Editor.ITEM_SUB_TITLE_COLOR + Lang.get("Rightclick") + ": §c" + Lang.get("Remove"));

                    return builder.getItem();
                }

                @Override
                public boolean canTrigger(InventoryClickEvent e, ClickType trigger, Player player) {
                    if (trigger == ClickType.LEFT && page.getDestination().getCustomOptions().getCustomMessage() != null) {
                        TextComponent tc = new TextComponent(Lang.getPrefix() + "§7" + Lang.get("Teleport_Message") + ": ");
                        TextComponent click = new TextComponent("§e" + ChatColor.stripColor(Lang.get("Click_Hover")));
                        click.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[] {new TextComponent(Lang.get("Click_Hover"))}));
                        click.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, page.getDestination().getCustomOptions().getCustomMessage().replace('§', '&')));

                        tc.addExtra(click);
                        player.spigot().sendMessage(tc);
                    }

                    return super.canTrigger(e, trigger, player);
                }

                @Override
                public boolean canClick(ClickType click) {
                    if (click == ClickType.RIGHT) {
                        return page.getDestination().getCustomOptions().getCustomMessage() != null;
                    }

                    return click == ClickType.LEFT || click == ClickType.SHIFT_LEFT;
                }

                @Override
                public void onOtherClick(InventoryClickEvent e) {
                    if (e.getClick() == ClickType.RIGHT) {
                        page.getDestination().getCustomOptions().setCustomMessage(null);
                        update();
                    } else if (e.getClick() == ClickType.SHIFT_LEFT) {
                        Boolean send = page.getDestination().getCustomOptions().getMessage();
                        boolean sending = page.getOrigin().sendTeleportMessage();

                        if (send == null) page.getDestination().getCustomOptions().setMessage(!sending);
                        else if (send != sending) page.getDestination().getCustomOptions().setMessage(null);
                        else if (send == sending) page.getDestination().getCustomOptions().setMessage(!send);
                        update();
                    }
                }
            }.setOption(option));

            page.addButton(new SyncButton(2, 2) {
                @Override
                public ItemStack craftItem() {
                    ItemBuilder builder = new ItemBuilder(XMaterial.ARMOR_STAND).setName("§6§n" + Lang.get("Rotation") + "§8 (§7Yaw + Pitch§8)");
                    boolean b = page.getDestination().getCustomOptions().isRotation();

                    builder.addLore(Editor.ITEM_SUB_TITLE_COLOR + Lang.get("Current") + ": " + (b ? "§a" + Lang.get("Enabled") : "§c" + Lang.get("Disabled")));
                    builder.addLore("", Editor.ITEM_SUB_TITLE_COLOR + Lang.get("Leftclick") + ": §7" + Lang.get("Toggle"));
                    return builder.getItem();
                }

                @Override
                public boolean canClick(ClickType click) {
                    return click == ClickType.LEFT;
                }

                @Override
                public void onClick(InventoryClickEvent e, Player player) {
                    page.getDestination().getCustomOptions().setRotation(!page.getDestination().getCustomOptions().isRotation());
                    update();
                }
            }.setOption(option));

            page.addButton(new SyncButton(3, 2) {
                @Override
                public ItemStack craftItem() {
                    ItemBuilder builder = new ItemBuilder(XMaterial.CLOCK).setName("§6§n" + Lang.get("Teleport_Delay") + Lang.PREMIUM_LORE);

                    builder.addLore(Editor.ITEM_SUB_TITLE_COLOR + Lang.get("Current") + ": " + (page.getDestination().getCustomOptions().getDelay(-1) == -1 ? "§7" + WarpSystem.opt().getTeleportDelay() + " §8(§e" + Lang.get("Default") + "§8)" : "§7" + page.getDestination().getCustomOptions().getDelay(-1)));
                    builder.addLore("", Editor.ITEM_SUB_TITLE_COLOR + Lang.get("Leftclick") + ": §c- §8(§7" + Lang.get("Shift") + "§8)");
                    builder.addLore(Editor.ITEM_SUB_TITLE_COLOR + Lang.get("Rightclick") + ": §a+ §8(§7" + Lang.get("Shift") + "§8)");
                    return builder.getItem();
                }

                @Override
                public boolean canClick(ClickType click) {
                    return click == ClickType.LEFT || click == ClickType.SHIFT_LEFT || click == ClickType.RIGHT || click == ClickType.SHIFT_RIGHT;
                }

                @Override
                public void onClick(InventoryClickEvent e, Player player) {
                    Lang.PREMIUM_CHAT(player);
                }
            }.setOption(option));

            page.addButton(new SyncButton(4, 2) {
                @Override
                public ItemStack craftItem() {
                    String current = page.getDestination().getCustomOptions().getColoredDisplayName();
                    ItemBuilder builder = new ItemBuilder(XMaterial.NAME_TAG).setName("§6§n" + Lang.get("Teleport_Name") + Lang.PREMIUM_LORE);

                    builder.addLore(Editor.ITEM_SUB_TITLE_COLOR + Lang.get("Current") + ": " + (current == null ? "§e" + Lang.get("Default") : "§7\"§f" + current + "§7\""));

                    builder.addLore("", Editor.ITEM_SUB_TITLE_COLOR + Lang.get("Leftclick") + ": §a" + (current == null ? Lang.get("Set") : Lang.get("Change")));
                    if (current != null) builder.addLore(Editor.ITEM_SUB_TITLE_COLOR + Lang.get("Rightclick") + ": §c" + Lang.get("Remove"));

                    return builder.getItem();
                }

                @Override
                public boolean canClick(ClickType click) {
                    return click == ClickType.LEFT || click == ClickType.SHIFT_LEFT || click == ClickType.RIGHT || click == ClickType.SHIFT_RIGHT;
                }

                @Override
                public void onClick(InventoryClickEvent e, Player player) {
                    Lang.PREMIUM_CHAT(player);
                }
            }.setOption(option));

            page.addButton(new SyncButton(5, 2) {
                @Override
                public ItemStack craftItem() {
                    ItemBuilder builder = new ItemBuilder(XMaterial.BLAZE_ROD).setName("§6§n" + Lang.get("Particle_Effects"));
                    boolean b = page.getDestination().getCustomOptions().isParticles();

                    builder.addLore(Editor.ITEM_SUB_TITLE_COLOR + Lang.get("Current") + ": " + (b ? "§a" + Lang.get("Enabled") : "§c" + Lang.get("Disabled")));
                    builder.addLore("", Editor.ITEM_SUB_TITLE_COLOR + Lang.get("Leftclick") + ": §7" + Lang.get("Toggle"));
                    return builder.getItem();
                }

                @Override
                public boolean canClick(ClickType click) {
                    return click == ClickType.LEFT || click == ClickType.SHIFT_LEFT || click == ClickType.RIGHT || click == ClickType.SHIFT_RIGHT;
                }

                @Override
                public void onClick(InventoryClickEvent e, Player player) {
                    Lang.PREMIUM_CHAT(player);
                }
            }.setOption(option));
        }
    }

    private static class Normal {
        private final DestinationPage page;

        public Normal(DestinationPage page) {
            this.page = page;
        }

        protected void setup(Player p) {
            ItemButtonOption option = new StandardButtonOption();

            int slot = 1;
            if (page.getExtra() != null && page.getExtra().length > 0) {
                for (Button button : page.getExtra()) {
                    if (slot == 7) break;
                    button.setSlot(slot++ + 18);
                    button.setOption(option);
                    page.addButton(button);
                }
            }

            page.addButton(new SyncButton(slot++, 2) {
                @Override
                public ItemStack craftItem() {
                    String name = null;
                    if (page.getDestination().getType() == DestinationType.SimpleWarp) name = page.getDestination().getId();

                    List<String> lore = new ArrayList<>();
                    if (name != null) lore.add("§3" + Lang.get("Rightclick") + ": §c" + Lang.get("Remove"));
                    else lore.add("§3" + Lang.get("Shift_Leftclick") + ": §a" + Lang.get("Create") + Lang.PREMIUM_LORE);

                    ItemBuilder builder = new ItemBuilder(XMaterial.ENDER_PEARL).setName(Editor.ITEM_TITLE_COLOR + Lang.get("SimpleWarps"))
                            .setLore("§3" + Lang.get("Current") + ": " + (name == null ? "§c" + Lang.get("Not_Set") : "§7'§f" + ChatColor.translateAlternateColorCodes('&', name.replace("_", " ")) + "§7'"),
                                    "", "§3" + Lang.get("Leftclick") + ": §a" + (name == null ? Lang.get("Set") : Lang.get("Change")))
                            .addLore(lore);

                    builder.addLore(" ");
                    builder.addLore("§6" + Lang.get("Max_Random_Offset") + Lang.PREMIUM_LORE);
                    builder.addLore("§3" + Lang.get("Shift_Rightclick") + ": §b" + Lang.get("Edit"));
                    builder.addLore("  §8» §7X: §e" + (page.getDestination().getOffsetX() == 0 ? "0" : "-" + page.getDestination().getOffsetX() + " - " + page.getDestination().getOffsetX()));
                    builder.addLore("  §8» §7Y: §e" + (page.getDestination().getOffsetY() == 0 ? "0" : "0 - " + page.getDestination().getOffsetY()));
                    builder.addLore("  §8» §7Z: §e" + (page.getDestination().getOffsetZ() == 0 ? "0" : "-" + page.getDestination().getOffsetZ() + " - " + page.getDestination().getOffsetZ()));

                    return builder.getItem();
                }

                @Override
                public void onClick(InventoryClickEvent e, Player player) {
                    if (e.isLeftClick()) {
                        if (e.isShiftClick()) {
                            Lang.PREMIUM_CHAT(player);
                        } else {

                            page.getLast().changeGUI(new GSimpleWarpList(p) {
                                @Override
                                public void onClick(SimpleWarp value, ClickType clickType) {
                                    page.getDestination().setId(value.getName());
                                    page.getDestination().setType(DestinationType.SimpleWarp);
                                    page.getDestination().setAdapter(DestinationType.SimpleWarp.getInstance());
                                    updateDestinationButtons();

                                    fallBack();
                                }

                                @Override
                                public void onClose() {
                                    fallBack();
                                }

                                @Override
                                public void buildItemDescription(List<String> lore) {
                                    lore.add("");
                                    lore.add("§3" + Lang.get("Leftclick") + ": §b" + Lang.get("Choose"));
                                }
                            }, true);
                        }
                    } else if (e.isRightClick()) {
                        if (e.isShiftClick()) {
                            Lang.PREMIUM_CHAT(player);
                        } else {
                            page.getDestination().setId(null);
                            page.getDestination().setAdapter(null);
                            page.getDestination().setType(null);

                            updateDestinationButtons();
                        }
                    }
                }
            }.setOption(option));

            if (WarpSystem.getInstance().isOnProxy()) {
                page.addButton(new SyncButton(slot++, 2) {
                    @Override
                    public ItemStack craftItem() {
                        String name = null;
                        if (page.getDestination().getType() == DestinationType.GlobalWarp) name = page.getDestination().getId();

                        List<String> lore = name == null ? null : new ArrayList<>();
                        if (lore != null) lore.add("§3" + Lang.get("Rightclick") + ": §c" + Lang.get("Remove"));

                        return new ItemBuilder(XMaterial.ENDER_EYE).setName(Editor.ITEM_TITLE_COLOR + Lang.get("GlobalWarps"))
                                .setLore("§3" + Lang.get("Current") + ": " + (name == null ? "§c" + Lang.get("Not_Set") : "§7'§f" + ChatColor.translateAlternateColorCodes('&', name) + "§7'"),
                                        "", "§3" + Lang.get("Leftclick") + ": §a" + (name == null ? Lang.get("Set") : Lang.get("Change")))
                                .addLore(lore)
                                .getItem();
                    }

                    @Override
                    public void onClick(InventoryClickEvent e, Player player) {
                        if (e.isLeftClick()) {
                            page.getLast().setClosingForGUI(true);
                            page.getLast().changeGUI(new GGlobalWarpList(player) {
                                @Override
                                public void onClick(String warp, ClickType clickType) {
                                    page.getDestination().setId(warp);
                                    page.getDestination().setType(DestinationType.GlobalWarp);
                                    page.getDestination().setAdapter(DestinationType.GlobalWarp.getInstance());
                                    updateDestinationButtons();

                                    this.setClosingForGUI(true);
                                    page.getLast().open();
                                }

                                @Override
                                public void onClose() {
                                }

                                @Override
                                public void buildItemDescription(List<String> lore) {
                                    lore.add("");
                                    lore.add("§3" + Lang.get("Leftclick") + ": §b" + Lang.get("Choose"));
                                }
                            }, true);
                        } else if (e.isRightClick()) {
                            page.getDestination().setId(null);
                            page.getDestination().setAdapter(null);
                            page.getDestination().setType(null);

                            updateDestinationButtons();
                        }
                    }
                }.setOption(option));

                page.addButton(new SyncButton(slot++, 2) {
                    @Override
                    public ItemStack craftItem() {
                        return new ItemBuilder(XMaterial.ENDER_CHEST).setName(Editor.ITEM_TITLE_COLOR + Lang.get("Server") + Lang.PREMIUM_LORE)
                                .setLore("§3" + Lang.get("Current") + ": " + "§c" + Lang.get("Not_Set"))
                                .addLore("", "§3" + Lang.get("Leftclick") + ": §a" + (Lang.get("Set")))
                                .getItem();
                    }

                    @Override
                    public void onClick(InventoryClickEvent e, Player player) {
                        Lang.PREMIUM_CHAT(player);
                    }
                }.setOption(option));
            }
        }

        public void updateDestinationButtons() {
            for (int i = 1; i < 8; i++) {
                Button button = page.getButton(i, 2);
                if (button instanceof SyncButton) {
                    ((SyncButton) button).update();
                }
            }
        }
    }
}
