package com.ikingsnipe.casino.models;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class CasinoConfig {
    public int minBet = 1000000; // 1M default
    public int maxBet = 1000000000; // 1B default
    public boolean autoAcceptTrades = true;
    public int tradeTimeoutSeconds = 30;
    public int chatCooldownMs = 5000;
    
    // Advertising
    public String adMessage = "Elite Casino | Fast Payouts | Dice, Flower, Craps! Trade me to start!";
    
    // Trade Window Messages
    public String tradeWelcomeMsg = "Welcome to Elite Casino! Please offer your bet.";
    public String tradeSafetyMsg = "It is safe to accept. I will roll immediately after the trade!";
    public String tradeConfirmMsg = "Bet received! Good luck!";
    
    // Game Messages
    public String winMsg = "Congratulations %s! You won %d GP with a roll of %d!";
    public String lossMsg = "Better luck next time %s. You rolled %d.";
    
    public Map<String, Object> crapsConfig = new HashMap<>();
    public Map<String, Object> diceDuelConfig = new HashMap<>();
    public Map<String, Object> flowerPokerConfig = new HashMap<>();

    public CasinoConfig() {
        crapsConfig.put("payoutMultiplier", 3);
        crapsConfig.put("winningNumbers", Arrays.asList(7, 9, 12));
        
        diceDuelConfig.put("payoutMultiplier", 2);
        
        flowerPokerConfig.put("payoutMultiplier", 2);
    }
}
