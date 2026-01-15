package com.ikingsnipe.casino.games;

import com.ikingsnipe.casino.models.CasinoConfig;
import java.security.MessageDigest;
import java.util.*;

/**
 * Dice Duel Game Implementation
 * 1v1 dice battle with higher roll winning
 */
public class DiceDuelGame extends AbstractGame {
    
    public DiceDuelGame(CasinoConfig config) {
        super(config, "dice");
    }
    
    @Override
    public GameResult play(int betAmount, String playerName) {
        if (!validateBet(betAmount)) {
            return new GameResult(false, "Invalid bet amount", 0, gameName);
        }
        
        try {
            // Generate seeded dice rolls
            long seed = generateSeed(playerName);
            Random rng = new Random(seed);
            
            int playerRoll, hostRoll;
            
            // Handle ties based on config
            if (config.isDiceAllowTies()) {
                playerRoll = rng.nextInt(6) + 1;
                hostRoll = rng.nextInt(6) + 1;
            } else {
                // Reroll on ties
                do {
                    playerRoll = rng.nextInt(6) + 1;
                    hostRoll = rng.nextInt(6) + 1;
                } while (playerRoll == hostRoll);
            }
            
            // Determine winner
            boolean win = playerRoll > hostRoll;
            int payout = win ? calculatePayout(true, betAmount) : 0;
            
            // Build result details
            Map<String, Object> details = new HashMap<>();
            details.put("playerRoll", playerRoll);
            details.put("hostRoll", hostRoll);
            details.put("difference", Math.abs(playerRoll - hostRoll));
            details.put("allowTies", config.isDiceAllowTies());
            details.put("seed", seed);
            
            String resultMessage;
            if (playerRoll == hostRoll) {
                resultMessage = String.format("⚄ Dice Duel: %d vs %d | TIE!", playerRoll, hostRoll);
            } else {
                resultMessage = String.format("⚄ Dice Duel: %d vs %d | %s WINS!",
                    playerRoll, hostRoll, win ? "PLAYER" : "HOST");
            }
            
            return new GameResult(win, resultMessage, payout, gameName, details);
            
        } catch (Exception e) {
            return new GameResult(false, "Game error: " + e.getMessage(), 0, gameName);
        }
    }
    
    private long generateSeed(String playerName) {
        try {
            String input = playerName + "dice" + System.currentTimeMillis();
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes());
            
            long seed = 0;
            for (int i = 0; i < 8; i++) {
                seed = (seed << 8) | (hash[i] & 0xFF);
            }
            return seed;
        } catch (Exception e) {
            return System.currentTimeMillis() ^ playerName.hashCode();
        }
    }
    
    @Override
    public String getRules() {
        return String.format(
            "⚄ Dice Duel Rules:\n" +
            "• Player and host each roll 1 die (1-6)\n" +
            "• Higher roll wins %dx bet\n" +
            "• Ties: %s\n" +
            "• Min bet: %,d GP | Max bet: %,d GP",
            config.getDicePayoutMultiplier(),
            config.isDiceAllowTies() ? "Push (no winner)" : "Reroll until winner",
            config.getMinBet(),
            config.getMaxBet()
        );
    }
    
    @Override
    public int calculatePayout(boolean win, int betAmount) {
        return win ? betAmount * config.getDicePayoutMultiplier() : 0;
    }
    
    @Override
    public Map<String, Object> getGameConfig() {
        Map<String, Object> gameConfig = new HashMap<>();
        gameConfig.put("enabled", config.isDiceEnabled());
        gameConfig.put("payoutMultiplier", config.getDicePayoutMultiplier());
        gameConfig.put("allowTies", config.isDiceAllowTies());
        return gameConfig;
    }
}