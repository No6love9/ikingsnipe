package com.ikingsnipe.casino.models;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class CasinoConfig {
    public int minBet = 1000;
    public int maxBet = 1000000000;
    public boolean autoAcceptTrades = true;
    public int tradeTimeoutSeconds = 30;
    public int chatCooldownMs = 15000;
    public String adMessage = "Elite Casino | Fast Payouts | Dice, Flower, Craps!";
    
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
