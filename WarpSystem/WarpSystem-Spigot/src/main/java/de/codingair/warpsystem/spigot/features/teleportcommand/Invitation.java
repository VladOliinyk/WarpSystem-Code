package de.codingair.warpsystem.spigot.features.teleportcommand;

import de.codingair.codingapi.player.chat.SimpleMessage;
import de.codingair.codingapi.tools.Callback;
import de.codingair.codingapi.utils.ChatColor;
import de.codingair.codingapi.utils.Value;
import de.codingair.warpsystem.api.Result;
import de.codingair.warpsystem.core.transfer.packets.general.StartTeleportToPlayerPacket;
import de.codingair.warpsystem.core.transfer.packets.spigot.PrepareTeleportPlayerToPlayerPacket;
import de.codingair.warpsystem.core.transfer.packets.spigot.PrepareTeleportRequestPacket;
import de.codingair.warpsystem.core.transfer.packets.spigot.TeleportRequestHandledPacket;
import de.codingair.warpsystem.spigot.api.bungee.HoverEventBuilder;
import de.codingair.warpsystem.spigot.api.players.ProxyPlayer;
import de.codingair.warpsystem.spigot.base.WarpSystem;
import de.codingair.warpsystem.spigot.base.utils.Lang;
import de.codingair.warpsystem.spigot.base.utils.teleport.Origin;
import de.codingair.warpsystem.spigot.base.utils.teleport.TeleportOptions;
import de.codingair.warpsystem.spigot.base.utils.teleport.destinations.Destination;
import de.codingair.warpsystem.spigot.base.utils.teleport.destinations.adapters.EmptyAdapter;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class Invitation {
    private final String sender;
    private final String recipient; //notify if receiver != null | if recipient == null ? ALL
    private final boolean toSender; //only send if receiver.length == 1
    private final List<String> handled = new ArrayList<>();
    private final boolean bukkitOnly;
    private int recipients = 1;

    protected Invitation(String sender, boolean bukkitOnly) {
        this.sender = sender;
        this.recipient = null;
        this.toSender = true;
        this.recipients = Bukkit.getOnlinePlayers().size();
        this.bukkitOnly = bukkitOnly;
    }

    protected Invitation(String sender, boolean toSender, String recipient, boolean bukkitOnly) {
        this.sender = sender;
        this.toSender = toSender;
        this.recipient = recipient;
        this.bukkitOnly = bukkitOnly;
    }

    public boolean isRecipient(String player) {
        return !sender.equalsIgnoreCase(player) && (recipient == null || recipient.equalsIgnoreCase(player));
    }

    public void handle(String recipient, boolean accepted) {
        //check cooldown
        Player sender = Bukkit.getPlayer(this.sender);

        if (accepted && sender != null) {
            WarpSystem.cooldown().register(sender, Origin.TeleportRequest);
        }

        handled.add(recipient);
        if (WarpSystem.getInstance().isOnProxy() && sender == null) WarpSystem.getDataHandler().send(new TeleportRequestHandledPacket(this.sender, recipient, accepted), null);
        TeleportCommandManager.getInstance().checkDestructionOf(this);
    }

    public void accept(Player player) {
        if (!isRecipient(player.getName())) return;
        //to sender
        ProxyPlayer sender = new ProxyPlayer(this.sender);

        if (WarpSystem.getInstance().getTeleportManager().isTeleporting(player)) {
            player.sendMessage(Lang.getPrefix() + Lang.get("Player_Is_Already_Teleporting"));
        } else {
            if (sender.onSpigot()) {
                if (sender.getSpigotPlayer().isOnline()) {
                    if (recipient != null)
                        sender.sendMessage(Lang.getPrefix() + Lang.get("TeleportRequest_accepted_sender").replace("%PLAYER%", ChatColor.stripColor(player.getName())));
                    player.sendMessage(Lang.getPrefix() + Lang.get("TeleportRequest_accepted_other").replace("%PLAYER%", ChatColor.stripColor(sender.getName())));

                    TeleportOptions options = new TeleportOptions(toSender ? sender.getSpigotPlayer().getLocation() : player.getLocation(), toSender ? sender.getName() : player.getName(), Origin.TeleportRequest);
                    options.setWaitForTeleport(true);
                    options.setCosts(TeleportCommandManager.getInstance().getTpaCosts());

                    options.addCallback(new Callback<Result>() {
                        @Override
                        public void accept(Result result) {
                            handle(player.getName(), result == Result.SUCCESS);
                        }
                    });

                    WarpSystem.getInstance().getTeleportManager().teleport(toSender ? player : sender.getSpigotPlayer(), options);
                    return;
                } else {
                    player.sendMessage(Lang.getPrefix() + Lang.get("TeleportRequest_not_valid").replace("%PLAYER%", ChatColor.stripColor(sender.getName())));
                }
            } else {
                handle(player.getName(), true);

                if (toSender) {
                    TeleportOptions options = new TeleportOptions(new Destination(new EmptyAdapter()), sender.getName(), Origin.TeleportRequest);
                    options.setWaitForTeleport(true);
                    options.setMessage(null);
                    options.setPayMessage(null);
                    options.setCosts(TeleportCommandManager.getInstance().getTpaCosts());
                    options.setPaymentDeniedMessage(null);
                    options.setAfterEffects(false, false);
                    options.addCallback(new Callback<Result>() {
                        @Override
                        public void accept(Result result) {
                            //move
                            if (result == Result.SUCCESS) {
                                WarpSystem.getDataHandler().send(new PrepareTeleportPlayerToPlayerPacket(player.getName(), sender.getName()).setCosts(TeleportCommandManager.getInstance().getTpaCosts()), player).thenAccept(packet -> {
                                    int i = packet.a();
                                    if (i == 0) {
                                        //teleported
                                        if (recipient != null)
                                            sender.sendMessage(Lang.getPrefix() + Lang.get("TeleportRequest_accepted_sender").replace("%PLAYER%", ChatColor.stripColor(player.getName())));
                                        player.sendMessage(Lang.getPrefix() + Lang.get("TeleportRequest_accepted_other").replace("%PLAYER%", ChatColor.stripColor(sender.getName())));
                                    } else {
                                        player.sendMessage(Lang.getPrefix() + Lang.get("TeleportRequest_not_valid").replace("%PLAYER%", ChatColor.stripColor(sender.getName())));
                                    }
                                });
                            } else {
                                if (recipient != null)
                                    sender.sendMessage(Lang.getPrefix() + Lang.get("TeleportRequest_denied_sender").replace("%PLAYER%", ChatColor.stripColor(player.getName())));
                                player.sendMessage(Lang.getPrefix() + Lang.get("TeleportRequest_denied_other").replace("%PLAYER%", ChatColor.stripColor(sender.getName())));
                            }
                        }
                    });

                    WarpSystem.getInstance().getTeleportManager().teleport(player, options);
                } else {
                    //tp other
                    WarpSystem.getDataHandler().send(new StartTeleportToPlayerPacket(sender.getName(), player.getName(), player.getName(), sender.getName()), player).thenAccept(packet -> {
                        int id = packet.a();
                        if (id == 0) {
                            if (recipient != null)
                                sender.sendMessage(Lang.getPrefix() + Lang.get("TeleportRequest_accepted_sender").replace("%PLAYER%", ChatColor.stripColor(player.getName())));
                            player.sendMessage(Lang.getPrefix() + Lang.get("TeleportRequest_accepted_other").replace("%PLAYER%", ChatColor.stripColor(sender.getName())));
                        } else {
                            //offline
                            player.sendMessage(Lang.getPrefix() + Lang.get("TeleportRequest_not_valid").replace("%PLAYER%", ChatColor.stripColor(sender.getName())));
                        }
                    });
                }

                return;
            }
        }

        handle(player.getName(), false);
    }

    public void deny(Player player) {
        if (!isRecipient(player.getName())) return;
        //to sender
        handle(player.getName(), false);

        ProxyPlayer sender = new ProxyPlayer(this.sender);

        if (recipient != null) sender.sendMessage(Lang.getPrefix() + Lang.get("TeleportRequest_denied_sender").replace("%PLAYER%", ChatColor.stripColor(player.getName())));
        player.sendMessage(Lang.getPrefix() + Lang.get("TeleportRequest_denied_other").replace("%PLAYER%", ChatColor.stripColor(sender.getName())));
    }

    public void timeOut(String player) {
        if (!isRecipient(player)) return;
        handle(player, false);
    }

    public boolean canBeDestroyed() {
        return handled.size() >= recipients;
    }

    public void destroy() {
        this.handled.clear();
        recipients = 0;
    }

    public void send() {
        send(new Callback<Long>() {
            @Override
            public void accept(Long object) {
            }
        });
    }

    public void send(Callback<Long> callback) {
        //to receiver
        Value<Integer> handled = new Value<>(0);
        Value<Integer> sent = new Value<>(0);

        if (recipient == null) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.getName().equalsIgnoreCase(sender)) continue;

                sendInvitation(player.getName(), new Callback<Long>() {
                    @Override
                    public void accept(Long result) {
                        boolean success = ((int) (result >> 32)) == 1;
                        if(success) handled.setValue(handled.getValue() + 1);
                        sent.setValue(sent.getValue() + result.intValue());
                    }
                });
            }

            if (WarpSystem.getInstance().isOnProxy() && !bukkitOnly) {
                Player p = Bukkit.getPlayer(sender);
                WarpSystem.getDataHandler().send(new PrepareTeleportRequestPacket(sender, null, true), p).thenAccept(packet -> {
                    long result = packet.a();
                    handled.setValue(handled.getValue() + (int) (result >> 32));
                    sent.setValue(sent.getValue() + (int) result);

                    callback.accept((((long) handled.getValue()) << 32) | (sent.getValue() & 0xffffffffL));
                });
            } else callback.accept((((long) handled.getValue()) << 32) | (sent.getValue() & 0xffffffffL));
        } else {
            sendInvitation(this.recipient, new Callback<Long>() {
                @Override
                public void accept(Long result) {
                    int handled = (int) (result >> 32);
                    int sent = result.intValue();

                    if (handled == 0 || sent == 0) Invitation.this.handled.add(recipient);
                    callback.accept(result);
                }
            });
        }
    }

    private void sendInvitation(String player, Callback<Long> callback) {
        Player recipient = Bukkit.getPlayer(player);
        if (recipient != null) {
            //on bukkit
            if (TeleportCommandManager.getInstance().deniesTpaRequests(recipient.getName())) {
                callback.accept((((long) -1) << 32));
                return;
            }

            if (WarpSystem.opt().forbiddenRegion(recipient.getLocation())) {
                callback.accept((((long) -2) << 32));
                return;
            }

            SimpleMessage m = new SimpleMessage(Lang.getPrefix() + Lang.get("TeleportRequest_tpTo" + (toSender ? "Sender" : "Receiver")).replace("%PLAYER%", ChatColor.stripColor(sender)).replace("%SECONDS%", TeleportCommandManager.getInstance().getExpireDelay() + "").replace("%PLAYER%", sender), WarpSystem.getInstance()) {
                @Override
                public void onTimeOut() {
                    timeOut(recipient.getName());
                }
            };

            m.setTimeOut(TeleportCommandManager.getInstance().getExpireDelay());

            TextComponent accept = new TextComponent(Lang.get("Accept"));
            accept.setHoverEvent(HoverEventBuilder.build(HoverEvent.Action.SHOW_TEXT, Lang.get("Click_Hover")));
            accept.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpaccept " + sender));

            TextComponent deny = new TextComponent(Lang.get("Deny"));
            deny.setHoverEvent(HoverEventBuilder.build(HoverEvent.Action.SHOW_TEXT, Lang.get("Click_Hover")));
            deny.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpdeny " + sender));

            m.replace("%ACCEPT%", accept);
            m.replace("%DENY%", deny);

            m.send(recipient);

            callback.accept((((long) 1) << 32) | (1 & 0xffffffffL));
        } else if (WarpSystem.getInstance().isOnProxy() && !bukkitOnly) {
            //try on proxy
            Player p = Bukkit.getPlayer(sender);
            WarpSystem.getDataHandler().send(new PrepareTeleportRequestPacket(sender, this.recipient, toSender), p).thenAccept(packet -> callback.accept(packet.a()));
        } else callback.accept(0L);
    }

    public String getSender() {
        return sender;
    }

    public String getRecipient() {
        return recipient;
    }

    public boolean isToSender() {
        return toSender;
    }
}
