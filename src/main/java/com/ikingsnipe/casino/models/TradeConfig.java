package com.ikingsnipe.casino.models;

import org.dreambot.api.methods.input.Keyboard;


/**
 * Trade Configuration for snipes♧scripts Enterprise
 * All trade-related settings are configurable here
 */
public class TradeConfig {
    
    // ==================== TRADE DETECTION ====================
    
    /** Maximum distance to detect trade requests from players */
    public int maxTradeDistance = 15;
    
    /** Timeout for accepting trade requests (ms) */
    public int tradeAcceptTimeout = 5000;
    
    /** Overall trade session timeout (ms) - decline if exceeded */
    public int tradeTimeout = 60000;
    
    /** Cooldown before accepting trades from declined players (ms) */
    public int declineCooldown = 30000;
    
    // ==================== ANTI-SCAM VERIFICATION ====================
    
    /** Enable anti-scam verification checks */
    public boolean enableAntiScam = true;
    
    /** Time value must be stable before accepting (ms) */
    public int valueStabilityTime = 600;
    
    /** Minimum delay between verification checks (ms) */
    public int minVerifyDelay = 300;
    
    /** Maximum delay between verification checks (ms) */
    public int maxVerifyDelay = 600;
    
    /** Threshold for medium value bets (requires more verification) */
    public long mediumValueThreshold = 10_000_000L;
    
    /** Threshold for high value bets (requires most verification) */
    public long highValueThreshold = 100_000_000L;
    
    /** Verification count for low value bets */
    public int lowValueVerifyCount = 1;
    
    /** Verification count for medium value bets */
    public int mediumValueVerifyCount = 2;
    
    /** Verification count for high value bets */
    public int highValueVerifyCount = 3;
    
    // ==================== TRADE SCREEN 2 ====================
    
    /** Enable verification on trade screen 2 */
    public boolean enableScreen2Verification = true;
    
    /** Timeout for trade screen 2 (ms) */
    public int screen2Timeout = 30000;
    
    /** Wait time for screen 2 to open after accepting screen 1 (ms) */
    public int screen2WaitTime = 5000;
    
    /** Delay before verifying screen 2 values (ms) */
    public int screen2VerifyDelay = 400;
    
    /** Maximum verification attempts on screen 2 before declining */
    public int maxScreen2VerifyAttempts = 3;
    
    /** Wait time for trade to complete after accepting screen 2 (ms) */
    public int tradeCompleteWaitTime = 5000;
    
    // ==================== MESSAGING ====================
    
    /** Send welcome message when trade opens */
    public boolean sendWelcomeMessage = true;
    
    /** Send game command list after welcome */
    public boolean sendGameCommands = true;
    
    /** Send confirmation messages during trade */
    public boolean sendConfirmationMessages = true;
    
    /** Custom welcome message template
     * Placeholders: {player}, {hash}, {min}, {max}
     */
    public String customWelcomeMessage = "Welcome {player}! Safe trade active. Hash: {hash}";
    
    /** Message sent when bet is confirmed */
    public String betConfirmedMessage = "Bet confirmed: {amount}. Good luck!";
    
    /** Message sent when trade is accepted */
    public String tradeAcceptedMessage = "Trade accepted! Starting game...";
    
    // ==================== PAYOUT SETTINGS ====================
    
    /** Check if we have enough to pay potential winnings before accepting */
    public boolean checkPayoutCapacity = true;
    
    /** Delay before initiating payout trade (ms) */
    public int payoutInitDelay = 1000;
    
    /** Maximum attempts to initiate payout trade */
    public int maxPayoutAttempts = 3;
    
    /** Timeout for payout trade acceptance (ms) */
    public int payoutTradeTimeout = 30000;
    
    // ==================== PLAYER EXPERIENCE ====================
    
    /** Enable fast-accept mode for returning players */
    public boolean enableFastAcceptReturning = true;
    
    /** Number of successful trades before player is considered "trusted" */
    public int trustedPlayerTradeCount = 3;
    
    /** Reduced verification for trusted players */
    public boolean reducedVerifyForTrusted = true;
    
    /** Auto-accept small bets without full verification */
    public boolean autoAcceptSmallBets = false;
    
    /** Threshold for auto-accepting small bets */
    public long smallBetThreshold = 1_000_000L;
    
    // ==================== ADVANCED SETTINGS ====================
    
    /** Enable trade request queuing */
    public boolean enableTradeQueue = true;
    
    /** Maximum queued trade requests */
    public int maxQueuedRequests = 5;
    
    /** Log all trade events for debugging */
    public boolean verboseLogging = false;
    
    /** Enable trade statistics tracking */
    public boolean trackTradeStats = true;
    
    /**
     * Create default trade configuration
     */
    public TradeConfig() {
        // Defaults are set in field declarations
    }
    
    /**
     * Create trade configuration with preset
     */
    public static TradeConfig fromPreset(TradePreset preset) {
        TradeConfig config = new TradeConfig();
        
        switch (preset) {
            case FAST_FRIENDLY:
                // Optimized for speed and player experience
                config.valueStabilityTime = 400;
                config.minVerifyDelay = 200;
                config.maxVerifyDelay = 400;
                config.lowValueVerifyCount = 1;
                config.mediumValueVerifyCount = 1;
                config.highValueVerifyCount = 2;
                config.screen2VerifyDelay = 300;
                config.autoAcceptSmallBets = true;
                break;
                
            case BALANCED:
                // Default balanced settings
                // Uses field defaults
                break;
                
            case MAXIMUM_SECURITY:
                // Maximum anti-scam protection
                config.valueStabilityTime = 1000;
                config.minVerifyDelay = 500;
                config.maxVerifyDelay = 800;
                config.lowValueVerifyCount = 2;
                config.mediumValueVerifyCount = 3;
                config.highValueVerifyCount = 4;
                config.screen2VerifyDelay = 600;
                config.autoAcceptSmallBets = false;
                config.reducedVerifyForTrusted = false;
                break;
                
            case HIGH_ROLLER:
                // Optimized for high value trades
                config.mediumValueThreshold = 50_000_000L;
                config.highValueThreshold = 500_000_000L;
                config.tradeTimeout = 120000;
                config.screen2Timeout = 60000;
                config.checkPayoutCapacity = true;
                break;
        }
        
        return config;
    }
    
    /**
     * Validate configuration values
     */
    public boolean validate() {
        if (maxTradeDistance < 1 || maxTradeDistance > 50) return false;
        if (tradeAcceptTimeout < 1000 || tradeAcceptTimeout > 30000) return false;
        if (tradeTimeout < 10000 || tradeTimeout > 300000) return false;
        if (valueStabilityTime < 100 || valueStabilityTime > 5000) return false;
        if (minVerifyDelay < 100 || minVerifyDelay > maxVerifyDelay) return false;
        if (mediumValueThreshold <= 0 || highValueThreshold <= mediumValueThreshold) return false;
        return true;
    }
    
    /**
     * Trade configuration presets
     */
    public enum TradePreset {
        FAST_FRIENDLY("Fast & Friendly - Quick trades, great player experience"),
        BALANCED("Balanced - Good security with reasonable speed"),
        MAXIMUM_SECURITY("Maximum Security - Thorough verification, slower trades"),
        HIGH_ROLLER("High Roller - Optimized for large value trades");
        
        private final String description;
        
        TradePreset(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
}
