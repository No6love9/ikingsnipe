package com.ikingsnipe.casino.games;
import java.util.*;
public class CrapsGame extends AbstractGame {
    public GameResult play(long bet, Map<String, Object> config) {
        int t = (random.nextInt(6) + 1) + (random.nextInt(6) + 1);
        List<Integer> w = (List<Integer>) config.get("winningNumbers"); boolean win = w.contains(t);
        double m = (double) config.getOrDefault("payoutMultiplier", 2.0);
        return new GameResult(win, win ? (long)(bet * m) : 0, "rolled " + t, String.valueOf(t));
    }
    public String getRules() { return "7/11 wins."; }
}
