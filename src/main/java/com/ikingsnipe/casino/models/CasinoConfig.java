package com.ikingsnipe.casino.models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CasinoConfig {
    // Currency IDs
    public static final int COINS_ID = 995;
    public static final int PLATINUM_TOKEN_ID = 13204;
    public static final int TOKEN_VALUE = 1000;

    // Betting Limits (in GP value)
    public long minBet = 1000000; // 1M
    public long maxBet = 2147000000L; // Max GP
    
    // Configurable Intervals (ms)
    public int adIntervalMs = 15000;
    public int tradeTimeoutMs = 45000;
    public int messageDelayMinMs = 800;
    public int messageDelayMaxMs = 1500;
    public int loopDelayMinMs = 200;
    public int loopDelayMaxMs = 400;
    
    // Advanced Features
    public boolean useProvablyFair = true;
    public boolean autoRestock = true;
    public long restockThreshold = 10000000; // 10M
    public List<String> blacklist = new ArrayList<>();
    
    // Configurable Messages
    public String adMessage = "Elite Casino | Provably Fair | Dice, Flower, Craps! Trade me!";
    public String tradeWelcome = "Welcome! Provably Fair active. Hash: %s";
    public String tradeSafety = "Safe to accept. Payouts are automated!";
    public String tradeConfirm = "Bet received! Rolling...";
    public String winAnnouncement = "WIN! %s rolled %d and won %d GP! Verify: %s";
    public String lossAnnouncement = "LOSS! %s rolled %d. Better luck next time!";
    public String blacklistMsg = "Sorry %s, you are not allowed to play here.";
    public String errorMsg = "Error detected. Resetting session for safety.";

    // Per-Game Robust Configuration
    public Map<String, GameSettings> games = new HashMap<>();

    public CasinoConfig() {
        games.put("craps", new GameSettings(3, Arrays.asList(7, 9, 12), "Craps"));
        games.put("dice", new GameSettings(2, Arrays.asList(55, 100), "Dice")); // High roll wins
        games.put("flower", new GameSettings(2, null, "Flower Poker"));
    }

    public static class GameSettings {
        public int multiplier;
        public List<Integer> winningNumbers;
        public String displayName;
        public boolean enabled = true;

        public GameSettings(int multiplier, List<Integer> winningNumbers, String displayName) {
            this.multiplier = multiplier;
            this.winningNumbers = winningNumbers;
            this.displayName = displayName;
        }
    }
}
