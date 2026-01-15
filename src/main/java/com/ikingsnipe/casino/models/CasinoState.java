package com.ikingsnipe.casino.models;

public enum CasinoState {
    INITIALIZING("Initializing System"),
    WALKING_TO_LOCATION("Walking to Location"),
    BANKING("Banking Operations"),
    IDLE("Waiting for Players"),
    TRADING_WINDOW_1("Trade Window 1"),
    TRADING_WINDOW_2("Trade Window 2"),
    PROCESSING_GAME("Processing Game"),
    PAYOUT_PENDING("Payout Pending"),
    ERROR_RECOVERY("Recovering from Error");

    private final String status;

    CasinoState(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}
