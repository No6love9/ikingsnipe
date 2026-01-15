package com.ikingsnipe.casino.games.impl;

import com.ikingsnipe.casino.games.AbstractGame;
import com.ikingsnipe.casino.games.GameResult;
import java.util.*;

public class FlowerPokerGame extends AbstractGame {
    private static final String[] FLOWERS = {"Red", "Blue", "Yellow", "Purple", "Orange", "White", "Black"};

    @Override
    public GameResult play(long bet, double multiplier) {
        int playerRank = getRank();
        int hostRank = getRank();
        boolean win = playerRank > hostRank;
        return new GameResult(win, win ? (long)(bet * multiplier) : 0,
            "Got " + getRankName(playerRank) + " vs Host " + getRankName(hostRank), playerRank + ":" + hostRank);
    }

    private int getRank() {
        Map<String, Integer> counts = new HashMap<>();
        for (int i = 0; i < 5; i++) {
            String f = FLOWERS[random.nextInt(FLOWERS.length)];
            counts.put(f, counts.getOrDefault(f, 0) + 1);
        }
        List<Integer> v = new ArrayList<>(counts.values());
        Collections.sort(v, Collections.reverseOrder());
        if (v.get(0) == 5) return 6; // 5 OAK
        if (v.get(0) == 4) return 5; // 4 OAK
        if (v.get(0) == 3 && v.size() > 1 && v.get(1) == 2) return 4; // Full House
        if (v.get(0) == 3) return 3; // 3 OAK
        if (v.get(0) == 2 && v.size() > 1 && v.get(1) == 2) return 2; // 2 Pair
        if (v.get(0) == 2) return 1; // 1 Pair
        return 0; // Bust
    }

    private String getRankName(int r) {
        String[] names = {"Bust", "One Pair", "Two Pair", "Three of a Kind", "Full House", "Four of a Kind", "Five of a Kind"};
        return names[r];
    }
}
