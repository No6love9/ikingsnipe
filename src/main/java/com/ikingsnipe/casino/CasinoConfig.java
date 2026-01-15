package com.ikingsnipe.casino.models;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Comprehensive configuration for the casino system
 * All settings are accessible through GUI
 */
public class CasinoConfig {
    
    // === GENERAL SETTINGS ===
    private int minBet = 1000;
    private int maxBet = 1000000;
    private int maxActiveSessions = 3;
    private int sessionTimeoutMinutes = 5;
    
    // === TRADE SETTINGS ===
    private boolean autoAcceptTrades = true;
    private int tradeTimeoutSeconds = 30;
    private boolean verifyPlayerLevel = false;
    private int minPlayerLevel = 0;
    
    // === ADVERTISING SETTINGS ===
    private boolean enableAdvertising = true;
    private int adCooldownSeconds = 15;
    private List<String> adMessages = new ArrayList<>();
    
    // === CHAT MESSAGES ===
    private Map<String, String> winMessages = new HashMap<>();
    private Map<String, String> lossMessages = new HashMap<>();
    private Map<String, String> gameMessages = new HashMap<>();
    
    // === GAME SPECIFIC SETTINGS ===
    // Craps
    private boolean crapsEnabled = true;
    private int crapsPayoutMultiplier = 3;
    private List<Integer> crapsWinningNumbers = Arrays.asList(7, 9, 12);
    
    // Dice Duel
    private boolean diceEnabled = true;
    private int dicePayoutMultiplier = 2;
    private boolean diceAllowTies = false;
    
    // Flower Poker
    private boolean flowerEnabled = true;
    private int flowerTypes = 6;
    private boolean flowerEscalatingPayouts = true;
    private Map<Integer, Integer> flowerPayouts = new HashMap<>();
    
    // === BANK SETTINGS ===
    private boolean enableBankWithdrawal = false;
    private int bankWithdrawThreshold = 1000000;
    private String bankPin = "";
    
    // === SAFETY SETTINGS ===
    private boolean emergencyStopOnError = true;
    private int maxErrorsBeforeStop = 5;
    private boolean logAllTransactions = true;
    private String logFilePath = "";
    
    public CasinoConfig() {
        loadDefaultMessages();
        loadDefaultAds();
        loadDefaultFlowerPayouts();
    }
    
    private void loadDefaultMessages() {
        // Win messages
        winMessages.put("craps", "Congratulations! You rolled {total} and won {payout} GP!");
        winMessages.put("dice", "You won the duel with a {playerRoll} vs {hostRoll}! Payout: {payout} GP");
        winMessages.put("flower", "Flower Poker victory! Your {handRank} beats the host! Won {payout} GP");
        
        // Loss messages
        lossMessages.put("craps", "Better luck next time! You rolled {total}.");
        lossMessages.put("dice", "House wins with {hostRoll} vs your {playerRoll}. Try again!");
        lossMessages.put("flower", "The host's {handRank} beats your hand. Next time!");
        
        // Game messages
        gameMessages.put("craps_rules", "🎲 Craps: Roll 2 dice. Win 3x on 7, 9, or 12.");
        gameMessages.put("dice_rules", "⚄ Dice Duel: Higher roll wins 2x. Ties reroll.");
        gameMessages.put("flower_rules", "🌸 Flower Poker: 5 flowers, best hand wins up to 10x.");
    }
    
    private void loadDefaultAds() {
        adMessages.add("🎰 Elite Casino | Fast Payouts | High Limits! | !rules");
        adMessages.add("💰 Trusted Casino Host | Instant Trades | Fair Games!");
        adMessages.add("🎲 Professional Casino Service | Craps, Dice, Flower Poker!");
    }
    
    private void loadDefaultFlowerPayouts() {
        flowerPayouts.put(7, 10); // Five of a kind
        flowerPayouts.put(6, 6);  // Four of a kind
        flowerPayouts.put(5, 4);  // Full house
        flowerPayouts.put(4, 3);  // Three of a kind
        flowerPayouts.put(3, 2);  // Two pair
        flowerPayouts.put(2, 2);  // One pair
        flowerPayouts.put(1, 1);  // High flower
    }
    
    // === VALIDATION ===
    
    public boolean validate() {
        if (minBet <= 0) {
            return false;
        }
        if (maxBet < minBet) {
            return false;
        }
        if (maxActiveSessions <= 0) {
            return false;
        }
        if (sessionTimeoutMinutes < 1) {
            return false;
        }
        return true;
    }
    
    public boolean isValidBet(int amount) {
        return amount >= minBet && amount <= maxBet;
    }
    
    // === UTILITY METHODS ===
    
    public String getRandomAdMessage() {
        if (adMessages.isEmpty()) {
            return "🎰 Elite Casino hosting now! | !rules for info";
        }
        return adMessages.get(ThreadLocalRandom.current().nextInt(adMessages.size()));
    }
    
    public String getWinMessage(String game, Map<String, String> placeholders) {
        String message = winMessages.getOrDefault(game, "Congratulations! You won!");
        return replacePlaceholders(message, placeholders);
    }
    
    public String getLossMessage(String game, Map<String, String> placeholders) {
        String message = lossMessages.getOrDefault(game, "Better luck next time!");
        return replacePlaceholders(message, placeholders);
    }
    
    private String replacePlaceholders(String message, Map<String, String> placeholders) {
        if (placeholders == null) return message;
        
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            message = message.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return message;
    }
    
    // === GETTERS & SETTERS ===
    
    public int getMinBet() { return minBet; }
    public void setMinBet(int minBet) { this.minBet = Math.max(1, minBet); }
    
    public int getMaxBet() { return maxBet; }
    public void setMaxBet(int maxBet) { this.maxBet = Math.max(this.minBet, maxBet); }
    
    public int getMaxActiveSessions() { return maxActiveSessions; }
    public void setMaxActiveSessions(int maxActiveSessions) { 
        this.maxActiveSessions = Math.max(1, maxActiveSessions); 
    }
    
    public int getSessionTimeoutMinutes() { return sessionTimeoutMinutes; }
    public void setSessionTimeoutMinutes(int minutes) { 
        this.sessionTimeoutMinutes = Math.max(1, minutes); 
    }
    
    public boolean isAutoAcceptTrades() { return autoAcceptTrades; }
    public void setAutoAcceptTrades(boolean autoAcceptTrades) { this.autoAcceptTrades = autoAcceptTrades; }
    
    public int getTradeTimeoutSeconds() { return tradeTimeoutSeconds; }
    public void setTradeTimeoutSeconds(int seconds) { 
        this.tradeTimeoutSeconds = Math.max(5, seconds); 
    }
    
    public boolean isAdvertisingEnabled() { return enableAdvertising; }
    public void setAdvertisingEnabled(boolean enableAdvertising) { this.enableAdvertising = enableAdvertising; }
    
    public int getAdCooldown() { return adCooldownSeconds * 1000; }
    public void setAdCooldownSeconds(int seconds) { 
        this.adCooldownSeconds = Math.max(10, seconds); 
    }
    
    public List<String> getAdMessages() { return new ArrayList<>(adMessages); }
    public void setAdMessages(List<String> adMessages) { this.adMessages = new ArrayList<>(adMessages); }
    
    public boolean isCrapsEnabled() { return crapsEnabled; }
    public void setCrapsEnabled(boolean crapsEnabled) { this.crapsEnabled = crapsEnabled; }
    
    public int getCrapsPayoutMultiplier() { return crapsPayoutMultiplier; }
    public void setCrapsPayoutMultiplier(int multiplier) { 
        this.crapsPayoutMultiplier = Math.max(1, multiplier); 
    }
    
    public List<Integer> getCrapsWinningNumbers() { return new ArrayList<>(crapsWinningNumbers); }
    public void setCrapsWinningNumbers(List<Integer> numbers) { this.crapsWinningNumbers = new ArrayList<>(numbers); }
    
    public boolean isDiceEnabled() { return diceEnabled; }
    public void setDiceEnabled(boolean diceEnabled) { this.diceEnabled = diceEnabled; }
    
    public int getDicePayoutMultiplier() { return dicePayoutMultiplier; }
    public void setDicePayoutMultiplier(int multiplier) { 
        this.dicePayoutMultiplier = Math.max(1, multiplier); 
    }
    
    public boolean isDiceAllowTies() { return diceAllowTies; }
    public void setDiceAllowTies(boolean allowTies) { this.diceAllowTies = allowTies; }
    
    public boolean isFlowerEnabled() { return flowerEnabled; }
    public void setFlowerEnabled(boolean flowerEnabled) { this.flowerEnabled = flowerEnabled; }
    
    public int getFlowerTypes() { return flowerTypes; }
    public void setFlowerTypes(int types) { 
        this.flowerTypes = Math.max(3, Math.min(10, types)); 
    }
    
    public boolean isFlowerEscalatingPayouts() { return flowerEscalatingPayouts; }
    public void setFlowerEscalatingPayouts(boolean escalatingPayouts) { 
        this.flowerEscalatingPayouts = escalatingPayouts; 
    }
    
    public Map<Integer, Integer> getFlowerPayouts() { return new HashMap<>(flowerPayouts); }
    public void setFlowerPayouts(Map<Integer, Integer> payouts) { this.flowerPayouts = new HashMap<>(payouts); }
    
    public boolean isEnableBankWithdrawal() { return enableBankWithdrawal; }
    public void setEnableBankWithdrawal(boolean enable) { this.enableBankWithdrawal = enable; }
    
    public int getBankWithdrawThreshold() { return bankWithdrawThreshold; }
    public void setBankWithdrawThreshold(int threshold) { 
        this.bankWithdrawThreshold = Math.max(0, threshold); 
    }
    
    public String getBankPin() { return bankPin; }
    public void setBankPin(String pin) { this.bankPin = pin; }
    
    public boolean isEmergencyStopOnError() { return emergencyStopOnError; }
    public void setEmergencyStopOnError(boolean stop) { this.emergencyStopOnError = stop; }
    
    public int getMaxErrorsBeforeStop() { return maxErrorsBeforeStop; }
    public void setMaxErrorsBeforeStop(int maxErrors) { 
        this.maxErrorsBeforeStop = Math.max(1, maxErrors); 
    }
    
    public boolean isLogAllTransactions() { return logAllTransactions; }
    public void setLogAllTransactions(boolean log) { this.logAllTransactions = log; }
    
    public String getLogFilePath() { return logFilePath; }
    public void setLogFilePath(String path) { this.logFilePath = path; }
    
    // === CONFIG PERSISTENCE ===
    
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        
        // General
        map.put("minBet", minBet);
        map.put("maxBet", maxBet);
        map.put("maxActiveSessions", maxActiveSessions);
        map.put("sessionTimeoutMinutes", sessionTimeoutMinutes);
        
        // Trade
        map.put("autoAcceptTrades", autoAcceptTrades);
        map.put("tradeTimeoutSeconds", tradeTimeoutSeconds);
        
        // Advertising
        map.put("enableAdvertising", enableAdvertising);
        map.put("adCooldownSeconds", adCooldownSeconds);
        map.put("adMessages", new ArrayList<>(adMessages));
        
        // Messages
        map.put("winMessages", new HashMap<>(winMessages));
        map.put("lossMessages", new HashMap<>(lossMessages));
        map.put("gameMessages", new HashMap<>(gameMessages));
        
        // Games
        map.put("crapsEnabled", crapsEnabled);
        map.put("crapsPayoutMultiplier", crapsPayoutMultiplier);
        map.put("crapsWinningNumbers", new ArrayList<>(crapsWinningNumbers));
        
        map.put("diceEnabled", diceEnabled);
        map.put("dicePayoutMultiplier", dicePayoutMultiplier);
        map.put("diceAllowTies", diceAllowTies);
        
        map.put("flowerEnabled", flowerEnabled);
        map.put("flowerTypes", flowerTypes);
        map.put("flowerEscalatingPayouts", flowerEscalatingPayouts);
        map.put("flowerPayouts", new HashMap<>(flowerPayouts));
        
        // Bank
        map.put("enableBankWithdrawal", enableBankWithdrawal);
        map.put("bankWithdrawThreshold", bankWithdrawThreshold);
        
        // Safety
        map.put("emergencyStopOnError", emergencyStopOnError);
        map.put("maxErrorsBeforeStop", maxErrorsBeforeStop);
        map.put("logAllTransactions", logAllTransactions);
        map.put("logFilePath", logFilePath);
        
        return map;
    }
    
    public static CasinoConfig fromMap(Map<String, Object> map) {
        CasinoConfig config = new CasinoConfig();
        
        // General
        config.minBet = (int) map.getOrDefault("minBet", 1000);
        config.maxBet = (int) map.getOrDefault("maxBet", 1000000);
        config.maxActiveSessions = (int) map.getOrDefault("maxActiveSessions", 3);
        config.sessionTimeoutMinutes = (int) map.getOrDefault("sessionTimeoutMinutes", 5);
        
        // Trade
        config.autoAcceptTrades = (boolean) map.getOrDefault("autoAcceptTrades", true);
        config.tradeTimeoutSeconds = (int) map.getOrDefault("tradeTimeoutSeconds", 30);
        
        // Advertising
        config.enableAdvertising = (boolean) map.getOrDefault("enableAdvertising", true);
        config.adCooldownSeconds = (int) map.getOrDefault("adCooldownSeconds", 15);
        config.adMessages = (List<String>) map.getOrDefault("adMessages", config.adMessages);
        
        // Messages
        config.winMessages = (Map<String, String>) map.getOrDefault("winMessages", config.winMessages);
        config.lossMessages = (Map<String, String>) map.getOrDefault("lossMessages", config.lossMessages);
        config.gameMessages = (Map<String, String>) map.getOrDefault("gameMessages", config.gameMessages);
        
        // Games
        config.crapsEnabled = (boolean) map.getOrDefault("crapsEnabled", true);
        config.crapsPayoutMultiplier = (int) map.getOrDefault("crapsPayoutMultiplier", 3);
        config.crapsWinningNumbers = (List<Integer>) map.getOrDefault("crapsWinningNumbers", config.crapsWinningNumbers);
        
        config.diceEnabled = (boolean) map.getOrDefault("diceEnabled", true);
        config.dicePayoutMultiplier = (int) map.getOrDefault("dicePayoutMultiplier", 2);
        config.diceAllowTies = (boolean) map.getOrDefault("diceAllowTies", false);
        
        config.flowerEnabled = (boolean) map.getOrDefault("flowerEnabled", true);
        config.flowerTypes = (int) map.getOrDefault("flowerTypes", 6);
        config.flowerEscalatingPayouts = (boolean) map.getOrDefault("flowerEscalatingPayouts", true);
        config.flowerPayouts = (Map<Integer, Integer>) map.getOrDefault("flowerPayouts", config.flowerPayouts);
        
        // Bank
        config.enableBankWithdrawal = (boolean) map.getOrDefault("enableBankWithdrawal", false);
        config.bankWithdrawThreshold = (int) map.getOrDefault("bankWithdrawThreshold", 1000000);
        
        // Safety
        config.emergencyStopOnError = (boolean) map.getOrDefault("emergencyStopOnError", true);
        config.maxErrorsBeforeStop = (int) map.getOrDefault("maxErrorsBeforeStop", 5);
        config.logAllTransactions = (boolean) map.getOrDefault("logAllTransactions", true);
        config.logFilePath = (String) map.getOrDefault("logFilePath", "");
        
        return config;
    }
}