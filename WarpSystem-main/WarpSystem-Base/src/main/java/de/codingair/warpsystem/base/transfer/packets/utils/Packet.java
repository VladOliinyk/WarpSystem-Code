package de.codingair.warpsystem.base.transfer.packets.utils;

public interface Packet extends de.codingair.codingapi.transfer.packets.utils.Packet {
    default PacketType getType() {
        return PacketType.getByObject(this);
    }
}
