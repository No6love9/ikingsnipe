package com.ikingsnipe.casino.games;
import java.util.Map;
public class BlackjackGame extends AbstractGame {
    public GameResult play(long bet, Map<String, Object> config) {
        int p = random.nextInt(10) + 12, h = random.nextInt(10) + 12; boolean win = p > h;
        double m = (double) config.getOrDefault("payoutMultiplier", 2.5);
        return new GameResult(win, win ? (long)(bet * m) : 0, "got " + p + " vs host " + h, p + ":" + h);
    }
    public String getRules() { return "21 wins."; }
}
