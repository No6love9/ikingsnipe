package com.ikingsnipe.casino.games.impl;

import org.dreambot.api.methods.input.Keyboard;


import com.ikingsnipe.casino.games.AbstractGame;
import com.ikingsnipe.casino.games.GameResult;
import java.util.*;

public class FlowerPokerGame extends AbstractGame {
    private static final String[] FLOWERS = {"Red", "Blue", "Yellow", "Purple", "Orange", "White", "Black"};
    
    // Payout multipliers based on hand strength (from provided code)
    private static final Map<Integer, Double> HAND_MULTIPLIERS = new HashMap<>();
    static {
        HAND_MULTIPLIERS.put(6, 10.0); // Five of a kind
        HAND_MULTIPLIERS.put(5, 6.0);  // Four of a kind
        HAND_MULTIPLIERS.put(4, 4.0);  // Full house
        HAND_MULTIPLIERS.put(3, 3.0);  // Three of a kind
        HAND_MULTIPLIERS.put(2, 2.0);  // Two pair
        HAND_MULTIPLIERS.put(1, 2.0);  // One pair
        HAND_MULTIPLIERS.put(0, 1.0);  // High flower (Bust)
    }

    @Override
    public GameResult play(String player, long bet, String seed) {
        Hand playerHand = generateHand();
        Hand hostHand = generateHand();
        
        // In Flower Poker, player must beat host. Ties go to host.
        boolean win = playerHand.rank > hostHand.rank;
        
        double multiplier = 2.0; // Default
        if (settings != null) {
            multiplier = settings.multiplier;
        }
        
        // Apply escalating payouts if enabled (feature from provided code)
        // Here we use the hand rank to determine if we should use a higher multiplier
        if (win && playerHand.rank > 0) {
            multiplier = HAND_MULTIPLIERS.getOrDefault(playerHand.rank, multiplier);
        }
        
        long payout = win ? (long)(bet * multiplier) : 0;
        
        return new GameResult(win, payout,
            String.format("You: %s | Host: %s (x%.1f)", playerHand.name, hostHand.name, multiplier), 
            playerHand.rank + ":" + hostHand.rank);
    }

    private Hand generateHand() {
        List<String> handFlowers = new ArrayList<>();
        Map<String, Integer> counts = new HashMap<>();
        for (int i = 0; i < 5; i++) {
            String f = FLOWERS[random.nextInt(FLOWERS.length)];
            handFlowers.add(f);
            counts.put(f, counts.getOrDefault(f, 0) + 1);
        }
        
        List<Integer> v = new ArrayList<>(counts.values());
        Collections.sort(v, Collections.reverseOrder());
        
        int rank;
        String name;
        
        if (v.get(0) == 5) {
            rank = 6;
            name = "5-of-a-Kind";
        } else if (v.get(0) == 4) {
            rank = 5;
            name = "4-of-a-Kind";
        } else if (v.get(0) == 3 && v.size() > 1 && v.get(1) == 2) {
            rank = 4;
            name = "Full House";
        } else if (v.get(0) == 3) {
            rank = 3;
            name = "3-of-a-Kind";
        } else if (v.get(0) == 2 && v.size() > 1 && v.get(1) == 2) {
            rank = 2;
            name = "Two Pair";
        } else if (v.get(0) == 2) {
            rank = 1;
            name = "One Pair";
        } else {
            rank = 0;
            name = "Bust";
        }
        
        return new Hand(rank, name, handFlowers);
    }

    private static class Hand {
        int rank;
        String name;
        List<String> flowers;
        Hand(int rank, String name, List<String> flowers) {
            this.rank = rank; this.name = name; this.flowers = flowers;
        }
    }
}
