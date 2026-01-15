package com.ikingsnipe.casino.games;
import java.util.*;
public class FlowerPokerGame extends AbstractGame {
    private static final String[] F = {"Red", "Blue", "Yellow", "Purple", "Orange", "White", "Black"};
    public GameResult play(long bet, Map<String, Object> config) {
        int pr = getR(), hr = getR(); boolean win = pr > hr;
        double m = (double) config.getOrDefault("payoutMultiplier", 2.0);
        return new GameResult(win, win ? (long)(bet * m) : 0, "got " + getRN(pr) + " vs host " + getRN(hr), pr + ":" + hr);
    }
    private int getR() {
        Map<String, Integer> c = new HashMap<>(); for (int i=0; i<5; i++) { String fl = F[random.nextInt(F.length)]; c.put(fl, c.getOrDefault(fl, 0) + 1); }
        List<Integer> v = new ArrayList<>(c.values()); Collections.sort(v, Collections.reverseOrder());
        if (v.get(0) == 5) return 6; if (v.get(0) == 4) return 5; if (v.get(0) == 3 && v.size() > 1 && v.get(1) == 2) return 4;
        if (v.get(0) == 3) return 3; if (v.get(0) == 2 && v.size() > 1 && v.get(1) == 2) return 2; if (v.get(0) == 2) return 1; return 0;
    }
    private String getRN(int r) { String[] n = {"Bust", "One Pair", "Two Pair", "Three of a Kind", "Full House", "Four of a Kind", "Five of a Kind"}; return n[r]; }
    public String getRules() { return "Best hand wins."; }
}
