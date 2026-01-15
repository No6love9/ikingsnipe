package com.ikingsnipe.casino.games;

import com.ikingsnipe.casino.models.CasinoConfig;
import java.security.MessageDigest;
import java.util.*;

/**
 * Flower Poker Game Implementation
 * 5-flower hand comparison with escalating payouts
 */
public class FlowerPokerGame extends AbstractGame {
    
    public FlowerPokerGame(CasinoConfig config) {
        super(config, "flower");
    }
    
    @Override
    public GameResult play(int betAmount, String playerName) {
        if (!validateBet(betAmount)) {
            return new GameResult(false, "Invalid bet amount", 0, gameName);
        }
        
        try {
            // Generate seeded flower hands
            long seed = generateSeed(playerName);
            Random rng = new Random(seed);
            
            // Generate hands
            int[] playerHand = generateHand(rng);
            int[] hostHand = generateHand(rng);
            
            // Evaluate hand strength
            int playerRank = evaluateHand(playerHand);
            int hostRank = evaluateHand(hostHand);
            
            // Determine winner
            boolean win = playerRank > hostRank;
            int payoutMultiplier = getPayoutMultiplier(playerRank);
            int payout = win ? betAmount * payoutMultiplier : 0;
            
            // Build result details
            Map<String, Object> details = new HashMap<>();
            details.put("playerHand", playerHand);
            details.put("hostHand", hostHand);
            details.put("playerRank", playerRank);
            details.put("hostRank", hostRank);
            details.put("playerRankName", getRankName(playerRank));
            details.put("hostRankName", getRankName(hostRank));
            details.put("payoutMultiplier", payoutMultiplier);
            details.put("flowerTypes", config.getFlowerTypes());
            
            String resultMessage = String.format("🌸 Flower Poker: %s vs %s | %s",
                getRankName(playerRank), getRankName(hostRank),
                win ? "PLAYER WINS!" : "HOST WINS");
            
            return new GameResult(win, resultMessage, payout, gameName, details);
            
        } catch (Exception e) {
            return new GameResult(false, "Game error: " + e.getMessage(), 0, gameName);
        }
    }
    
    private int[] generateHand(Random rng) {
        int[] hand = new int[5];
        for (int i = 0; i < 5; i++) {
            hand[i] = rng.nextInt(config.getFlowerTypes()) + 1;
        }
        return hand;
    }
    
    private int evaluateHand(int[] hand) {
        // Count frequencies of each flower type
        Map<Integer, Integer> freq = new HashMap<>();
        for (int flower : hand) {
            freq.put(flower, freq.getOrDefault(flower, 0) + 1);
        }
        
        List<Integer> counts = new ArrayList<>(freq.values());
        Collections.sort(counts, Collections.reverseOrder());
        
        // Determine hand rank (7 = best, 1 = worst)
        if (counts.get(0) == 5) return 7; // Five of a kind
        if (counts.get(0) == 4) return 6; // Four of a kind
        if (counts.get(0) == 3 && counts.size() >= 2 && counts.get(1) == 2) return 5; // Full house
        if (counts.get(0) == 3) return 4; // Three of a kind
        if (counts.get(0) == 2 && counts.size() >= 2 && counts.get(1) == 2) return 3; // Two pair
        if (counts.get(0) == 2) return 2; // One pair
        return 1; // High flower
    }
    
    private String getRankName(int rank) {
        switch (rank) {
            case 7: return "Five of a kind";
            case 6: return "Four of a kind";
            case 5: return "Full house";
            case 4: return "Three of a kind";
            case 3: return "Two pair";
            case 2: return "One pair";
            case 1: return "High flower";
            default: return "Unknown";
        }
    }
    
    private int getPayoutMultiplier(int rank) {
        if (config.isFlowerEscalatingPayouts()) {
            return config.getFlowerPayouts().getOrDefault(rank, 1);
        } else {
            return config.getDicePayoutMultiplier(); // Use dice multiplier if not escalating
        }
    }
    
    private long generateSeed(String playerName) {
        try {
            String input = playerName + "flower" + System.currentTimeMillis();
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
        StringBuilder rules = new StringBuilder();
        rules.append("🌸 Flower Poker Rules:\n");
        rules.append("• Both player and host get 5 random flowers (1-").append(config.getFlowerTypes()).append(")\n");
        rules.append("• Hand rankings (best to worst):\n");
        
        if (config.isFlowerEscalatingPayouts()) {
            for (int i = 7; i >= 1; i--) {
                rules.append("  ").append(getRankName(i)).append(": ").append(getPayoutMultiplier(i)).append("x\n");
            }
        } else {
            rules.append("  Higher hand wins ").append(config.getDicePayoutMultiplier()).append("x bet\n");
        }
        
        rules.append("• Min bet: ").append(String.format("%,d", config.getMinBet())).append(" GP\n");
        rules.append("• Max bet: ").append(String.format("%,d", config.getMaxBet())).append(" GP");
        
        return rules.toString();
    }
    
    @Override
    public int calculatePayout(boolean win, int betAmount) {
        // This is handled in play() with rank-specific multipliers
        return 0; // Override not used
    }
    
    @Override
    public Map<String, Object> getGameConfig() {
        Map<String, Object> gameConfig = new HashMap<>();
        gameConfig.put("enabled", config.isFlowerEnabled());
        gameConfig.put("flowerTypes", config.getFlowerTypes());
        gameConfig.put("escalatingPayouts", config.isFlowerEscalatingPayouts());
        gameConfig.put("payouts", config.getFlowerPayouts());
        return gameConfig;
    }
}