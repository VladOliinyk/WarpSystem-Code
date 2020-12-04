package de.codingair.warpsystem.base.transfer.packets.utils;

public interface PacketHandler {
    void handle(Packet packet, String... extra);
}
