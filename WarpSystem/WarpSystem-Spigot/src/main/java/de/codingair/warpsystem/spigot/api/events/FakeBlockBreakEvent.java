package de.codingair.warpsystem.spigot.api.events;

import com.mojang.authlib.GameProfile;
import de.codingair.codingapi.player.data.GameProfileUtils;
import de.codingair.codingapi.server.reflections.IReflection;
import de.codingair.codingapi.server.reflections.PacketUtils;
import de.codingair.codingapi.server.specification.Version;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.permissions.PermissibleBase;
import org.jetbrains.annotations.NotNull;

public class FakeBlockBreakEvent extends BlockBreakEvent {
    private static final IReflection.ConstructorAccessor PLAYER;
    private static final IReflection.ConstructorAccessor PLAYER_INTERACT_MANAGER;
    private static final IReflection.ConstructorAccessor PLAYER_CONNECTION;
    private static final IReflection.ConstructorAccessor NETWORK_MANAGER;
    private static final Object PROTOCOL_DIRECTION;
    private static final IReflection.FieldAccessor<PermissibleBase> PERMISSION_BASE = IReflection.getField(PacketUtils.CraftPlayerClass, PermissibleBase.class, 0);
    private static final IReflection.FieldAccessor<?> PLAYER_CONNECTION_FIELD = IReflection.getField(PacketUtils.EntityPlayerClass, PacketUtils.PlayerConnectionClass, 0);

    static {
        if (Version.atLeast(17)) {
            PLAYER_INTERACT_MANAGER = null;
            PLAYER = IReflection.getConstructor(PacketUtils.EntityPlayerClass, PacketUtils.MinecraftServerClass, PacketUtils.WorldServerClass, GameProfile.class);
        } else {
            PLAYER_INTERACT_MANAGER = IReflection.getConstructor(PacketUtils.PlayerInteractManagerClass, PacketUtils.WorldServerClass);
            PLAYER = IReflection.getConstructor(PacketUtils.EntityPlayerClass, PacketUtils.MinecraftServerClass, PacketUtils.WorldServerClass, GameProfile.class, PacketUtils.PlayerInteractManagerClass);
        }

        PLAYER_CONNECTION = IReflection.getConstructor(PacketUtils.PlayerConnectionClass, PacketUtils.MinecraftServerClass, PacketUtils.NetworkManagerClass, PacketUtils.EntityPlayerClass);

        Class<?> protocolDirection = IReflection.getClass(IReflection.ServerPacket.PROTOCOL, "EnumProtocolDirection");
        NETWORK_MANAGER = IReflection.getConstructor(PacketUtils.NetworkManagerClass, protocolDirection);
        PROTOCOL_DIRECTION = protocolDirection.getEnumConstants()[1];
    }

    public FakeBlockBreakEvent(@NotNull Block theBlock, @NotNull Player player) {
        super(theBlock, player);
    }

    /**
     * The position of the player will be checked for any protection.
     *
     * @param player The player we use to create our fake player to simulate the BlockBreakEvent. We use fake players to prevent player specific particles from plugins like WorldGuard
     * @return true if the location is protected (the event is cancelled).
     */
    public static boolean tryFake(@NotNull Player player) {
        return tryFake(player, player.getLocation());
    }

    /**
     * @param player   The player we use to create our fake player to simulate the BlockBreakEvent. We use fake players to prevent player specific particles from plugins like WorldGuard
     * @param location The Location where we try to build.
     * @return true if the location is protected (the event is cancelled).
     */
    public static boolean tryFake(@NotNull Player player, @NotNull Location location) {
        return tryWithFake(buildFake(player), location);
    }

    /**
     * @param fakePlayer The player who simulate the BlockBreakEvent. Use fake players to prevent player specific particles from plugins like WorldGuard
     * @param location   The Location where we try to build.
     * @return true if the location is protected (the event is cancelled).
     */
    public static boolean tryWithFake(@NotNull Player fakePlayer, @NotNull Location location) {
        FakeBlockBreakEvent event = new FakeBlockBreakEvent(location.getBlock(), fakePlayer);
        Bukkit.getPluginManager().callEvent(event);
        return event.isCancelled();
    }

    @NotNull
    public static Player buildFake(@NotNull Player player) {
        GameProfile profile = GameProfileUtils.getGameProfile(player);
        Object fakePlayer = createFakePlayerInstance(player, profile);

        PLAYER_CONNECTION_FIELD.set(fakePlayer, createConnectionDump(fakePlayer));

        Player craftFakePlayer = (Player) PacketUtils.getBukkitEntity(fakePlayer);

        craftFakePlayer.setGameMode(player.getGameMode());

        //apply permissible base
        PERMISSION_BASE.set(craftFakePlayer, new PermissibleBase(player));

        return craftFakePlayer;
    }

    private static Object createFakePlayerInstance(@NotNull Player player, GameProfile profile) {
        if (PLAYER_INTERACT_MANAGER != null)
            return PLAYER.newInstance(
                    PacketUtils.getMinecraftServer(),
                    PacketUtils.getWorldServer(player.getWorld()),
                    profile,
                    PLAYER_INTERACT_MANAGER.newInstance(PacketUtils.getWorldServer(player.getWorld()))
            );
        else
            return PLAYER.newInstance(
                    PacketUtils.getMinecraftServer(),
                    PacketUtils.getWorldServer(player.getWorld()),
                    profile
            );
    }

    private static Object createConnectionDump(@NotNull Object entityPlayer) {
        return PLAYER_CONNECTION.newInstance(PacketUtils.getMinecraftServer(), NETWORK_MANAGER.newInstance(PROTOCOL_DIRECTION), entityPlayer);
    }
}
