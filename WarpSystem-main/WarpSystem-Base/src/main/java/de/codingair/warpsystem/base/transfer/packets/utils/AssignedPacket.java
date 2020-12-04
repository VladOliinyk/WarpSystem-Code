package de.codingair.warpsystem.base.transfer.packets.utils;

import java.util.UUID;

public abstract class AssignedPacket extends de.codingair.codingapi.transfer.packets.utils.AssignedPacket implements Packet {
    public AssignedPacket() {
        this.uniqueId = UUID.randomUUID();
    }

    public AssignedPacket(UUID uniqueId) {
        this.uniqueId = uniqueId;
    }
}
