package de.codingair.warpsystem.base.transfer;

public interface DataHandler {
    String GET_CHANNEL = "warpsystem:get";
    String REQUEST_CHANNEL = "warpsystem:request";

    void onEnable();

    void onDisable();
}
