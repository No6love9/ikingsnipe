package com.ikingsnipe.casino.games.impl;

import com.ikingsnipe.casino.games.AbstractGame;
import com.ikingsnipe.casino.games.GameResult;
import java.util.*;

public class FlowerPokerGame extends AbstractGame {
    private static final String[] FLOWERS = {"Red", "Blue", "Yellow", "Purple", "Orange", "White", "Black"};

    @Override
    public GameResult play(long bet, double multiplier) {
        Hand playerHand = generateHand();
        Hand hostHand = generateHand();
        
        boolean win = playerHand.rank > hostHand.rank;
        // If ranks are equal, it's a draw (host wins in most OSRS casino rules, or re-roll)
        // Here we assume host wins on tie for simplicity, or you can adjust.
        
        return new GameResult(win, win ? (long)(bet * multiplier) : 0,
            "Got " + playerHand.name + " vs Host " + hostHand.name, 
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
            name = "Five of a Kind (" + handFlowers.get(0) + ")";
        } else if (v.get(0) == 4) {
            rank = 5;
            name = "Four of a Kind";
        } else if (v.get(0) == 3 && v.size() > 1 && v.get(1) == 2) {
            rank = 4;
            name = "Full House";
        } else if (v.get(0) == 3) {
            rank = 3;
            name = "Three of a Kind";
        } else if (v.get(0) == 2 && v.size() > 1 && v.get(1) == 2) {
            rank = 2;
            name = "Two Pair";
        } else if (v.get(0) == 2) {
            rank = 1;
            name = "One Pair";
        } else {
            rank = 0;
            name = "Bust (High Flower)";
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
