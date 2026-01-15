package com.ikingsnipe.casino.models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CasinoConfig {
    // Betting Limits
    public int minBet = 1000000; // 1M
    public int maxBet = 2147000000; // Max GP
    
    // Timing & Safety
    public int tradeTimeoutMs = 45000;
    public int adIntervalMs = 15000;
    public boolean autoAccept = true;
    
    // Advanced Features
    public boolean useProvablyFair = true;
    public boolean autoRestock = true;
    public int restockThreshold = 10000000; // 10M
    public List<String> blacklist = new ArrayList<>();
    
    // Professional Messaging
    public String adMessage = "Elite Casino | Provably Fair | Dice, Flower, Craps! Trade me!";
    public String tradeWelcome = "Welcome! Provably Fair active. Hash: %s";
    public String tradeSafety = "Safe to accept. Payouts are automated!";
    public String tradeConfirm = "Bet received! Rolling...";
    
    public String winAnnouncement = "WIN! %s rolled %d and won %d GP! Verify: %s";
    public String lossAnnouncement = "LOSS! %s rolled %d. Better luck next time!";
    public String blacklistMsg = "Sorry %s, you are not allowed to play here.";

    // Game Specifics
    public Map<String, Object> gameSettings = new HashMap<>();

    public CasinoConfig() {
        gameSettings.put("craps_multiplier", 3);
        gameSettings.put("craps_wins", Arrays.asList(7, 9, 12));
        gameSettings.put("dice_multiplier", 2);
        gameSettings.put("flower_multiplier", 2);
    }
}
