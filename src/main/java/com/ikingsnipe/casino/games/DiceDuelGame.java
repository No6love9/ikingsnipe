package com.ikingsnipe.casino.games;
import java.util.Map;
public class DiceDuelGame extends AbstractGame {
    public GameResult play(long bet, Map<String, Object> config) {
        int p = random.nextInt(100) + 1, h = random.nextInt(100) + 1;
        boolean win = p > h; double m = (double) config.getOrDefault("payoutMultiplier", 2.0);
        return new GameResult(win, win ? (long)(bet * m) : 0, "rolled " + p + " vs host " + h, p + ":" + h);
    }
    public String getRules() { return "Higher roll wins."; }
}
