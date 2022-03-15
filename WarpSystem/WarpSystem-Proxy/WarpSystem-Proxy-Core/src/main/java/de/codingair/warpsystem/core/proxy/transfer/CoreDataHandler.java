package de.codingair.warpsystem.core.proxy.transfer;

import de.codingair.packetmanagement.utils.Direction;
import de.codingair.packetmanagement.variants.bytestream.StreamDataHandler;
import de.codingair.warpsystem.core.proxy.Core;
import de.codingair.warpsystem.core.proxy.redis.RedisCore;
import de.codingair.warpsystem.core.proxy.transfer.handlers.*;
import de.codingair.warpsystem.core.proxy.utils.ProxyPlugin;
import de.codingair.warpsystem.core.proxy.utils.Server;
import de.codingair.warpsystem.core.transfer.packets.general.*;
import de.codingair.warpsystem.core.transfer.packets.proxy.*;
import de.codingair.warpsystem.core.transfer.packets.spigot.*;
import de.codingair.warpsystem.core.transfer.packets.spigot.utils.ConnectionPacket;
import de.codingair.warpsystem.core.transfer.packets.utils.PacketType;

public abstract class CoreDataHandler<C> extends StreamDataHandler<Server<C>> {
    public static final String redisChannel = "warpsystem:redis";

    public CoreDataHandler(ProxyPlugin plugin) {
        super("warpsystem", plugin);
        timeOut = RedisCore.TIME_OUT;
    }

    @Override
    public void registering() {
        for (PacketType value : PacketType.values()) {
            registerPacket(value.getPacket());
        }

        registerHandler(SendPlayerWarpsPacket.class, new SendPlayerWarpsPacketHandler());
        registerHandler(RegisterServerForPlayerWarpsPacket.class, new RegisterServerForPlayerWarpsPacketHandler());
        registerHandler(MoveLocalPlayerWarpsPacket.class, new MoveLocalPlayerWarpsPacketHandler());
        registerHandler(SendPlayerWarpUpdatePacket.class, new SendPlayerWarpUpdatePacketHandler());
        registerHandler(PlayerWarpTeleportProcessPacket.class, new PlayerWarpTeleportProcessPacketHandler());
        registerHandler(DeletePlayerWarpPacket.class, new DeletePlayerWarpPacketHandler());
        registerHandler(TeleportCommandOptionsPacket.class, new TeleportCommandOptionsPacketHandler());
        registerHandler(TeleportRequestHandledPacket.class, new TeleportRequestHandledPacketHandler());
        registerHandler(PrepareTeleportPlayerToPlayerPacket.class, new PrepareTeleportPlayerToPlayerPacketHandler());
        registerHandler(StartTeleportToPlayerPacket.class, new StartTeleportToPlayerPacketHandler());
        registerHandler(PrepareTeleportRequestPacket.class, new PrepareTeleportRequestPacketHandler());
        registerHandler(PrepareTeleportPacket.class, new PrepareTeleportPacketHandler());
        registerHandler(ToggleForceTeleportsPacket.class, new ToggleForceTeleportsPacketHandler());
        registerHandler(PublishGlobalWarpPacket.class, new PublishGlobalWarpPacketHandler());
        registerHandler(DeleteGlobalWarpPacket.class, new DeleteGlobalWarpPacketHandler());
        registerHandler(GlobalWarpTeleportPacket.class, new GlobalWarpTeleportPacketHandler());
        registerHandler(RequestGlobalWarpNamesPacket.class, new RequestGlobalWarpNamesPacketHandler());
        registerHandler(PrepareCoordinationTeleportPacket.class, new PrepareCoordinationTeleportPacketHandler());
        registerHandler(RequestUUIDPacket.class, new SendUUIDPacketHandler());
        registerHandler(MessagePacket.class, new MessagePacketHandler());
        registerHandler(PrepareServerSwitchPacket.class, new PrepareServerSwitchPacketHandler());
        registerHandler(RequestFullNamePacket.class, new RequestFullNamePacketHandler());
        registerHandler(RequestInitialPacket.class, new RequestInitialPacketHandler());
        registerHandler(RequestServerStatusPacket.class, new RequestServerStatusPacketHandler());
        registerHandler(UpdatePlayerDataPacket.class, new UpdatePlayerDataPacketHandler());
        registerHandler(RandomTPPacket.class, new RandomTPPacketHandler());
        registerHandler(QueueRTPUsagePacket.class, new QueueRTPUsagePacketHandler());
        registerHandler(RandomTPWorldsPacket.class, new RandomTPWorldsPacketHandler());
        registerHandler(SendGlobalSpawnOptionsPacket.class, new SendGlobalSpawnOptionsPacketHandler());
        registerHandler(TeleportBackPacket.class, new TeleportBackPacketHandler());
        registerHandler(InitialPacket.class, new InitialPacketHandler());
        registerHandler(ProvidePlayerDataPacket.class, new ProvidePlayerDataPacketHandler());
        registerHandler(PlayerJoinPacket.class, new PlayerJoinPacketHandler());
        registerHandler(PlayerQuitPacket.class, new PlayerQuitPacketHandler());
        registerHandler(SendWorldNamesPacket.class, new SendWorldNamesPacketHandler());
        registerHandler(ProxyAwarenessPacket.class, new ProxyAwarenessPacketHandler());
        registerHandler(PerformCommandOnProxyPacket.class, new PerformCommandOnProxyPacketHandler());
        registerHandler(ConnectionPacket.class, new ConnectionPacketHandler());
    }

    public void onEnable() {
        if (RedisCore.core().getHandler() != null) {
            RedisCore.core().getHandler().registerChannel();
            send(new InitialPacket(Core.getPlugin().getVersion(), RedisCore.core().getHandler().getSource()), null, Direction.UP);
        }
    }

    public void onDisable() {
        if (RedisCore.core().getHandler() != null) {
            RedisCore.core().getHandler().unregisterChannel();
        }
    }

    @Override
    protected boolean isConnected(Direction direction) {
        return direction == Direction.DOWN || direction == Direction.UP && RedisCore.core().getHandler() != null;
    }

    @Override
    protected void send(byte[] data, Server<C> connection, Direction direction) {
        if (direction == Direction.DOWN) {
            if (connection.isEmpty()) return;
            connection.sendData(getBackendChannel(), data);
        } else if (direction == Direction.UP && RedisCore.core().getHandler() != null) {
            RedisCore.core().getHandler().send(data);
        }
    }

    public abstract C getBackendChannel();

    public abstract C getProxyChannel();
}
