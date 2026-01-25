package com.ikingsnipe.casino.games.impl;

import com.ikingsnipe.casino.games.AbstractGame;
import com.ikingsnipe.casino.games.GameResult;
import com.ikingsnipe.casino.utils.ProvablyFairCraps;
import org.dreambot.api.utilities.Logger;

/**
 * FiftyFiveGame (Dice Game) - Elite Titan Casino Bot
 * 
 * Rules:
 * - Roll 1-100
 * - Configurable threshold (default 55.00+)
 * - Configurable multiplier (default 1.818× for break-even at 55%)
 * 
 * Provably Fair:
 * - SHA-256 pre-commit hash announced BEFORE roll
 * - Full seed/nonce reveal AFTER outcome
 * 
 * @author EliteForge / iKingSnipe
 * @version 8.2.7-velocity-bezier
 */
public class FiftyFiveGame extends AbstractGame {
    
    // Provably fair system
    private final ProvablyFairCraps provablyFair;
    
    // Default configuration
    private double threshold = 55.0;
    private double defaultMultiplier = 1.818; // Break-even multiplier for 55%
    
    public FiftyFiveGame() {
        this.provablyFair = new ProvablyFairCraps();
    }
    
    /**
     * Get the commitment hash to announce BEFORE the roll
     */
    public String getPreRollCommitment() {
        return provablyFair.getShortCommitmentHash();
    }
    
    /**
     * Get the full commitment hash
     */
    public String getFullCommitment() {
        return provablyFair.getCommitmentHash();
    }
    
    @Override
    public GameResult play(String player, long bet, String seed) {
        // Generate provably fair roll (1-100)
        int roll = provablyFair.generateDiceRoll();
        String revealString = provablyFair.getShortRevealString();
        
        // Determine win based on threshold
        boolean win = roll >= threshold;
        
        // Get multiplier from settings or use default
        double multiplier = defaultMultiplier;
        if (settings != null && settings.multiplier > 0) {
            multiplier = settings.multiplier;
        }
        
        long payout = win ? (long)(bet * multiplier) : 0;
        
        String resultMsg = win ? "WIN" : "LOSS";
        
        // Build description with provably fair info
        String description = String.format("%s: Rolled %d (%.0f+ wins x%.3f) | Seed: %s", 
            resultMsg, roll, threshold, multiplier, revealString);
        
        Logger.log("[FiftyFiveGame] " + player + " - " + description + " - Payout: " + payout);
        
        return new GameResult(win, payout, description, String.valueOf(roll));
    }
    
    /**
     * Play with custom threshold and multiplier
     */
    public GameResult playCustom(String player, long bet, double customThreshold, double customMultiplier) {
        // Generate provably fair roll (1-100)
        int roll = provablyFair.generateDiceRoll();
        String revealString = provablyFair.getShortRevealString();
        
        // Determine win based on custom threshold
        boolean win = roll >= customThreshold;
        
        long payout = win ? (long)(bet * customMultiplier) : 0;
        
        String resultMsg = win ? "WIN" : "LOSS";
        
        // Build description with provably fair info
        String description = String.format("%s: Rolled %d (%.2f+ wins x%.3f) | Seed: %s", 
            resultMsg, roll, customThreshold, customMultiplier, revealString);
        
        Logger.log("[FiftyFiveGame] " + player + " - " + description + " - Payout: " + payout);
        
        return new GameResult(win, payout, description, String.valueOf(roll));
    }
    
    /**
     * Get the reveal string for verification
     */
    public String getRevealString() {
        return provablyFair.getRevealString();
    }
    
    /**
     * Set the default threshold
     */
    public void setThreshold(double threshold) {
        this.threshold = threshold;
        // Recalculate break-even multiplier
        this.defaultMultiplier = 100.0 / (100.0 - threshold + 1);
    }
    
    /**
     * Get current threshold
     */
    public double getThreshold() {
        return threshold;
    }
    
    /**
     * Get current multiplier
     */
    public double getMultiplier() {
        return defaultMultiplier;
    }
    
    /**
     * Get current nonce for display
     */
    public long getCurrentNonce() {
        return provablyFair.getCurrentNonce();
    }
}
