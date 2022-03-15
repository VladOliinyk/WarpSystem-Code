package de.codingair.warpsystem.spigot.versionfactory.handlers;

import de.codingair.codingapi.player.gui.anvil.*;
import de.codingair.codingapi.player.gui.inventory.gui.itembutton.ItemButtonOption;
import de.codingair.codingapi.player.gui.inventory.gui.simple.Button;
import de.codingair.codingapi.player.gui.inventory.gui.simple.SyncAnvilGUIButton;
import de.codingair.codingapi.player.gui.inventory.gui.simple.SyncButton;
import de.codingair.codingapi.player.gui.inventory.gui.simple.SyncHotbarGUIButton;
import de.codingair.codingapi.tools.items.ItemBuilder;
import de.codingair.codingapi.tools.items.XMaterial;
import de.codingair.codingapi.utils.Node;
import de.codingair.codingapi.utils.TextAlignment;
import de.codingair.warpsystem.api.destinations.utils.IDestinationOptions;
import de.codingair.warpsystem.core.transfer.packets.spigot.RequestServerStatusPacket;
import de.codingair.warpsystem.spigot.api.chatinput.ChatInputEvent;
import de.codingair.warpsystem.spigot.api.chatinput.SyncChatInputGUIButton;
import de.codingair.warpsystem.spigot.api.placeholders.PAPI;
import de.codingair.warpsystem.spigot.base.WarpSystem;
import de.codingair.warpsystem.spigot.base.guis.editor.Editor;
import de.codingair.warpsystem.spigot.base.guis.editor.StandardButtonOption;
import de.codingair.warpsystem.spigot.base.guis.editor.pages.DestinationPage;
import de.codingair.warpsystem.spigot.base.utils.Lang;
import de.codingair.warpsystem.spigot.base.utils.teleport.destinations.Destination;
import de.codingair.warpsystem.spigot.base.utils.teleport.destinations.DestinationType;
import de.codingair.warpsystem.spigot.base.utils.teleport.destinations.adapters.ServerAdapter;
import de.codingair.warpsystem.spigot.base.utils.teleport.destinations.adapters.VelocityAdapter;
import de.codingair.warpsystem.spigot.features.globalwarps.guis.GGlobalWarpList;
import de.codingair.warpsystem.spigot.features.simplewarps.SimpleWarp;
import de.codingair.warpsystem.spigot.features.simplewarps.guis.GSimpleWarpList;
import de.codingair.warpsystem.spigot.features.simplewarps.managers.SimpleWarpManager;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DestinationPageHandler {

    public DestinationPageHandler(DestinationPage page, Player p, boolean showOptions) {
        if (showOptions) new Options(page).setup(p);
        else new Normal(page).setup(p);
    }

    private static double trim(double d) {
        return ((double) (int) (d * 100)) / 100;
    }

    @NotNull
    private static String getBooleanDescription(boolean enabled, boolean standard) {
        if (standard) return  "§7" + getBooleanDescription(standard) + " §8(§e" + Lang.get("Default") + "§8)";
        return getBooleanDescription(enabled);
    }

    @NotNull
    private static String getBooleanDescription(boolean enabled) {
        if (enabled) return  "§a" + Lang.get("Enabled");
        else return  "§c" + Lang.get("Disabled");
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
                    ItemBuilder builder = new ItemBuilder(XMaterial.ENDER_EYE).setName("§6§n" + Lang.get("Teleport_Message") + "§8 (" + (sending ? (send == null ? "§7" : "§a") + Lang.get("Enabled") : (send == null ? "§7" : "§c") + Lang.get("Disabled")) + "§8)");

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
                        else page.getDestination().getCustomOptions().setMessage(!send);
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
                    //PREMIUM
                    ItemBuilder builder = new ItemBuilder(XMaterial.CLOCK).setName("§6§n" + Lang.get("Teleport_Delay"));

                    builder.addLore(Editor.ITEM_SUB_TITLE_COLOR + Lang.get("Current") + ": " + (page.getDestination().getCustomOptions().getDelay(-1) == -1 ? "§7" + WarpSystem.opt().getTeleportDelay() + " §8(§e" + Lang.get("Default") + "§8)" : "§7" + page.getDestination().getCustomOptions().getDelay(-1)));
                    builder.addLore("", Editor.ITEM_SUB_TITLE_COLOR + Lang.get("Leftclick") + ": §c- §8(§7" + Lang.get("Shift") + "§8)");
                    builder.addLore(Editor.ITEM_SUB_TITLE_COLOR + Lang.get("Rightclick") + ": §a+ §8(§7" + Lang.get("Shift") + "§8)");
                    return builder.getItem();
                }

                @Override
                public boolean canClick(ClickType click) {
                    int sys = WarpSystem.opt().getTeleportDelay();
                    Integer i = page.getDestination().getCustomOptions().getDelay(sys);
                    if (click == ClickType.LEFT) i--;
                    else if (click == ClickType.SHIFT_LEFT) i -= 5;
                    else if (click == ClickType.RIGHT) i++;
                    else if (click == ClickType.SHIFT_RIGHT) i += 5;

                    return (click == ClickType.LEFT && i >= 0) || (click == ClickType.SHIFT_LEFT && i > -5) || (click == ClickType.RIGHT && i <= 60) || (click == ClickType.SHIFT_RIGHT && i < 65);
                }

                @Override
                public void onClick(InventoryClickEvent e, Player player) {
                    int sys = WarpSystem.opt().getTeleportDelay();
                    Integer i = page.getDestination().getCustomOptions().getDelay(sys);
                    if (e.getClick() == ClickType.LEFT) i--;
                    else if (e.getClick() == ClickType.SHIFT_LEFT) i -= 5;
                    else if (e.getClick() == ClickType.RIGHT) i++;
                    else if (e.getClick() == ClickType.SHIFT_RIGHT) i += 5;

                    i = Math.max(Math.min(i, 60), 0);

                    if (i == sys) i = null;
                    page.getDestination().getCustomOptions().setDelay(i);
                    update();
                }
            }.setOption(option));

            page.addButton(new SyncChatInputGUIButton(4, 2, ClickType.LEFT) {
                @Override
                public void onEnter(ChatInputEvent e) {
                    if (e.getText().isEmpty()) page.getDestination().getCustomOptions().setDisplayName(null);
                    else page.getDestination().getCustomOptions().setDisplayName(e.getText());
                    e.setClose(true);
                }

                @Override
                public ItemStack craftItem() {
                    String current = page.getDestination().getCustomOptions().getColoredDisplayName();
                    ItemBuilder builder = new ItemBuilder(XMaterial.NAME_TAG).setName("§6§n" + Lang.get("Teleport_Name"));

                    builder.addLore(Editor.ITEM_SUB_TITLE_COLOR + Lang.get("Current") + ": " + (current == null ? "§e" + Lang.get("Default") : "§7\"§f" + current + "§7\""));

                    builder.addLore("", Editor.ITEM_SUB_TITLE_COLOR + Lang.get("Leftclick") + ": §a" + (current == null ? Lang.get("Set") : Lang.get("Change")));
                    if (current != null) builder.addLore(Editor.ITEM_SUB_TITLE_COLOR + Lang.get("Rightclick") + ": §c" + Lang.get("Remove"));

                    return builder.getItem();
                }

                @Override
                public boolean canClick(ClickType click) {
                    if (click == ClickType.RIGHT) {
                        return page.getDestination().getCustomOptions().getDisplayName() != null;
                    }

                    return click == ClickType.LEFT;
                }

                @Override
                public void onOtherClick(InventoryClickEvent e) {
                    if (e.getClick() == ClickType.RIGHT) {
                        page.getDestination().getCustomOptions().setDisplayName(null);
                        update();
                    }
                }
            }.setOption(option));

            page.addButton(new SyncButton(5, 2) {
                @Override
                public ItemStack craftItem() {
                    ItemBuilder builder = new ItemBuilder(XMaterial.BLAZE_ROD).setName("§6§n" + Lang.get("Particle_Effects"));

                    IDestinationOptions options = page.getDestination().getCustomOptions();
                    boolean enabled = options.isParticles();
                    boolean standard = options.getParticles() == null;

                    String description = getBooleanDescription(enabled, standard);

                    builder.addLore(Editor.ITEM_SUB_TITLE_COLOR + Lang.get("Current") + ": " + description);
                    builder.addLore("", Editor.ITEM_SUB_TITLE_COLOR + Lang.get("Leftclick") + ": §7" + Lang.get("Toggle"));
                    return builder.getItem();
                }

                @Override
                public boolean canClick(ClickType click) {
                    return click == ClickType.LEFT;
                }

                @Override
                public void onClick(InventoryClickEvent e, Player player) {
                    page.getDestination().getCustomOptions().setParticles(!page.getDestination().getCustomOptions().isParticles());
                    update();
                }
            }.setOption(option));

            page.addButton(new SyncButton(6, 2) {
                @Override
                public ItemStack craftItem() {
                    ItemBuilder builder = new ItemBuilder(XMaterial.FEATHER).setName("§6§n" + Lang.get("Safe_Teleport"));

                    IDestinationOptions options = page.getDestination().getCustomOptions();
                    boolean enabled = options.isSafeTP();
                    boolean standard = options.getSafeTP() == null;

                    String description = getBooleanDescription(enabled, standard);

                    builder.addLore(Editor.ITEM_SUB_TITLE_COLOR + Lang.get("Current") + ": " + description);
                    builder.addLore("", Editor.ITEM_SUB_TITLE_COLOR + Lang.get("Leftclick") + ": §7" + Lang.get("Toggle"));
                    return builder.getItem();
                }

                @Override
                public boolean canClick(ClickType click) {
                    return click == ClickType.LEFT;
                }

                @Override
                public void onClick(InventoryClickEvent e, Player player) {
                    page.getDestination().getCustomOptions().setSafeTP(!page.getDestination().getCustomOptions().isSafeTP());
                    update();
                }
            }.setOption(option));
        }
    }

    private static class Normal {
        private final DestinationPage page;
        private String server = null;
        private boolean pinging = false;
        private boolean online = false;

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
                private int editingOffset = 0;

                @Override
                public ItemStack craftItem() {
                    String name = null;
                    if (page.getDestination().getType() == DestinationType.SimpleWarp) name = page.getDestination().getId();

                    List<String> lore = new ArrayList<>();
                    if (editingOffset == 0) {
                        lore.add("");
                        lore.add("§3" + Lang.get("Leftclick") + ": §a" + (name == null ? Lang.get("Set") : Lang.get("Change")));
                        if (name != null) lore.add("§3" + Lang.get("Rightclick") + ": §c" + Lang.get("Remove"));
                        else lore.add("§3" + Lang.get("Shift_Leftclick") + ": §a" + Lang.get("Create"));
                    }

                    ItemBuilder builder = new ItemBuilder(XMaterial.ENDER_PEARL).setName(Editor.ITEM_TITLE_COLOR + Lang.get("SimpleWarps"))
                            .setLore("§3" + Lang.get("Current") + ": " + (name == null ? "§c" + Lang.get("Not_Set") : "§7'§f" + ChatColor.translateAlternateColorCodes('&', name.replace("_", " ")) + "§7'"))
                            .addLore(lore);

                    builder.addLore(" ");
                    builder.addLore("§6" + (editingOffset == 0 ? "" : "§n") + Lang.get("Max_Random_Offset"));

                    if (editingOffset != 0) {
                        double value = editingOffset == 1 ? page.getDestination().getOffsetX() : editingOffset == 2 ? page.getDestination().getOffsetY() : page.getDestination().getOffsetZ();

                        builder.addLore(" ");
                        builder.addLore("§3" + Lang.get("Leftclick") + ": §" + (value == 0 ? "c" : "a") + Lang.get("Reduce"));
                        builder.addLore("§3" + Lang.get("Rightclick") + ": §" + (value == 100 ? "c" : "a") + Lang.get("Enlarge"));
                        builder.addLore("§3" + Lang.get("Shift_Leftclick") + ": §a" + Lang.get("Choose"));
                    }

                    builder.addLore("§3" + Lang.get("Shift_Rightclick") + ": §b" + (editingOffset == 0 ? Lang.get("Edit") : "↓ §8(§7" + Lang.get("Close") + "§8)"));

                    if (editingOffset != 0) builder.addLore(" ");

                    builder.addLore("  §8» §7X: §e" + page.getDestination().getOffsetX() + (editingOffset == 1 ? " §c§l«" : ""));
                    builder.addLore("  §8» §7Y: §e" + page.getDestination().getOffsetY() + (editingOffset == 2 ? " §c§l«" : ""));
                    builder.addLore("  §8» §7Z: §e" + page.getDestination().getOffsetZ() + (editingOffset == 3 ? " §c§l«" : ""));

                    return builder.getItem();
                }

                @Override
                public void onClick(InventoryClickEvent e, Player player) {
                    if (editingOffset == 0) {
                        if (e.isLeftClick()) {
                            page.getLast().setClosingForGUI(true);
                            if (e.isShiftClick()) {
                                AnvilGUI.openAnvil(WarpSystem.getInstance(), player, new AnvilListener() {
                                    @Override
                                    public void onClick(AnvilClickEvent e) {
                                        if (e.getSlot() == AnvilSlot.OUTPUT) {
                                            String input = e.getInput();

                                            if (input == null) {
                                                e.getPlayer().sendMessage(Lang.getPrefix() + Lang.get("Enter_Name"));
                                                return;
                                            }

                                            if (SimpleWarpManager.getInstance().existsWarp(input)) {
                                                e.getPlayer().sendMessage(Lang.getPrefix() + Lang.get("Name_Already_Exists"));
                                                return;
                                            }

                                            player.sendMessage(Lang.getPrefix() + Lang.get("SimpleWarp_Created").replace("%WARP%", ChatColor.translateAlternateColorCodes('&', input)));
                                            SimpleWarp w;
                                            SimpleWarpManager.getInstance().addWarp(w = new SimpleWarp(player, input, null));

                                            page.getDestination().setId(w.getName());
                                            page.getDestination().setType(DestinationType.SimpleWarp);
                                            page.getDestination().setAdapter(DestinationType.SimpleWarp.getInstance());
                                            page.updateDestinationButtons();

                                            e.setClose(true);
                                            playSound(e.getClickType(), player);
                                        }
                                    }

                                    @Override
                                    public void onClose(AnvilCloseEvent e) {
                                        e.setPost(() -> page.getLast().open());
                                    }
                                }, new ItemBuilder(XMaterial.NAME_TAG).setName(Lang.get("Name") + "...").getItem());
                            } else {
                                page.getLast().changeGUI(new GSimpleWarpList(p) {
                                    @Override
                                    public void onClick(SimpleWarp value, ClickType clickType) {
                                        page.getDestination().setId(value.getName());
                                        page.getDestination().setType(DestinationType.SimpleWarp);
                                        page.getDestination().setAdapter(DestinationType.SimpleWarp.getInstance());
                                        page.updateDestinationButtons();

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
                                editingOffset++;
                                if (editingOffset == 4) editingOffset = 0;
                                update();
                            } else {
                                page.getDestination().setId(null);
                                page.getDestination().setAdapter(null);
                                page.getDestination().setType(null);

                                page.updateDestinationButtons();
                            }
                        }
                    } else {
                        if (e.isLeftClick()) {
                            if (e.isShiftClick()) {
                                page.getLast().setClosingForGUI(true);
                                AnvilGUI.openAnvil(WarpSystem.getInstance(), player, new AnvilListener() {
                                    @Override
                                    public void onClick(AnvilClickEvent e) {
                                        if (e.getSlot() == AnvilSlot.OUTPUT) {
                                            String input = e.getInput();

                                            if (input == null) {
                                                e.getPlayer().sendMessage(Lang.getPrefix() + Lang.get("Enter_A_Positive_Number"));
                                                return;
                                            }

                                            input = input.replace(",", ".");
                                            double value;

                                            try {
                                                value = Double.parseDouble(input);
                                            } catch (NumberFormatException ex) {
                                                e.getPlayer().sendMessage(Lang.getPrefix() + Lang.get("Enter_A_Positive_Number"));
                                                return;
                                            }

                                            if (value < 0) {
                                                e.getPlayer().sendMessage(Lang.getPrefix() + Lang.get("Enter_A_Positive_Number"));
                                                return;
                                            }

                                            if (value > 100) value = 100;
                                            value = trim(value);

                                            if (editingOffset == 1) {
                                                page.getDestination().setOffsetX(value);
                                            } else if (editingOffset == 2) {
                                                page.getDestination().setOffsetY(value);
                                            } else if (editingOffset == 3) {
                                                page.getDestination().setOffsetZ(value);
                                            }

                                            update();

                                            e.setClose(true);
                                            playSound(e.getClickType(), player);
                                        }
                                    }

                                    @Override
                                    public void onClose(AnvilCloseEvent e) {
                                        e.setPost(() -> page.getLast().open());
                                    }
                                }, new ItemBuilder(XMaterial.NAME_TAG).setName("" + (editingOffset == 1 ? page.getDestination().getOffsetX() : editingOffset == 2 ? page.getDestination().getOffsetY() : page.getDestination().getOffsetZ())).getItem());
                            } else {
                                if (editingOffset == 1) {
                                    page.getDestination().setOffsetX(trim(page.getDestination().getOffsetX() - 1));
                                    if (page.getDestination().getOffsetX() < 0) page.getDestination().setOffsetX(0);
                                } else if (editingOffset == 2) {
                                    page.getDestination().setOffsetY(trim(page.getDestination().getOffsetY() - 1));
                                    if (page.getDestination().getOffsetY() < 0) page.getDestination().setOffsetY(0);
                                } else if (editingOffset == 3) {
                                    page.getDestination().setOffsetZ(trim(page.getDestination().getOffsetZ() - 1));
                                    if (page.getDestination().getOffsetZ() < 0) page.getDestination().setOffsetZ(0);
                                }

                                update();
                            }
                        } else if (e.isRightClick()) {
                            if (e.isShiftClick()) {
                                editingOffset++;
                                if (editingOffset == 4) editingOffset = 0;
                            } else {
                                if (editingOffset == 1) {
                                    page.getDestination().setOffsetX(trim(page.getDestination().getOffsetX() + 1));
                                    if (page.getDestination().getOffsetX() > 100) page.getDestination().setOffsetX(0);
                                } else if (editingOffset == 2) {
                                    page.getDestination().setOffsetY(trim(page.getDestination().getOffsetY() + 1));
                                    if (page.getDestination().getOffsetY() > 100) page.getDestination().setOffsetY(0);
                                } else if (editingOffset == 3) {
                                    page.getDestination().setOffsetZ(trim(page.getDestination().getOffsetZ() + 1));
                                    if (page.getDestination().getOffsetZ() > 100) page.getDestination().setOffsetZ(0);
                                }

                            }
                            update();
                        }
                    }
                }
            }.setOption(option));

            if (WarpSystem.getInstance().isProxyConnected()) {
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
                                    page.updateDestinationButtons();

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

                            page.updateDestinationButtons();
                        }
                    }
                }.setOption(option));

                page.addButton(new SyncAnvilGUIButton(slot++, 2, ClickType.LEFT) {
                    @Override
                    public ItemStack craftItem() {
                        String name = null;
                        ServerAdapter serverAdapter = null;

                        if (page.getDestination().getType() == DestinationType.Server) {
                            serverAdapter = (ServerAdapter) page.getDestination().getAdapter();
                            name = serverAdapter.getServer();
                        }

                        if (!Objects.equals(server, name)) {
                            server = name;
                            if (server != null) {
                                pinging = true;
                                WarpSystem.getDataHandler().send(new RequestServerStatusPacket(server), p).thenAccept(booleanPacket -> {
                                    pinging = false;
                                    online = booleanPacket.getBoolean();
                                    update();
                                });
                            }
                        }

                        ItemBuilder builder = new ItemBuilder(XMaterial.ENDER_CHEST).setName(Editor.ITEM_TITLE_COLOR + Lang.get("Server"));

                        builder.setLore("§3" + Lang.get("Current") + ": " + (name == null ? "§c" + Lang.get("Not_Set") : "§7'§f" + ChatColor.translateAlternateColorCodes('&', name) + "§7'"));

                        if (serverAdapter != null && serverAdapter.getServer() != null) {
                            builder.addLore("§3" + Lang.get("Status") + ": " + (pinging ? "§7" + Lang.get("Pinging") + "..." : (online ? "§a" + Lang.get("Online") : "§c" + Lang.get("Offline"))));
                            builder.addLore("§3" + Lang.get("Keep_Position") + ": " + (serverAdapter.isKeepPosition() ? "§a" + Lang.get("Yes") : "§c" + Lang.get("No")));
                        }

                        builder.addLore("", "§3" + Lang.get("Leftclick") + ": §a" + (name == null ? Lang.get("Set") : Lang.get("Toggle")));

                        if (name != null) {
                            builder.addLore("§3" + Lang.get("Rightclick") + ": §c" + Lang.get("Remove"),
                                    "",
                                    "§3" + Lang.get("Shift_Leftclick") + ": §b" + Lang.get("Refresh"));
                        }

                        return builder.getItem();
                    }

                    @Override
                    public void onClick(AnvilClickEvent e) {
                        if (!e.getSlot().equals(AnvilSlot.OUTPUT)) return;

                        String input = e.getInput();

                        if (input == null) {
                            e.getPlayer().sendMessage(Lang.getPrefix() + Lang.get("Enter_Name"));
                            return;
                        }

                        page.getDestination().setType(DestinationType.Server);
                        page.getDestination().setAdapter(DestinationType.Server.getInstance());
                        ServerAdapter serverAdapter = (ServerAdapter) page.getDestination().getAdapter();
                        serverAdapter.setServer(input);

                        page.updateDestinationButtons();
                        e.setClose(true);
                    }

                    @Override
                    public boolean canTrigger(InventoryClickEvent e, ClickType trigger, Player player) {
                        if (page.getDestination().getType() == DestinationType.Server) {
                            ServerAdapter serverAdapter = (ServerAdapter) page.getDestination().getAdapter();
                            if (serverAdapter.getServer() == null) return true;

                            serverAdapter.setKeepPosition(!serverAdapter.isKeepPosition());
                            update();
                            return false;
                        } else return true;
                    }

                    @Override
                    public void onClose(AnvilCloseEvent e) {
                    }

                    @Override
                    public ItemStack craftAnvilItem(ClickType trigger) {
                        String name = null;
                        if (page.getDestination().getType() == DestinationType.Server) name = page.getDestination().getId();

                        return new ItemBuilder(XMaterial.PAPER).setName(name != null ? name : (Lang.get("Server") + "...")).getItem();
                    }

                    @Override
                    public void onOtherClick(InventoryClickEvent e) {
                        if (e.isRightClick()) {
                            page.getDestination().setId(null);
                            page.getDestination().setAdapter(null);
                            page.getDestination().setType(null);

                            page.updateDestinationButtons();
                        } else if (e.isShiftClick() && e.isLeftClick()) {
                            if (server != null && !pinging) {
                                pinging = true;
                                update();
                                WarpSystem.getDataHandler().send(new RequestServerStatusPacket(server), p).thenAccept(booleanPacket -> {
                                    pinging = false;
                                    online = booleanPacket.getBoolean();
                                    if (page.getLast().getCurrent() == page) update();
                                });
                            }
                        }
                    }
                }.setOption(option));
            }

            page.addButton(new SyncHotbarGUIButton(slot++, 2, new Node<>(ClickType.LEFT, new VelocityHotbarEditor(page, p.getPlayer()))) {
                @Override
                public void onFinish(Player player) {
                    page.updateDestinationButtons();
                }

                @Override
                public void onTrigger(InventoryClickEvent e, ClickType trigger, Player player) {
                    super.onTrigger(e, trigger, player);

                    if (trigger == ClickType.RIGHT) {
                        Destination current = page.getDestination();
                        boolean active = current.getType() == DestinationType.Velocity;

                        if (active) {
                            page.getDestination().setId(null);
                            page.getDestination().setAdapter(null);
                            page.getDestination().setType(null);
                            page.updateDestinationButtons();
                        }
                    }
                }

                @Override
                public ItemStack craftItem() {
                    Destination current = page.getDestination();
                    boolean active = current.getType() == DestinationType.Velocity;

                    ItemBuilder builder = new ItemBuilder(XMaterial.BLAZE_POWDER).setName(Editor.ITEM_TITLE_COLOR + Lang.get("Velocity"));

                    String name;
                    if (active) {
                        VelocityAdapter adapter = (VelocityAdapter) current.getAdapter();
                        Vector vector = adapter.getVector();
                        double multiplier = adapter.getMultiplier();

                        Location location = new Location(null, 0, 0, 0);
                        location.setDirection(vector);
                        String direction = "§8(§7→ §e" + Math.round(location.getYaw() * 100) / 100 + "° §7| ↑ §e" + Math.round(location.getPitch() * 100) / 100 + "°§8)";
                        name = direction + "§7 • §e" + multiplier;
                    } else name = "§c" + Lang.get("Not_Set");

                    builder.addLore("§3" + Lang.get("Current") + ": " + name);

                    builder.addLore("", "§3" + Lang.get("Leftclick") + ": §a" + Lang.get("Edit"));
                    if (active) builder.addLore("§3" + Lang.get("Rightclick") + ": §c" + Lang.get("Remove"));

                    return builder.getItem();
                }
            });
        }
    }
}
