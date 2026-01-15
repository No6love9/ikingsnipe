package com.ikingsnipe.casino.games;

import com.ikingsnipe.casino.models.CasinoConfig;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.*;

/**
 * Craps Game Implementation
 * 2d6 dice game with win on 7, 9, or 12
 */
public class CrapsGame extends AbstractGame {
    
    private final Random seededRandom;
    
    public CrapsGame(CasinoConfig config) {
        super(config, "craps");
        this.seededRandom = new Random();
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
            
            int die1 = rng.nextInt(6) + 1;
            int die2 = rng.nextInt(6) + 1;
            int total = die1 + die2;
            
            // Check win condition
            boolean win = config.getCrapsWinningNumbers().contains(total);
            int payout = win ? calculatePayout(true, betAmount) : 0;
            
            // Build result details
            Map<String, Object> details = new HashMap<>();
            details.put("dice1", die1);
            details.put("dice2", die2);
            details.put("total", total);
            details.put("winningNumbers", config.getCrapsWinningNumbers());
            details.put("seed", seed);
            details.put("payoutMultiplier", config.getCrapsPayoutMultiplier());
            
            String resultMessage = String.format("🎲 Craps: %d + %d = %d | %s",
                die1, die2, total, win ? "WIN!" : "LOSE");
            
            return new GameResult(win, resultMessage, payout, gameName, details);
            
        } catch (Exception e) {
            return new GameResult(false, "Game error: " + e.getMessage(), 0, gameName);
        }
    }
    
    private long generateSeed(String playerName) {
        try {
            String input = playerName + System.currentTimeMillis() + Math.random();
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes());
            
            long seed = 0;
            for (int i = 0; i < 8; i++) {
                seed = (seed << 8) | (hash[i] & 0xFF);
            }
            return seed;
        } catch (Exception e) {
            return System.currentTimeMillis();
        }
    }
    
    @Override
    public String getRules() {
        return String.format(
            "🎲 Craps Rules:\n" +
            "• Roll 2 six-sided dice\n" +
            "• Win %dx bet on total: %s\n" +
            "• Any other total loses\n" +
            "• Min bet: %,d GP | Max bet: %,d GP",
            config.getCrapsPayoutMultiplier(),
            config.getCrapsWinningNumbers().toString(),
            config.getMinBet(),
            config.getMaxBet()
        );
    }
    
    @Override
    public int calculatePayout(boolean win, int betAmount) {
        return win ? betAmount * config.getCrapsPayoutMultiplier() : 0;
    }
    
    @Override
    public Map<String, Object> getGameConfig() {
        Map<String, Object> gameConfig = new HashMap<>();
        gameConfig.put("enabled", config.isCrapsEnabled());
        gameConfig.put("payoutMultiplier", config.getCrapsPayoutMultiplier());
        gameConfig.put("winningNumbers", config.getCrapsWinningNumbers());
        gameConfig.put("minBet", config.getMinBet());
        gameConfig.put("maxBet", config.getMaxBet());
        return gameConfig;
    }
}