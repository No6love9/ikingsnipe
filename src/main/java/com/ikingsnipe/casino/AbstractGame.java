package com.ikingsnipe.casino.games;

import com.ikingsnipe.casino.models.CasinoConfig;
import java.util.Map;

/**
 * Base class for all casino games
 * Ensures consistent interface and error handling
 */
public abstract class AbstractGame {
    
    protected final CasinoConfig config;
    protected final String gameName;
    
    public AbstractGame(CasinoConfig config, String gameName) {
        this.config = config;
        this.gameName = gameName;
    }
    
    /**
     * Main game execution method
     * @param betAmount Player's bet amount
     * @param playerName Player's username for RNG seeding
     * @return GameResult containing outcome and details
     */
    public abstract GameResult play(int betAmount, String playerName);
    
    /**
     * Get game rules for display
     */
    public abstract String getRules();
    
    /**
     * Validate if a bet is acceptable for this game
     */
    public boolean validateBet(int amount) {
        return config.isValidBet(amount);
    }
    
    /**
     * Get payout amount based on win condition
     */
    public abstract int calculatePayout(boolean win, int betAmount);
    
    /**
     * Get game-specific configuration
     */
    public abstract Map<String, Object> getGameConfig();
    
    public String getGameName() {
        return gameName;
    }
    
    public CasinoConfig getConfig() {
        return config;
    }
}