package com.ikingsnipe.casino.models;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class CasinoConfig {
    // Betting Limits
    public int minBet = 1000000; // 1M
    public int maxBet = 2147000000; // Max GP
    
    // Timing & Safety
    public int tradeTimeoutMs = 45000;
    public int adIntervalMs = 15000;
    public boolean autoAccept = true;
    
    // Professional Messaging
    public String adMessage = "Elite Casino | Fast Payouts | Dice, Flower, Craps! Trade me to play!";
    public String tradeWelcome = "Welcome to Elite Casino! Please offer your bet.";
    public String tradeSafety = "Trade is safe. I will roll immediately after confirmation!";
    public String tradeConfirm = "Bet received! Rolling now, good luck!";
    
    public String winAnnouncement = "Congratulations %s! You won %d GP with a %d!";
    public String lossAnnouncement = "Better luck next time %s. You rolled a %d.";
    public String errorAnnouncement = "An error occurred. Please trade me again for a refund if needed.";

    // Game Specifics
    public Map<String, Object> gameSettings = new HashMap<>();

    public CasinoConfig() {
        gameSettings.put("craps_multiplier", 3);
        gameSettings.put("craps_wins", Arrays.asList(7, 9, 12));
        gameSettings.put("dice_multiplier", 2);
        gameSettings.put("flower_multiplier", 2);
    }
}
