package com.ikingsnipe.casino.games;

import com.ikingsnipe.casino.models.CasinoConfig;

/**
 * Game Interface - Elite Titan Casino
 * 
 * All games must implement this interface for proper integration
 * with the casino system.
 * 
 * @author iKingSnipe / GoatGang
 * @version 9.0.0
 */
public interface Game {
    
    /**
     * Get the unique identifier for this game
     */
    String getId();
    
    /**
     * Get the display name for this game
     */
    String getDisplayName();
    
    /**
     * Get the command trigger for this game (e.g., "!c" for craps)
     */
    String getTrigger();
    
    /**
     * Get the default multiplier for this game
     */
    double getDefaultMultiplier();
    
    /**
     * Check if this game supports provably fair verification
     */
    boolean isProvablyFair();
    
    /**
     * Get the pre-roll commitment hash (for provably fair games)
     * @return Commitment hash or null if not provably fair
     */
    String getPreRollCommitment();
    
    /**
     * Get the full commitment hash (for provably fair games)
     * @return Full commitment hash or null if not provably fair
     */
    String getFullCommitment();
    
    /**
     * Get the reveal string after a roll (for provably fair games)
     * @return Reveal string or null if not provably fair
     */
    String getRevealString();
    
    /**
     * Play the game
     * @param player Player name
     * @param bet Bet amount in GP
     * @param context Optional game context (e.g., player choice for hot/cold)
     * @return GameResult containing outcome
     */
    GameResult play(String player, long bet, GameContext context);
    
    /**
     * Get game rules description
     */
    String getRulesDescription();
    
    /**
     * Configure the game with settings
     */
    void configure(CasinoConfig.GameSettings settings);
    
    /**
     * Check if the game is enabled
     */
    boolean isEnabled();
    
    /**
     * Set enabled state
     */
    void setEnabled(boolean enabled);
}
