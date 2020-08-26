package de.codingair.warpsystem.transfer.packets.utils;

import de.codingair.warpsystem.bungee.base.utils.PacketVanishInfo;
import de.codingair.warpsystem.spigot.base.setupassistant.bungee.SetupAssistantStorePacket;
import de.codingair.warpsystem.spigot.base.setupassistant.bungee.ToggleSetupAssistantPacket;
import de.codingair.warpsystem.spigot.base.utils.cooldown.CooldownDataPacket;
import de.codingair.warpsystem.spigot.base.utils.cooldown.CooldownPacket;
import de.codingair.warpsystem.spigot.features.randomteleports.packets.RandomTPWorldsPacket;
import de.codingair.warpsystem.spigot.features.teleportcommand.packets.ToggleForceTeleportsPacket;
import de.codingair.warpsystem.transfer.jar.SendJarPacket;
import de.codingair.warpsystem.transfer.packets.bungee.*;
import de.codingair.warpsystem.transfer.packets.general.*;
import de.codingair.warpsystem.transfer.packets.spigot.*;

public enum PacketType {
    InitialPacket(InitialPacket.class),
    RequestInitialPacket(RequestInitialPacket.class),
    RequestServerStatusPacket(RequestServerStatusPacket.class),
    ChatInputGUITogglePacket(ChatInputGUITogglePacket.class),
    SendGlobalSpawnOptionsPacket(SendGlobalSpawnOptionsPacket.class),
    TeleportSpawnPacket(TeleportSpawnPacket.class),
    PacketVanishInfo(PacketVanishInfo.class),
    SendJarPacket(SendJarPacket.class),
    SendOptionsPacket(SendOptionsPacket.class),

    PublishGlobalWarpPacket(PublishGlobalWarpPacket.class),
    GlobalWarpTeleportPacket(GlobalWarpTeleportPacket.class),
    TeleportPacket(GlobalWarpTeleportPacket.class),
    DeleteGlobalWarpPacket(DeleteGlobalWarpPacket.class),
    RequestGlobalWarpNamesPacket(RequestGlobalWarpNamesPacket.class),
    SendGlobalWarpNamesPacket(SendGlobalWarpNamesPacket.class),
    UpdateGlobalWarpPacket(UpdateGlobalWarpPacket.class),
    PerformCommandOnSpigotPacket(PerformCommandOnSpigotPacket.class),
    PerformCommandOnBungeePacket(PerformCommandOnBungeePacket.class),
    RequestUUIDPacket(RequestUUIDPacket.class),
    SendUUIDPacket(SendUUIDPacket.class),
    TeleportPlayerToPlayerPacket(TeleportPlayerToPlayerPacket.class),
    PrepareServerSwitchPacket(PrepareServerSwitchPacket.class),
    PrepareLoginMessagePacket(PrepareLoginMessagePacket.class),
    MessagePacket(MessagePacket.class),
    CooldownPacket(CooldownPacket.class),
    CooldownDataPacket(CooldownDataPacket.class),
    ToggleForceTeleportsPacket(ToggleForceTeleportsPacket.class),

    SendPlayerWarpsPacket(SendPlayerWarpsPacket.class),
    RegisterServerForPlayerWarpsPacket(RegisterServerForPlayerWarpsPacket.class),
    MoveLocalPlayerWarpsPacket(MoveLocalPlayerWarpsPacket.class),
    SendPlayerWarpUpdatesPacket(SendPlayerWarpUpdatePacket.class),
    PrepareCoordinationTeleportPacket(PrepareCoordinationTeleportPacket.class),
    SendPlayerWarpOptionsPacket(SendPlayerWarpOptionsPacket.class),
    DeletePlayerWarpPacket(DeletePlayerWarpPacket.class),
    PlayerWarpTeleportProcessPacket(PlayerWarpTeleportProcessPacket.class),
    RandomTPWorldsPacket(RandomTPWorldsPacket.class),
    ToggleSetupAssistantPacket(ToggleSetupAssistantPacket.class),
    SetupAssistantStorePacket(SetupAssistantStorePacket.class),

    IsOperatorPacket(IsOperatorPacket.class),
    SendDisablePacket(SendDisablePacket.class),
    IsOnlinePacket(IsOnlinePacket.class),
    GetOnlineCountPacket(GetOnlineCountPacket.class),

    ApplyUUIDPacket(ApplyUUIDPacket.class),

    BooleanPacket(BooleanPacket.class),
    IntegerPacket(IntegerPacket.class),
    LongPacket(LongPacket.class),

    AnswerPacket(AnswerPacket.class),
    ;

    private final Class<?> packet;

    PacketType(Class<?> packet) {
        this.packet = packet;
    }

    public static PacketType getById(int id) {
        for(PacketType packetType : values()) {
            if(packetType.getId() == id) return packetType;
        }

        return null;
    }

    public static PacketType getByObject(Object packet) {
        if(packet == null) return null;

        for(PacketType packetType : values()) {
            if(packetType.getPacket().equals(packet.getClass())) return packetType;
        }

        return null;
    }

    public int getId() {
        return ordinal();
    }

    public Class<?> getPacket() {
        return packet;
    }
}
