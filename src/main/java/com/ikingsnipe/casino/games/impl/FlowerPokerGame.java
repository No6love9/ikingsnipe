package com.ikingsnipe.casino.games.impl;

import com.ikingsnipe.casino.games.AbstractGame;
import com.ikingsnipe.casino.games.GameContext;
import com.ikingsnipe.casino.games.GameResult;
import java.util.*;

/**
 * FlowerPokerGame - Elite Titan Casino
 * 
 * Professional Flower Poker with Provably Fair card generation.
 */
public class FlowerPokerGame extends AbstractGame {
    private static final String[] FLOWERS = {"Red", "Blue", "Yellow", "Purple", "Orange", "White", "Black"};

    @Override
    public String getId() { return "flower"; }

    @Override
    public String getDisplayName() { return "Flower Poker"; }

    @Override
    public String getTrigger() { return "!fp"; }

    @Override
    public double getDefaultMultiplier() { return 2.0; }

    @Override
    public GameResult play(String player, long bet, GameContext context) {
        // Use provably fair to generate 10 numbers (5 for player, 5 for host)
        int[] rolls = new int[10];
        for (int i = 0; i < 10; i++) {
            rolls[i] = provablyFair.generateDiceRoll(); // 1-100
        }
        String reveal = provablyFair.getShortRevealString();

        Hand playerHand = generateHand(Arrays.copyOfRange(rolls, 0, 5));
        Hand hostHand = generateHand(Arrays.copyOfRange(rolls, 5, 10));

        boolean win = playerHand.rank > hostHand.rank;
        double multiplier = (settings != null) ? settings.multiplier : getDefaultMultiplier();
        long payout = win ? calculatePayout(bet, multiplier) : 0;

        String description = String.format("%s: You [%s] vs Host [%s] | Seed: %s", 
            win ? "WIN" : "LOSS", playerHand.name, hostHand.name, reveal);

        logGame(player, description, payout);
        return new GameResult(win, payout, description, playerHand.rank + ":" + hostHand.rank);
    }

    private Hand generateHand(int[] rolls) {
        Map<String, Integer> counts = new HashMap<>();
        for (int roll : rolls) {
            String f = FLOWERS[(roll - 1) % FLOWERS.length];
            counts.put(f, counts.getOrDefault(f, 0) + 1);
        }

        List<Integer> v = new ArrayList<>(counts.values());
        Collections.sort(v, Collections.reverseOrder());

        int rank;
        String name;
        if (v.get(0) == 5) { rank = 6; name = "5-of-a-Kind"; }
        else if (v.get(0) == 4) { rank = 5; name = "4-of-a-Kind"; }
        else if (v.get(0) == 3 && v.size() > 1 && v.get(1) == 2) { rank = 4; name = "Full House"; }
        else if (v.get(0) == 3) { rank = 3; name = "3-of-a-Kind"; }
        else if (v.get(0) == 2 && v.size() > 1 && v.get(1) == 2) { rank = 2; name = "Two Pair"; }
        else if (v.get(0) == 2) { rank = 1; name = "One Pair"; }
        else { rank = 0; name = "Bust"; }

        return new Hand(rank, name);
    }

    private static class Hand {
        int rank;
        String name;
        Hand(int rank, String name) { this.rank = rank; this.name = name; }
    }

    @Override
    public String getRulesDescription() {
        return "Standard Flower Poker rules. Ties go to host. Win x2.0.";
    }
}
