package de.codingair.warpsystem.base.transfer.packets.utils;

import de.codingair.warpsystem.base.transfer.packets.bungee.*;
import de.codingair.warpsystem.base.transfer.packets.general.*;
import de.codingair.warpsystem.base.transfer.packets.spigot.*;
import de.codingair.warpsystem.base.transfer.packets.bungee.SendJarPacket;
import de.codingair.warpsystem.base.transfer.packets.spigot.CooldownDataPacket;
import de.codingair.warpsystem.base.transfer.packets.spigot.CooldownPacket;
import de.codingair.warpsystem.transfer.packets.spigot.GetOnlineCountPacket;
import de.codingair.warpsystem.transfer.packets.spigot.IsOnlinePacket;

public enum PacketType {
    InitialPacket(de.codingair.warpsystem.base.transfer.packets.bungee.InitialPacket.class),
    RequestInitialPacket(de.codingair.warpsystem.base.transfer.packets.spigot.RequestInitialPacket.class),
    RequestServerStatusPacket(de.codingair.warpsystem.base.transfer.packets.spigot.RequestServerStatusPacket.class),
    ChatInputGUITogglePacket(de.codingair.warpsystem.base.transfer.packets.spigot.ChatInputGUITogglePacket.class),
    SendGlobalSpawnOptionsPacket(de.codingair.warpsystem.base.transfer.packets.general.SendGlobalSpawnOptionsPacket.class),
    TeleportSpawnPacket(de.codingair.warpsystem.base.transfer.packets.general.TeleportSpawnPacket.class),
    PacketVanishInfo(PacketVanishInfo.class),
    SendJarPacket(SendJarPacket.class),
    SendOptionsPacket(de.codingair.warpsystem.base.transfer.packets.spigot.SendOptionsPacket.class),

    PublishGlobalWarpPacket(de.codingair.warpsystem.base.transfer.packets.spigot.PublishGlobalWarpPacket.class),
    GlobalWarpTeleportPacket(GlobalWarpTeleportPacket.class),
    TeleportPacket(GlobalWarpTeleportPacket.class),
    DeleteGlobalWarpPacket(DeleteGlobalWarpPacket.class),
    RequestGlobalWarpNamesPacket(RequestGlobalWarpNamesPacket.class),
    SendGlobalWarpNamesPacket(de.codingair.warpsystem.base.transfer.packets.bungee.SendGlobalWarpNamesPacket.class),
    UpdateGlobalWarpPacket(de.codingair.warpsystem.base.transfer.packets.bungee.UpdateGlobalWarpPacket.class),
    PerformCommandOnSpigotPacket(de.codingair.warpsystem.base.transfer.packets.bungee.PerformCommandOnSpigotPacket.class),
    PerformCommandOnBungeePacket(PerformCommandOnBungeePacket.class),
    RequestUUIDPacket(RequestUUIDPacket.class),
    SendUUIDPacket(de.codingair.warpsystem.base.transfer.packets.bungee.SendUUIDPacket.class),
    TeleportPlayerToPlayerPacket(TeleportPlayerToPlayerPacket.class),
    PrepareServerSwitchPacket(PrepareServerSwitchPacket.class),
    PrepareLoginMessagePacket(PrepareLoginMessagePacket.class),
    MessagePacket(MessagePacket.class),
    CooldownPacket(CooldownPacket.class),
    CooldownDataPacket(CooldownDataPacket.class),

    TeleportCommandOptions(TeleportCommandOptionsPacket.class),
    TeleportRequestHandledPacket(TeleportRequestHandledPacket.class),
    PrepareTeleportPlayerToPlayerPacket(PrepareTeleportPlayerToPlayerPacket.class),
    PrepareTeleportRequestPacket(PrepareTeleportRequestPacket.class),
    StartTeleportToPlayerPacket(StartTeleportToPlayerPacket.class),
    ToggleForceTeleportsPacket(ToggleForceTeleportsPacket.class),

    SendPlayerWarpsPacket(de.codingair.warpsystem.base.transfer.packets.general.SendPlayerWarpsPacket.class),
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

    BooleanPacket(BooleanPacket.class),
    IntegerPacket(IntegerPacket.class),
    LongPacket(LongPacket.class),
    StringPacket(StringPacket.class),

    AnswerPacket(AnswerPacket.class),
    RequestFullNamePacket(RequestFullNamePacket.class),

    SendServerPropertiesPacket(SendServerPropertiesPacket.class),
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
