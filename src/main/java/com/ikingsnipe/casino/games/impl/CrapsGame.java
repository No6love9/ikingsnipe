package com.ikingsnipe.casino.games.impl;

import com.ikingsnipe.casino.games.AbstractGame;
import com.ikingsnipe.casino.games.GameResult;
import com.ikingsnipe.casino.utils.ProvablyFairCraps;
import org.dreambot.api.utilities.Logger;

import java.util.Arrays;
import java.util.List;

/**
 * ChasingCraps Implementation - Elite Titan Casino Bot
 * 
 * Rules:
 * - Natural 7 or 11 on come-out = Win (x3)
 * - 2, 3, 12 (craps) = Loss
 * - Point established (4, 5, 6, 8, 9, 10) → roll again until point hit (Win x3) or 7 (Loss)
 * - B2B (Back-to-Back) Win = x9
 * - Double-or-Nothing offer chain available
 * 
 * Provably Fair:
 * - SHA-256 pre-commit hash announced BEFORE roll
 * - Full seed/nonce reveal AFTER outcome
 * - Auto-rotate seed every ~50 rolls
 * 
 * @author EliteForge / iKingSnipe
 * @version 8.2.7-velocity-bezier
 */
public class CrapsGame extends AbstractGame {
    
    // Win numbers for simplified craps (natural winners)
    private static final List<Integer> NATURAL_WINNERS = Arrays.asList(7, 11);
    private static final List<Integer> CRAPS_LOSERS = Arrays.asList(2, 3, 12);
    private static final List<Integer> POINT_NUMBERS = Arrays.asList(4, 5, 6, 8, 9, 10);
    
    // Provably fair system
    private final ProvablyFairCraps provablyFair;
    
    // B2B tracking
    private boolean lastWasWin = false;
    private String lastPlayer = "";
    
    // Point tracking (for full craps rules)
    private Integer currentPoint = null;
    private String pointPlayer = null;
    
    public CrapsGame() {
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
        // Generate provably fair roll
        int[] roll = provablyFair.generateRoll();
        int d1 = roll[0];
        int d2 = roll[1];
        int total = roll[2];
        
        boolean isWin = false;
        double multiplier = 0;
        String resultType = "LOSS";
        String revealString = provablyFair.getShortRevealString();
        
        // Check if we're in point phase
        if (currentPoint != null && player.equals(pointPlayer)) {
            // Point phase - roll until point hit or 7
            if (total == currentPoint) {
                // Point hit - WIN!
                isWin = true;
                multiplier = 3.0;
                resultType = "POINT HIT (x3)";
                currentPoint = null;
                pointPlayer = null;
            } else if (total == 7) {
                // Seven out - LOSS
                isWin = false;
                multiplier = 0;
                resultType = "SEVEN OUT";
                currentPoint = null;
                pointPlayer = null;
            } else {
                // Continue rolling (no resolution yet)
                // For simplicity, we'll treat this as a push and let them roll again
                resultType = "ROLL AGAIN (Point: " + currentPoint + ")";
                // Return a special result indicating to roll again
                String description = "Rolled " + d1 + " + " + d2 + " = " + total + " [" + resultType + "] | Seed: " + revealString;
                return new GameResult(false, 0, description, String.valueOf(total));
            }
        } else {
            // Come-out roll
            if (NATURAL_WINNERS.contains(total)) {
                // Natural winner (7 or 11)
                isWin = true;
                
                // Check for B2B
                if (lastWasWin && lastPlayer.equals(player)) {
                    multiplier = 9.0;
                    resultType = "NATURAL B2B (x9)";
                } else {
                    multiplier = 3.0;
                    resultType = "NATURAL WIN (x3)";
                }
            } else if (CRAPS_LOSERS.contains(total)) {
                // Craps (2, 3, 12)
                isWin = false;
                multiplier = 0;
                resultType = "CRAPS";
            } else if (POINT_NUMBERS.contains(total)) {
                // Point established
                currentPoint = total;
                pointPlayer = player;
                resultType = "POINT SET: " + total;
                
                // Return special result indicating point is set
                String description = "Rolled " + d1 + " + " + d2 + " = " + total + " [" + resultType + "] | Seed: " + revealString;
                return new GameResult(false, 0, description, String.valueOf(total));
            }
        }
        
        // Update B2B tracking
        if (isWin) {
            lastWasWin = true;
        } else {
            lastWasWin = false;
        }
        lastPlayer = player;
        
        // Calculate payout
        long payout = (long) (bet * multiplier);
        
        // Build description with provably fair info
        String description = "Rolled " + d1 + " + " + d2 + " = " + total + " [" + resultType + "] | Seed: " + revealString;
        
        Logger.log("[CrapsGame] " + player + " - " + description + " - Payout: " + payout);
        
        return new GameResult(isWin, payout, description, String.valueOf(total));
    }
    
    /**
     * Get the reveal string for verification
     */
    public String getRevealString() {
        return provablyFair.getRevealString();
    }
    
    /**
     * Check if seed rotation is coming soon
     */
    public boolean isSeedRotationSoon() {
        return provablyFair.shouldRotateSoon();
    }
    
    /**
     * Get current nonce for display
     */
    public long getCurrentNonce() {
        return provablyFair.getCurrentNonce();
    }
    
    /**
     * Get rolls since last seed rotation
     */
    public int getRollsSinceRotation() {
        return provablyFair.getRollsSinceRotation();
    }
    
    /**
     * Force seed rotation (for admin use)
     */
    public void forceRotateSeed() {
        provablyFair.rotateSeed(false);
    }
    
    /**
     * Get the current point (if any)
     */
    public Integer getCurrentPoint() {
        return currentPoint;
    }
    
    /**
     * Check if a player has an active point
     */
    public boolean hasActivePoint(String player) {
        return currentPoint != null && player.equals(pointPlayer);
    }
}
