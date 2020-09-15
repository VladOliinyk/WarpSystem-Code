package de.codingair.warpsystem.spigot.features.signs.guis;

import de.codingair.codingapi.player.gui.sign.SignTools;
import de.codingair.codingapi.tools.items.ItemBuilder;
import de.codingair.codingapi.utils.ChatColor;
import de.codingair.warpsystem.spigot.base.guis.editor.Backup;
import de.codingair.warpsystem.spigot.base.guis.editor.Editor;
import de.codingair.warpsystem.spigot.base.guis.editor.pages.DestinationPage;
import de.codingair.warpsystem.spigot.base.guis.editor.pages.SoundPage;
import de.codingair.warpsystem.spigot.base.language.Lang;
import de.codingair.warpsystem.spigot.base.utils.featureobjects.actions.types.SoundAction;
import de.codingair.warpsystem.spigot.base.utils.teleport.Origin;
import de.codingair.warpsystem.spigot.features.signs.guis.pages.OptionPage;
import de.codingair.warpsystem.spigot.features.signs.managers.SignManager;
import de.codingair.warpsystem.spigot.features.signs.utils.WarpSign;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class WarpSignGUI extends Editor<WarpSign> {

    public WarpSignGUI(Player p, WarpSign sign) {
        this(p, sign, sign.clone().createDestinationIfAbsent().createTeleportSoundIfAbsent());
    }

    private WarpSignGUI(Player p, WarpSign sign, WarpSign clone) {
        super(p, clone, new Backup<WarpSign>(sign) {
                    private final String[] backupLines = ((Sign) sign.getLocation().getBlock().getState()).getLines();

                    @Override
                    public void applyTo(WarpSign clone) {
                        sign.apply(clone);

                        if(SignManager.getInstance().getByLocation(sign.getLocation()) == null) {
                            SignManager.getInstance().getWarpSigns().add(sign);
                        }

                        Sign s = (Sign) sign.getLocation().getBlock().getState();

                        SignTools.updateSign(s, s.getLines());
                    }

                    @Override
                    public void cancel(WarpSign value) {
                        Sign s = ((Sign) sign.getLocation().getBlock().getState());

                        SignTools.updateSign(s, backupLines);
                    }
                }, new ShowIcon((Sign) sign.getLocation().getBlock().getState()),
                new OptionPage(p, clone),
                new DestinationPage(p, getMainTitle(), clone.getDestination(), Origin.WarpSign),
                new SoundPage(p, getMainTitle(), clone.getAction(SoundAction.class).getValue())
        );
    }

    public static String getMainTitle() {
        return Editor.TITLE_COLOR + Lang.get("WarpSigns");
    }

    public static class ShowIcon implements de.codingair.warpsystem.spigot.base.guis.editor.ShowIcon {
        private final Sign sign;
        private String[] lines;

        public ShowIcon(Sign s) {
            this.sign = s;
            this.lines = s.getLines();
        }

        public void applyLines(String[] lines) {
            this.lines = lines;
        }

        @Override
        public ItemStack buildIcon() {
            ItemBuilder builder = new ItemBuilder(getType());

            for(String line : lines) {
                builder.addText("§7'§f" + ChatColor.translateAll('&', line) + "§7'");
            }

            return builder.getItem();
        }

        private Material getType() {
            if(sign.getType().name().contains("WALL_")) {
                return Material.valueOf(sign.getType().name().replace("WALL_", ""));
            } else return sign.getType();
        }
    }
}
