package com.ikingsnipe.casino.core;

public enum CasinoState {
    IDLE,               // Advertising and waiting for trade requests
    TRADE_REQUESTED,    // Detected a trade request from a player
    TRADING_WINDOW_1,   // First trade window open, waiting for items
    TRADING_WINDOW_2,   // Second trade window open, confirming
    PROCESSING_GAME,    // Trade finished, executing game logic
    PAYOUT_PENDING,     // Player won, preparing to pay out
    ERROR_RECOVERY      // Something went wrong, resetting state
}
