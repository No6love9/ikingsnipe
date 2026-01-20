package com.ikingsnipe.casino.core;

import org.dreambot.api.methods.input.Keyboard;


public enum CasinoState {
    INITIALIZING("Initializing Systems"),
    WALKING_TO_LOCATION("Walking to Location"),
    BANKING("Banking Operations"),
    IDLE("Waiting for Players"),
    TRADING_WINDOW_1("Trade Screen 1"),
    TRADING_WINDOW_2("Trade Screen 2"),
    PROCESSING_GAME("Processing Game Logic"),
    PAYOUT_PENDING("Handling Payout"),
    ERROR_RECOVERY("Recovering from Error"),
    TRADING("Trading with Player"),
    PLAYING_GAME("Playing Game"),
    MULING("Muling Operations"),
    BREAK("Taking Break");

    private final String status;
    CasinoState(String status) { this.status = status; }
    public String getStatus() { return status; }
}
